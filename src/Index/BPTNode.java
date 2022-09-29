package Index;

import KVTable.Table;

/**
 * B+树结点相关的数据结构，包括结点(Node)以及数据项(Entry)，作用域为{@link Index}.
 * <p>
 * 一个结点内有多个Entry，其个数介于 M / 2 与 M 之间，其中M为B+树的阶数.
 * <p>
 * 结点有内外部之分，具体为内部结点(internal node)与外部结点(external node)；同理Entry也有
 * 内外部之分，具体可以分为：IndexEntry, Page.
 * @author Episode-Zhang
 * @version 1.0
 */
public class BPTNode {

    /**
     * 内部结点和外部结点共同的抽象接口.
     * <p></p>
     * 对于所有类型的结点而言, 其内部数据项中，键的集合都是“被动更新”的，即在加入下层结点
     * 的索引或外部K-V表的地址后，对应的键会“自适应”地设置或更新自己的范围以覆盖到这些数据.
     * <p></p>
     * 对于内部结点，除了存储每个块的索引范围作为单个的键，其单个值表现为下一层对应结点的地址；
     * 对于外部结点，其单个值表现为磁盘上的单张K-V表的地址.
     * @param <K>
     */
    public interface Node<K> {

        /** 获取当前结点中索引区间的个数. */
        int length();

        /** 获取当前结点中索引区间的集合. */
        Range<K>[] keys();

        /** 获取当前结点的parent结点. */
        InternalNode<K> parent();

        /** 将当前结点的parent指向另一个结点. */
        void setParent(InternalNode<K> parent);

        /** 获取当前“结点”的索引区域，其定义为数据项的索引区域的最小闭覆盖区间. */
        Range<K> getRange();

        /** 向对应类型的结点中加入“值”. 值可以是下一层对应结点的地址，也可以是磁盘上表的地址. */
        void add(Object value);
    }

    /**
     * B+树中的内部结点，用来存放指向下一级结点的索引.
     * <p></p>
     * 一个内部结点可以存放多个索引，每个索引表示一个搜索区域，这个区域可以是内部结点，也可以是
     * 外部结点。前者表示索引仍能继续向下，后者表示当前层级就是最终读写进行的层级了.
     * <p></p>
     * 继承得到的键集可以理解为不同索引的集合. 每个键都是一个区间，是与之对应的，下一层“搜索区域”
     * 的起止范围. 而到了下一层，这一“大而化之”的区域起止又将被划分称更小的“索引区域”.
     * @param <K> 对应外部结点所存放的，K-V表中的键的类型.
     */
    public static class InternalNode<K> implements Node<K> {

        /** 当前内部结点存储的 下一层结点的索引范围 的集合. */
        public Range<K>[] _keys;

        /** 对应索引集合的下一层结点的地址. */
        public Node<K>[] _nextLevel;

        /** 指向父节点. */
        public InternalNode<K> _parent;

        /**当前内部结点储存的下一层结点的个数. */
        public int _length = 0;

        /** 对应所属B+树的阶. */
        private final int M;

        /** 默认构造函数. 需要指定对应所属B+树的阶. */
        public InternalNode(int M) {
            this.M = M;
            _keys = new Range[this.M];
            _nextLevel = new Node[this.M];
        }

        /** 获取当前结点中索引区间以及下层结点的个数. */
        @Override
        public int length() { return _length; }

        /** 获取当前结点中索引区间的集合. */
        @Override
        public Range<K>[] keys() { return _keys; }

        /** 获取当前内部结点的parent结点. */
        @Override
        public InternalNode<K> parent() { return _parent; }

        /** 为当前内部结点设立parent. */
        @Override
        public void setParent(InternalNode<K> parent) { _parent = parent; }

        /** 获取下一层结点索引范围的集合的最小闭覆盖区间. */
        @Override
        public Range<K> getRange() {
            if (_nextLevel == null) { return null; }
            Node<K> minNode = _nextLevel[0], maxNode = _nextLevel[_length - 1];
            K minKey = (minNode.keys())[0]._left;
            K maxKey = (maxNode.keys())[maxNode.length() - 1]._right;
            return new Range<>(minKey, maxKey);
        }

        /** 计入下一层对应结点的地址. */
        @Override
        public void add(Object node) {
            Node<K> nextLevelNode = (Node<K>) node;
            // 上层连接下层
            _nextLevel[_length] = nextLevelNode;
            _keys[_length] = nextLevelNode.getRange();
            // 下层连接上层
            nextLevelNode.setParent(this);
            _length += 1;
        }

    }

    /**
     * B+树中的外部结点，是最终的读写层.
     * @param <K> 对应所存放的K-V表中的键的类型.
     * @param <V> 对应所存放的K-V表中的值的类型.
     */
    public static class ExternalNode<K, V> implements Node<K> {

        /** 当前外部结点中每张表的键的范围(索引区域)组成的集合. */
        public Range<K>[] _keys;

        /** 对应每个索引区域的K-V表. */
        public Table<K, V>[] _pages;

        /** 指向父节点. */
        public InternalNode<K> _parent;

        /**当前外部结点储存的表的张数. */
        public int _length = 0;

        /** 对应所属B+树的阶. */
        private final int M;

        /** 指向下一个外部结点的引用. */
        public ExternalNode<K, V> _next = null;

        /** 默认构造函数.  需要指定对应所属B+树的阶. */
        public ExternalNode(int M) {
            this.M = M;
            _keys = new Range[this.M]; //(Range<K>[]) new Object[this.M];
            _pages = new Table[this.M]; //(Table<K, V>[]) new Object[this.M];
        }

        /** 获取当前结点中索引区间以及表的个数. */
        @Override
        public int length() { return _length; }

        /** 获取当前结点中索引区间的集合. */
        @Override
        public Range<K>[] keys() { return _keys; }

        /** 获取当前内部结点的parent结点. */
        @Override
        public InternalNode<K> parent() { return _parent; }

        /** 为当前外部结点设立parent. */
        @Override
        public void setParent(InternalNode<K> parent) { _parent = parent; }

        /** 将缓冲区中达到阈值的K-V表写入并记录索引. */
        @Override
        public void add(Object Table) {
            Table<K, V> fullTable = (Table<K, V>) Table;
            _pages[_length] = fullTable;
            _keys[_length] = new Range<>(fullTable.minKey(), fullTable.maxKey());
            _length += 1;
        }

        /** 获取磁盘中的表的索引范围集合的最小闭覆盖区间. */
        @Override
        public Range<K> getRange() {
            if (_pages == null) { return null; }
            K minKey = _keys[0]._left;
            K maxKey = _keys[_length - 1]._right;
            return new Range<>(minKey, maxKey);
        }
    }

}
