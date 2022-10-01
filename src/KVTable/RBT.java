package KVTable;

/**
 * 实现了KVTable所有接口的红黑树.
 * @param <K> K-V表中键的类型.
 * @param <V> K-V表中值的类型.
 * @author Episode-Zhang
 * @version 1.0
 */
public class RBT<K, V> implements KVTable<K, V> {

    /** 红黑树的根结点. */
    protected RBTNode<K, V> _root;

    /** 作为哨兵，红黑树的叶子结点以及树根的父节点默认为黑色的NIL. */
    protected final RBTNode<K, V> NIL;

    /** 结点个数. */
    protected int _size;

    /** 默认构造函数. */
    public RBT() {
        this.NIL = new RBTNode<K, V>();
        _root = this.NIL; // 树空时，默认存在一个不可见的叶子结点
        _size = 0;
    }

    /** 返回当前表中数据项的个数. */
    @Override
    public int size() { return _size; }

    /**
     * 根据给定的键查询当前表中是否有相关的记录.
     * @param key 用于查询记录的键.
     * @return 如果记录存在则返回true，否则false.
     */
    @Override
    public boolean contains(K key) {
        return find(_root, key) != null;
    }

    /**
     * 根据给定的键查询表中的值.
     * @param key 待查询的键.
     * @return 对应的值, 如果记录不存在则返回null.
     */
    @Override
    public V get(K key) {
        RBTNode<K, V> record = find(_root, key);
        return record == null ? null : record._value;
    }

    /**
     * 将给定的键值对存入表中. 需要保证给定的键不为{@code null}.
     * @param key 待插入表中的键.
     * @param value 待插入表中的值.
     * @throws IllegalArgumentException 如果给定的键为{@code null}.
     */
    @Override
    public void put(K key, V value) throws IllegalArgumentException {
        if (key == null) {
            throw new IllegalArgumentException("The parameter key cannot be null.");
        }
        insert(_root, key, value);
    }

    /**
     * 删除表中对应给定键的记录，并返回值.
     * @param key 待删除记录的键.
     * @return 被删除记录的值.
     */
    @Override
    public V delete(K key) {
        RBTNode<K, V> target = find(_root, key);
        if (target == null) {
            return null;
        }
        removeNode(target);
        return target._value;
    }

    /** 获取当前表中所有记录的键的集合. */
    @Override
    public Object[] keys() {
        return new KeySet().get();
    }

    /** 获取当前表中所有记录的值的集合. */
    @Override
    public Object[] values() {
        return new ValueSet().get();
    }

    /**
     * 判断两个键的大小关系.
     * <p>
     * 如果键的参数类型K已经定义了内部元素之间的序，则返回其比较结果；否则就将元素内建的hashCode
     * 的值的大小作为“序”来定义.
     * @param k1 待比较键的前者.
     * @param k2 待比较键的后者.
     * @return 前一个键是否小于后一个键，是返回true，否返回false.
     */
    protected boolean lessThan(K k1, K k2) {
        if (k1 instanceof Comparable && k2 instanceof Comparable) {
            return ((Comparable)k1).compareTo(k2) < 0;
        } else {
            return k1.hashCode() - k2.hashCode() < 0;
        }
    }

    /**
     * 判断两个键的大小关系.
     * <p>
     * 如果键的参数类型K已经定义了内部元素之间的序，则返回其比较结果；否则就将元素内建的hashCode
     * 的值的大小作为“序”来定义.
     * @param k1 待比较键的前者.
     * @param k2 待比较键的后者.
     * @return 前一个键是否大于后一个键，是返回true，否返回false.
     */
    protected boolean greaterThan(K k1, K k2) {
        if (k1 instanceof Comparable && k2 instanceof Comparable) {
            return ((Comparable)k1).compareTo(k2) > 0;
        } else {
            return k1.hashCode() - k2.hashCode() > 0;
        }
    }

