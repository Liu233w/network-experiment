package com.liu233w.proxy;

import java.io.*;
import java.util.HashMap;

/**
 * 从流中读取 Request，便于查询。本类会将 Content 全部一次性读取进内存中，
 * 使用 byte数组 存储数据。不适合过大的内容。
 */
public class RequestReader {
    private InputStream inputStream;

    private byte[] headersAndContent;
    private String host;
    private int port;
    private String uri;
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
        String url = split[1];
        httpVersion = split[2];

        // 处理 url
        String[] split1 = url.split("\\/{2}", 2);
        /* -> { "http:", "www.somesite.com:80/index.html"} */
        String[] split2;
        if (split1.length == 1) {
            // splite1 -> { "www.somesite.com:80/index.html" }
            split2 = split1[0].split(":", 2);
        } else {
            split2 = split1[1].split(":", 2);
        }
        /* -> { "www.somesite.com", "80/index.html" } 或者 { "www.somesite.com/index.html" } */
        if (split2.length == 1) {
            // 后者
            String[] split3 = split2[0].split("\\/", 2);
            /* -> { "www.somesite.com", "index.html" } */
            port = 80;
            if (split3.length == 2) {
                host = split3[0];
                uri = "/" + split3[1];
            } else {
                // 还有可能 url 就是 www.somesite.com ，没有最后的斜杠
                host = split3[0];
                uri = "/";
            }
        } else {
            // 前者，不考虑格式不正确（url中有多个冒号）的情况
            host = split2[0];
            String[] split3 = split2[1].split("\\/", 2);
            /* -> { "80", "index.html" } */
            port = Integer.parseInt(split3[0]);
            uri = "/" + split3[1];
        }

        // header and content
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

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
                readed += inputStream.read(content, readed, length - readed);
                System.out.println("readed: " + readed);
            }

            byteArrayOutputStream.write(content);
        }

        headersAndContent = byteArrayOutputStream.toByteArray();
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

    public HttpMethod getMethod() {
        return method;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUri() {
        return uri;
    }

    /**
     * 获取请求中除了第一行以外的其他部分
     *
     * @return
     */
    public byte[] getHeadersAndContent() {
        return headersAndContent;
    }
}
