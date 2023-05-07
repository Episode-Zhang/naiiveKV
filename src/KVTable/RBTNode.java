package KVTable;

import java.io.Serializable;

/**
 * 红黑树结点类. 作用域为 {@link KVTable}
 * 1.1在原版的基础上支持了结点的序列化与反序列化存储
 *
 * @param <K> 红黑树结点存放的键的类型
 * @param <V> 红黑树结点存放的值的类型
 * @author Episode-Zhang
 * @version 1.1
 */
class RBTNode<K, V> implements Serializable {
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
