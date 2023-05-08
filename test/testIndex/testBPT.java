package testIndex;

import static org.junit.Assert.*;
import org.junit.Test;
import edu.princeton.cs.algs4.StdRandom;

import static Utils.Utils.*;
import Index.BPlusTree;
import KVTable.Table;
import java.io.IOException;
import java.util.TreeMap;

public class testBPT {
    private final int LOWER = (int) -1e8;
    private final int UPPER = (int) 1e8;
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
    public void testAddTableWithOneSplit() throws IOException {
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
    public void testAddTableWithTwoSplits() throws IOException {
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
    public void testSimpleInsert() throws IOException, ClassNotFoundException {
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
    public void testSimpleInsertWithBuffer() throws IOException, ClassNotFoundException {
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
    public void testSimpleInsertDeleteWithBuffer1() throws IOException, ClassNotFoundException {
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(4, 6);
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        // 插入
        int[] keys = {42, 90, 42, 38, 46, 29, 20, 92, 5, 72, 54, 41, 1, 90, 33, 29, 11, 93, 61, 54};
        for (int key : keys) {
            if (index.empty() || greaterThan(key, index.indexRange()._right)) {
                buffer.put(key, 1);
                // 缓冲区满以后，将内容写入索引区，刷新缓冲.
                if (buffer.size() == 4) {
                    index.write(buffer);
                    buffer = new Table<Integer, Integer>();
                }
            } else { // 否则在索引区中相应的表插入记录
                index.insert(key, 1);
            }
        }
        // 剩余非空缓存写入
        if (!buffer.empty()) { index.write(buffer); buffer = new Table<Integer, Integer>(); }
        // 删除
        assertNull(index.delete(66));
        assertEquals(1, (int) index.delete(11));
    }

    @Test
    public void testSimpleInsertDeleteWithBuffer2() throws IOException, ClassNotFoundException {
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(4, 6);
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        // 插入
        int[] keys = {1, 72, 33, 49, 52, 96, 60, 48, 33, 73, 20, 95, 85, 67, 79, 30, 8, 2, 63, 31};
        for (int key : keys) {
            if (index.empty() || greaterThan(key, index.indexRange()._right)) {
                buffer.put(key, 1);
                // 缓冲区满以后，将内容写入索引区，刷新缓冲.
                if (buffer.size() == 4) {
                    index.write(buffer);
                    buffer = new Table<Integer, Integer>();
                }
            } else { // 否则在索引区中相应的表插入记录
                index.insert(key, 1);
            }
        }
        // 剩余非空缓存写入
        if (!buffer.empty()) { index.write(buffer); buffer = new Table<Integer, Integer>(); }
        // 删除
        assertNull(index.delete(26));
        assertNull(index.delete(83));
        assertNull(index.delete(83));
        assertNull(index.delete(17));
        assertNull(index.delete(6));
        assertEquals(1, (int) index.delete(2));
        assertEquals(1, (int) index.delete(95));
        assertEquals(1, (int) index.delete(60));
        assertEquals(1, (int) index.delete(30));
        System.out.println(index.indexView());
    }

    @Test
    public void testSimpleInsertDeleteWithBuffer3() throws IOException, ClassNotFoundException {
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(4, 8);
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        // 插入
        int[] keys = {-9, 86, -24, 56, -24, -68, 85, 44, 59, -92, 93, -89, 31, 78, 68, -61, 11, 52, -100, -5};
        for (int key : keys) {
            if (index.empty() || greaterThan(key, index.indexRange()._right)) {
                buffer.put(key, 1);
                // 缓冲区满以后，将内容写入索引区，刷新缓冲.
                if (buffer.size() == 5) {
                    index.write(buffer);
                    buffer = new Table<Integer, Integer>();
                }
            } else { // 否则在索引区中相应的表插入记录
                index.insert(key, 1);
            }
        }
        // 剩余非空缓存写入
        if (!buffer.empty()) { index.write(buffer); buffer = new Table<Integer, Integer>(); }
        // 删除
        assertNull(index.delete(89));
        assertEquals(1, (int) index.delete(44));
        System.out.println(index.indexView());
    }

    @Test
    public void testSimpleInsertDeleteWithBuffer4() throws IOException, ClassNotFoundException {
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(4, 6);
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        // 插入
        int[] keys = {-1, 8, -5, 11, 10, 9, 0, 9, 8, 4, -15, -2, 13, -9, 1};
        int[] values = {1, 3, 10, 1, -6, -5, -8, 13, 4, -14, -3, -6, -9, 4, 2};
        for (int i = 0; i < 15; i++) {
            if (index.empty() || greaterThan(keys[i], index.indexRange()._right)) {
                buffer.put(keys[i], values[i]);
                // 缓冲区满以后，将内容写入索引区，刷新缓冲.
                if (buffer.size() == 4) {
                    index.write(buffer);
                    buffer = new Table<Integer, Integer>();
                }
            } else { // 否则在索引区中相应的表插入记录
                index.insert(keys[i], values[i]);
            }
        }
        // 剩余非空缓存写入
        if (!buffer.empty()) { index.write(buffer); buffer = new Table<Integer, Integer>(); }
        // 删除
        assertEquals(4, (int) index.delete(8));
        assertEquals(13, (int) index.delete(9));
        assertEquals(-9, (int) index.delete(13));
        assertEquals(-6, (int) index.delete(-2));
        assertEquals(-14, (int) index.delete(4));
        System.out.println(index.indexView());
    }

    @Test
    public void testSimpleInsertDeleteWithBuffer5() throws IOException, ClassNotFoundException {
        final int BUFFERCAPACITY = 20, M = 4, TABLECAPACITY = 60;
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, TABLECAPACITY);
        TreeMap<Integer, Integer> table = new TreeMap<>();
        // 插入数据
        final int N = 50;
        int[] keys = {93, 73, 78, 30, 31, 86, 96, 9, 77, 22,
                78, 4, 69, 38, 83, 41, 38, 69, 14, 22,
                30, 2, 94, 12, 20, 91, 79, 14, 36, 64,
                60, 65, 56, 76, 53, 31, 31, 37, 85, 74,
                37, 6, 94, 0, 96, 13, 33, 74, 58, -52};
        int[] values = {-78, 17, 70, 73, -100, 69, 77, 29, 17, 75,
                0, 84, -52, 84, 22, 8, 33, -83, 60, -82,
                0, 50, -39, -44, -14, -28, -23, 11, 50, 28,
                -73, -3, 20, -19, -40, 7, 64, -27, 30, 0,
                -65, -29, -61, -24, 53, -46, -63, 73, 85, 83};
        for (int i = 0; i < N; i++) {
            table.put(keys[i], values[i]);
            // 如果索引区中尚未有数据表或当前记录的键不在索引范围中，将记录存入缓冲区
            if (index.empty() || greaterThan(keys[i], index.indexRange()._right)) {
                buffer.put(keys[i], values[i]);
                // 缓冲区满以后，将内容写入索引区，刷新缓冲.
                if (buffer.size() == BUFFERCAPACITY) {
                    index.write(buffer);
                    buffer = new Table<Integer, Integer>();
                }
            } else { // 否则在索引区中相应的表插入记录
                index.insert(keys[i], values[i]);
            }
        }
        if (!buffer.empty()) { index.write(buffer); }
        // 删除
        assertEquals(table.remove(56), index.delete(56));
        assertEquals(table.remove(96), index.delete(96));
        assertEquals(table.remove(94), index.delete(94));
        // 查看视图
        System.out.println(index.indexView());
    }

    @Test
    public void testRandomInsert() throws IOException, ClassNotFoundException {
        Table<Integer, Integer> t1 = new Table<Integer, Integer>();
        t1.put(LOWER, -1); t1.put(UPPER, 1);
        // 初始化索引 插入表
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(10, 1000);
        index.write(t1);
        final int N = (int) 1e3;
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            index.insert(key, value);
        }
        System.out.println(index.indexView());
    }

    @Test
    public void testRandomInsertWithBuffer() throws IOException, ClassNotFoundException {
        final int BUFFERCAPACITY = 32, M = 512, TABLECAPACITY = 64;
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, TABLECAPACITY);
        // 插入数据
        final int N = (int) 1e5;
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

    @Test
    public void testSimpleRandomInsertPutDeleteWithBuffer() throws IOException, ClassNotFoundException {
        final int BUFFERCAPACITY = 4, M = 4, TABLECAPACITY = 6;
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, TABLECAPACITY);
        TreeMap<Integer, Integer> table = new TreeMap<>();
        // 插入数据
        final int N = (int) 1e3;
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(-100, 100), value = StdRandom.uniform(-100, 100);
            table.put(key, value);
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
        if (!buffer.empty()) { index.write(buffer); }
        // 读取与删除
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(-100, 100);
            assertEquals(table.get(key), index.get(key));
            assertEquals(table.remove(key), index.delete(key)); // 删除
        }
        // 查看视图
        System.out.println(index.indexView());
    }

    @Test
    public void testRandomInsertPutDeleteWithBuffer() throws IOException, ClassNotFoundException {
        final int BUFFERCAPACITY = 16, M = 512, TABLECAPACITY = 32;
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, TABLECAPACITY);
        TreeMap<Integer, Integer> table = new TreeMap<>();
        // 插入数据
        final int N = (int) 1e5;
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER), value = StdRandom.uniform(LOWER, UPPER);
            table.put(key, value);
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
        if (!buffer.empty()) { index.write(buffer); }
        // 读取与删除
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int mode = StdRandom.uniform(0, 2);
            switch (mode) {
                case 0 -> assertEquals(table.remove(key), index.delete(key)); // 删除
                case 1 -> assertEquals(table.get(key), index.get(key)); // 读取
                default -> {}
            }
        }
        // 查看视图
        System.out.println(index.indexView());
    }
}
