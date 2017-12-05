package com.liu233w.network.ftp.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static Path path;
    private static ExecutorService executorService;  //线程池
    private static final int POOL_SIZE = 4; //单个处理器线程池工作线程数目
    private static ServerSocket serverSocket;
    private static DatagramSocket udpSocket;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("usage: java FileServer <dir>");
            return;
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("路径不存在");
            return;
        }
        if (!file.isDirectory()) {
            System.err.println("该位置不是文件夹");
            return;
        }

        path = Paths.get(file.getPath());

        try {
            serverSocket = new ServerSocket(2021);
            udpSocket = new DatagramSocket(2020);
        } catch (IOException e) {
            e.printStackTrace();
            // 在进程终止的时候两个 Socket 会被释放，不需要显式释放
            return;
        }

        executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * POOL_SIZE);


        startServerLoop();
    }

    private static void startServerLoop() {
        System.out.println("服务器启动");

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                executorService.execute(new ServiceHandler(socket, udpSocket, path));
            } catch (IOException e) {
                System.err.println("IO 异常");
                e.printStackTrace();
            }
        }
    }
}
