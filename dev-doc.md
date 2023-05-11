# 开发日志

## V1.0

整体结构：

存储单条数据的结构是`Table`，它继承了红黑树`RBT`，在拥有红黑树的**增删改查**与自平衡的基础上，`Table`缓存了当前表中的最大键与最小键，从而便于**索引**；同时，`Table`还支持分裂，分裂的逻辑是当单表的数据容量超过阈值时，根节点的左子树与右子树会各自分成不同的`Table`，因为根节点的左右子树黑高相同，所以分裂后不用再平衡，同时根节点加入右子树。

用来管理和调度单表`Table`的是索引`BPlusTree`，其本身是一棵**M**阶的B+树，分成内部结点和外部结点：内部结点是除所有叶子结点以外的结点，只负责维护**索引的序列**，使得索引在增删改查、分裂合并的操作中整体有序；外部结点是所有叶子结点，其除了负责维护索引以外，还负责维护`Table`引用组成的数组，在某个记录插入导致对应`Table`的容量超过阈值时，`BPlusTree`会触发`Table`的`split`机制，然后将分裂出来的“大表”用链表插入的方法插到当前表的下一位。

```java
private void insertRecord(Page<K, V> page, K key, V value) {
    Range<K>[] ranges = page.subRanges();
    for (int i = 0; i < page.length(); i++) {
        if (ranges[i].contains(key) || lessThan(key, ranges[i]._left)) {
            // 打开表，插入记录
            Table<K, V> target = (Table<K, V>) page.get(i);
            target.put(key,value);
            // 检查表是否需要分裂
            if (target.size() >= UPPERTHRESHOLD * CAPACITY) {
                Table<K, V> split = target.split();
                insertTable(page, i + 1, split);
            }
            // 更新表索引
            ranges[i] = new Range<>(target.minKey(), target.maxKey());
            break;
        }
    }
}

/** 在底层对应页的对应位置中加入新的表. */
private void insertTable(Page<K, V> page, int pos, Table<K, V> table) {
    page.addAt(table, pos);
    _size += 1;
    // 页内分裂，将分裂出来的页加入链表，在父节点添加新的索引.
    if (page.length() == this.M) {
        Page<K, V> splitPage = (Page<K, V>) splitBlock(page);
        _pages.add( _pages.indexOf(page) + 1, splitPage);
        insertSplit(splitPage.parent(), splitPage);
        // 更新分裂结点的祖先索引
        updateIndex(splitPage);
    }
    // 更新原有页的祖先的索引.
    updateIndex(page);
}
```

如果删除某条记录后，对应的表是空的且当前表数大于1，则删除对应`Table`：

```java
private V removeKey(Page<K, V> page, K key) {
    Range<K>[] range = page.subRanges();
    int tablePos;
    for (int i = 0; i < page.length(); i++) {
        if (range[i].contains(key)) {
            Table<K, V> targetTable = (Table<K, V>) page.get(i);
            tablePos = i;
            V value = targetTable.delete(key);
            if (value == null) { break; } // 对应表中不存在该记录，直接返回空
            // 删除键后若当前表空且表的个数大于1，则删除表.
            if (targetTable.empty()) {
                removeTable(page, tablePos);
            } else { // 否则，删除可能影响整体的状态，需要检查索引
                if (range[i]._left != targetTable.minKey() || range[i]._right != targetTable.maxKey()) {
                    range[i]._left = targetTable.minKey();
                    range[i]._right = targetTable.maxKey();
                    int pagePos = page.loc();;
                    if (page.blockRange() != page.parent().subRanges()[pagePos]) {
                        updateIndex(page);
                    }
                }
            }
            return value;
        }
    }
    return null;
}

/** 将一张表从给定的页的指定位置中删除. */
private void removeTable(Page<K, V> page, int pos) {
    page.removeAt(pos); // 删除表
    _size -= 1;
    // 页中表的个数大于等于 M/2，更新上级索引，直接返回
    if (page.length() >= this.M / 2) {
        updateIndex(page);
        return;
    }
    // 小于 M/2，根据是否在根处，直接删除or请求前驱/后继或合并
    IndexBlock<K> parent = page.parent();
    // 无兄弟可以请求数据项，直接返回
    if (parent.length() == 1) {
        return;
    }
    Page<K, V> sibling;
    // 除非当前页已是上级索引中的最后一块，否则兄弟页总是位于右侧.
    if (page.loc() < parent.length() - 1) {
        sibling = (Page<K, V>) parent.get(page.loc() + 1);
        // 向兄弟请求后继页
        if (sibling.length() > this.M / 2) {
            moveSuccessor(page, sibling);
            updateIndex(sibling); // 更新兄弟的上级索引
        } else { // 将兄弟向当前页合并
            mergeBlock(page, sibling);
            _pages.remove(sibling); // 在链表中删除记录
            removeBlock(sibling); // 递归删除空页
        }
        updateIndex(page);
    } else {
        sibling = (Page<K, V>) parent.get(page.loc() - 1);
        // 向兄弟请求前驱页
        if (sibling.length() > this.M / 2) {
            movePredecessor(page, sibling);
            updateIndex(page);
        } else { // 将当前页向兄弟合并
            mergeBlock(sibling, page);
            _pages.remove(page);
            removeBlock(page);
        }
        updateIndex(sibling); // 统一更新兄弟的索引
    }
}
```

---

## V 2.0

新增：

- 用于前端解析、提交用户输入命令的解析器`Parser`与执行器`Executor`模块；
- 为整个项目增加了一个入口函数`Main.main`；
- 为数据表结构对应的类`Table`新增了基于序列化与反序列化的磁盘读写功能，对应方法为`public void open() throws IOException, ClassNotFoundException`与`public void close () throws IOException`；

修改：

- 将原来基于内存的B+树重命名为`InMemBPlusTree`，从而避免设计两套增删改查方法；
- `BPlusTree`类现在在实现`Index`接口时，其增删改查操作都会设计到对应`Table`实例的`open`与`close`；
