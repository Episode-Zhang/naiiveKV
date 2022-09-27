package KVTable;

/**
 * 基于内存的K-V表的接口，要求能实现基本的读写功能.
 * @param <K> 键的类型.
 * @param <V> 值的类型.
 * @author Episode-Zhang
 * @version 1.0
 */
public interface KVTable<K, V> {

    /** 给定键，查询表中对应的值. */
    V get(K key);

    /** 给定键值对，将其存入表中. */
    void put(K key, V value);

    /** 给定键，删除表中对应的键值对. */
    V delete(K key);

    /** 给定键，查询表中是否存有对应的数据. */
    boolean contains(K key);

    /** 返回当前表中存储的键值对的个数. */
    int size();

    /** 判断当前KV表是否为空 */
    default boolean empty() { return size() == 0; }

    /** 获取表中当前的键的集合. */
    Object[] keys();

    /** 获取表中当前的值的集合. */
    Object[] values();
}
