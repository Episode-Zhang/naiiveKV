package testIndex;

import org.junit.Test;
import static org.junit.Assert.*;

import static Index.BPTNode.*;
import Index.Range;
import KVTable.Table;

/**
 * 测试B+树的结点{@link Index.BPTNode}
 * @author Episode-Zhang
 * @version 1.0
 */
public class testBPTNode {

    /** 假设B+树的度为4，即除了根以外的结点长度必须介于 [2, 4]. */
    private final int M = 4;

    /** 建表函数. */
    private Table<Integer, Integer> generate(int low, int high) {
        Table<Integer, Integer> t = new Table<Integer, Integer>();
        for (int i = low; i <= high; i++) { t.put(i, 1); }
        return t;
    }

    /** 若干个表. */
    Table<Integer, Integer> t1 = generate(-3, 0);
    Table<Integer, Integer> t2 = generate(1, 3);
    Table<Integer, Integer> t3 = generate(4, 7);
    Table<Integer, Integer> t4 = generate(11, 20);
    Table<Integer, Integer> t5 = generate(21, 27);
    Table<Integer, Integer> t6 = generate(30, 32);
    Table<Integer, Integer> t7 = generate(35, 43);

    @Test
    public void testExternalWithoutSplit() {
        Node<Integer> e1 = new ExternalNode<Integer, Integer>(M);
        Node<Integer> e2 = new ExternalNode<Integer, Integer>(M);
        Node<Integer> e3 = new ExternalNode<Integer, Integer>(M);
        /* e1 应该看上去长这样：e1: {([-3, 0] : t1), ([1, 3] : t2)} */
        e1.add(t1);
        e1.add(t2);
        assertEquals(-3, (int) e1.getRange()._left);
        assertEquals(3, (int) e1.getRange()._right);
        /* e2 应该看上去长这样：e1: {(4, 7] : t3), ([11, 20] : t4)} */
        e2.add(t3);
        e2.add(t4);
        assertEquals(4, (int) e2.getRange()._left);
        assertEquals(20, (int) e2.getRange()._right);
        /* e3 应该看上去长这样：e1: {(21, 27] : t5), ([30, 32] : t6), ([35, 43] : t7)} */
        e3.add(t5);
        e3.add(t6);
        e3.add(t7);
        assertEquals(21, (int) e3.getRange()._left);
        assertEquals(43, (int) e3.getRange()._right);
        // 其它信息
        assertEquals(7, e1.length() + e2.length() + e3.length());
    }

    @Test
    public void testInternalWithoutSplit() {
        // 前置数据准备，第一层外部结点
        Node<Integer> e1 = new ExternalNode<Integer, Integer>(M);
        e1.add(t1); e1.add(t2);
        Node<Integer> e2 = new ExternalNode<Integer, Integer>(M);
        e2.add(t3); e2.add(t4);
        Node<Integer> e3 = new ExternalNode<Integer, Integer>(M);
        e3.add(t5); e3.add(t6); e3.add(t7);
        // 第二层内部结点
        Node<Integer> internal = new InternalNode<>(M);
        internal.add(e1);
        internal.add(e2);
        internal.add(e3);
        assertEquals(-3, (int) internal.getRange()._left);
        assertEquals(43, (int) internal.getRange()._right);
        assertEquals(3, internal.length());
        // 第三层内部结点
        Node<Integer> root = new InternalNode<>(M);
        root.add(internal);
        assertEquals(-3, (int) root.getRange()._left);
        assertEquals(43, (int) root.getRange()._right);
        assertEquals(1, root.length());
    }
}
