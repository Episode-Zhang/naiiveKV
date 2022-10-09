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

    /**
     * B+树的构造函数，需要在初始化时指定B+树的阶以及每张表的容量. 规定B+树的阶至少为4.
     * <p>
     * B+树在构造化时会通过调用{@link #init}函数，来建立一个空页区与索引区.
     * @param order B+树的阶.
     * @param capacity 每张表的最大容量，超过这个值的80%时会发生表内分裂.
     * @throws IllegalArgumentException 如果当前B+树的阶小于4时.
     */
    public BPlusTree(int order, int capacity) {
        if (order < 4) {
            String errorMsg = String.format("""
                    Order of B+ tree should at least be 4. Got
                    order: %d
                    """, order);
            throw new IllegalArgumentException(errorMsg);
        }
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
        updateIndex(targetPage);
    }

    /** 根据键在数据库中查找对应值，若无相关记录则返回null. */
    @Override
    public V get(K key) {
        if (_size == 0 || !_root.blockRange().contains(key)) { return null; }
        V value = null;
        Page<K, V> page = find(_root, key);
        if (page != null && page.length() > 0) {
            Range<K>[] range = page.subRanges();
            for (int i = 0; i < page.length(); i++) {
                if (range[i].contains(key)) {
                    Table<K, V> targetTable = (Table<K, V>) page.get(i);
                    value = targetTable.get(key);
                }
            }
        }
        return value;
    }

    /**
     * 删除一条记录.
     * @param key 待删除记录对应的键.
     * @return 删除掉的记录中的值. 如果对应记录不存在，则返回null.
     */
    @Override
    public V delete(K key) {
        if (_size == 0 || !_root.blockRange().contains(key)) { return null; }
        Page<K, V> page = find(_root, key);
        if (page != null && page.length() > 0) {
            return removeKey(page, key);
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
        if (_size == 0) { return "(empty)"; }
        return viewInString(_root, _pages);
    }

    /** 给定键，查找其所在页，可以假定键总是能命中的. */
    private Page<K, V> find(IndexBlock<K> startLevel, K key) {
        Block<K> searchBlock = startLevel;
        while (!(searchBlock instanceof Page)) {
            int indexesNum = searchBlock.length();
            Range<K>[] nextLevelIndexes = searchBlock.subRanges();
            for (int i = 0; i < indexesNum; i++) {
                if (nextLevelIndexes[i].contains(key)) {
                    searchBlock = (Block<K>) searchBlock.get(i);
                    break;
                }
                // 如果当前索引区域包括key，但子索引不包括，则实际上key不在当前索引区中
                if (i == indexesNum - 1) { return null; }
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
        Block<K> searchBlock = startBlock;
        while (!(searchBlock instanceof Page)) {
            Range<K>[] nextLevelIndexes = searchBlock.subRanges();
            int indexesNum = searchBlock.length();
            for (int i = 0; i < indexesNum; i++) {
                Range<K> index = nextLevelIndexes[i];
                // 命中索引区间，前往下一级索引.
                if (index.contains(key) || lessThan(key, index._left)) {
                    searchBlock = (Block<K>) searchBlock.get(i);
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
        for (int i = 0; i < page.length(); i++) {
            if (ranges[i].contains(key) || lessThan(key, ranges[i]._left)) {
                // 打开表，插入记录
                Table<K, V> target = (Table<K, V>) page.get(i);
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


    /** 在给定表中删除给定键对应的记录，返回值，若无相关记录则返回null. */
    private V removeKey(Page<K, V> page, K key) {
        Range<K>[] range = page.subRanges();
        int tablePos;
        for (int i = 0; i < page.length(); i++) {
            if (range[i].contains(key)) {
                Table<K, V> targetTable = (Table<K, V>) page.get(i);
                tablePos = i;
                V value = targetTable.delete(key);
                if (value == null) { break; } // 对应表中不存在该记录，直接返回空
                // 删除键后若当前表空且表的个数大于1，则删除表.
                if (targetTable.empty()) {
                    removeTable(page, tablePos);
                } else { // 否则，删除可能影响整体的状态，需要检查索引
                    if (range[i]._left != targetTable.minKey() || range[i]._right != targetTable.maxKey()) {
                        range[i]._left = targetTable.minKey();
                        range[i]._right = targetTable.maxKey();
                        int pagePos = page.loc();;
                        if (page.blockRange() != page.parent().subRanges()[pagePos]) {
                            updateIndex(page);
                        }
                    }
                }
                return value;
            }
        }
        return null;
    }

    /** 将一张表从给定的页的指定位置中删除. */
    private void removeTable(Page<K, V> page, int pos) {
        page.removeAt(pos); // 删除表
        _size -= 1;
        // 页中表的个数大于等于 M/2，更新上级索引，直接返回
        if (page.length() >= this.M / 2) {
            updateIndex(page);
            return;
        }
        // 小于 M/2，根据是否在根处，直接删除or请求前驱/后继或合并
        IndexBlock<K> parent = page.parent();
        // 无兄弟可以请求数据项，直接返回
        if (parent.length() == 1) {
            return;
        }
        Page<K, V> sibling;
        // 除非当前页已是上级索引中的最后一块，否则兄弟页总是位于右侧.
        if (page.loc() < parent.length() - 1) {
            sibling = (Page<K, V>) parent.get(page.loc() + 1);
            // 向兄弟请求后继页
            if (sibling.length() > this.M / 2) {
                moveSuccessor(page, sibling);
                updateIndex(sibling); // 更新兄弟的上级索引
            } else { // 将兄弟向当前页合并
                mergeBlock(page, sibling);
                _pages.remove(sibling); // 在链表中删除记录
                removeBlock(sibling); // 递归删除空页
            }
            updateIndex(page);
        } else {
            sibling = (Page<K, V>) parent.get(page.loc() - 1);
            // 向兄弟请求前驱页
            if (sibling.length() > this.M / 2) {
                movePredecessor(page, sibling);
                updateIndex(page);
            } else { // 将当前页向兄弟合并
                mergeBlock(sibling, page);
                _pages.remove(page);
                removeBlock(page);
            }
            updateIndex(sibling); // 统一更新兄弟的索引
        }
    }

    /** 当sibling为page右侧的兄弟且满足其内部表的数量大于M/2时，将sibling的第一张表移入
     * page的尾部. */
    private void moveSuccessor(Page<K, V> page, Page<K, V> sibling) {
        Table<K, V> successorTable = (Table<K, V>) sibling.removeAt(0);
        page.add(successorTable);
    }

    /** 当sibling为block右侧的兄弟且满足其内部子块的数量大于M/2时，将sibling中的第一个子块
     * 移入block的尾部. */
    private void moveSuccessor(Block<K> block, Block<K> sibling) {
        Block<K> successorBlock = (Block<K>) sibling.removeAt(0);
        block.add(successorBlock);
    }

    /** 当sibling为page左侧的兄弟且满足其内部表的数量大于M/2时，将sibling的最后一张表移入
     * page的头部. */
    private void movePredecessor(Page<K, V> page, Page<K, V> sibling) {
        Table<K, V> predecessorTable = (Table<K, V>) sibling.removeAt(sibling.length() - 1);
        page.addAt(predecessorTable, 0);
    }

    /** 当sibling为block左侧的兄弟且满足其内部子块的数量大于M/2时，将sibling中的最后一个子块
     * 移入block的头部. */
    private void movePredecessor(Block<K> block, Block<K> sibling) {
        Block<K> successorBlock = (Block<K>) sibling.removeAt(sibling.length() - 1);
        block.addAt(successorBlock, 0);
    }

    /** 将块left与right进行合并，且为right合入left. 定义“合并”为将right中的索引和数据
     * 移入left. */
    private void mergeBlock(Block<K> left, Block<K> right) {
        assert left.blockRange().compareTo(right.blockRange()) < 0;
        int length = right.length();
        for (int i = 0; i < length; i++) {
            left.add(right.pop(i));
        }
    }

    /** 删除一个块. */
    private void removeBlock(Block<K> block) {
        IndexBlock<K> parent = block.parent();
        parent.removeAt(block.loc());
        // 根节点无需大于等于 M/2
        if (parent == _root) { return; }
        // 非根节点的parent，删除子块后长度大于等于 M/2 的，只需要更新parent以上的索引即可
        if (parent.length() >= this.M / 2) {
            updateIndex(parent);
            return;
        }
        // 检查父结点数据项少于 M/2，视情况向兄弟请求数据项或合并
        IndexBlock<K> grandparent = parent.parent();
        // 此时无兄弟结点可以请求数据，示其到根节点的深度决定是否删除
        if (grandparent.length() == 1) {
            return;
        }
        IndexBlock<K> sibling;
        // 检查父结点的sibling吗，总是取右侧的兄弟，除非本身已是最右侧
        if (parent.loc() < grandparent.length() - 1) {
            sibling = (IndexBlock<K>) grandparent.get(parent.loc() + 1);
            // 向sibling请求子块
            if (sibling.length() > this.M / 2) {
                moveSuccessor(parent, sibling);
                updateIndex(sibling); // 更新sibling的上级索引.
            } else { // 将sibling向parent合并，递归删除sibling
                mergeBlock(parent, sibling);
                removeBlock(sibling); // 删除空块
            }
            updateIndex(parent); // 更新父节点
        } else {
            sibling = (IndexBlock<K>) grandparent.get(parent.loc() - 1);
            // 向sibling请求子块
            if (sibling.length() > this.M / 2) {
                movePredecessor(parent, sibling);
            } else { // 将parent向sibling合并，递归删除parent
                mergeBlock(sibling, parent);
                removeBlock(parent); // 删除空块
            }
            updateIndex(sibling); // 统一更新兄弟的索引
        }
    }

}
