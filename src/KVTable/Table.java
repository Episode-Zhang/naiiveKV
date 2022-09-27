package KVTable;

/**
 * 继承了红黑树K-V表，并且缓存了最大/最小键，支持在原有红黑树基础上对分重构的K-V表.
 * <p>
 * 用于适配类 {@link Index.Entry.Page} 中存储的K-V表的要求.
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
        // 查看被删除键是否为最大/最小键
        if (key.equals(_minKey)) { _minKey = nextMinKey(target); }
        if (key.equals(_maxKey)) { _maxKey = nextMaxKey(target); }
        super.removeNode(target);
        return target._value;
    }

    /** 将当前表划分为两个子表，保证前一个子表中的所有键都小于后一个子表中的键，返回后一个子表. */
    public Table<K, V> split() {
        if (empty()) { return null; }
        // 记录原root信息，重置当前表中成员变量
        RBTNode<K, V> originalRoot = _root;
        _root = this.NIL;
        _size = 0;
        _minKey = _maxKey = null;
        // 搬运根节点及其右子树的所有的键值对
        Table<K, V> rightSideTable = new Table<K, V>();
        rightSideTable.put(originalRoot._key, originalRoot._value);
        traverseAndMove(originalRoot._right, rightSideTable);
        // 搬运左子树
        traverseAndMove(originalRoot._left, this);
        return rightSideTable;
    }

    /** 根据所给定的当前表中最小键所在的结点找到并返回第二小的键. */
    private K nextMinKey(final RBTNode<K, V> minNode) {
        RBTNode<K, V> parent = minNode._parent;
        RBTNode<K, V> sibling = parent._right;
        if (sibling == this.NIL) { return parent._key; }
        K siblingMinKey = min(sibling);
        return lessThan(siblingMinKey, parent._key) ? siblingMinKey : parent._key;
    }

    private K nextMaxKey(final RBTNode<K, V> maxNode) {
        RBTNode<K, V> parent = maxNode._parent;
        RBTNode<K, V> sibling = parent._left;
        if (sibling == this.NIL) { return parent._key; }
        K siblingMaxKey = max(sibling);
        return greaterThan(siblingMaxKey, parent._key) ? siblingMaxKey : parent._key;
    }

    /** 获取以{@code start} 为根节点的树中的最小键. */
    private K min(final RBTNode<K, V> start) {
        RBTNode<K, V> node = start;
        while (node._left != this.NIL) {
            node = node._left;
        }
        return node._key;
    }

    /** 获取以{@code start} 为根节点的树中的最大键. */
    private K max(final RBTNode<K, V> start) {
        RBTNode<K, V> node = start;
        while (node._right != this.NIL) {
            node = node._right;
        }
        return node._key;
    }

    /** 遍历一棵子树，并将其中所有的数据项写入另一个树.
     * @param origin 将要被读出数据项的树.
     * @param container 将要被写入数据项的树.
     */
    private void traverseAndMove(final RBTNode<K, V> origin, final Table<K, V> container) {
        if (origin == this.NIL) { return; }
        traverseAndMove(origin._left, container);
        // 移动数据项
        container.put(origin._key, origin._value);
        if (origin == _root) { _root = this.NIL; }
        if (origin._parent._left == origin) { origin._parent._left = this.NIL; }
        if (origin._parent._right == origin) { origin._parent._right = this.NIL; }
        traverseAndMove(origin._right, container);
    }
}
