package com.liu233w.http.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

/**
 * 用于构造返回值的类，会自动根据状态码生成状态信息、自动生成日期信息、自动生成
 * Content-Length。
 */
public class ResponseBuilder {
    private HashMap<String, String> headers;
    private byte[] content;
    private int code;
    private static final String HttpVersion = "HTTP/1.1";

    private static HashMap<Integer, String> codeReason;

    /**
     * 无参构造函数，将生成长度为0的content。code默认为0
     */
    public ResponseBuilder() {
        headers = new HashMap<>();
        content = new byte[0];

        headers.put("Date", new Date().toGMTString());
        headers.put("Connection", "Close");

        initCodeReason();

        code = 0;
    }

    /**
     * 使用指定的HTTP Status Code来初始化
     *
     * @param initCode
     */
    public ResponseBuilder(int initCode) {
        this();
        code = initCode;
    }

    /**
     * 生成 CodeReason。这个哈希表用于生成 response 第一行的第三个参数。
     * 根据用户输入的 status code 来自动填上这个参数。
     */
    private void initCodeReason() {
        if (codeReason == null) {
            codeReason = new HashMap<>();

            codeReason.put(404, "Not Found");
            codeReason.put(200, "OK");
            codeReason.put(400, "Bad Request");
            codeReason.put(403, "Permission Denied");
            codeReason.put(500, "Internal Server Error");
            codeReason.put(501, "Not Implemented");
        }
    }

    /**
     * 添加 Header。如果有相同的header，原来的值将被覆盖
     *
     * @param key   header 的名字
     * @param value header 的参数
     * @return 当前对象，用于链式调用
     */
    public ResponseBuilder addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * 构造 Response
     *
     * @return byte数组形式的返回值
     */
    public byte[] build() throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             PrintWriter pw = new PrintWriter(byteArrayOutputStream)) {

            // 这里不处理“没有输入 code”的边界条件
            pw.print(HttpVersion);
            pw.print(" ");
            pw.print(code);
            pw.print(" ");
            pw.println(codeReason.get(code));

            headers.forEach((String key, String value) -> {
                pw.print(key);
                pw.print(": ");
                pw.println(value);
            });

            pw.print("Content-Length: ");
            pw.println(content.length);

            pw.println("");

            pw.flush();

            byteArrayOutputStream.write(content);

            return byteArrayOutputStream.toByteArray();
        }
    }

    public byte[] getContent() {
        return content;
    }

    public ResponseBuilder setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public int getCode() {
        return code;
    }

    /**
     * 设置status code
     *
     * @param code
     * @return 当前对象，用于链式调用
     */
    public ResponseBuilder setCode(int code) {
        this.code = code;
        return this;
    }
}
