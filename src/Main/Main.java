package Main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import Main.Parser;

public class Main {

    /** 简易数据库的启动问候语 */
    private static void greeting() {
        System.out.println("这是一个简单的单机数据库\n\n" +
                "它支持:\n" +
                " - 同一类型数据的插入、删除、查询、修改\n" +
                " - 同时利用了内存与磁盘，支持大批量数据的操作\n" +
                " - 单表数据量过大时会自动剖分，负载均衡\n" +
                "遗憾的是，它不支持：\n" +
                " - 多线程并发读写数据\n" +
                " - 事务\n" +
                " - 非正常退出(如断电、程序崩溃)下的数据恢复\n\n" +
                "你可以输入\"help\"来获取详细操作说明\n" +
                "现在你可以尽情地用它玩耍:)\n");
    }

    /** 简易数据库的命令介绍 */
    private static void help() {
        System.out.println("""
                假设待插入的记录是: key: (value1, value2, value3, ...)
                则该数据库支持的语法有:
                  1. 插入语句: INSERT {key} AS {(value1, value2, value3, ...)}
                  2. 更新语句: UPDATE {key} AS {(value1, value2, value3, ...)}
                  3. 删除语句: DELETE {key}
                  4. 查看语句：
                    4.1 查看键对应的值: SHOW KEY {key}
                    4.2 查看表名对应的视图: SHOW TABLE {tableFileName}
                    4.3 查看索引区的视图: SHOW INDEX
                注: 所有的关键字均大小写不敏感
                """);
    }

    /** 从文件中加载预先准备好的数据 */
    public static void loadDemo(Parser parser) throws IOException, ClassNotFoundException {
        String fileName = "./test/testDataSet.txt";
        // 从文件中读取指令并逐行输入到命令行中
        File file = new File(fileName);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            System.setIn(new ByteArrayInputStream(line.getBytes()));
            parser.setUserInput(line);
            parser.parseWithExecutor();
        }
        scanner.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        greeting();
        // 初始化解析器与执行器
        Scanner sc = new Scanner(System.in);
        Executor<String, String> bpt_engine = new Executor<String, String>("B+-Tree", 16, 20, 12);
        Parser parser = new Parser();
        parser.setExecutor(bpt_engine);
        // 事件循环
        while (true) {
            String input = sc.nextLine();
            if (input.equals("exit")) {
                break;
            } else if (input.equals("help")) {
                help();
            } else if (input.equals("load demo")) {
                loadDemo(parser);
            }
            else {
                parser.setUserInput(input);
                parser.parseWithExecutor();
            }
        }
    }
}
