package Main;

import KVTable.Table;
import Index.Index;
import Index.BPlusTree;
import static Utils.Utils.*;
import edu.princeton.cs.algs4.Stopwatch;
import java.io.IOException;

/**
 * 用户输入的query语句的执行器. <p>
 * 由{@link Parser}解析用户输入的query语句后通过{@link Parser#parseWithExecutor()}执行<p>
 * Executor在初始化时会制定一个数据库的引擎层，当前仅支持基于B+树的引擎. <p>
 * @param <K> 存储引擎的键的类型.
 * @param <V> 存储引擎的值的类型.
 * @author Episode-Zhang
 * @version 1.0
 */
public class Executor<K, V> {

    /** 数据库的存储引擎. */
    private Index<K, V> _storageEngine;

    /** 数据在内存中的暂存区，始终位于所有数据表分块的右侧. */
    private Table<K, V> _buffer;

    /** 缓冲区的容量. */
    private int _bufferCapacity;

    /**
     * query语句执行器的构造函数.
     * @param engineName 引擎的类型，当前版本为“B+-Tree”
     * @param order B+树的阶，或者称之为M
     * @param capacity B+树外部节点管理的表的最大容量
     * @throws IllegalArgumentException 若给出未知的存储引擎名
     */
    public Executor(String engineName, int order, int capacity, int bufferCapacity) throws IllegalArgumentException {
        if (!engineName.equals("B+-Tree")) {
            throw new IllegalArgumentException(String.format("未知的存储引擎名: %s", engineName));
        }
        _storageEngine = new BPlusTree<K, V>(order, capacity);
        _buffer = new Table<K, V>();
        _bufferCapacity = bufferCapacity;
    }

    /**
     * 判断一条记录是否应该写入内存中的缓冲区
     * @param key 待判定的记录的键
     * @return 若该条记录应该写入内存中的缓冲区，则返回true，否则返回false
     */
    public boolean recordBelongToBuffer(K key) {
        // 缓冲区维护的数据的范围始终在最右侧
        return _storageEngine.empty() || greaterThan(key, _storageEngine.indexRange()._right);
    }

    /**
     * 执行记录的插入操作.
     * @param key 待插入记录的键
     * @param value 待插入记录的值
     * @return 单次插入记录的耗时(秒)
     * @throws IOException 发生IO异常
     * @throws ClassNotFoundException 发生反序列化异常
     */
    public String executeInsert(K key, V value) throws IOException, ClassNotFoundException {
        Stopwatch sw = new Stopwatch();
        boolean hitBuffer = false;
        if (recordBelongToBuffer(key)) {
            hitBuffer = true;
            // 记录计入缓冲区
            _buffer.put(key, value);
            // 查看缓冲区是否达到临界容量
            if (_buffer.size() == _bufferCapacity) {
                _storageEngine.write(_buffer);
                _buffer = new Table<K, V>();
            }
        } else {
            _storageEngine.insert(key, value);
        }
        double timeInSeconds = sw.elapsedTime();
        return String.format("插入记录耗时%.5fs，命中缓冲: %b\n", timeInSeconds, hitBuffer);
    }

    /**
     * 执行记录的插入操作.
     * @param key 待插入记录的键
     * @param value 待插入记录的值
     * @return 单次插入记录的耗时(秒)
     * @throws IOException 发生IO异常
     * @throws ClassNotFoundException 发生反序列化异常
     */
    public String executeUpdate(K key, V value) throws IOException, ClassNotFoundException {
        Stopwatch sw = new Stopwatch();
        boolean hitBuffer = recordBelongToBuffer(key);
        executeInsert(key, value);
        double timeInSeconds = sw.elapsedTime();
        return String.format("更新记录耗时%.5fs，命中缓冲: %b\n", timeInSeconds, hitBuffer);
    }

    /**
     * 执行记录的删除操作.
     * @param key 待删除记录的键
     * @throws IOException 发生IO异常
     * @throws ClassNotFoundException 发生反序列化异常
     */
    public String executeDelete(K key) throws IOException, ClassNotFoundException {
        Stopwatch sw = new Stopwatch();
        boolean hitBuffer = false;
        V deletedValue = null;
        if (recordBelongToBuffer(key)) {
            hitBuffer = true;
            // 记录计入缓冲区
            deletedValue = _buffer.delete(key);
        } else {
            deletedValue = _storageEngine.delete(key);
        }
        double timeInSeconds = sw.elapsedTime();
        return deletedValue == null ? "目标记录不存在" :
                String.format("删除记录耗时%.5fs，命中缓冲: %b\n", timeInSeconds, hitBuffer);
    }

    /** 查看对应键的记录. */
    public String executeShowKey(K key) throws IOException, ClassNotFoundException {
        Stopwatch sw = new Stopwatch();
        boolean hitBuffer = false;
        V targetValue = null;
        if (recordBelongToBuffer(key)) {
            hitBuffer = true;
            // 记录计入缓冲区
            targetValue = _buffer.get(key);
        } else {
            targetValue = _storageEngine.get(key);
        }
        double timeInSeconds = sw.elapsedTime();
        return targetValue == null ? "目标记录不存在" :
                String.format("目标记录为 key: %s, value: %s\n" +
                        "查询记录耗时%.5fs，命中缓冲: %b\n", key, targetValue, timeInSeconds, hitBuffer);
    }

    /** 查看对应表的视图 */
    public String executeShowTable(String tableName) throws IOException, ClassNotFoundException {
        Stopwatch sw = new Stopwatch();
        String view = _storageEngine.tableView(tableName);
        double timeInSeconds = sw.elapsedTime();
        return String.format("目标表的视图为\n%s\n" +
                "查询记录耗时%.5fs\n", view, timeInSeconds);
    }

    /** 查看整个索引区的视图 */
    public String executeShowIndex() throws IOException, ClassNotFoundException {
        Stopwatch sw = new Stopwatch();
        String view = _storageEngine.indexView();
        double timeInSeconds = sw.elapsedTime();
        return String.format("索引区的视图为\n%s\n" +
                "查询记录耗时%.5fs\n", view, timeInSeconds);
    }
}
