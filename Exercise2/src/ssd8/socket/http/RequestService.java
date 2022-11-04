package ssd8.socket.http;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * @author 2ndElement
 * @version v1.0
 * @description 单请求服务
 * @date 2022/10/22 12:19
 */
public class RequestService implements Runnable {
    private final Socket socket;
    private BufferedInputStream in;
    private static final int BUFFER_SIZE = 8192;
    private BufferedOutputStream out;
    private byte[] buffer;
    /**
     * 存储请求头
     */
    private HashMap<String, String> headers;
    private static final String CRLF = "\r\n";
    /**
     * 简单的 Extension-MIME 映射
     */
    private static final HashMap<String, String> MIME_MAP = new HashMap<String, String>() {
        {
            put("htm", "text/html");
            put("html", "text/html");
            put("jpg", "image/jpeg");
            put("jpeg", "image/jpeg");
            put("png", "image/png");
            put("css", "text/css");
            put("js", "application/javascript");
            put("json", "application/json");
        }
    };

    private void initStream() throws Exception {
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        // 请求头处理
        try {
            int last = 0, c = 0;
            StringBuilder header = new StringBuilder();
            boolean inHeader = true;
            while (inHeader && ((c = in.read()) != -1)) {
                switch (c) {
                    case '\r':
                        break;
                    case '\n':
                        if (c == last) {
                            inHeader = false;
                            break;
                        }
                        last = c;
                        header.append("\n");
                        break;
                    default:
                        last = c;
                        header.append((char) c);
                }
            }
            StringTokenizer tokenizer = new StringTokenizer(header.toString());
            String[] temp;
            String requestLine = tokenizer.nextToken("\n");
            temp = requestLine.split(" ");
            String method = temp[0];
            String path = temp[1].substring(1);
            String response;
            while (tokenizer.hasMoreTokens()) {
                temp = tokenizer.nextToken().split(":");
                headers.put(temp[0].trim().toLowerCase(), temp[1].trim().toLowerCase());
            }
            // 请求方法分支
            if ("GET".equals(method)) {
                File targetFile = new File(path);
                // 若目标文件不存在返回404页面, 否则返回目标页面
                if (!targetFile.isFile()) {
                    response = "HTTP/1.1 404 NOT FOUND" + CRLF;
                    out.write(response.getBytes(), 0, response.length());
                    File notFoundPage = new File("404.html");
                    sendFile(notFoundPage);
                } else {
                    response = "HTTP/1.1 200 OK" + CRLF;
                    out.write(response.getBytes(), 0, response.length());
                    sendFile(targetFile);
                }
            } else if ("PUT".equals(method)) {
                File targetFile = new File(path);
                // 若目标文件不存在, 将其保存返回 201. 若存在, 保存返回 204
                if (!targetFile.exists()) {
                    targetFile.getParentFile().mkdirs();
                    saveFile(targetFile);
                    response = "HTTP/1.1 201 Created" + CRLF + CRLF;
                    out.write(response.getBytes(), 0, response.length());
                    out.flush();
                } else {
                    saveFile(targetFile);
                    response = "HTTP/1.1 204 No Content" + CRLF + CRLF;
                    out.write(response.getBytes(), 0, response.length());
                    out.flush();
                }
            } else {
                // 请求方法不在可选分支内返回400页面
                File badRequestPage = new File("400.html");
                response = "HTTP/1.1 400 Bad Request" + CRLF;
                out.write(response.getBytes(), 0, response.length());
                sendFile(badRequestPage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public RequestService(Socket socket) throws Exception {
        this.socket = socket;
        buffer = new byte[BUFFER_SIZE];
        headers = new HashMap<>();
        initStream();
    }

    /**
     * 保存输入流的文件到本地
     *
     * @param file 目标保存路径逻辑位置
     * @throws IOException
     */
    private void saveFile(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        int size;
        int contentLength = -1;
        String contentLengthString = headers.get("content-length");
        if (contentLengthString != null && !contentLengthString.isEmpty()) {
            contentLength = Integer.parseInt(contentLengthString);
        }
        if (contentLength == -1) {
            while ((size = in.read(buffer)) != -1) {
                fos.write(buffer, 0, size);
            }
        } else {
            int count = 0;
            while (true) {
                if (contentLength - count <= BUFFER_SIZE) {
                    count += in.read(buffer, 0, contentLength - count);
                    fos.write(buffer, 0, contentLength - count);
                    break;
                }
                count += in.read(buffer, 0, BUFFER_SIZE);
                fos.write(buffer, 0, BUFFER_SIZE);
            }
        }
        fos.close();
    }

    /**
     * 将目标文件发送到流, 需要保证文件存在, 会刷新缓存
     *
     * @param file 待发送文件的逻辑位置
     * @throws IOException
     */
    private void sendFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String extension = file.getName().split("\\.")[1];
        String tempHeader;

        tempHeader = "content-length: " + file.length() + CRLF;
        out.write(tempHeader.getBytes(), 0, tempHeader.length());

        tempHeader = "content-type: " + MIME_MAP.get(extension) + CRLF + CRLF;
        out.write(tempHeader.getBytes(), 0, tempHeader.length());

        int size;
        while ((size = fis.read(buffer)) != -1) {
            out.write(buffer, 0, size);
        }
        fis.close();
        out.flush();
    }
}
