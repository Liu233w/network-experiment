package com.liu233w.http.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 处理 Service
 */
public class ServiceHandler implements Runnable {

    private final Socket socket;
    private final Path basePath;
    private OutputStream outputStream;
    private RequestReader request;

    // 超时： 30 分钟
    private static final int SOCKET_TIMEOUT_MILLISECONDS = 1000 * 60 * 30;

    public ServiceHandler(Socket socket, Path basePath) {
        this.socket = socket;
        this.basePath = basePath;
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

        if (request.getMethod().equals(HttpMethod.GET)) {
            // 直接将url作为路径使用
            // 不考虑路径中 url 转义的情况
            handleGet(request.getUrl());
        } else if (request.getMethod().equals(HttpMethod.PUT)) {

            byte[] content = request.getContent();
            if (content.length == 0) {
                processPlainTextResponse(400, "400 Bad Request. Body must have content.");
                return;
            }

            // 直接将url作为路径使用
            // 不考虑路径中 url 转义的情况
            handlePut(request.getUrl(), content);

        } else {
            // 不支持其他操作
            processPlainTextResponse(501,
                    "Server don't support this method.");
        }
    }

    /**
     * 处理 Get 请求
     *
     * @param path 请求的路径（要获取的文件相对于 webroot 的路径，以‘/’打头）
     * @throws IOException
     */
    private void handleGet(String path) throws IOException {
        System.out.println("GET " + path);

        if (!path.startsWith("/")) {
            process404();
            return;
        }

        File file = getFileFromPathOrNull(path);
        if (file == null || !file.isFile()) {
            // 不考虑请求 "/" 路径且存在 "/index.html" 的情况（默认文档）
            // 只能读取文件
            process404();
            return;
        }

        processResponse(
                new ResponseBuilder(200)
                        .addHeader("Content-Type",
                                resolveMimeTypeFromFileName(file.getName()))
                        .setContent(Files.readAllBytes(file.toPath()))
        );
    }

    /**
     * 根据 http 的 uri 获取在服务器上的文件（夹）。如果路径路径非法，返回 null。
     * 不考虑文件（夹）是否存在。
     *
     * @param path uri 的路径（必须以斜杠打头）
     * @return 在服务器上的绝对路径
     */
    private File getFileFromPathOrNull(String path) {
        // 去除头部的斜杠，方便处理
        path = path.substring(1);

        Path newPath = basePath.resolve(path);

        // 如果两个路径不在同一个分区下，会抛出异常
        Path relPath = basePath.relativize(newPath).normalize();
        if (relPath.startsWith("..")) {
            // 跑到根路径外面了
            return null;
        }

        return newPath.toFile();
    }

    /**
     * 从文件名获取 mime 类型
     *
     * @param fileName
     * @return
     */
    private String resolveMimeTypeFromFileName(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        } else if (fileName.endsWith(".jpg")) {
            return "image/jpg";
        } else {
            // 默认返回值
            return "application/octet-stream";
        }
    }

    /**
     * 处理put操作
     *
     * @param path put 的url（路径），必须包含文件名。例如： /foo/bar/baz.jpg
     * @throws IOException
     */
    private void handlePut(String path, byte[] content) throws IOException {
        System.out.println("PUT " + path);

        if (!path.startsWith("/")) {
            process404();
            return;
        }

        File file = getFileFromPathOrNull(path);

        if (file == null) {
            processPlainTextResponse(403, "403 Permission Denied. " +
                    "You don't have permission to visit this path.");
            return;
        }
        if (file.isDirectory()) {
            processPlainTextResponse(400, "400 Bad Requests. The url must be a file.");
            return;
        }

        boolean newFile = file.createNewFile();
        if (!newFile) {
            // Bug1： 没有遵循 put 的定义。 put 操作是如果资源存在，则更新资源。
            processPlainTextResponse(400, "400 Bad Requests. The file is already exist.");
            return;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file.getPath())) {
            fileOutputStream.write(content);
        }

        // 不需要 content
        processResponse(new ResponseBuilder(200));
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
        );
    }

    /**
     * 向客户端发送相应，根据 {@link ResponseBuilder} 来生成相应
     *
     * @param rb
     * @throws IOException
     */
    private void processResponse(ResponseBuilder rb) throws IOException {
        // PrintStream outputStream = System.out; // DEBUG
        outputStream.write(rb.build());
        outputStream.flush();
    }
}
