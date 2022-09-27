package KVTable;

/** 红黑树结点类. 作用域为 {@link KVTable}
 * @param <K> 红黑树结点存放的键的类型
 * @param <V> 红黑树结点存放的值的类型
 * @author Episode-Zhang
 * @version 1.0
 */
class RBTNode<K, V> {
    /** 存放键. */
    public K _key;

    /** 存放值. */
    public V _value;

    /** 存放指向父结点和子结点的引用. */
    public RBTNode<K, V> _left, _right, _parent;

    /** 记录结点颜色. */
    public boolean _isRed;

    /** 默认构造函数，红黑树的结点默认染为黑色. */
    public RBTNode() { _isRed = false; }

    /** 构造函数 */
    public RBTNode(K key, V value, boolean isRed) {
        _key = key;
        _value = value;
        _isRed = isRed;
    }
}
