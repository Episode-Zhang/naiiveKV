package testIndex;

import static org.junit.Assert.*;
import org.junit.Test;
import edu.princeton.cs.algs4.StdRandom;

import static Utils.Utils.*;
import Index.BPlusTree;
import KVTable.Table;

public class testInMemoryBPT {

    private final int LOWER = (int) -1e5;
    private final int UPPER = (int) 1e5;
    private final int M = 4;
    private final int CAPACITY = 10;

    private Table<Integer, Integer> generate(int low, int high) {
        Table<Integer, Integer> t = new Table<Integer, Integer>();
        for (int i = low; i <= high; i++) { t.put(i, 1); }
        return t;
    }

    @Test
    public void testEmptyTree() {
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, CAPACITY);
    }

    @Test
    public void testAddTableWithOneSplit() {
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, CAPACITY);
        // 插入4张表
        Table<Integer, Integer> t1 = generate(1, 10); index.write(t1);
        Table<Integer, Integer> t2 = generate(11, 20); index.write(t2);
        Table<Integer, Integer> t3 = generate(21, 30); index.write(t3);
        Table<Integer, Integer> t4 = generate(31, 40); index.write(t4);
        assertEquals(4, index.size());
        System.out.println(index);
    }

    @Test
    public void testAddTableWithTwoSplits() {
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, CAPACITY);
        // 插入4张表
        Table<Integer, Integer> t1 = generate(1, 10); index.write(t1);
        Table<Integer, Integer> t2 = generate(11, 20); index.write(t2);
        Table<Integer, Integer> t3 = generate(21, 30); index.write(t3);
        Table<Integer, Integer> t4 = generate(31, 40); index.write(t4);
        Table<Integer, Integer> t5 = generate(41, 50); index.write(t5);
        Table<Integer, Integer> t6 = generate(51, 60); index.write(t6);
        Table<Integer, Integer> t7 = generate(61, 70); index.write(t7);
        Table<Integer, Integer> t8 = generate(71, 80); index.write(t8);
        assertEquals(8, index.size());
        System.out.println(index);
    }

    @Test
    public void testSimpleInsert() {
        Table<Integer, Integer> t1 = new Table<Integer, Integer>();
        t1.put(1, -1); t1.put(100, 1);
        // 初始化索引 插入表
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, 6);
        index.write(t1);
        index.insert(37, -50);
        index.insert(79, -10);
        index.insert(73, 3);
        index.insert(30, 83);
        index.insert(79, 97);
        index.insert(89, 3);
        index.insert(16, -19);
        index.insert(5, 58);
        index.insert(4, 95);
        index.insert(94, -34);
        index.insert(75, 11);
        index.insert(93, 76);
        index.insert(18, -69);
        index.insert(20, -1);
        index.insert(7, -54);
        index.insert(54, -76);
        index.insert(96, -56);
        System.out.println(index);
    }

    @Test
    public void testSimpleInsertWithBuffer() {
        final int BUFFERCAPACITY = 5;
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(6, 12);
        int[] keys = {-71, -96, -22, 77, 79, 9, 12, -32, -83, 18, -33, -67, 66, -33, 66, 16, 61};
        // 插入数据
        for (int key : keys) {
            // 如果索引区中尚未有数据表或当前记录的键在索引范围的右侧，将记录存入缓冲区
            if (index.empty() || greaterThan(key, index.indexRange()._right)) {
                buffer.put(key, 1);
                // 缓冲区满以后，将内容写入索引区，刷新缓冲.
                if (buffer.size() == BUFFERCAPACITY) {
                    index.write(buffer);
                    buffer = new Table<Integer, Integer>();
                }
            } else { // 否则在索引区中相应的表插入记录
                index.insert(key, 1);
            }
        }
        // 查看视图
        System.out.println(index.indexView());
    }

    @Test
    public void testRandomInsert() {
        Table<Integer, Integer> t1 = new Table<Integer, Integer>();
        t1.put(LOWER, -1); t1.put(UPPER, 1);
        // 初始化索引 插入表
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(10, 1000);
        index.write(t1);
        final int N = (int) 1e7;
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            index.insert(key, value);
        }
        System.out.println(index.indexView());
    }

    @Test
    public void testRandomInsertWithBuffer() {
        final int BUFFERCAPACITY = 2048, M = 32, TABLECAPACITY = 4096;
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, TABLECAPACITY);
        // 插入数据
        final int N = (int) 1e7;
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER), value = StdRandom.uniform(LOWER, UPPER);
            // 如果索引区中尚未有数据表或当前记录的键不在索引范围中，将记录存入缓冲区
            if (index.empty() || greaterThan(key, index.indexRange()._right)) {
                buffer.put(key, value);
                // 缓冲区满以后，将内容写入索引区，刷新缓冲.
                if (buffer.size() == BUFFERCAPACITY) {
                    index.write(buffer);
                    buffer = new Table<Integer, Integer>();
                }
            } else { // 否则在索引区中相应的表插入记录
                index.insert(key, value);
            }
        }
        // 查看视图
        System.out.println(index.indexView());
    }

}
