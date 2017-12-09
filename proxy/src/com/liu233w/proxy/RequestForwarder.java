package com.liu233w.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

/**
 * 用于将用户的请求转发至指定服务器的类
 */
public class RequestForwarder {

    private RequestReader requestReader;

    /**
     * Allow a maximum buffer size of 8192 bytes
     */
    private static int buffer_size = 8192;

    /**
     * Response is stored in a byte array.
     */
    private byte[] buffer;

    /**
     * My socket to the world.
     */
    private Socket socket = null;

    private InputStream in = null;

    private OutputStream out = null;

    /**
     * 初始化类
     *
     * @param requestReader
     */
    public RequestForwarder(RequestReader requestReader) {
        this.requestReader = requestReader;
    }

    /**
     * 初始化流
     * @throws IOException
     */
    private void initStream() throws IOException {
        socket = new Socket(requestReader.getHost(), requestReader.getPort());
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    /**
     * 向指定位置转发请求并且获取返回的字节数组，保证不抛出异常
     *
     * @return 如果请求成功，返回字节数组，否则返回 null
     */
    public byte[] forwardAndGetResponseByteArray() {
        byte[] res = null;

        try {
            initStream();

            // 向要请求的服务器写入
            out.write("GET ".getBytes());
            out.write(requestReader.getUri().getBytes());
            out.write(" HTTP/1.0".getBytes());
            out.write("\r\n".getBytes());
            out.write(requestReader.getHeadersAndContent());
            out.flush();

            // 从服务器读取
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int c;
            while((c = in.read()) != -1) {
                byteArrayOutputStream.write(c);
            }

            res = byteArrayOutputStream.toByteArray();
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

            return res;
        }
    }
}