    /**
     * 查找给定键对应的结点.
     * @param start 查找的起始位置.
     * @param key 要求查找的键.
     * @return 若查找命中则返回对应结点，否则返回null.
     */
    protected RBTNode<K, V> find(final RBTNode<K, V> start, K key) {
        RBTNode<K, V> node = start;
        while (node != this.NIL) {
            if (lessThan(key, node._key)) {
                node = node._left;
            } else if (greaterThan(key, node._key)) {
                node = node._right;
            } else {
                break;
            }
        }
        return node == this.NIL ? null : node;
    }

    /**
     * 将给定结点 node 的右孩子设置或替换为另一个给定的结点 beLinked.
     * @param node 右孩子待更新的结点.
     * @param beLinked 插入为右孩子的结点.
     */
    private void linkedRightSide(final RBTNode<K, V> node, final RBTNode<K, V> beLinked) {
        node._right = beLinked;
        beLinked._parent = node;
    }

    /**
     * 将给定结点 node 的左孩子设置或替换为另一个给定的结点 beLinked.
     * @param node 左孩子待更新的结点.
     * @param beLinked 插入为左孩子的结点.
     */
    private void linkedLeftSide(final RBTNode<K, V> node, final RBTNode<K, V> beLinked) {
        node._left = beLinked;
        beLinked._parent = node;
    }

    /**
     * 用另一个结点来代替原来的结点.
     * @param origin 原有待替换的结点.
     * @param replacement 用于替换原有结点的结点.
     */
    private void replace(final RBTNode<K, V> origin, final RBTNode<K, V> replacement) {
        // 如果原有结点为树根
        if (origin == _root) {
            replacement._parent = _root._parent;
            _root = replacement;
        } else {
            // 原有结点为左孩子
            if (origin == origin._parent._left) {
                linkedLeftSide(origin._parent, replacement);
            } else { // 原有结点为右孩子
                linkedRightSide(origin._parent, replacement);
            }
        }
    }

    /**
     * 给定一个结点，删除并返回以该结点为根的子树中的最小键所在的结点.
     * @param start 待删除最小项的结点.
     * @return 给定结点的子树中含最小项的结点.
     */
    private RBTNode<K, V> deleteMin(final RBTNode<K, V> start) {
        if (start == this.NIL) { return this.NIL; }
        // 寻找最小结点
        RBTNode<K, V> node = start;
        while (node._left != this.NIL) {
            node = node._left;
        }
        // 删除并返回最小节点
        replace(node, node._right);
        return node;
    }

    /** 删除并返回给定结点的后继结点. */
    private RBTNode<K, V> popSuccessor(RBTNode<K, V> node) {
        return deleteMin(node._right);
    }

    /**
     * 将一个结点进行左旋.
     * @param node 左旋的结点.
     */
    private void rotateLeft(final RBTNode<K, V> node) {
        /*
         *   (param node)   2                       (new root) 4
         *                 / \                                / \
         *                1   4     ----->   (original node) 2   6
         *                   / \                            / \
         *                  3   6                          1   3
         */
        RBTNode<K, V> newRoot = node._right;
        // 连接 “2” 和 “3”
        if (newRoot._left != this.NIL) {
            linkedRightSide(node, newRoot._left);
        } else {
            node._right = newRoot._left;
        }
        // “4” 作为新的根
        replace(node, newRoot);
        // 连接 “4” 和 “2”
        linkedLeftSide(newRoot, node);
    }

    /**
     * 将一个结点进行右旋.
     * @param node 右旋的结点.
     */
    private void rotateRight(final RBTNode<K, V> node) {
        /*
         *   (param node) 4            (new root) 2
         *               / \                     / \
         *              2   6    ----->         1   4 (original node)
         *             / \                         / \
         *            1   3                       3   6
         */
        RBTNode<K, V> newRoot = node._left;
        // 连接 “4” 和 “3”
        if (newRoot._right != this.NIL) {
            linkedLeftSide(node, newRoot._right);
        } else {
            node._left = newRoot._right;
        }
        // “2” 作为新的根
        replace(node, newRoot);
        // 将 “2” 和 “4” 相连
        linkedRightSide(newRoot, node);
    }

