package testKVTable;

import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.algs4.StdRandom;

import KVTable.*;
import java.util.Arrays;
import java.util.TreeMap;

/**
 * 基于内存的K-V表的单元测试与集成测试. 涵盖了简单的增查删改，随机增查删改，以及键的遍历和值的遍历.
 * @author Episode-Zhang
 * @version 1.0
 */
public class testKVTable {
    private final int LOWER = (int) -1e5;
    private final int UPPER = (int) 1e5;

    @Test
    public void testSimplePut1() {
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        myTable.put(2, 1);
        myTable.put(1, 1);
        myTable.put(4, 1);
        myTable.put(3, 1);
        myTable.put(5, 1);
    }

    @Test
    public void testSimplePut2() {
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        myTable.put(2, 1);
        myTable.put(1, 1);
        myTable.put(7, 1);
        myTable.put(9, 1);
        myTable.put(8, 1);
    }

    @Test
    public void testSimplePut3() {
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
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
    }

    @Test
    public void testPutNull() {
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        myTable.get(null);
        myTable.put(null, 12);
        assertEquals(12, (int) myTable.get(null));
        myTable.put(null, 21);
        myTable.put(12, 3);
        assertEquals(3, (int) myTable.get(12));
        assertEquals(21, (int) myTable.delete(null));
    }

    @Test
    public void testDeleteEmpty() {
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        assertNull(myTable.delete(13));
        assertNull(myTable.delete(-4));
    }

    @Test
    public void testSimpleDelete1() {
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        myTable.put(2, 1);
        myTable.put(1, 2);
        myTable.put(7, -3);
        myTable.put(9, 4);
        myTable.put(8, 5);
        assertEquals(-3, (int) myTable.delete(7));
        assertEquals(5, (int) myTable.delete(8));
    }

    @Test
    public void testSimpleDelete2() {
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        myTable.put(18, 16);
        myTable.put(4, 18);
        myTable.put(0, 18);
        myTable.put(12, 18);
        myTable.put(16, 10);
        myTable.put(7, 0);
        assertEquals(10, (int) myTable.delete(16));
        assertEquals(18, (int) myTable.delete(4));
        assertEquals(18, (int) myTable.delete(12));
        myTable.put(6, 1);
        assertEquals(16, (int) myTable.delete(18));
        assertEquals(3, myTable.size());
    }

    @Test
    public void testSimpleKeysValues() {
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        myTable.put(18, 16);
        myTable.put(4, 18);
        myTable.put(0, 18);
        myTable.put(null, 114514);
        myTable.put(12, 18);
        myTable.put(16, 10);
        myTable.put(7, 0);
        System.out.println(Arrays.asList(myTable.keys()));
        System.out.println(Arrays.asList(myTable.values()));
    }

    @Test
    public void testRandomPut() {
        final int N = (int) 1e7;
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        System.out.println("测试随机化插入");
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            myTable.put(key, value);
        }
        System.out.println("测试完成");
    }

    @Test
    public void testRandomPutGet() {
        final int N = (int) 1e7;
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
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
        System.out.println("测试完成");
    }

    @Test
    public void testRandomPutDelete() {
        final int N = (int) 1e7;
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
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
        System.out.println("测试完成");
    }

    @Test
    public void testRandomPutGetContainsDelete() {
        final int N = (int) 1e7;
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
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
        System.out.println("测试完成");
    }

    @Test
    public void testRandomPutGetContainsDeleteWithNull() {
        final int N = (int) 1e7;
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
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
        System.out.println("测试完成");
    }

    @Test
    public void testRandomKeys() {
        final int N = (int) 1e7;
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("随机化插入，测试键的集合");
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            myTable.put(key, value);
            officialTable.put(key, value);
        }
        assertArrayEquals(officialTable.keySet().toArray(), myTable.keys());
        System.out.println("测试完成");
    }

    @Test
    public void testRandomValues() {
        final int N = (int) 1e7;
        KVTable<Integer, Integer> myTable = new RBT<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("随机化插入，测试值的集合");
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            myTable.put(key, value);
            officialTable.put(key, value);
        }
        assertArrayEquals(officialTable.values().toArray(), myTable.values());
        System.out.println("测试完成");
    }
}
