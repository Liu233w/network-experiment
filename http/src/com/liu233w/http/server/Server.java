package com.liu233w.http.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    /**
     * default HTTP port is port 80
     */
    private static int port = 80;

    /**
     * Allow a maximum buffer size of 8192 bytes
     */
    private static int buffer_size = 8192;

    /**
     * The end of line character sequence.
     */
    private static String CRLF = "\r\n";

    private static Path path;
    private static ExecutorService executorService;  //线程池
    private static final int POOL_SIZE = 4; //单个处理器线程池工作线程数目
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        // 设置行末符号，用在 socket 的 PrintWriter 上
        System.setProperty("line.separator", CRLF);

        if (args.length == 0) {
            System.out.println("usage: java HttpServer <dir>");
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
            serverSocket = new ServerSocket(port);
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
                executorService.execute(new ServiceHandler(socket, path));
            } catch (IOException e) {
                System.err.println("IO 异常");
                e.printStackTrace();
            }
        }
    }
}
