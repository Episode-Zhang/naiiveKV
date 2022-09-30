package Index;
/**
 * 整个B+树是基于“区间-表”存储而非单个“键-值”的，这要求结点的键需要为可以用来表示“区间”的
 * 数据结构. {@code Range}类就是用来抽象区间的数据结构.
 * <p>
 * 若组成区间的元素内部定义了“序”关系，则可以在区间类{@code Range}上定义“序”关系.
 * <p>
 * 在Range上定义的序为：若两个区间有交集，则同一视为两者是“equal”关系，否则，若区间 R1
 * 整体在 R2 的左侧 (R1右端点小于R2左端点)，则视为二者存在关系 R1 < R2；同理若 R1 整体
 * 在 R2 的右侧，则视作存在关系 R1 > R2.
 * @param <K> 组成区间的元素的类型
 * @author Episode-Zhang
 * @version 1.0
 */
public class Range<K> implements Comparable<Range<K>> {

    /** 区间的左右端点. */
    public K _left, _right;

    /**
     * 默认构造函数. 如果参数类型{@code <K>}继承了{@code Comparable}，则根据其实现的
     * {@code compareTo}进行比较；否则就按照其{@code hashCode}的值进行比较.
     * @param left 区间的左端点
     * @param right 区间的右端点
     * @throws IllegalArgumentException 如果左端点大于等于右端点，这里我们不允许退化的区间存在.
     */
    public Range(K left, K right) {
        // 类型参数 K 继承了 Comparable，则按照 compareTo 比较
        if (left instanceof Comparable) {
            if (((Comparable) left).compareTo(right) < 0) {
                _left = left;
                _right = right;
                return;
            }
        } else { // 否则按照 hashCode 比较
            /* 使用对象内置的 hashCode 进行排序时，需要构造函数进行自适应.
             * 因为此时对用户而言，孰为left孰为right并不那么显然. */
            if (left.hashCode() < right.hashCode()) {
                _left = left;
                _right = right;
                return;
            } else if (left.hashCode() > right.hashCode()) {
                _left = right;
                _right = left;
                return;
            }
        }
        throw new IllegalArgumentException("parameter left should be less than the right.");
    }

    @Override
    public int compareTo(Range<K> other) {
        // 如果组成区间的元素本身是Comparable的，则调用其compareTo接口
        if (_left instanceof Comparable) { // 说明 K extends Comparable<K>
            // 若 this 整体位于 other 的左侧，则认为 this < other
            // 若 this 整体位于 other 的右侧，则认为 this > other
            if (((Comparable) this._right).compareTo(other._left) < 0) {
                return -1;
            } else if (((Comparable) this._left).compareTo(other._right) > 0) {
                return 1;
            }
        } else { // 否则拿 hashCode 作为序来对比
            if (this._right.hashCode() < other._left.hashCode()) {
                return -1;
            } else if (this._left.hashCode() > other._right.hashCode()) {
                return 1;
            }
        }
        // 其它情况，区间有相交就视为相等
        return 0;
    }

    /** 规定区间类的打印格式，便于调试. */
    @Override
    public String toString() {
        return String.format("[%s, %s]", _left.toString(), _right.toString());
    }
}
