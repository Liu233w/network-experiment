package com.liu233w.network.ftp.client;

import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 2021);
            System.out.println("连接成功");

            new Client(socket).startLoop();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != socket) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
