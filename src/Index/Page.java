package Index;

import KVTable.Table;
import static View.BlockView.pageView;
import java.util.LinkedList;

/**
 * B+树的外部结点，称其为一张页，一张页由若干个K-V表以及其对应的索引区间组成.
 * <p>
 *  K-V表之间需要满足增序关系. 增序由区间类{@link Range}中进行定义——若一个区间在另一个
 *  区间的*右边*，就称该区间大于另一个区间.
 * <p>
 * B+树外部结点中的K-V表由缓冲区溢出后写入磁盘而来，属于mutable，即其状态会受到
 * 后序的读写而发生改变. 可能的改变有索引区域的改变以及体积的改变. 前者的改变会同步到
 * 当前结点的所有祖先结点对应索引区间上，后者的改变会导致 表(Table)内 的分裂与合并. 另外
 * 一种分裂和合并是页(Page)内发生的，即当前页中的表过多导致页分裂.
 * @param <K> 对应K-V表中的键的类型.
 * @param <V> 对应K-V表中的值的类型.
 * @author Episode-Zhang
 * @version 1.0
 */
public class Page<K, V> implements Block<K> {

    /** 每张表对应的索引区间(键的范围)组成的集合. */
    private Range<K>[] _ranges;

    /** 磁盘上的表. */
    private Table<K, V>[] _tables;

    /** 当前页所对应的上一级索引块. */
    private IndexBlock<K> _parent;

    /** 当前页在上一级索引块中所处的位置. */
    private int _loc;

    /** 当前页的id. */
    private int _pageId;

    /** 当前页中表的张数. */
    private int _length;

    /** 每张表的最大容量. */
    private final int _tableCapacity;

    /** 当前结点对应的B+树的阶. */
    private final int M;

    /**
     * 当内部结点不存在时，B+树外部结点的构造函数. 其主要功能是管理与调度磁盘中的K-V表.
     * @param order 外部结点对应B+树的阶.
     * @param tableCapacity 单张K-V表的最大容量(定义为记录条数). TODO 后期可以定义为文件大小
     */
    public Page(int order, int tableCapacity) {
        this.M = order;
        _tableCapacity = tableCapacity;
        initData(this.M);
        _loc = -1; // 若上一级索引不存在.
    }

    /**
     * 当内部结点存在时，B+树外部结点的构造函数.
     * @param order 外部结点对应B+树的阶.
     * @param tableCapacity 单张K-V表的最大容量(定义为记录条数).
     * @param parent 对应上一级的索引块.
     * @param loc 对应上一级索引块中当前页所处的位置.
     */
    public Page(int order, int tableCapacity, IndexBlock<K> parent, int loc) {
        this.M = order;
        _tableCapacity = tableCapacity;
        initData(this.M);
        _parent = parent;
        _loc = loc;
    }

    /** 返回表的张数. */
    @Override
    public int length() { return _length; }

    /** 获取{@code index}对应位置的表. */
    @Override
    public Object get(int index) {
        return _tables[index];
    }

    /** 弹出{@code index}对应位置的表. */
    @Override
    public Object pop(int index) {
        Table<K, V> table = _tables[index];
        _tables[index] = null;
        _ranges[index] = null; // 对应位置的索引区域也要跟随弹出.
        _length -= 1;
        return table;
    }

    /** 获取当前页的索引区域. */
    @Override
    public Range<K> blockRange() {
        if (_length == 0) { return null; }
        K minKey = _ranges[0]._left, maxKey = _ranges[_length - 1]._right;
        return new Range<>(minKey, maxKey);
    }

    /** 获取当前页中每个表对应的索引区域. */
    @Override
    public Range<K>[] subRanges() { return _ranges; }

    /** 返回当前页中所存的表的引用. */
    public Table<K, V>[] tables() { return _tables; }

    /** 获取当前表所对应的上级索引块. */
    @Override
    public IndexBlock<K> parent() { return _parent; }

    /** 获取当前表在对应的上级索引块中的位置. */
    @Override
    public int loc() { return _loc; }

    /** 为当前页设立对应的上级索引块以及该页在其中的位置. */
    @Override
    public void setParent(IndexBlock<K> parent, int loc) {
        _parent = parent;
        _loc = loc;
    }

    /** 更新当前结点在对应位置的索引区间. */
    @Override
    public void setRange(int pos, Range<K> range) { _ranges[pos] = range; }

    /** 获取该页对应的id. */
    public int id() { return _pageId; }

    /** 向页中加入一张新的表. 由缓冲区满写而来. */
    @Override
    public void add(Object table) {
        Table<K, V> fullTable = (Table<K, V>) table;
        // 更新表以及索引
        _tables[_length] = fullTable;
        _ranges[_length] = new Range<>(fullTable.minKey(), fullTable.maxKey());
        _length += 1;
    }

    /**
     * 将给定表插入到当前页中指定的位置.
     * @param table 给定待插入的表.
     * @param pos 当前页中指定插入的位置.
     */
    public void addAt(Table<K, V> table, int pos) {
        // 腾出空间
        for (int i = _length; i > pos; i--) {
            _tables[i] = _tables[i - 1];
            _ranges[i] = _ranges[i - 1];
        }
        // 插入块
        _tables[pos] = table;
        _ranges[pos] = new Range<>(table.minKey(), table.maxKey());
        _length += 1;
    }

    /** 返回当前页的文字视图. */
    @Override
    public String toString() {
        if (_length == 0) { return ""; }
        LinkedList<String> rangesInString = new LinkedList<>();
        for (int i = 0; i < _length; i++) {
            Range<K> range = _ranges[i];
            // 考虑空表存在的可能性，此时range值是null
            rangesInString.add(range != null ? range.toString() : "");
        }
        return pageView(rangesInString);
    }

    /**
     * 构造函数的*辅助函数*，初始化当前页的索引区域以及表.
     * @param order 对应B+树的阶.
     */
    private void initData(int order) {
        _length = 0;
        _ranges = new Range[order];
        _tables = new Table[order];
    }
}
