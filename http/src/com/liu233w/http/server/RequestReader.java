package com.liu233w.http.server;

import java.io.*;
import java.util.HashMap;

/**
 * 从流中读取 Request，便于查询。本类会将 Content 全部一次性读取进内存中，
 * 使用 byte数组 存储数据。不适合过大的内容。
 */
public class RequestReader {
    private InputStream inputStream;

    private HashMap<String, String> headers;
    private byte[] content;
    private String url;
    private HttpMethod method;
    private String httpVersion;

    /**
     * 从输入流初始化 requests，忽略格式不正确的情况。
     *
     * @param in
     */
    public RequestReader(InputStream in) throws IOException {

        inputStream = in;

        parseStream();
    }

    /**
     * 从流中读取内容以初始化对象。
     *
     * @throws IOException
     */
    private void parseStream() throws IOException {
        String line = readLineFromStream().toLowerCase();

        String[] split = line.split(" ", 3);

        // 不考虑用户输入的 Method 不正确的情况
        method = HttpMethod.valueOf(split[0].toUpperCase());
        url = split[1];
        httpVersion = split[2];

        // 处理 header
        headers = new HashMap<>();
        line = readLineFromStream();
        while (line != null && !line.equals("")) {
            // 处理 headers
            String[] header = line.split(": ", 2);
            headers.put(header[0], header[1]);
            line = readLineFromStream();
        }

        // 处理 content
        String lengthS = getHeader("Content-Length");
        if (lengthS == null) {
            content = new byte[0];
        } else {
            // 不考虑格式不正确的情况
            int length = Integer.parseInt(lengthS);
            content = new byte[length];
            int readed = 0;
            while (readed < length) {
                // 在读够之前一直读取，read不会一次返回所有数据
                readed += inputStream.read(content, readed, length - readed);
                System.out.println("readed: " + readed);
            }
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
        while ((c = inputStream.read()) != -1) {
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

    /**
     * 获取指定名称的 header，如果不存在，返回 null
     *
     * @param field
     * @return
     */
    public String getHeader(String field) {
        return headers.getOrDefault(field, null);
    }

    public byte[] getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}
