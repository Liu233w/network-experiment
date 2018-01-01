package com.liu233w.network.webservice.client;

import com.liu233w.network.webservice.server.Item;
import com.liu233w.network.webservice.server.QueryResult;
import com.liu233w.network.webservice.server.TodoListResult;
import com.liu233w.network.webservice.server.TodoListService;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * 实际处理客户端交互的类
 */
public class ClientHandler {
    private TodoListService service;
    private SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
    private BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    private String username;
    private String password;

    public ClientHandler(TodoListService service) {
        this.service = service;
    }

    /**
     * 开始处理业务
     */
    public void start() throws IOException, LoginFailedException {
        // 处理登陆注册
        System.out.print("TodoList 客户端 \n" +
                "刘书敏 14011501 2015303087\n" +
                "请登陆或注册，命令格式：\n" +
                "1. 登陆： login [username] [password]\n" +
                "2. 注册： register [username] [password]\n");
        while (username == null || username.equals("")) {
            System.out.print(">>> ");
            String line = bufferedReader.readLine();
            String[] split = line.split(" ");
            switch (split[0]) {
                case "login":
                    handleLogin(split[1], split[2]);
                    break;
                case "register":
                    handleRegister(split[1], split[2]);
                    break;
                default:
                    System.out.println("命令格式不正确");
            }
        }

        // 开始业务
        handleHelp();
        startRepl();
    }

    /**
     * 检查结果是否正确，如果不正确则打印结果中的异常信息，使用这个可以较方便的处理业务
     *
     * @param result 从 Service 获取的结果
     * @return 结果是否正确
     * @throws LoginFailedException
     */
    private boolean checkSuccess(TodoListResult result) throws LoginFailedException {

        if (result.isAuthorizeFailed()) {
            // 由于在刚开始登陆的时候就已经检查了用户名和密码了（登陆失败时不会把这个位置设为 true）
            // 所以这个异常在正常情况下不会抛出。这里只是额外处理一下。
            throw new LoginFailedException(result.getMessage());
        }

        boolean success = result.isSuccess();
        if (!success) {
            System.out.println(result.getMessage());
        }

        return success;
    }

    /**
     * 登陆，检查用户名和密码是否正确。
     *
     * @return 不正确或用户不存在时返回 false
     */
    private boolean handleLogin(String username, String password) throws LoginFailedException {
        TodoListResult result = service.checkUser(username, password);
        boolean success = checkSuccess(result);
        if (success) {
            System.out.println("登陆成功");
            this.username = username;
            this.password = password;
        }
        return success;
    }

    /**
     * 注册
     */
    private boolean handleRegister(String username, String password) throws LoginFailedException {
        TodoListResult result = service.register(username, password);
        boolean success = checkSuccess(result);
        if (success) {
            System.out.println("注册成功");
            this.username = username;
            this.password = password;
        }
        return success;
    }

    /**
     * 开启 REPL
     *
     * @throws IOException
     * @throws LoginFailedException
     */
    private void startRepl() throws IOException, LoginFailedException {
        String line;
        do {
            System.out.print(">>> ");
            line = bufferedReader.readLine();
            // line -> "method arg1 arg2..."
            String[] split = line.split(" ");
            switch (split[0]) {
                case "add":
                    handleAdd(split);
                    break;
                case "query":
                    handleQuery(split);
                    break;
                case "delete":
                    handleDelete(split);
                    break;
                case "clear":
                    handleClear();
                    break;

                case "quit":
                    break;
                case "help":
                    handleHelp();
                case "":
                    break;
                default:
                    System.out.println("无法识别指令");
            }
        } while (!line.equals("quit"));
    }

    private void handleHelp() {
        System.out.println("欢迎使用代办事项管理系统 v1.0\n" +
                "作者： 刘书敏 2015303087\n" +
                "使用说明：\n" +
                "1. 添加项目： add [description] [start] [end]\n" +
                "日期格式为： 2017-12-15-11:00\n" +
                "2. 查询项目： query [start] [end]\n" +
                "日期格式为： 2017-12-15-11:00\n" +
                "3. 删除项目： delete [meetingId]\n" +
                "4. 清空项目： clear\n" +
                "5. 再次显示此帮助： help\n" +
                "6. 退出： quit");
    }

    private void handleClear() throws LoginFailedException {
        boolean success = checkSuccess(service.clear(username, password));
        if (success) {
            System.out.println("清除成功");
        }
    }

    private void handleDelete(String[] args) throws LoginFailedException {
        TodoListResult result = service.delete(Integer.parseInt(args[1]), username, password);
        boolean success = checkSuccess(result);
        if (success) {
            System.out.println("删除成功");
        }
    }

    /**
     * 从字符串获取指定格式的时间数据
     *
     * @param str 表示时间的字符串，格式： 2017-12-13-11:23
     * @return
     * @throws GetXmlDateFromStringException 转换失败时抛出异常
     */
    private XMLGregorianCalendar getXmlDateFromString(String str) throws GetXmlDateFromStringException {
        try {
            Date date = dateParser.parse(str);
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.setTime(date);
            return DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
            throw new GetXmlDateFromStringException(e.getMessage(), e);
        } catch (ParseException e) {
            throw new GetXmlDateFromStringException("时间格式不正确，应有的格式： 2017-12-13-11:23", e);
        }
    }

    /**
     * 查询待办事项
     *
     * @param args 分别为： query [start] [end]
     * @throws LoginFailedException
     */
    private void handleQuery(String[] args) throws LoginFailedException {

        XMLGregorianCalendar start, end;
        try {
            // 转换时间
            start = getXmlDateFromString(args[1]);
            end = getXmlDateFromString(args[2]);
        } catch (GetXmlDateFromStringException e) {
            System.out.println(e.getMessage());
            return;
        }

        QueryResult result = service.query(start, end, username, password);
        boolean success = checkSuccess(result);
        if (success) {
            System.out.println("查询成功");
            List<Item> items = result.getResults();
            printItems(items);
        }
    }

    /**
     * 添加待办事项
     *
     * @param args 分别为： add [title] [start] [end]
     * @throws RemoteException
     */
    private void handleAdd(String[] args) throws LoginFailedException {

        XMLGregorianCalendar start, end;
        try {
            // 转换时间
            start = getXmlDateFromString(args[2]);
            end = getXmlDateFromString(args[3]);
        } catch (GetXmlDateFromStringException e) {
            System.out.println(e.getMessage());
            return;
        }

        Item item = new Item();
        item.setDescription(args[1]);
        item.setStart(start);
        item.setEnd(end);
        TodoListResult result = service.add(item, username, password);

        boolean success = checkSuccess(result);
        if (success) {
            System.out.println("添加成功");
        }
    }

    /**
     * 打印出参数所给的待办事项
     *
     * @param items
     */
    private void printItems(List<Item> items) {
        ConsoleTable consoleTable = new ConsoleTable(5, true)
                .appendRow()
                .appendColum("ID").appendColum("内容").appendColum("开始时间")
                .appendColum("结束时间").appendColum("创建者");
        items.stream().forEach(item ->
                consoleTable.appendRow()
                        .appendColum(item.getId())
                        .appendColum(item.getDescription())
                        .appendColum(item.getStart())
                        .appendColum(item.getEnd())
                        .appendColum(item.getCreator())
        );

        System.out.println(consoleTable);
    }
}
