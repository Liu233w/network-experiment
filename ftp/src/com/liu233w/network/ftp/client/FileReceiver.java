package com.liu233w.network.ftp.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class FileReceiver {
    private final DatagramSocket socket;
    private static final int DATAGRAM_LENGTH = 512;

    /**
     * 初始化 udp socket
     *
     * @throws SocketException
     */
    public FileReceiver() throws SocketException {
        socket = new DatagramSocket();
    }

    /**
     * 获取socket的端口号
     *
     * @return 端口号
     */
    public int getUdpPort() {
        return socket.getLocalPort();
    }

    /**
     * 从 udp 读取文件并保存在指定目录
     *
     * @param path   文件保存的路径（包括文件名）
     * @param length 文件的长度
     * @throws IOException
     */
    public void ReceiveFile(String path, int length) throws IOException {

        System.out.println("将文件保存至： " + path);
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(path, false);

        final int loopCnt = length / DATAGRAM_LENGTH + 1;

        for (int i = 0; i < loopCnt; ++i) {

            DatagramPacket datagramPacket = new DatagramPacket(new byte[DATAGRAM_LENGTH], DATAGRAM_LENGTH);
            socket.receive(datagramPacket);

            fileOutputStream.write(
                    datagramPacket.getData(),
                    0,
                    // length 会被设置成实际接收的长度
                    datagramPacket.getLength());
        }

        fileOutputStream.close();
    }

    /**
     * 关闭 socket，释放资源
     */
    public void close() {
        socket.close();
    }
}
