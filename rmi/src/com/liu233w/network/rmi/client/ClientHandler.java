package com.liu233w.network.rmi.client;

import com.liu233w.network.rmi.share.Meeting;
import com.liu233w.network.rmi.share.MeetingService;
import com.liu233w.network.rmi.share.exceptions.AddMeetingException;
import com.liu233w.network.rmi.share.exceptions.BadInputException;
import com.liu233w.network.rmi.share.exceptions.LoginFailedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

/**
 * 实际处理客户端交互的类
 */
public class ClientHandler {
    private MeetingService service;
    private SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
    private String username;
    private String password;

    public ClientHandler(MeetingService service) {
        this.service = service;
    }

    /**
     * 开始处理交互
     *
     * @param startArgs 程序的启动参数
     * @throws RemoteException
     */
    public void process(String[] startArgs) throws RemoteException {

        username = startArgs[3];
        password = startArgs[4];
        String method = startArgs[2];

        if (method.equals("register")) {
            boolean success = handleRegister();
            if (!success) {
                return;
            }
        } else if (method.equals("login")) {
            boolean success = handleLogin();
            if (!success) {
                return;
            }
        } else {
            System.out.println("无法识别启动参数\n" +
                    "用法： java [clientName] [hostAddress] [portNumber] login|register [username] [password]");
            return;
        }

        try {
            handleHelp();
            startRepl();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LoginFailedException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 登陆，检查用户名和密码是否正确。
     *
     * @return 不正确或用户不存在时返回 false
     * @throws RemoteException
     */
    private boolean handleLogin() throws RemoteException {
        boolean success = service.checkUser(username, password);
        if (!success) {
            System.out.println("登陆失败，请检查您的用户名和密码");
        } else {
            System.out.println("登陆成功");
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
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        String line;
        do {
            System.out.print(">>> ");
            line = in.readLine();
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
        System.out.println("欢迎使用会议管理系统 v1.0\n" +
                "作者： 刘书敏 2015303087\n" +
                "使用说明：\n" +
                "1. 添加会议： add [title] [otherusername] [start] [end]\n" +
                "日期格式为： 2017-12-15-11:00\n" +
                "2. 查询会议： query [start] [end]\n" +
                "日期格式为： 2017-12-15-11:00\n" +
                "3. 删除会议： delete [meetingId]\n" +
                "4. 清除会议： clear\n" +
                "5. 再次显示此帮助： help\n" +
                "6. 退出： quit");
    }

    private void handleClear() throws RemoteException, LoginFailedException {
        service.clear(username, password);
        System.out.println("清除成功");
    }

    private void handleDelete(String[] args) throws RemoteException, LoginFailedException {
        service.delete(Integer.parseInt(args[1]), username, password);
        System.out.println("删除成功");
    }

    private void handleQuery(String[] args) throws RemoteException, LoginFailedException {
        List<Meeting> meetings = null;
        try {
            meetings = service.query(
                    dateParser.parse(args[1]),
                    dateParser.parse(args[2]),
                    username, password
            );
        } catch (ParseException e) {
            System.out.println("时间格式不正确，应有的格式： 2017-12-13-11:23");
        }
        System.out.println("查询成功");
        printMeetings(meetings);
    }

    /**
     * 添加会议
     *
     * @param args 分别为： add [title] [otherUserName] [start] [end]
     * @throws RemoteException
     */
    private void handleAdd(String[] args) throws RemoteException, LoginFailedException {
        try {
            Meeting meeting = new Meeting(0, username, args[2], args[1],
                    dateParser.parse(args[3]),
                    dateParser.parse(args[4]));
            service.add(meeting, username, password);
            System.out.println("添加成功");
        } catch (AddMeetingException e) {
            System.out.println("当前会议与已有会议冲突，冲突会议已在下方列出");
            printMeetings(e.getConfilctMeetings());
        } catch (BadInputException e) {
            System.out.println(e.getMessage());
        } catch (ParseException e) {
            System.out.println("时间格式不正确，应有的格式： 2017-12-13-11:23");
        }
    }

    /**
     * 打印出参数所给的会议
     *
     * @param meetings
     */
    private void printMeetings(List<Meeting> meetings) {
        ConsoleTable consoleTable = new ConsoleTable(6, true)
                .appendRow()
                .appendColum("ID").appendColum("会议标题").appendColum("开始时间")
                .appendColum("结束时间").appendColum("发起人").appendColum("参与者");
        meetings.stream().forEach(item -> {
            consoleTable.appendRow()
                    .appendColum(item.getId())
                    .appendColum(item.getTitle())
                    .appendColum(item.getStart())
                    .appendColum(item.getEnd())
                    .appendColum(item.getCreator())
                    .appendColum(item.getOtherUser());
        });

        System.out.println(consoleTable);
    }

    /**
     * 注册
     *
     * @throws RemoteException
     */
    private boolean handleRegister() throws RemoteException {
        boolean success = service.register(username, password);
        if (success) {
            System.out.println("注册成功");
        } else {
            System.out.println("注册失败，可能用户名重复");
        }
        return success;
    }
}
