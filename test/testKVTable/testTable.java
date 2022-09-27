package testKVTable;

import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;

import KVTable.*;
import java.util.TreeMap;
import java.util.Arrays;
import static java.util.Collections.max;
import static java.util.Collections.min;

/**
 * 用于索引外部结点类的Table类型，除了基本的随机集成测试外，添加了简单的对split的测试.
 * @author Episode-Zhang
 * @version 1.0
 */
public class testTable {

    private final int LOWER = (int) -1e5;
    private final int UPPER = (int) 1e5;

    @Test
    public void testSimpleSplit() {
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        myTable.put(-97, -11);
        myTable.put(-15, -44);
        myTable.put(41, 96);
        myTable.put(94, 53);
        myTable.put(-92, 20);
        myTable.put(-98, -21);
        myTable.put(58, -6);
        myTable.put(-69, 54);
        myTable.put(-53, -48);
        myTable.put(-73, -13);
        myTable.put(44, -10);
        myTable.put(-31, -36);
        myTable.put(80, -90);
        myTable.put(96, -74);
        myTable.put(30, 49);
        myTable.put(-9, -76);
        myTable.put(86, -85);
        myTable.put(-67, -7);
        myTable.put(81, -16);
        Table<Integer, Integer> split = myTable.split();
        System.out.println(Arrays.asList(split.keys()));
        System.out.println(Arrays.asList(split.values()));
    }

