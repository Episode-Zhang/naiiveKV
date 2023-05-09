package benchmark;

import Index.BPlusTree;
import KVTable.Table;
import org.junit.Test;
import edu.princeton.cs.algs4.StdRandom;
import edu.princeton.cs.algs4.Stopwatch;

import java.io.IOException;
import static Utils.Utils.greaterThan;

public class engineBenchmark {

    private final int LOWER = (int) -1e7;
    private final int UPPER = (int) 1e7;

    @Test
    public void testRandomInsertPutDeleteWithBuffer() throws IOException, ClassNotFoundException {
        Stopwatch sw = new Stopwatch();
        final int BUFFERCAPACITY = 32, M = 16, TABLECAPACITY = 128;
        Table<Integer, Integer> buffer = new Table<Integer, Integer>();
        BPlusTree<Integer, Integer> index = new BPlusTree<Integer, Integer>(M, TABLECAPACITY);
        // 插入数据
        final int N = (int) 1e4;
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
        if (!buffer.empty()) { index.write(buffer); }
        // 读取与删除
        for (int i = 0; i < N; i++) {
            int key = StdRandom.uniform(LOWER, UPPER);
            int mode = StdRandom.uniform(0, 2);
            switch (mode) {
                case 0 -> index.delete(key); // 删除
                case 1 -> index.get(key); // 读取
                default -> {}
            }
        }
        // 查看视图
        System.out.println(index.indexView());
        System.out.printf("耗时 %.3f s", sw.elapsedTime());
    }

}
