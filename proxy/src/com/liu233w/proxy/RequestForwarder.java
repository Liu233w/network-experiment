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

            String line = readLineFromStream();
            byteArrayOutputStream.write(line.getBytes());

            // 处理 header
            HashMap<String, String> headers = new HashMap<>();
            line = readLineFromStream();
            while (line != null && !line.equals("")) {
                byteArrayOutputStream.write(line.getBytes());
                // 处理 headers
                String[] header = line.split(": ", 2);
                headers.put(header[0], header[1]);
                line = readLineFromStream();
            }
            byteArrayOutputStream.write("\r\n".getBytes());

            // 处理 content
            String lengthS = headers.get("Content-Length");
            if (lengthS != null) {
                // 不考虑格式不正确的情况
                int length = Integer.parseInt(lengthS);
                byte[] content = new byte[length];
                int readed = 0;
                while (readed < length) {
                    // 在读够之前一直读取，read不会一次返回所有数据
                    readed += in.read(content, readed, length - readed);
                    System.out.println("readed: " + readed);
                }

                byteArrayOutputStream.write(content);
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

    /**
     * 从流中读取一行并返回
     *
     * @return
     */
    private String readLineFromStream() throws IOException {

        StringBuilder sb = new StringBuilder();

        int c;
        while ((c = in.read()) != -1) {
            switch (c) {
                case '\r':
                    break;
                case '\n':
                    return sb.toString();
                default:
                    sb.append((char) c);
            }
        }
        return sb.toString();
    }
}
