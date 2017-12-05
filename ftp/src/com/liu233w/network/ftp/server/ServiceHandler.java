package com.liu233w.network.ftp.server;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.Arrays;

public class ServiceHandler implements Runnable {

    private final Socket socket;
    private final DatagramSocket udpSocket;
    private final Path basePath;
    private Path path;
    private BufferedReader br;
    private PrintWriter pw;

    // 超时： 30 分钟
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 1000 * 60 * 30;

    public ServiceHandler(Socket socket, DatagramSocket udpSocket, Path basePath) {
        this.socket = socket;
        this.udpSocket = udpSocket;
        this.path = this.basePath = basePath;
    }

    /**
     * 初始化 IO 对象
     *
     * @throws IOException
     */
    private void initStream() throws IOException {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        pw = new PrintWriter(bw, true);

        socket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            initStream();

            pw.println(socket.getRemoteSocketAddress() + " 连接成功");
            pw.println("");
            pw.flush();

            while (true) {
                String cmd = br.readLine();
                if (cmd == null) {
                    System.out.println("读取到结尾");
                    break;
                }
                if (cmd.equals("bye")) {
                    break;
                }
                handleCommand(cmd);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                    // 在关闭 socket 的同时会关闭流
                    // see https://stackoverflow.com/questions/17725254/does-closing-the-socket-close-the-stream
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 处理命令
     *
     * @param cmd 用户输入的命令
     */
    private void handleCommand(String cmd) {
        if (cmd.equals("ls")) {
            handleLs();
        } else if (cmd.startsWith("cd ")) { // 带上空格，防止误识别 cdxxxx 的情况
            String[] split = cmd.split(" ", 2);
            handleCd(split[1]);
        } else if (cmd.startsWith("get ")) {
            String[] split = cmd.split(" ", 2);
            handleGet(split[1]);
            // get 命令特殊处理
            return;
        } else {
            pw.println("未知命令");
        }

        pw.println("");
        pw.flush();
    }

    /**
     * 处理用户输入的 get 命令
     * <p>
     * 流程：
     * <p>
     * <p>
     * Client                         Server
     * |                              |
     * |                              |
     * 启动 UDP （随机端口）             |
     * |                              |
     * |     TCP: get 端口号 文件路径   |
     * -----------------------------> |
     * |                              |
     * |                              |
     * |    TCP: OK 文件长度 文件名     |
     * | <-----------------------------
     * |                              |
     * |                              |
     * UDP 接收数据                     UDP 发送数据
     *
     * @param param get 命令的参数（格式：端口号 文件路径）
     */
    private void handleGet(String param) {

        String[] split = param.split(" ", 2);

        File file = resolveFile(split[1]);

        if (file == null) {
            return;
        }

        int port = Integer.parseInt(split[0]);
        InetSocketAddress clientSocketAddress =
                new InetSocketAddress(socket.getInetAddress(), port);

        sendFile(file, clientSocketAddress);
    }

    /**
     * 获取文件并确保文件存在且合法
     *
     * @param p 文件路径
     * @return 如果文件存在且合法，得到文件对象，否则为 null
     */
    private File resolveFile(String p) {
        try {
            File file;

            Path newPath = path.resolve(p);

            // 如果两个路径不在同一个分区下，会抛出异常
            Path relPath = basePath.relativize(newPath).normalize();
            if (relPath.startsWith("..")) {
                // 跑到根路径外面了
                throw new Exception("用户试图访问根路径之外的路径");
            }

            file = newPath.toFile();
            if (!file.exists()) {
                throw new Exception("该路径不存在");
            }

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            pw.println("该文件不存在，请确保下载的是文件而不是文件夹");

            return null;
        }
    }

    /**
     * 向指定端口发送文件
     *
     * @param file    要发送的文件对象
     * @param address 客户端的地址
     */
    private void sendFile(File file, InetSocketAddress address) {
        try {
            pw.println("OK " + file.length() + " " + file.getName());
            pw.println("");
            pw.flush(); // 先发 tcp，后发udp

            new FileSender(socket.getInetAddress(), udpSocket)
                    .sendFileAndCloseSocket(file, address);
        } catch (SocketException e) {
            System.err.println("客户端没有在 2021 端口开启服务");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println("文件一开始存在，后来被删除");
            e.printStackTrace();

            pw.println("该文件不存在");

            pw.println("");
            pw.flush();
        } catch (IOException e) {
            pw.println("文件传输异常");

            pw.println("");
            pw.flush();
        }
    }

    /**
     * 处理用户的 ls 命令
     */
    private void handleLs() {
        File[] files = path.toFile().listFiles();
        Arrays.sort(files);
        for (File file :
                files) {

            StringBuilder stringBuilder = new StringBuilder();

            if (file.isDirectory()) {
                stringBuilder.append("<dir>\t");
            } else {
                stringBuilder.append("<file>\t");
            }

            stringBuilder.append(file.getName());
            stringBuilder.append("\t\t");

            stringBuilder.append(file.length() + " bytes");

            pw.println(stringBuilder);
        }
    }

    /**
     * 处理用户的 cd 命令
     *
     * @param p cd 命令的参数
     */
    private void handleCd(String p) {

        if (p.equals("/")) {
            // 回到根目录
            path = basePath;
            pw.println("OK ");
            return;
        }

        try {
            Path newPath = path.resolve(p);

            // 如果两个路径不在同一个分区下，会抛出异常
            Path relPath = basePath.relativize(newPath).normalize();
            if (relPath.startsWith("..")) {
                // 跑到根路径外面了
                throw new Exception("用户试图访问根路径之外的路径");
            }

            File file = newPath.toFile();
            if (!file.exists() || !file.isDirectory()) {
                throw new Exception("该路径不存在或不是文件夹");
            }

            path = newPath;

            // 程序运行在 Windows 上，替换输出的反斜杠
            pw.println("OK " + relPath.normalize()
                    .toString().replaceAll("\\\\", "/"));
        } catch (Exception e) {
            e.printStackTrace();
            pw.println("该路径不存在或不是文件夹");
        }
    }
}
