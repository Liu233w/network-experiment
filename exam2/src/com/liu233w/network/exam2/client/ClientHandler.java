package com.liu233w.network.exam2.client;

import com.liu233w.network.exam2.share.*;
import com.liu233w.network.exam2.share.exceptions.LoginFailedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.List;

/**
 * 实际处理客户端交互的类
 */
public class ClientHandler {
    private MessageService service;
    private String username;
    private String password;

    public ClientHandler(MessageService service) {
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
                case "users":
                    handleShowUsers(split);
                    break;
                case "messages":
                    handleCheckMessages(split);
                    break;
                case "new":
                    handleLeaveMessages(split);
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

    private void handleLeaveMessages(String[] split) throws RemoteException, LoginFailedException {
        boolean success = service.leaveMessage(username, password, split[1], split[2]);
        if (success) {
            System.out.println("添加成功");
        } else {
            System.out.println("添加失败，目标用户可能不存在。");
        }
    }

    private void handleCheckMessages(String[] split) throws RemoteException, LoginFailedException {
        List<Message> messages = service.checkMessage(username, password);
        printMessages(messages);
    }

    private void handleShowUsers(String[] split) throws RemoteException, LoginFailedException {
        List<String> users = service.showUsers(username, password);
        printUsers(users);
    }

    private void handleHelp() {
        System.out.println("欢迎使用信息管理系统 v1.0\n" +
                "作者： 刘书敏 2015303087\n" +
                "使用说明：\n" +
                "1. 显示所有用户： users\n" +
                "2. 打印本用户的所有留言： messages\n" +
                "3. 给其他用户留言： new [receiver] [content] \n" +
                "4. 再次显示此帮助： help\n" +
                "5. 退出： quit");
    }

    /**
     * 打印出参数所给的消息
     *
     * @param messages
     */
    private void printMessages(List<Message> messages) {
        if (messages.size() == 0) {
            System.out.println("该用户没有留言");
        } else {
            ConsoleTable consoleTable = new ConsoleTable(3, true)
                    .appendRow()
                    .appendColum("留言者").appendColum("留言时间").appendColum("内容");
            messages.stream().forEach(item -> {
                consoleTable.appendRow()
                        .appendColum(item.getCreator())
                        .appendColum(item.getCreateTime())
                        .appendColum(item.getContent());
            });

            System.out.println(consoleTable);
        }
    }

    /**
     * 打印出参数所给的用户信息
     *
     * @param users
     */
    private void printUsers(List<String> users) {
        if (users.size() == 0) {
            // 这个不会发生
            System.out.println("当前系统没有用户");
        } else {
            ConsoleTable consoleTable = new ConsoleTable(1, true)
                    .appendRow()
                    .appendColum("用户名");
            users.stream().forEach(item -> {
                consoleTable.appendRow()
                        .appendColum(item);
            });

            System.out.println(consoleTable);
        }
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
