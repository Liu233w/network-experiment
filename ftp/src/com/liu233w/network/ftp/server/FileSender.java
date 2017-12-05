package com.liu233w.network.ftp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class FileSender {
    private final DatagramSocket socket;
    private static final int DATAGRAM_LENGTH = 512;

    /**
     * 初始化发送方，需要知道接收方的 ip
     *
     * @param address 接收方的 ip 地址
     * @param socket
     * @throws SocketException 如果socket初始化错误，会抛出此异常
     */
    public FileSender(InetAddress address, DatagramSocket socket) throws SocketException {

        this.socket = socket;
    }

    /**
     * 向指定端口发送文件
     *
     * @param file    文件对象
     * @param address 客户端的地址
     * @throws FileNotFoundException 当文件不存在时抛出
     * @throws IOException           文件读取或者 udp 发送失败时抛出
     */
    public void sendFileAndCloseSocket(File file, InetSocketAddress address) throws FileNotFoundException, IOException {

        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());

        byte[] bytes = new byte[DATAGRAM_LENGTH];
        int realLength;
        while ((realLength = fileInputStream.read(bytes)) != -1) {
            DatagramPacket datagramPacket = new DatagramPacket(bytes, realLength, address);
            socket.send(datagramPacket);
            try {
                TimeUnit.MICROSECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
