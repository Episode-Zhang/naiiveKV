package View;

import java.util.LinkedList;
import java.util.List;

/**
 * 绘制表{@link KVTable.Table}中数据的视图. 其大致形如：
 * <p></p>
 *  <p>   +--------------+---------------+---------------+"
 *  <p>   |    record    |      key      |     value     |"
 *  <p>   +--------------+---------------+---------------+
 *  <p>   |      0       |    -100000    |     76441     |"
 *  <p>   |      1       |    -99999     |     -31277    |"
 *  <p>   |      2       |    -99997     |     -61996    |"
 *  <p>   +--------------+---------------+---------------+"
 * <p></p>
 * @author Episode-Zhang
 * @version 1.0
 */
public class TableView {

    /**
     * 给出表中指定的键值对的文本形式，绘制整个表的视图.
     * @param keys 键的文本组成的集合.
     * @param values 值的文本组成的集合.
     * @return 给定键值对组成的表的文字视图.
     */
    public static String viewInString(List<String> keys, List<String> values) {
        StringBuilder view = new StringBuilder();
        view.append("\n");
        // 设置每个键和值输出时的宽度，宽度为最宽键/值或字符串“key”/“value”的宽度
        int keyWidth = max(getWidth(keys), 3);
        int valueWidth = max(getWidth(values), 5);
        // 边界框的文本视图
        final String BLANK = "   ";
        final String PREFIX = "+---";
        final String SUFFIX = "---+";
        String boundary = String.format("%s%s%s", PREFIX, "-".repeat("record".length()), SUFFIX)
                + String.format("%s%s%s", "---", "-".repeat(keyWidth), SUFFIX)
                + String.format("%s%s%s\n", "---", "-".repeat(valueWidth), SUFFIX);
        // 列名的视图，字符串“key”与“value”的宽度和后面具体键值的宽度*相统一*
        String keyDescFormat = "%-" + String.format("%d", keyWidth) + "s";
        String valueDescFormat = "%-" + String.format("%d", valueWidth) + "s";
        String colDesc = String.format("|%s%s%s", BLANK, "record", BLANK)
                + String.format("|%s%s%s", BLANK, String.format(keyDescFormat, "key"), BLANK)
                + String.format("|%s%s%s|\n", BLANK, String.format(valueDescFormat, "value"), BLANK);
        // 设置列名部分
        view.append(boundary).append(colDesc).append(boundary);
        // 表中键值对的视图
        for (int i = 0; i < keys.size(); i++) {
            String keyFormat = "%s %-" + String.format("%d", keyWidth + 2) + "s|";
            String valueFormat = "%s %-" + String.format("%d", valueWidth + 2) + "s|\n";
            String record = String.format("|%s%-7d|", " ".repeat(5), i)
                    + String.format(keyFormat, BLANK, keys.get(i))
                    + String.format(valueFormat, BLANK, values.get(i));
            view.append(record);
        }
        // 表尾视图
        view.append(boundary);
        view.append("...(Rest of the records are hidden)\n");
        view.append(boundary);
        // 返回
        return view.toString();
    }

    /** 简单的取二者中较大者的函数. */
    private static int max(int a, int b) { return a > b ? a : b; }

    /**
     * 给定一组字符，要求为这组字符设置一个同一的输出宽度.
     * @param strs 需要设置统一输出宽度的字符串集.
     * @return 该字符集输出时每个字符串的宽度.
     */
    private static int getWidth(List<String> strs) {
        int maxLen = 0;
        for (String s : strs) {
            if (s.length() > maxLen) { maxLen = s.length(); }
        }
        return maxLen;
    }
}
