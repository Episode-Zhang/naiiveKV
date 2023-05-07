package KVTable;

import static View.TableView.viewInString;
import static Utils.Utils.*;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Function;
import java.io.*;

/**
 * 继承了红黑树K-V表，并且缓存了最大/最小键，支持在原有红黑树基础上对分重构的K-V表.
 * 1.1在原版的基础上其父类RBT支持了序列化与反序列化存储
 * <p>
 * 用于适配类 {@link Index.Page} 中存储的K-V表的要求.
 * @param <K> K-V表中键的类型.
 * @param <V> K-V表中值的类型.
 * @author Episode-Zhang
 * @version 1.1
 */
public class Table<K, V> extends RBT<K, V> {

    /** 存放当前表中最小的key. */
    private K _minKey;

    /** 存放当前表中最大的key. */
    private K _maxKey;

    /** 存放当前表对应的文件名，规则：文件名 = this.hashCode().bin */
    private final String _filename;

    /** 默认构造函数. */
    public Table() {
        super();
        _filename = String.format("%s.table", this.hashCode());
    }

    /** 用一棵已知的结点表示的红黑树来初始化一张表. */
    private Table(RBTNode<K, V> root, RBTNode<K, V> NIL, int rootSize) {
        super(root, NIL, rootSize);
        _filename = String.format("%s.bin", this.hashCode());
    }

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

    /**
     * 关闭当前Table，将Table中所有的K-V对写入项目根路径下的data目录，随后将root置空.
     * @throws IOException 发生IO异常时抛出.
     */
    public void close () throws IOException {
        // 写入磁盘
        String path = String.format("./data/%s", _filename);
        try(FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(this);
            oos.flush();
        }
        // 置空root
        this._root = null;
    }

    /**
     * 打开当前的Table，将Table中所有的K-V对从根路径的data目录中对应的文件加载回内存里.
     * @throws IOException 发生IO异常时抛出.
     * @throws ClassNotFoundException 发生类加载异常时抛出.
     */
    public void open() throws IOException, ClassNotFoundException {
        Table<K, V> inDiskTable;
        String path = String.format("./data/%s", _filename);
        try (FileInputStream fis = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            inDiskTable = (Table<K, V>) ois.readObject();
        }
        _root = inDiskTable._root;
        NIL = inDiskTable.NIL;
    }

    /** 判断根节点是否为空 */
    public boolean nullRoot() {
        return _root == null;
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
        public ValueSet(RBTNode<K, V> root, int n, Function<V, R> operator) {
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
