package benchmark;

import org.junit.Test;
import edu.princeton.cs.algs4.Stopwatch;
import edu.princeton.cs.algs4.StdRandom;

import KVTable.*;
import java.util.TreeMap;

public class tableBenchmark {

    final int LOWER = (int) -1e7;
    final int UPPER = (int) 1e7;

    @Test
    public void testRandomPutGetContainsDeleteTable() {
        final int N = (int) 1e7;
        Table<Integer, Integer> myTable = new Table<Integer, Integer>();
        TreeMap<Integer, Integer> officialTable = new TreeMap<>();
        System.out.println("测试随机化插入，读取，查询，删除");
        double tableCostTime = 0, mapCostTime = 0;
        // 循环检测
        for (int i = 0; i < N; i++) {
            int ops = StdRandom.uniform(0, 4); // 生成均匀分布下的0-1随机数
            int key = StdRandom.uniform(LOWER, UPPER);
            int value = StdRandom.uniform(LOWER, UPPER);
            Stopwatch tableWatcher = new Stopwatch();
            // 统计自实现的数据表的性能
            switch (ops) {
                case 0 -> myTable.put(key, value); // 存入
                case 1 -> myTable.get(key); // 读取
                case 2 -> myTable.contains(key); // 查询
                case 3 -> myTable.delete(key); // 删除
            }
            tableCostTime += tableWatcher.elapsedTime();
            tableWatcher = null;
            // 统计Java内置的TreeMap的性能
            Stopwatch mapWatcher = new Stopwatch();
            switch (ops) {
                case 0 -> officialTable.put(key, value); // 存入
                case 1 -> officialTable.get(key); // 读取
                case 2 -> officialTable.containsKey(key); // 查询
                case 3 -> officialTable.remove(key); // 删除
            }
            mapCostTime += mapWatcher.elapsedTime();
            mapWatcher = null;
        }
        System.out.printf("自实现的数据表耗时%.3f\n", tableCostTime);
        System.out.printf("Java内置的TreeMap耗时%.3f\n", mapCostTime);
        System.out.println("测试完成");
    }
}
