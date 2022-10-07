package Utils;

import java.util.List;

/**
 * 一些辅助函数的集合.
 * @author Episode-Zhang
 * @version 1.0
 */
public class Utils {

    /** 简单的取二者中较大者的函数. */
    public static int max(int a, int b) { return a > b ? a : b; }

    /**
     * 给定一组字符，要求为这组字符设置一个同一的输出宽度.
     * @param strs 需要设置统一输出宽度的字符串集.
     * @return 该字符集输出时每个字符串的宽度.
     */

    public static int getWidth(List<String> strs) {
        int maxLen = 0;
        for (String s : strs) {
            if (s.length() > maxLen) { maxLen = s.length(); }
        }
        return maxLen;
    }

    /**
     * 判断两个键的大小关系.
     * <p>
     * 如果键的参数类型K已经定义了内部元素之间的序，则返回其比较结果；否则就将元素内建的hashCode
     * 的值的大小作为“序”来定义.
     * @param k1 待比较键的前者.
     * @param k2 待比较键的后者.
     * @return 前一个键是否小于后一个键，是返回true，否返回false.
     */
    public static<K> boolean lessThan(K k1, K k2) {
        if (k1 instanceof Comparable && k2 instanceof Comparable) {
            return ((Comparable)k1).compareTo(k2) < 0;
        } else {
            return k1.hashCode() - k2.hashCode() < 0;
        }
    }

    /**
     * 判断两个键的大小关系.
     * <p>
     * 如果键的参数类型K已经定义了内部元素之间的序，则返回其比较结果；否则就将元素内建的hashCode
     * 的值的大小作为“序”来定义.
     * @param k1 待比较键的前者.
     * @param k2 待比较键的后者.
     * @return 前一个键是否大于后一个键，是返回true，否返回false.
     */
    public static<K> boolean greaterThan(K k1, K k2) {
        if (k1 instanceof Comparable && k2 instanceof Comparable) {
            return ((Comparable)k1).compareTo(k2) > 0;
        } else {
            return k1.hashCode() - k2.hashCode() > 0;
        }
    }
}
