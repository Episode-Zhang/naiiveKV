package Index;

import static Utils.Utils.*;
import KVTable.Table;
import static View.BPTView.*;
import java.util.ArrayList;

/**
 * 用于为磁盘上的K-V表提供多级索引结构的B+树.
 * <p>
 * @param <K> 对应外部结点所存放的，K-V表中的键的类型.
 * @param <V> 对应外部结点所存放的，K-V表中的值的类型.
 * @author Episode-Zhang
 * @version 1.0
 */
public class BPlusTree<K, V> implements Index<K, V> {

    /** B+树底层是若干个页组成的一根链表. */
    private final ArrayList<Page<K, V>> _pages;

    /** B+树的根节点. */
    private IndexBlock<K> _root;

    /** B+树存放的表的张数. */
    private int _size;

    /** B+树的阶. */
    private final int M;

    /** B+树中每张表允许的最大容量(定义为记录条数). */
    private final int CAPACITY;

    /** B+树中每张表的容量上限因子, 当某张大表达到上限时会将自己分成两张小表. */
    private final double UPPERTHRESHOLD = 0.8;

    /** B+树中每张表的容量下限因子, 当某两张小表达到下限时会将自己合成一张大表. */
    private final double LOWERTHRESHOLD = 0.2;

    /**
     * B+树的构造函数，需要在初始化时指定B+树的阶以及每张表的容量.
     * <p>
     * B+树在构造化时会通过调用{@link #init}函数，来建立一个空页区与索引区.
     * @param order B+树的阶.
     * @param capacity 每张表的最大容量，超过这个值的80%时会发生表内分裂.
     */
    public BPlusTree(int order, int capacity) {
        this.M = order;
        this.CAPACITY = capacity;
        _pages = new ArrayList<>();
        _size = 0;
        init();
    }

    /** 返回B+树中表的张数. */
    @Override
    public int size() { return _size; }

    /** 判断并返回当前索引区是否为空. */
    public boolean empty() { return _size == 0; }

    /** 返回当前B+树的顶级索引范围. */
    public Range<K> indexRange() { return _root.blockRange(); }

    /**
     * 将缓冲区中达到阈值的表写入索引区.
     * @param fullTable 缓冲区中达到阈值的KV表.
     */
    @Override
    public void write(Table<K, V> fullTable) {
        // 新表总是在末尾追加.
        Page<K, V> tailPage = _pages.get(_pages.size() - 1);
        insertTable(tailPage, tailPage.length(), fullTable);
    }

    /**
     * 插入一条记录. 可以认为记录的键一定在B+树的索引范围内.
     * @param key 待插入记录的键.
     * @param value 待插入记录的值.
     * @throws IllegalArgumentException 如果当前键在B+树索引区间的右侧.
     */
    @Override
    public void insert(K key, V value) throws IllegalArgumentException {
        if (greaterThan(key, _root.blockRange()._right)) {
            String errorMsg = String.format("""
                    The key is at the right side of the index range, and should be inserted into buffer.
                    key: %s
                    Index Range of B+Tree: %s
                    """, key, _root.blockRange());
            throw new IllegalArgumentException(errorMsg);
        }
        Page<K, V> targetPage = findInsert(_root, key);
        insertRecord(targetPage, key, value);
    }

    /** 根据键在数据库中查找对应值，若无相关记录则返回null. */
    @Override
    public V get(K key) {
        if (!_root.blockRange().contains(key)) { return null; }
        Page<K, V> page = find(_root, key);
        Range<K>[] range = page.subRanges();
        Table<K, V>[] table = page.tables();
        for (int i = 0; i < page.length(); i++) {
            if (range[i].contains(key)) {
                Table<K, V> targetTable = table[i];
                return targetTable.get(key);
            }
        }
        return null;
    }

    /**
     * 删除一条记录.
     * @param key 待删除记录对应的键.
     * @return 删除掉的记录中的值. 如果对应记录不存在，则返回null.
     */
    @Override
    public V delete(K key) {
        if (!_root.blockRange().contains(key)) { return null; }
        Page<K, V> page = find(_root, key);
        Range<K>[] range = page.subRanges();
        Table<K, V>[] table = page.tables();
        for (int i = 0; i < page.length(); i++) {
            if (range[i].contains(key)) {
                Table<K, V> targetTable = table[i];
                return targetTable.delete(key);
            }
        }
        return null;
    }

