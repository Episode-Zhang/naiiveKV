package Index;

import KVTable.Table;

import java.util.LinkedList;
import static View.BlockView.indexBlockView;

/**
 * B+的内部结点，称其为一个“索引块”，作为“多级”索引的实现.
 * <p>
 * 其内部包括了一个索引区域的集合，每个索引区域“完美覆盖”了下一级结点的索引区域；以及
 * 对应的一个包含了下一级结点引用的集合.
 * <p>
 * 完美覆盖：若一个*闭*区间可以完成对一系列闭区间的“覆盖”，且找不出一个区间长度小于
 * 这个给定的覆盖区间的长度，则称这个区间“完美覆盖”了给定的一系列区间.
 * eg. [21, 43] 完美覆盖了 {[21, 27], [30, 32] ,[35, 43]}.
 * @param <K> 对应表中键的类型.
 * @author Episode-Zhang
 * @version 1.0
 */
public class IndexBlock<K> implements Block<K> {

    /** 下一级的“块”的索引. */
    private Range<K>[] _indexes;

    /** 下一级的“块”. 可能是索引块，也可能是存放表的页. */
    private Block<K>[] _blocks;

    /** 当前块所对应的上一级索引块. */
    private IndexBlock<K> _parent;

    /** 当前块在上一级索引块中所处的位置. */
    private int _loc;

    /** 当前结点中存储的块的个数. */
    private int _length;

    /** 对应B+树的阶. */
    private final int M;

    /**
     * 当上一级索引不存在时，内部结点(索引块)的构造函数.
     * @param order 对应的B+树的阶.
     */
    public IndexBlock(int order) {
        this.M = order;
        initData(this.M);
        _loc = -1; // 上一级索引不存在
    }

    /**
     * 当上一级索引存在时，内部结点(索引块)的构造函数.
     * @param order 对应的B+树的阶.
     * @param parent 对应上一级的索引块.
     * @param loc 对应上一级索引块中当前块所处的位置.
     */
    public IndexBlock(int order, IndexBlock<K> parent, int loc) {
        this.M = order;
        initData(this.M);
        _parent = parent;
        _loc = loc;
    }

    /** 返回当前索引块中索引区域的个数. */
    @Override
    public int length() { return _length; }

    /** 获取{@code index}对应位置的块. */
    @Override
    public Object get(int index) {
        return _blocks[index];
    }

    /** 弹出{@code index}对应位置的块. *不移动*其它块. */
    @Override
    public Object pop(int index) {
        Block<K> block = _blocks[index];
        _blocks[index] = null;
        _indexes[index] = null; // 对应位置的索引区域也要跟随弹出.
        _length -= 1;
        return block;
    }

    /** 删除并返回{@code index}对应位置的块. 同时重新排列其余的块. */
    @Override
    public Object removeAt(int index) {
        Block<K> block = _blocks[index];
        for (int i = index + 1; i < _length; i++) {
            _blocks[i - 1] = _blocks[i];
            _blocks[i].setParent(this, i - 1);
            _indexes[i - 1] = _indexes[i];
        }
        this.pop(_length - 1);
        return block;
    }

    /** 获取当前块的索引区域. */
    @Override
    public Range<K> blockRange() {
        if (_length == 0) { return null; }
        Block<K> minNode = _blocks[0], maxNode = _blocks[_length - 1];
        K minKey = minNode.blockRange()._left, maxKey = maxNode.blockRange()._right;
        return new Range<>(minKey, maxKey);
    }

    /** 获取当前块中每个子块对应的索引区域. */
    @Override
    public Range<K>[] subRanges() { return _indexes; }

    /** 返回当前块中所存的块的引用. */
    public Block<K>[] subBlocks() { return _blocks; }

    /** 获取当前表块对应的上级索引块. */
    @Override
    public IndexBlock<K> parent() { return _parent; }

    /** 获取当前块在对应的上级索引块中的位置. */
    @Override
    public int loc() { return _loc; }

    /** 为当前索引块设立对应的上级索引块以及该块在其中的位置. */
    @Override
    public void setParent(IndexBlock<K> parent, int loc) {
        _parent = parent;
        _loc = loc;
    }

    /** 更新当前结点在对应位置的索引区间. */
    @Override
    public void setRange(int pos, Range<K> range) { _indexes[pos] = range; }

    /** 更新当前索引块中索引区域的个数. */
    @Override
    public void setLength(int length) { _length = length; }

    /** 向索引块中添加下一级的块. */
    @Override
    public void add(Object node) {
        Block<K> block = (Block<K>) node;
        // 更新块以及索引
        _blocks[_length] = block;
        _indexes[_length] = block.blockRange();
        // 为新加入的下一级的块设置parent结点以及位置
        block.setParent(this, _length);
        _length += 1;
    }

    /**
     * 将给定块插入到当前索引块中指定的位置.
     * @param block 给定待插入的块.
     * @param pos 当前结点中指定插入的位置.
     */
    @Override
    public void addAt(Object block, int pos) {
        Block<K> targetBlock = (Block<K>) block;
        // 腾出空间
        for (int i = _length; i > pos; i--) {
            _blocks[i] = _blocks[i - 1];
            _blocks[i - 1].setParent(this, i);
            _indexes[i] = _indexes[i - 1];
        }
        // 插入块
        _blocks[pos] = targetBlock;
        _indexes[pos] = targetBlock.blockRange();
        // 更新parent和位置pos
        targetBlock.setParent(this, pos);
        _length += 1;
    }

    /** 返回当前块的文字视图. */
    @Override
    public String toString() {
        if (_length == 0) { return ""; }
        LinkedList<String> rangesInString = new LinkedList<>();
        for (int i = 0; i < _length; i++) {
            Range<K> range = _indexes[i];
            // 子索引块可能为空(已初始化但不包括任何下级索引)，此时对应的索引区域为null
            rangesInString.add(range != null ? range.toString() : "");
        }
        return indexBlockView(rangesInString);
    }

    /**
     * 构造函数的*辅助函数*，初始化当前索引块的索引区域以及块的区域.
     * @param order 对应B+树的阶.
     */
    private void initData(int order) {
        _length = 0;
        _indexes = new Range[this.M];
        _blocks = new Block[this.M];
    }
}
