package com.liu233w.network.ftp.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Paths;

public class Client {
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private String basePath;

    /**
     * 初始化客户端相应实例
     *
     * @param socket tcp socket 连接
     * @throws IOException
     */
    public Client(Socket socket) throws IOException {
        this.socket = socket;

        br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        pw = new PrintWriter(bw, true);

        // 默认是 ftp 的根目录
        basePath = "/";
    }

    /**
     * 启动客户端 REPL
     *
     * @throws IOException
     */
    public void startLoop() throws IOException {

        // 打印服务端返回的欢迎信息
        readAndPrintToEndAndGetLastLine();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print(basePath + " >> ");
            String line = reader.readLine();
            if (line.equals("")) {
                // 用户啥也没输入
                // 什么都不用做
            } else if (line.startsWith("get ")) {
                handleGet(line);
            } else {
                pw.println(line);
                pw.flush();

                String lastRes = readAndPrintToEndAndGetLastLine();

                // 处理一些例外情况（需要客户端配合的）
                if (line.startsWith("cd ") && lastRes.startsWith("OK ")) {
                    basePath = lastRes.replaceFirst("OK ", "/");
                }

                if (line.startsWith("bye")) {
                    return;
                }
            }
        }
    }

    /**
     * 处理用户的 get 指令
     *
     * @param line 用户输入的指令（包括 get 在内）
     * @throws IOException
     */
    private void handleGet(String line) throws IOException {

        String path = line.split(" ", 2)[1];

        // 读取操作，需要客户端先进行准备
        FileReceiver fileReceiver = new FileReceiver();

        pw.println("get " + fileReceiver.getUdpPort() + " " + path);
        pw.flush();

        String lastRes = readAndPrintToEndAndGetLastLine();

        try {
            if (!lastRes.startsWith("OK")) {
                throw new Exception("接收错误");
            }
            // 格式： OK 长度 文件名
            String[] responses = lastRes.split(" ", 3);
            int length = Integer.parseInt(responses[1]);

            fileReceiver.ReceiveFile(
                    // 接收路径：工作路径
                    Paths.get("").resolve(responses[2]).toAbsolutePath().toString(),
                    length);
        } catch (IOException e) {
            e.printStackTrace();
            // 直接不接收 UDP 即可，丢掉了所有的剩余内容
        } catch (Exception e) {
            e.printStackTrace();
            // 直接不接收 UDP 即可，丢掉了所有的剩余内容
        } finally {
            fileReceiver.close();
        }
    }

    /**
     * 从 socket 中读取全部内容（直到空行为止），然后返回读取到的最后一行（不包括空行）
     *
     * @return 读取到的最后一行（不包括空行）
     * @throws IOException
     */
    private String readAndPrintToEndAndGetLastLine() throws IOException {
        String resp, lastRes = "";
        while ((resp = br.readLine()) != null && !resp.equals("")) {
            System.out.println(resp);
            lastRes = resp;
        }
        return lastRes;
    }
}
