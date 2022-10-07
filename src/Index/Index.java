package Index;

import KVTable.Table;

/**
 * K-V表的索引，采用B+树实现.
 * @param <K> K-V表中所存结点的键的类型.
 * @param <V> K-V表中所存结点的值的类型.
 * @author Episode-Zhang
 * @version 1.0
 */
public interface Index<K, V>  {

    /** 返回当前数据库中数据表的个数. */
    int size();

    /** 将内存中的 table 写入磁盘并记录其索引. */
    void write(Table<K, V> table);

    /** 将给定的 K-V 对插入到对应的表中 */
    void insert(K key, V value);

    /** 返回 key 对应的值. */
    V get(K key);

    /** 删除给定键对应的记录. */
    V delete(K key);

    /** 返回索引层级. */
    String indexView();

    /** 返回某张数据表的视图. */
    String tableView(int pageId, int tableId);
}
