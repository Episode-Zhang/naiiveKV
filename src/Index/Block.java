package Index;

/**
 * B+树的结点类接口，其内部结点{@link IndexBlock}与外部结点{@link Page}都
 * 需要实现该接口.
 * @param <K> 结点的键的类型
 * @author Episode-Zhang
 * @version 1.0
 */
public interface Block<K> {

    /** 获取当前结点中所存数据的长度. */
    int length();

    /** 获取当前结点对应整体的索引区域. eg, {[-3, 0], [1, 3], [4, 7]} -> [-3, 7]. */
    Range<K> blockRange();

    /** 当前结点索引到的子区间. */
    Range<K>[] subRanges();

    /** 获取当前结点所对应的上级索引块. */
    IndexBlock<K> parent();

    /** 获取当前结点在对应的上级索引块中的位置. */
    int loc();

    /** 为当前结点设置上一级对应的索引块以及其在上一级索引块中的位置. */
    void setParent(IndexBlock<K> parent, int loc);

    /** 更新当前结点在对应位置的索引区间. */
    void setRange(int pos, Range<K> range);

    /** 向结点中添加对应的数据. 内部结点为包括了下一层级索引的结点, 外部结点则为对应的表. */
    void add(Object value);

    /** 获取结点对应位置的块的引用. */
    Object get(int index);

    /** 弹出结点对应位置的块的引用. */
    Object pop(int index);
}
