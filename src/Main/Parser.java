package Main;

import java.io.IOException;
import java.util.regex.*;

/**
 * 简单的命令行输入解析器，用于解析用户输入，然后提交给执行器{@link Executor}.
 * <p>
 * @author Episode-Zhang
 * @version 1.0
 */
public class Parser {

    /** 提交给Parser的用户输入字符串. */
    private String _userInput;

    /** 用于匹配的正则表达式规则. */
    private String _rule;

    /** 正则匹配的模式. */
    private Pattern _pattern;

    /** 正则匹配的结果. */
    private Matcher _matcher;

    /** 用户输入语句的执行器. */
    private Executor<String, String> _executor;

    /** 设置待解析的用户输入. */
    public void setUserInput(String userInput) {
        _userInput = userInput.strip().toLowerCase();
    }

    /** 设置执行器. */
    public void setExecutor(Executor<String, String> executor) {
        _executor = executor;
    }

    /** 解析用户输入的同时将关键字与变量提交给执行器. */
    public void parseWithExecutor() throws IOException, ClassNotFoundException {
        if (_userInput.startsWith("insert")) {
            parseInsert();
        } else if (_userInput.startsWith("update")) {
            parseUpdate();
        } else if (_userInput.startsWith("delete")) {
            parseDelete();
        } else if (_userInput.startsWith("show")) {
            parseShow();
        } else {
            System.out.println("不支持的命令，请输入help命令查看更多.");
        }
    }

    /**
     * 解析用户的插入query，并提交给执行器{@link Executor}.<p>
     * 插入query的形式类似于(关键字大小写不敏感):<p>
     * INSERT {@code {key}} AS {@code (val1, val2, ...)}
     * <p>
     * @throws RuntimeException 若用户输入的insert query语句不合语法
     */
    private void parseInsert() throws IOException, ClassNotFoundException {
        // 匹配查询语句
        final String insertRule = "^insert\\s[a-zA-Z0-9 \\u0000-\\uffff]+\\sas\\s[a-zA-Z0-9 \\u0000-\\uffff]+";
        _rule = insertRule;
        _pattern = Pattern.compile(_rule);
        _matcher = _pattern.matcher(_userInput);
        if (!_matcher.find()) {
            throw new RuntimeException("查询语句的语法非法！请输入\"help\"命令查看详情.");
        }
        // 匹配插入的键值
        String query = _matcher.group(0);
        // 匹配(val1, val2,...)这一部分
        String valueRule = "\\(.+?\\)";
        Pattern valuePattern = Pattern.compile(valueRule);
        Matcher valueMatcher = valuePattern.matcher(query);
        if (!valueMatcher.find()) {
            throw new RuntimeException("查询语句中值的格式非法！请输入\"help\"命令查看详情.");
        }
        // 提取键和值
        final int keyIndex = 1;
        String key = (query.split(" "))[keyIndex];
        String value = valueMatcher.group(0);
        // 提交给Executor
        String res = _executor.executeInsert(key, value);
        System.out.println(res);
    }

    /**
     * 解析用户的更新query，并提交给执行器{@link Executor}.<p>
     * 更新query的形式类似于(关键字大小写不敏感):<p>
     * UPDATE {@code {key}} AS {@code (val1, val2, ...)}
     * <p>
     * @throws RuntimeException 若用户输入的update query语句不合语法
     */
    private void parseUpdate() throws IOException, ClassNotFoundException {
        // 匹配更新语句
        final String updateRule = "^update\\s[a-zA-Z0-9 \\u0000-\\uffff]+\\sas\\s[a-zA-Z0-9 \\u0000-\\uffff]+";
        _rule = updateRule;
        _pattern = Pattern.compile(_rule);
        _matcher = _pattern.matcher(_userInput);
        if (!_matcher.find()) {
            throw new RuntimeException("更新语句的语法非法！请输入\"help\"命令查看详情.");
        }
        // 匹配更新的键值
        String query = _matcher.group(0);
        // 匹配(val1, val2,...)这一部分
        String valueRule = "\\(.*?\\)";
        Pattern valuePattern = Pattern.compile(valueRule);
        Matcher valueMatcher = valuePattern.matcher(query);
        if (!valueMatcher.find()) {
            throw new RuntimeException("更新语句中值的格式非法！请输入\"help\"命令查看详情.");
        }
        // 提取键和值
        final int keyIndex = 1;
        String key = (query.split(" "))[keyIndex];
        String value = valueMatcher.group(0);
        // 提交给Executor
        String res = _executor.executeUpdate(key, value);
        System.out.println(res);
    }

    /**
     * 解析用户的删除query，并提交给执行器{@link Executor}.<p>
     * 删除query的形式类似于(关键字大小写不敏感):<p>
     * DELETE {@code {key}}
     * <p>
     * @throws RuntimeException 若用户输入的delete query语句不合语法
     */
    private void parseDelete() throws IOException, ClassNotFoundException {
        // 匹配更新语句
        final String deleteRule = "^delete\\s[a-zA-Z0-9 \\u0000-\\uffff]+";
        _rule = deleteRule;
        _pattern = Pattern.compile(_rule);
        _matcher = _pattern.matcher(_userInput);
        if (!_matcher.find()) {
            throw new RuntimeException("删除语句的语法非法！请输入\"help\"命令查看详情.");
        }
        // 匹配删除的键
        String query = _matcher.group(0);
        final int keyIndex = 1;
        String key = (query.split(" "))[keyIndex];
        // 提交给Executor
        String res = _executor.executeDelete(key);
        System.out.println(res);
    }

    /**
     * 解析用户的查看query，并提交给执行器{@link Executor}.<p>
     * 查看query类型有3种，其形式类似于(关键字大小写不敏感):<p>
     *   1. 查看某个键的值：SHOW KEY {@code {key}} <p>
     *   2. 查看某张表的视图：SHOW TABLE {@code {tableName}} <p>
     *   3. 查看索引的视图：SHOW INDEX
     * <p>
     * @throws RuntimeException 若用户输入的show query语句不合语法
     */
    private void parseShow() throws IOException, ClassNotFoundException {
        // 配置每种类型的SHOW对应的规则
        String[] rules = { "^show key\\s[a-zA-Z0-9 \\u0000-\\uffff]+",
                "^show table\\s[a-zA-Z0-9 \\u0000-\\uffff]+",
                "^show index\\s*" };
        for (int i = 0; i < rules.length; i++) {
            _pattern = Pattern.compile(rules[i]);
            _matcher = _pattern.matcher(_userInput);
            if (!_matcher.find()) {
                continue;
            }
            // 匹配命中，根据不同的show语句调度执行器
            final int keyIndex = 2;
            String res = null;
            switch (i) {
                case 0 -> {
                    String key = (_userInput.split(" "))[keyIndex];
                    // 提交给Executor
                    res = _executor.executeShowKey(key);
                }
                case 1 -> {
                    String table = (_userInput.split(" "))[keyIndex];
                    // 提交给Executor
                    res = _executor.executeShowTable(table);
                }
                case 2 -> {
                    // 提交给Executor
                    res = _executor.executeShowIndex();
                }
            }
            System.out.println(res);
            return;
        }
        // for语句未命中，则抛出异常
        throw new RuntimeException("查看语句的语法非法！请输入\"help\"命令查看详情.");
    }
}
