package KVTable;

import static View.TableView.viewInString;
import static Utils.Utils.*;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;

/**
 * 继承了红黑树K-V表，并且缓存了最大/最小键，支持在原有红黑树基础上对分重构的K-V表.
 * <p>
 * 用于适配类 {@link Index.Page} 中存储的K-V表的要求.
 * @param <K> K-V表中键的类型.
 * @param <V> K-V表中值的类型.
 * @author Episode-Zhang
 * @version 1.0
 */
public class Table<K, V> extends RBT<K, V> {

    /** 存放当前表中最大的key. */
    private K _minKey;

    /** 存放当前表中最大的key. */
    private K _maxKey;

    /** 默认构造函数. */
    public Table() { super(); }

    /** 用一棵已知的结点表示的红黑树来初始化一张表. */
    private Table(RBTNode<K, V> root, RBTNode<K, V> NIL, int rootSize) { super(root, NIL, rootSize); }

    /** 获取当前表中键的最小值. */
    public K minKey() { return _minKey; }

    /** 获取当前表中键的最大值. */
    public K maxKey() { return _maxKey; }

    @Override
    public void put(K key, V value) {
        // 检查新插入的key
        if (_minKey == null || lessThan(key, _minKey)) { _minKey = key; }
        if (_maxKey == null || greaterThan(key, _maxKey)) { _maxKey = key; }
        // 其它情况和普通红黑树插入无异
        super.put(key, value);
    }

    @Override
    public V delete(K key) {
        RBTNode<K, V> target = find(_root, key);
        if (target == null) { return null; }
        super.removeNode(target);
        // 查看被删除键是否为最大/最小键，是则更新
        if (key.equals(_minKey)) { _minKey = min(_root); }
        if (key.equals(_maxKey)) { _maxKey =  max(_root); }
        return target._value;
    }

    /** 获取表中前n个键. */
    public K[] keys(int n) {
        KeySet<K> keys = new KeySet<K>(_root, n, (key) -> (key));
        return (K[]) keys.get().toArray();
    }

    /** 获取表中前n个值. */
    public V[] values(int n) {
        ValueSet<V> values = new ValueSet<V>(_root, n, (value) -> (value));
        return (V[]) values.get().toArray();
    }

    /**
     * 将当前的红黑树按*根结点*的键划分成左右两棵子树，保证左子树中所有的键小于等于根节点，
     * 右子树中所有的键大于根节点，令当前红黑树为划分出的左子树，然后返回右子树.
     * @return 划分后的右半边的红黑树.
     * @throws RuntimeException 当前红黑树不足以支持划分时.
     */
    public Table<K, V> split() {
        if (_size < 3) {
            String errorMsg = String.format("""
                            As for split, the size of table is supposed to be at least 3. now is
                            size: %d
                            Thus, table cannot be split.""",
                    _size);
            throw new RuntimeException(errorMsg);
        }
        // 因为左右子树黑高相等，所以划分后无需再平衡.
        // 记录信息
        int rightTreeSize = new KeySet<K>(_root._right, _size, (key) -> (key)).keys.size();
        K rootKey = _root._key;
        V rootValue = _root._value;
        // 划分右子树
        Table<K, V> rightTree = new Table<K, V>(_root._right, this.NIL, rightTreeSize);
        this._root._right = this.NIL;
        // 令当前红黑树为左子树
        this._root = _root._left;
        this._root._isRed = false;
        this._root._parent = this.NIL;
        // 解耦后，根节点加入右子树
        rightTree.put(rootKey, rootValue);
        // 更新信息
        rightTree._minKey = rootKey;
        rightTree._maxKey = _maxKey;
        this._maxKey = max(_root);
        this._size -= rightTree.size();
        return rightTree;
    }

