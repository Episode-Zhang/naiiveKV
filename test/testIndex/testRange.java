package testIndex;

import org.junit.Test;
import static org.junit.Assert.*;

import Index.Range;

/**
 * 测试B+树结点中的键的类型 {@link Index.Range}.
 * @author Episode-Zhang
 * @version 1.0
 */
public class testRange {

    /** 自定义类，纯用于测试 */
    private class cat {
        int age; String say;
        cat (int age, String say) { this.age = age; this.say = say; }
    }

    @Test
    public void testIllegalConstruction() {
        int passed = 0;
        try { Range<Integer> r1 = new Range<>(2, 0); }
        catch (IllegalArgumentException e) { passed += 1; }
        try { Range<Integer> r1 = new Range<>(5, 5); }
        catch (IllegalArgumentException e) { passed += 1; }
        try { Range<String> r2 = new Range<>("HongKongVision", "HikVision"); }
        catch (IllegalArgumentException e) { passed += 1; }
        try { Range<String> r2 = new Range<>("UAreRight", "UAreRight"); }
        catch (IllegalArgumentException e) { passed += 1; }
        assertEquals(4, passed);
    }

    @Test
    public void testOrder() {
        // 整数区间的序
        Range<Integer> r1 = new Range<>(1, 3);
        Range<Integer> r2 = new Range<>(5, 8);
        assertTrue(r1.compareTo(r2) < 0);
        assertTrue(r2.compareTo(r1) > 0);
        // 字符串的序
        Range<String> r3 = new Range<>("HikVision", "HongKongVision");
        Range<String> r4 = new Range<>("KMP", "KMT");
        assertTrue(r3.compareTo(r4) < 0);
        assertTrue(r4.compareTo(r3) > 0);
        cat c1 = new cat(2, "meow");
        cat c2 = new cat(5, "meow meow");
        cat c3 = new cat(114514, "哇哦哇哦哇哦");
        cat c4 = new cat(1919810, "我是最神奇的猫咪");
        Range<cat> r5 = new Range<>(c1, c2);
        Range<cat> r6 = new Range<>(c3, c4);
        assertTrue(r5.compareTo(r6) < 0 || r5.compareTo(r6) > 0);
        assertFalse(r5.compareTo(r6) == 0);
    }
}
