package View;

import java.util.List;

/**
 * 一些绘制视图时需要用到的辅助函数.
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
}
