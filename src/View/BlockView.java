package View;

import java.util.List;
import static Utils.Utils.*;

/**
 * 绘制索引块{@link Index.IndexBlock}以及页块{@link Index.Page}的视图.
 * <p></p>
 * 索引块视图
 * <p>          type: Index Block
 * <p>          +--------------+--------------------+
 * <p>          |    record    |    Index Range     |
 * <p>          +--------------+--------------------+
 * <p>          |      0       |      [-3, 3]       |
 * <p>          |      1       |      [4, 20]       |
 * <p>          +--------------+--------------------+
 * <p></p>
 * 页视图
 * <p>          type: Page
 * <p>          +--------------+--------------------+
 * <p>          |    record    |    Index Range     |
 * <p>          +--------------+--------------------+
 * <p>          |      0       |      [4, 7]        |
 * <p>          |      1       |      [11, 20]      |
 * <p>          +--------------+--------------------+
 * <p></p>
 * @author Episode-Zhang
 * @version 1.0
 */
public class BlockView {

    /**
     * 绘制对应块(包括索引块和页)文本视图中的表格.
     *
     * @param ranges 对应块所存的索引区间.
     * @return 对应快索引区间组成的表格的文本视图.
     */
    public static String viewInString(List<String> ranges) {
        StringBuilder view = new StringBuilder();
        // 设置索引区间的输出宽度，要么为最宽区间的长度，要么为列名“Index Range”的宽度
        int rangeWidth = max(getWidth(ranges), "Index Range".length());
        // 边界框的文本视图
        final String BLANK = "   ";
        final String PREFIX = "+---";
        final String SUFFIX = "---+";
        String boundary = String.format("%s%s%s", PREFIX, "-".repeat("record".length()), SUFFIX)
                + String.format("%s%s%s\n", "---", "-".repeat(rangeWidth), SUFFIX);
        // 列名的视图
        String rangeDescFormat = "%-" + String.format("%d", rangeWidth) + "s";
        String colDesc = String.format("|%s%s%s", BLANK, "record", BLANK)
                + String.format("|%s%s%s|\n", BLANK, String.format(rangeDescFormat, "Index Range"), BLANK);
        // 设置列名部分
        view.append(boundary).append(colDesc).append(boundary);
        // 设置表视图
        for (int i = 0; i < ranges.size(); i++) {
            String rangeFormat = "|%s %-" +String.format("%d", rangeWidth + 2) + "s|\n";
            String record = String.format("|%s%-7d", " ".repeat(5), i)
                    + String.format(rangeFormat, BLANK, ranges.get(i));
            view.append(record);
            if (i == ranges.size() - 1) { view.append(boundary); }
        }
        return view.toString();
    }

    /** 返回索引块类型的文字视图. */
    public static String indexBlockView(List<String> ranges) {
        StringBuilder view = new StringBuilder();
        view.append("\n").append("type: Index Block\n");
        view.append(viewInString(ranges));
        return view.toString();
    }

    /** 返回表类型的文字视图. */
    public static String pageView(List<String> ranges) {
        StringBuilder view = new StringBuilder();
        view.append("\n").append("type: Page\n");
        view.append(viewInString(ranges));
        return view.toString();
    }
}
