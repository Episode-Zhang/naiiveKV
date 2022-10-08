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

    /** 更新当前块中数据项的个数. */
    void setLength(int length);

    /** 向结点中添加对应的数据. 内部结点为包括了下一层级索引的结点, 外部结点则为对应的表. */
    void add(Object item);

    /** 在结点指定位置插入对应数据项，内部结点为下一级的索引块，外部结点为表. */
    void addAt(Object item, int index);

    /** 获取结点对应位置的块的引用. */
    Object get(int index);

    /** 弹出结点对应位置的块的引用. 但不更改对应快的位置. 因此方法会修改块的长度，因此使用时请!注意解耦!. */
    Object pop(int index);

    /** 删除并返回对应位置的块的引用，且每次删除回重新排列块的位置. */
    Object removeAt(int index);
}