    /** 返回索引层级结构. */
    @Override
    public String indexView() { return this.toString(); }

    /**
     * 返回给定页id中指定位置的数据表的视图
     * @param pageId 数据表所在的页的id. 规定id为该表在链表{@code _pages}中的位置.
     * @param pos 所请求数据表在目标页中的位置.
     * @return 请求的数据表的视图.
     * @throws IllegalArgumentException 当请求的页id与表的位置不存在时.
     */
    @Override
    public String tableView(int pageId, int pos) throws IllegalArgumentException {
        if (pageId >= _pages.size()) {
            String errorMsg = String.format("""
                    The queried page id doesn't exists.
                    queried page id: %d
                    max page id existed: %d
                    """, pageId, _pages.size() - 1);
            throw new IllegalArgumentException(errorMsg);
        }
        Page<K, V> targetPage = _pages.get(pageId);
        if (pos >= targetPage.length()) {
            String errorMsg = String.format("""
                    The queried table doesn't exist.
                    queried table location: %d
                    the last legal table location is: %d
                    """, pos, targetPage.length() - 1);
            throw new IllegalArgumentException(errorMsg);
        }
        return targetPage.tables()[pos].toString();
    }

    @Override
    public String toString() {
        if (_size == 0) { return ""; }
        return viewInString(_root, _pages);
    }

    /** 给定键，查找其所在页，可以假定键总是能命中的. */
    private Page<K, V> find(IndexBlock<K> startLevel, K key) {
        Block<K> searchBlock = startLevel;
        while (!(searchBlock instanceof Page)) {
            Range<K>[] nextLevelIndexes = searchBlock.subRanges();
            Block<K>[] nextLevelBlocks = ((IndexBlock<K>) searchBlock).subBlocks();
            int indexesNum = searchBlock.length();
            for (int i = 0; i < indexesNum; i++) {
                // 命中索引区间，前往下一级索引.
                if (nextLevelIndexes[i].contains(key)) {
                    searchBlock = nextLevelBlocks[i];
                    break;
                }
            }
        }
        return (Page<K, V>) searchBlock;
    }

    /**
     * 为一个键寻找一个适当的表来进行插入操作. 定义“适当”为：
     * <p> 1. 若存在一个索引区间I可以覆盖key，则区间I为适当区间；
     * <p> 2. 若不存在这样的I，则取key右侧区间中的第一个区间I'为适当区间.
     * @param startBlock 寻找插入位置的入口结点.
     * @param key 待插入记录的键.
     * @return 用来插入对应记录的目标表.
     */
    private Page<K, V> findInsert(IndexBlock<K> startBlock, K key) {
        Block<K> searchBlock = _root;
        while (!(searchBlock instanceof Page)) {
            Range<K>[] nextLevelIndexes = searchBlock.subRanges();
            Block<K>[] nextLevelBlocks = ((IndexBlock<K>) searchBlock).subBlocks();
            int indexesNum = searchBlock.length();
            for (int i = 0; i < indexesNum; i++) {
                Range<K> index = nextLevelIndexes[i];
                // 命中索引区间，前往下一级索引.
                if (index.contains(key) || lessThan(key, index._left)) {
                    searchBlock = nextLevelBlocks[i];
                    break;
                }
            }
        }
        return (Page<K, V>) searchBlock;
    }

    /** 初始化B+树. 建立一个空页以及到空页的索引. */
    private void init() {
        _root = new IndexBlock<K>(this.M);
        Page<K, V> newPage = new Page<K, V>(this.M, this.CAPACITY, _root, 0);
        _pages.add(newPage);
        _root.add(newPage);
    }