    /**
     * 仅用于内部私有方法{@link #fixupInsertion}，根据相应的规则进行颜色的翻转，
     * 以保证插入后红黑树的黑高平衡.
     * @param node 待修复结点.
     * @param uncle 待修复结点父节点的兄弟结点.
     */
    private void colorFlip(final RBTNode<K, V> node, final RBTNode<K, V> uncle) {
        // 父节点和叔结点染黑
        node._parent._isRed = false;
        uncle._isRed = false;
        // 祖父结点染红
        node._parent._parent._isRed = true;
    }

    /**
     * 将给定的键值对作为新建结点插入到红黑树中.
     * @param start 入口结点.
     * @param key 待插入的键.
     * @param value 待插入的值.
     */
    private void insert(final RBTNode<K, V> start, K key, V value) {
        // 新插入结点为根
        if (_size == 0) {
            _root = new RBTNode<>(key, value, false); // 根节点为黑色
            _root._parent = _root._left = _root._right = this.NIL;
            _size += 1;
            return;
        }
        RBTNode<K, V> node = start;
        RBTNode<K, V> parent = start._parent;
        // 查找合适的位置插入
        while (node != this.NIL) {
            parent = node;
            if (lessThan(key, node._key)) {
                node = node._left;
            } else if (greaterThan(key, node._key)) {
                node = node._right;
            } else {
                // 命中已有记录，无需创建新结点，直接更新现有值
                node._value = value;
                return;
            }
        }
        RBTNode<K, V> newNode = new RBTNode<>(key, value, true); // 新插入的结点总是红色的
        newNode._left = newNode._right = this.NIL; // NIL作为新插入结点的叶子
        if (lessThan(key, parent._key)) {
            linkedLeftSide(parent, newNode);
        } else {
            linkedRightSide(parent, newNode);
        }
        // 修复可能出现的双红结点
        fixupInsertion(newNode);
        _size += 1;
    }

    /**
     * 修复插入新结点后可能出现的整棵红黑树黑高不平衡的问题.
     * @param node 检查开始处，为新插入的结点.
     */
    private void fixupInsertion(RBTNode<K, V> node) {
        if (!node._parent._isRed) { return; } // 不再双红，退出
        // 分为8种情况，左右对称各四种
        // 父结点作为左孩子
        if (node._parent == node._parent._parent._left) {
            RBTNode<K, V> uncle = node._parent._parent._right;
            // 叔结点红色，将颜色翻转，红色传导给祖父结点
            if (uncle._isRed) {
                colorFlip(node, uncle);
                fixupInsertion(node._parent._parent); // 检查祖父是否双红并修复
            } else {
                // 叔结点黑色，根据情况进行左旋或右旋
                if (node == node._parent._right) {
                    node = node._parent;
                    rotateLeft(node);
                }
                node._parent._isRed = false;
                node._parent._parent._isRed = true;
                rotateRight(node._parent._parent); // -15, 44重边
            }
        } else { // 父结点作为右孩子，对称
            RBTNode<K, V> uncle = node._parent._parent._left;
            if (uncle._isRed) {
                colorFlip(node, uncle);
                fixupInsertion(node._parent._parent);
            } else {
                if (node == node._parent._left) {
                    node = node._parent;
                    rotateRight(node);
                }
                node._parent._isRed = false;
                node._parent._parent._isRed = true;
                rotateLeft(node._parent._parent);
            }
        }
        // 最后保证根节点为黑色
        _root._isRed = false;
    }

    /**
     * 删除红黑树中的一个结点，需要保证结点的!有效性!.
     * @param node 待删除的结点.
     */
    protected void removeNode(final RBTNode<K, V> node) {
        // 变量 supervisor 追踪将要被删除或者移动的结点
        RBTNode<K, V> supervisor = node;
        // 变量 replacement 追踪 supervisor 在移动或删除后用于填充其原本位置的结点
        RBTNode<K, V> replacement;
        // 记录 supervisor 原始颜色
        boolean supervisorOriginallyRed = supervisor._isRed;
        // 待删除结点至多一个孩子，做删除
        if (node._left == this.NIL) {
            replacement = node._right;
            replace(node, replacement);
        } else if (node._right == this.NIL) {
            replacement = node._left;
            replace(node, replacement);
        } else {
            // 待删除结点有两个孩子，则其后继结点移动至原结点处并替换之
            supervisor = popSuccessor(node);
            supervisorOriginallyRed = supervisor._isRed;
            replacement = supervisor._right;
            // 替换
            replace(node, supervisor);
            linkedLeftSide(supervisor, node._left);
            linkedRightSide(supervisor, node._right);
            supervisor._isRed = node._isRed;
        }
        // 检查删除或移动结点是否造成了某一路径的黑高不平衡
        if (!supervisorOriginallyRed) {
            fixupRemove(replacement);
        }
        _size -= 1;
    }