    /**
     * 将给定表并入当前表. 且保证
     * <p> 1. 当前表的索引区域和另一张表的索引区域*无交集*；
     * <p> 2. table的黑高小于当前表(总是小表加入大表)；
     * <p> 3. 不允许合并过程中出现空表.
     * @param table 将要与当前表进行合并的另一张表.
     * @deprecated 因为各个红黑树NIL结点的不同一性导致该方法实现时具有困难.
     * @throws RuntimeException 若待合并的表中出现空表或重复的键.
     */
    @Deprecated
    public void buggyMerge(Table<K, V> table) throws RuntimeException {
        if (_size == 0 || table.size() == 0) {
            String errorMsg = String.format("""
                    Empty tables are not allowed.
                    Size of this to-be-merged table: %d
                    Size of parameter merging table: %d
                    """, _size, table._size);
            throw new RuntimeException(errorMsg);
        }
        // 如果小表，直接散装搬入
        if (table.size() < 2) {
            Object[] tableKeys = table.keys();
            for (Object key : tableKeys) {
                this.put((K) key, table.delete((K) key));
            }
            return;
        }
        // 当前表的索引区间在table的左侧
        if (lessThan(_maxKey, table._minKey)) {
            K partitionKey = table._minKey;
            V partitionValue = table.delete(table._minKey);
            joinRight(partitionKey, partitionValue, table._root);
            // 更新信息
            this._size += table._size + 1;
            table._root = table.NIL;
            table._size = 0;
            _maxKey = max(_root);
        } else if (greaterThan(_minKey, table._maxKey)) { // 当前表的区间索引在table的右侧
            K partitionKey = this._minKey;
            V partitionValue = this.delete(_minKey);
            joinLeft(partitionKey, partitionValue, table._root);
            // 更新信息
            this._size += table._size + 1;
            table._root = table.NIL;
            table._size = 0;
            _minKey = min(_root);
        } else {
            String errorMsg = String.format("""
                            There are duplicated keys in both tables. Please check:
                            Max key in this table: %s
                            Min key in this table: %s
                            Max key in parameter table: %s
                            Min key in parameter table: %s""",
                    _maxKey, _minKey, table._maxKey, table._minKey);
            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * 按照顺序读入读出的方法将给定表并入当前表. 且保证
     * <p> 1. 当前表的索引区域和另一张表的索引区域*无交集*；
     * <p> 2. table的黑高小于当前表(总是小表加入大表)；
     * @param table 将要与当前表进行合并的另一张表.
     * @throws RuntimeException 若待合并的表中出现空表或重复的键.
     */
    public void merge(Table<K, V> table) throws RuntimeException {
        moveTo(table._root, table._root, table.NIL, this);
    }

    /** 返回一张表格的视图，通过打印表格中的前10项记录条数. */
    @Override
    public String toString() {
        // 取前10项键和值
        final int topN = 10;
        KeySet<String> topNKeys = new KeySet<String>(_root, topN, Objects::toString);
        ValueSet<String> topNValues = new ValueSet<String>(_root, topN, Object::toString);
        LinkedList<String> keys = topNKeys.get();
        LinkedList<String> values = topNValues.get();
        // 返回文字视图
        StringBuilder view = new StringBuilder(viewInString(keys, values));
        if (_size > 10) {
            view.append("...(Rest of the records are hidden)\n");
        }
        return view.toString();
    }

    /** 根据所给定的当前表中最小键所在的结点找到并返回第二小的键. */
    private K nextMinKey(final RBTNode<K, V> minNode) {
        if (minNode == _root) { return null; }
        RBTNode<K, V> parent = minNode._parent;
        RBTNode<K, V> sibling = parent._right;
        if (sibling == this.NIL) { return parent._key; }
        K siblingMinKey = min(sibling);
        return lessThan(siblingMinKey, parent._key) ? siblingMinKey : parent._key;
    }

    private K nextMaxKey(final RBTNode<K, V> maxNode) {
        if (maxNode == _root) { return null; }
        RBTNode<K, V> parent = maxNode._parent;
        RBTNode<K, V> sibling = parent._left;
        if (sibling == this.NIL) { return parent._key; }
        K siblingMaxKey = max(sibling);
        return greaterThan(siblingMaxKey, parent._key) ? siblingMaxKey : parent._key;
    }

    /** 获取以{@code start} 为根节点的树中的最小键. */
    private K min(final RBTNode<K, V> start) {
        if (start == this.NIL) { return null; }
        RBTNode<K, V> node = start;
        while (node._left != this.NIL) {
            node = node._left;
        }
        return node._key;
    }

    /** 获取以{@code start} 为根节点的树中的最大键. */
    private K max(final RBTNode<K, V> start) {
        if (start == this.NIL) { return null; }
        RBTNode<K, V> node = start;
        while (node._right != this.NIL) {
            node = node._right;
        }
        return node._key;
    }

    /**
     * 规定：
     *   <p> 1. 当前表中所有的键都小于 key；
     *   <p> 2. table 中所有的键都大于 key；
     *   <p> 3. 给定的table的黑高小于当前表.
     * <p>
     * 将给定的table表合并入当前的表中.
     * @param key 用户划分的键.
     * @param value 和键匹配的值
     * @param table 待合并的红黑树，满足其中所有的键都大于 key.
     * @throws IllegalArgumentException 如果给定的表的黑高大于当前表的黑高.
     */
    private void joinRight(K key, V value, RBTNode<K, V> table) {
        RBTNode<K, V> thisNode = _root;
        int thisHeight = blackHeight(thisNode), mergedHeight = blackHeight(table);
        if (thisHeight <= mergedHeight) {
            String errorMsg = String.format("""
                It is supposed that the merging table's black height is greater than one be merged.
                The black height of this to-be-merged table is: %d
                The black height of parameter merging table is: %d
                """, thisHeight, mergedHeight);
            throw new IllegalArgumentException(errorMsg);
        }
        while (thisHeight >= mergedHeight) {
            // 黑高相等的情况下进行合并
            if (thisHeight == mergedHeight) {
                // 新插入的结点为红色
                RBTNode<K, V> mergedNode = new RBTNode<>(key, value, true);
                // 连接当前表的结点以及给定表的结点，作为整体插入回当前树中.
                mergedNode._left = thisNode;
                mergedNode._right = table;
                thisNode._parent._right = mergedNode;
                mergedNode._parent = thisNode._parent;
                // 调整可能出现的双红冲突
                fixupInsertion(mergedNode);
                return;
            }
            // 否则深入当前树中的右子树
            thisNode = thisNode._right;
            thisHeight = blackHeight(thisNode);
        }
    }

    /**
     * 规定：
     *   <p> 1. 当前表中所有的键都大于 key；
     *   <p> 2. table 中所有的键都小于 key；
     *   <p> 3. 给定的table的黑高小于当前表.
     * <p>
     * 将给定的table表合并入当前的表中.
     * @param key 用户划分的键.
     * @param value 和键匹配的值
     * @param table 待合并的红黑树，满足其中所有的键都小于 key.
     * @throws IllegalArgumentException 如果给定的表的黑高大于当前表的黑高.
     */
    private void joinLeft(K key, V value, RBTNode<K, V> table) {
        RBTNode<K, V> thisNode = _root;
        int thisHeight = blackHeight(thisNode), mergedHeight = blackHeight(table);
        if (thisHeight <= mergedHeight) {
            String errorMsg = String.format("""
                It is supposed that the merging table's black height is greater than one be merged.
                The black height of this to-be-merged table is: %d
                The black height of parameter merging table is: %d
                """, thisHeight, mergedHeight);
            throw new IllegalArgumentException(errorMsg);
        }
        while (thisHeight >= mergedHeight) {
            // 黑高相等的情况下进行合并
            if (thisHeight == mergedHeight) {
                // 新插入的结点为红色
                RBTNode<K, V> mergedNode = new RBTNode<>(key, value, true);
                // 连接当前表的结点以及给定表的结点，作为整体插入回当前树中.
                mergedNode._left = table;
                mergedNode._right = thisNode;
                thisNode._parent._left = mergedNode; // ! TODO 如果thisNode = root ? 中转站变成NIL了
                mergedNode._parent = thisNode._parent;
                // 调整可能出现的双红冲突
                fixupInsertion(mergedNode);
                return;
            }
            // 否则深入当前树中的左子树
            thisNode = thisNode._left;
            thisHeight = blackHeight(thisNode);
        }
    }

    /** 中序遍历一个根节点并把记录移入另一张表中. */
    private void moveTo(RBTNode<K, V> node, final RBTNode<K, V> root, final RBTNode<K, V> NIL, Table<K, V> container) {
        if (node == NIL) { return; }
        moveTo(node._left, root, NIL, container);
        container.put(node._key, node._value);
        if (node != root) {
            if (node == node._parent._left) { node._parent._left = NIL; }
            else if (node == node._parent._right) { node._parent._right = NIL; }
        }
        moveTo(node._right, root, NIL, container);
    }


    /**
     * 用于遍历当前表的键的集合，并且返回按*给定规则*处理后的前n项.
     * @param <R> 返回的集合中的元素的类型，若元素为键本身则为{@code <K>}, 否则需指明类型.
     */
    private class KeySet<R> {

        /** 存放指定前n个键按制定规则处理后得到的结果的容器. */
        private final LinkedList<R> keys = new LinkedList<>();

        /** 用于辅助循环遍历. */
        private LinkedList<RBTNode<K, V>> stack = new LinkedList<>();

        /**
         * 默认构造函数. 在初始化的时候即使用给定规则完成指定键的遍历.
         * @param root 待遍历的子树的入口处.
         * @param n 待获取的前n个键.
         * @param operator 用于处理每个键的规则, 是一个 K -> R 的一元运算.
         */
        public KeySet(RBTNode<K, V> root, int n, Function<K, R> operator) {
            if (operator == null) {
                throw new IllegalArgumentException("请指定处理键的对应法则.");
            }
            traverseInorder(root, n, operator);
        }

        /** 返回表中前n个键的集合. */
        public LinkedList<R> get() { return keys; }

        /**
         * 用于遍历获得前n个键并根据指定规则对其进行处理.
         * @param node 遍历入口结点.
         * @param n 待获取的前n个键.
         * @param operator 用于处理每个键的规则, 是一个 K -> R 的一元运算.
         */
        private void traverseInorder(RBTNode<K, V>node, int n, Function<K, R> operator) {
            while (node != NIL || stack.size() > 0) {
                // 向左试探
                while (node != NIL) {
                    stack.push(node);
                    node = node._left;
                }
                // 回溯，将键加入链表
                node = stack.pop();
                keys.add(operator.apply(node._key));
                // 检查项数
                if (keys.size() == n) { stack = null; return; }
                // 转向右侧
                node = node._right;
            }
        }
    }

    /**
     * 用于遍历当前表的值的集合，并且返回按给定规则处理后的前n项.
     * @param <R> 返回的集合中的元素的类型，若元素为键本身则为{@code <V>}, 否则需指明类型.
     */
    private class ValueSet<R> {

        /** 存放指定前n个值按制定规则处理后得到的结果的容器. */
        private final LinkedList<R> values = new LinkedList<>();

        /** 用于辅助循环遍历. */
        private LinkedList<RBTNode<K, V>> stack = new LinkedList<>();

        /**
         * 默认构造函数. 在初始化的时候即使用给定规则完成指定值的遍历.
         * @param root 待遍历的子树的入口处.
         * @param n 待获取的前n个值.
         * @param operator 用于处理每个值的规则, 是一个 V -> R 的一元运算.
         */
        public ValueSet(RBTNode root, int n, Function<V, R> operator) {
            if (operator == null) {
                throw new IllegalArgumentException("请指定处理值的对应法则.");
            }
            traverseInorder(root, n, operator);
        }

        /** 返回表中前n个值的集合. */
        public LinkedList<R> get() { return values; }

        /**
         * 用于遍历获得前n个值并根据指定规则对其进行处理.
         * @param node 遍历入口结点.
         * @param n 待获取的前n个值.
         * @param operator 用于处理每个值的规则, 是一个 K -> R 的一元运算.
         */
        private void traverseInorder(RBTNode<K, V> node, int n, Function<V, R> operator) {
            while (node != NIL || stack.size() > 0) {
                // 向左试探
                while (node != NIL) {
                    stack.push(node);
                    node = node._left;
                }
                // 回溯，将值加入链表
                node = stack.pop();
                values.add(operator.apply(node._value));
                // 检查项数
                if (values.size() == n) { stack = null; return; }
                // 转向右侧
                node = node._right;
            }
        }
    }
}
