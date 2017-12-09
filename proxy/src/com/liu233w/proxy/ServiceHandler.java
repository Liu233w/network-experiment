package com.liu233w.proxy;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 处理 Service
 */
public class ServiceHandler implements Runnable {

    private final Socket socket;
    private OutputStream outputStream;
    private RequestReader request;

    // 超时： 30 分钟
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 1000 * 60 * 30;

    public ServiceHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * 初始化 IO 对象
     *
     * @throws IOException
     */
    private void initStream() throws IOException {
        request = new RequestReader(socket.getInputStream());
        outputStream = socket.getOutputStream();

        socket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            initStream();

            try {
                handleRequest();
            } catch (Exception e) {
                // 处理所有异常，这样就可以给用户发送500应答了
                e.printStackTrace();
                processUnhandledException(e);
            }

        } catch (IOException e) {
            // 处理在初始化流或者 processUnhandledException 方法中产生的异常。
            // 这些异常通常无法恢复，因此只能释放掉 socket，没法给用户回应。
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
     * 处理其他 handle 函数未捕获的异常，本函数用来向用户返回 500 响应，防止处理函数
     * 抛出异常导致的 socket 无应答关闭。
     *
     * @param e 未捕获的异常
     * @throws IOException
     */
    private void processUnhandledException(Exception e) throws IOException {
        processPlainTextResponse(500,
                "500 Server Error. \n" +
                        e.toString());
    }

    /**
     * 分发请求。根据request来确定处理本请求的函数。
     *
     * @throws IOException
     */
    private void handleRequest() throws IOException {

        if (request.getMethod().equals(HttpMethod.GET)
                // 用在实际的浏览器代理中（未完成）
                || request.getMethod().equals(HttpMethod.CONNECT
        )) {
            handleGet();
        } else {
            // 不支持其他操作
            processPlainTextResponse(501,
                    "Server don't support this method.");
        }
    }

    /**
     * 处理 Get 请求
     *
     * @throws IOException
     */
    private void handleGet() throws IOException {
        System.out.println("GET " + request.getHost() + ":"
                + request.getPort() + request.getUri());

        RequestForwarder requestForwarder = new RequestForwarder(request);
        byte[] responseByteArray = requestForwarder.forwardAndGetResponseByteArray();

        if (responseByteArray == null) {
            processPlainTextResponse(500, "Server Error, please check log for more information");
        } else {
            // 直接将远程服务器的返回结果进行转发
            processResponse(responseByteArray);
        }

        System.out.println("Done");
    }

    /**
     * 向客户端返回 404 相应
     *
     * @throws IOException
     */
    private void process404() throws IOException {
        processPlainTextResponse(404, "404 Not Found. Please check your Url.");
    }

    /**
     * 向用户以文本格式进行应答
     *
     * @param code    Http Status Code
     * @param content 应答的内容，这里必须提供内容，如果不需要内容，可以直接使用 processResponse 进行应答
     * @throws IOException
     */
    private void processPlainTextResponse(int code, String content) throws IOException {
        processResponse(
                new ResponseBuilder(code)
                        .addHeader("Content-Type", "text/plain")
                        .setContent(content.getBytes())
                        .build()
        );
    }

    /**
     * 将指定的字节数组作为相应发送给客户端
     *
     * @param responseByteArray
     * @throws IOException
     */
    private void processResponse(byte[] responseByteArray) throws IOException {
        // PrintStream outputStream = System.out; // DEBUG
        outputStream.write(responseByteArray);
        outputStream.flush();
    }
}