    /**
     * 检查并修复红黑树删除可能导致的黑高少1的问题.
     * @param node 检查开始处，从发生删除或移动的位置开始.
     */
    private void fixupRemove(RBTNode<K, V> node) {
        // 当前结点为红色则直接染黑，或者到达根节点时完成修复退出循环
        while (!node._isRed && node != _root) {
            // 左右各4种情况
            if (node == node._parent._left) {
                RBTNode<K, V> sibling = node._parent._right;
                // 情况1：兄弟结点红色，重染色并旋转
                if (sibling._isRed) {
                    node._parent._isRed = true;
                    sibling._isRed = false;
                    rotateLeft(node._parent);
                    // 旋转后更新兄弟结点
                    sibling = node._parent._right;
                }
                // 情况2：兄弟结点黑色，有两个黑色的孩子，重新染色后继续向上检查
                if (!sibling._left._isRed && !sibling._right._isRed) {
                    sibling._isRed = true;
                    node = node._parent;
                } else { // 情况3：兄弟结点黑色，左孩子为红色，右孩子为黑色，旋转、重新染色
                    if (!sibling._right._isRed) {
                        sibling._left._isRed = false;
                        sibling._isRed = true;
                        rotateRight(sibling);
                        // 更新兄弟结点
                        sibling = node._parent._right;
                    }
                    // 情况4：兄弟结点黑色，右孩子为红色，旋转、重新染色
                    sibling._isRed = node._parent._isRed;
                    node._parent._isRed = false;
                    sibling._right._isRed = false;
                    rotateLeft(node._parent);
                    // 情况4结束时红黑树整体的黑高已经平衡，可以退出循环
                    node = _root;
                }
            } else { // node在右侧时对称处理
                RBTNode<K, V> sibling = node._parent._left;
                if (sibling._isRed) {
                    node._parent._isRed = true;
                    sibling._isRed = false;
                    rotateRight(node._parent);
                    sibling = node._parent._left;
                }
                if (!sibling._left._isRed && !sibling._right._isRed) {
                    sibling._isRed = true;
                    node = node._parent;
                } else {
                    if (!sibling._left._isRed) {
                        sibling._right._isRed = false;
                        sibling._isRed = true;
                        rotateLeft(sibling);
                        sibling = node._parent._left;
                    }
                    sibling._isRed = node._parent._isRed;
                    node._parent._isRed = false;
                    sibling._left._isRed = false;
                    rotateRight(node._parent);
                    node = _root;
                }
            }
        }
        node._isRed = false;
    }

    /** 获取红黑树中的键的集合. */
    private class KeySet {
        private K[] keys = (K[]) new Object[_size];
        private int pos = 0;
        private void traverseInorder(RBTNode<K, V> node) {
            if (node == NIL) { return; }
            traverseInorder(node._left);
            keys[pos++] = node._key;
            traverseInorder(node._right);
        }

        private KeySet() { traverseInorder(_root); }
        public K[] get() { return keys; }
    }

    /** 获取红黑树中的值的集合. */
    private class ValueSet {
        private V[] values = (V[]) new Object[_size];
        private int pos = 0;
        private void traverseInorder(RBTNode<K, V> node) {
            if (node == NIL) { return; }
            traverseInorder(node._left);
            values[pos++] = node._value;
            traverseInorder(node._right);
        }

        private ValueSet() { traverseInorder(_root); }
        public V[] get() { return values; }
    }
}
