package View;

import Index.Block;
import Index.Page;
import Index.IndexBlock;
import Index.Range;
import java.util.List;

/**
 * 绘制B+树{@link Index.BPlusTree}的视图.
 * @author Episode-Zhang
 * @version 1.0
 */
public class BPTView {

    /** 绘制B+树中的索引层级. */
    public static<K, V> String viewInString(IndexBlock<K> _root, final List<Page<K, V>> pages) {
        StringBuilder viewContainer = new StringBuilder();
        dfs(_root, viewContainer, 0, pages);
        viewContainer.append("(end)\n");
        return viewContainer.toString();
    }

    /**
     * 按深度优先来遍历一个索引块，并且将其中所有的索引区按层级写入 viewContainer.
     * @param root 将要被遍历的索引块
     * @param viewContainer 用来存放所有不同层级的索引的视图容器.
     * @param <K> 索引块中的键的类型.
     */
    private static<K, V> void dfs(Block<K> root, StringBuilder viewContainer, int level, List<Page<K, V>> pages) {
        final String HEAD = "├───";
        final String BLANK = "│       ";
        String PREFIX = level > 0 ? BLANK.repeat(level - 1) + HEAD.repeat(1) : "";
        Range<K> range = root.blockRange();
        viewContainer.append(PREFIX);
        if (root instanceof Page) { viewContainer.append(String.format("Page id %d: ", pages.indexOf(root))); }
        viewContainer.append(range);
        viewContainer.append("\n");
        // 当前块类型为页，到达底层
        if (root instanceof Page) {
            Range<K>[] ranges = root.subRanges();
            for (int i = 0; i < root.length(); i++) {
                viewContainer.append(BLANK.repeat(level)).append(HEAD);
                viewContainer.append("Table: ").append(ranges[i]);
                viewContainer.append("\n");
            }
        } else { // 继续递归
            Block<K>[] blocks = ((IndexBlock<K>) root).subBlocks();
            for (int i = 0; i < root.length(); i++) {
                dfs(blocks[i], viewContainer, level + 1, pages);
            }
        }
    }
}