    @Test
    public void testRandomPut() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("测试随机化插入");
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            myTable.put(key, value);
            officialTable.put(key, value);
        }
        assertEquals(min(officialTable.keySet()), myTable.minKey());
        assertEquals(max(officialTable.keySet()), myTable.maxKey());
        System.out.println("测试完成");
    }

    @Test
    public void testRandomPutSplit() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        System.out.println("测试随机化插入，分片");
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            myTable.put(key, value);
        }
        Table<Integer, Integer> split = myTable.split();
        assertTrue(myTable.maxKey().compareTo(split.minKey()) < 0);
        System.out.println("测试完成，分片后两表数据项个数相差" + (split.size() - myTable.size()));
    }

    @Test
    public void testRandomPutGet() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("测试随机化插入，读取");
        for (int i = 0; i < N; i++) {
            int ops = StdRandom.uniform(0, 2); // 生成均匀分布下的0-1随机数
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            if (ops == 1) {
                // 测试存入
                myTable.put(key, value);
                officialTable.put(key, value);
            } else {
                // 测试读出
                assertEquals(officialTable.get(key), myTable.get(key));
            }
        }
        assertEquals(min(officialTable.keySet()), myTable.minKey());
        assertEquals(max(officialTable.keySet()), myTable.maxKey());
        System.out.println("测试完成");
    }

    @Test
    public void testRandomPutDelete() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("测试随机化插入，删除");
        for (int i = 0; i < N; i++) {
            int ops = StdRandom.uniform(0, 2); // 生成均匀分布下的0-1随机数
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            if (ops == 1) {
                // 测试存入
                myTable.put(key, value);
                officialTable.put(key, value);
            } else {
                // 测试删除
                assertEquals(officialTable.remove(key), myTable.delete(key));
                assertEquals(officialTable.size(), myTable.size());
            }
        }
        assertEquals(min(officialTable.keySet()), myTable.minKey());
        assertEquals(max(officialTable.keySet()), myTable.maxKey());
        System.out.println("测试完成");
    }

    @Test
    public void testRandomPutDeleteSplit() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        System.out.println("测试随机化插入，删除，分片");
        for (int i = 0; i < N; i++) {
            int ops = StdRandom.uniform(0, 2); // 生成均匀分布下的0-1随机数
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            if (ops == 1) { myTable.put(key, value); }
            else { myTable.delete(key); }
        }
        Table<Integer, Integer> split = myTable.split();
        assertTrue(myTable.maxKey().compareTo(split.minKey()) < 0);
        System.out.println("测试完成，分片后两表数据项个数相差" + (split.size() - myTable.size()));
    }

    @Test
    public void testRandomPutGetContainsDelete() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("测试随机化插入，读取，查询，删除");
        for (int i = 0; i < N; i++) {
            int ops = StdRandom.uniform(0, 4); // 生成均匀分布下的0-1随机数
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            switch (ops) {
                case 0 -> {
                    // 存入
                    myTable.put(key, value);
                    officialTable.put(key, value);
                    assertEquals(officialTable.size(), myTable.size());
                }
                case 1 ->
                    // 读取
                        assertEquals(officialTable.get(key), myTable.get(key));
                case 2 ->
                    // 查询
                        assertEquals(officialTable.containsKey(key), myTable.contains(key));
                case 3 -> {
                    // 删除
                    assertEquals(officialTable.remove(key), myTable.delete(key));
                    assertEquals(officialTable.size(), myTable.size());
                }
                default -> {
                }
            }
        }
        assertEquals(min(officialTable.keySet()), myTable.minKey());
        assertEquals(max(officialTable.keySet()), myTable.maxKey());
        System.out.println("测试完成");
    }

    @Test
    public void testRandomPutGetContainsDeleteWithNull() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("测试随机化插入，读取，查询，删除");
        Integer nullValue = null;
        boolean nullAppears = false;
        for (int i = 0; i < N; i++) {
            int ops = StdRandom.uniform(0, 4); // 生成均匀分布下的0-1随机数
            // key 有几率为null
            Integer key = StdRandom.uniform(0, 10) == 1 ? null : StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            switch (ops) {
                case 0 -> {
                    // 存入
                    if (key == null) {
                        myTable.put(key, value);
                        nullAppears = true;
                        nullValue = value;
                    } else {
                        myTable.put(key, value);
                        officialTable.put(key, value);
                    }
                    if (nullAppears) {
                        assertEquals(officialTable.size() + 1, myTable.size());
                    } else {
                        assertEquals(officialTable.size(), myTable.size());
                    }
                }
                case 1 -> {
                    // 读取
                    if (key == null) {
                        assertEquals(nullValue, myTable.get(null));
                    } else {
                        assertEquals(officialTable.get(key), myTable.get(key));
                    }
                }
                case 2 -> {
                    // 查询
                    if (key == null) {
                        assertEquals(nullAppears, myTable.contains(null));
                    } else {
                        assertEquals(officialTable.containsKey(key), myTable.contains(key));
                    }
                }
                case 3 -> {
                    // 删除
                    if (key == null) {
                        assertEquals(nullValue, myTable.delete(key));
                        nullAppears = false;
                        nullValue = null;
                    } else {
                        assertEquals(officialTable.remove(key), myTable.delete(key));
                    }
                    if (nullAppears) {
                        assertEquals(officialTable.size() + 1, myTable.size());
                    } else {
                        assertEquals(officialTable.size(), myTable.size());
                    }
                }
                default -> {
                }
            }
        }
        assertEquals(min(officialTable.keySet()), myTable.minKey());
        assertEquals(max(officialTable.keySet()), myTable.maxKey());
        System.out.println("测试完成");
    }

    @Test
    public void testRandomKeys() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("随机化插入，测试键的集合");
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            myTable.put(key, value);
            officialTable.put(key, value);
        }
        assertArrayEquals(officialTable.keySet().toArray(), myTable.keys());
        assertEquals(min(officialTable.keySet()), myTable.minKey());
        assertEquals(max(officialTable.keySet()), myTable.maxKey());
        System.out.println("测试完成");
    }

    @Test
    public void testRandomValues() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("随机化插入，测试值的集合");
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            myTable.put(key, value);
            officialTable.put(key, value);
        }
        assertArrayEquals(officialTable.values().toArray(), myTable.values());
        assertEquals(min(officialTable.keySet()), myTable.minKey());
        assertEquals(max(officialTable.keySet()), myTable.maxKey());
        System.out.println("测试完成");
    }
}