    /** 在对应页中插入记录. 非分裂表内的插入不会影响已有索引的信息，若产生了表内分裂，则需要更新索引. */
    private void insertRecord(Page<K, V> page, K key, V value) {
        Range<K>[] ranges = page.subRanges();
        Table<K, V>[] tables = page.tables();
        for (int i = 0; i < page.length(); i++) {
            if (ranges[i].contains(key) || lessThan(key, ranges[i]._left)) {
                // 打开表，插入记录
                Table<K, V> target = tables[i];
                target.put(key,value);
                // 检查表是否需要分裂
                if (target.size() >= UPPERTHRESHOLD * CAPACITY) {
                    Table<K, V> split = target.split();
                    insertTable(page, i + 1, split);
                }
                // 更新表索引
                ranges[i] = new Range<>(target.minKey(), target.maxKey());
                break;
            }
        }
    }

    /** 在底层对应页的对应位置中加入新的表. */
    private void insertTable(Page<K, V> page, int pos, Table<K, V> table) {
        page.addAt(table, pos);
        _size += 1;
        // 页内分裂，将分裂出来的页加入链表，在父节点添加新的索引.
        if (page.length() == this.M) {
            Page<K, V> splitPage = (Page<K, V>) splitBlock(page);
            _pages.add( _pages.indexOf(page) + 1, splitPage);
            insertSplit(splitPage.parent(), splitPage);
            // 更新分裂结点的祖先索引
            updateIndex(splitPage);
        }
        // 更新原有页的祖先的索引.
        updateIndex(page);
    }

    /**
     * 在索引块中加入新分裂处的块.
     * @param start 加入分裂块的入口索引块.
     * @param block 待加入的新的分裂块.
     */
    private void insertSplit(IndexBlock<K> start, Block<K> block) {
        // 根节点发生上溢, 此时block是原根的后半段
        if (start == null) {
            IndexBlock<K> newRoot = new IndexBlock<K>(this.M);
            // 新的根对原来的根以及分裂出的后半段建立索引.
            newRoot.add(_root);
            newRoot.add(block);
            _root = newRoot;
            return;
        }
        // 在父结点对应位置进行插入
        start.addAt(block, block.loc());
        // 插入后，更新子块在父结点中的位置
        Block<K>[] subBlocks = start.subBlocks();
        for (int i = block.loc() + 1; i < start.length(); i++) {
            subBlocks[i].setParent(start, i);
        }
        // 如果索引块中的索引区域达到M，则进行块的分裂，并将新的块插入上层对应索引.
        if (start.length() == this.M) {
            IndexBlock<K> splitBlock = (IndexBlock<K>) splitBlock(start);
            // 分裂后，原先索引块中子块记录的位置需要重新设置
            rearrange(splitBlock);
            // 插入，更新分裂结点的祖先索引.
            insertSplit(splitBlock.parent(), splitBlock);
            updateIndex(splitBlock);
        }
        // 更新插入结点的祖先索引
        updateIndex(start);
    }

    /** 对一个分裂出来的索引块中的子块进行位置更新. */
    private void rearrange(IndexBlock<K> splitBlock) {
        for (int i = 0; i < splitBlock.length(); i++) {
            Block<K> subBlock = (Block<K>) splitBlock.get(i);
            subBlock.setParent(splitBlock, i);
        }
    }

    /** 将一满块进行对半划分并返回后半部分组成的新块. */
    private Block<K> splitBlock(Block<K> fullBlock) {
        Block<K> latter;
        // 块类型适配
        if (fullBlock instanceof Page) { latter = new Page<K, V>(this.M, this.CAPACITY); }
        else { latter = new IndexBlock<K>(this.M); }
        latter.setParent(fullBlock.parent(), fullBlock.loc() + 1);
        // 搬运后半部分[M/2, M-1]，返回
        for(int i = this.M / 2; i < M; i++) { latter.add(fullBlock.pop(i)); }
        return latter;
    }

    /** 在B+树的状态发生改变时，向上更新{@code start}结点的祖先对应的索引区间. */
    private void updateIndex(final Block<K> start) {
        Block<K> node = start, ancestor = node.parent();
        // 当前块为非根结点
        while (ancestor != null) {
            ancestor.setRange(node.loc(), node.blockRange());
            node = ancestor;
            ancestor = node.parent();
        }
        // 当前块为根节点
    }
}
