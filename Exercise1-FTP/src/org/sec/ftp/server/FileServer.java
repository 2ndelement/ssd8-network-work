package org.sec.ftp.server;

import org.sec.ftp.server.command.Command;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileServer {
    /** 标准输出 */
    public static PrintWriter stdout = new PrintWriter(System.out, true);
    /** 错误输出 */
    public static PrintWriter stderr = new PrintWriter(System.err, true);
    /** 根目录 */
    private final String rootDir;
    /** 服务套接字 */
    private final ServerSocket serverSocket;
    /** 线程池 */
    private final ExecutorService executor;

    public FileServer(String rootDir) throws IOException {
        Command.init();
        this.rootDir = rootDir;
        serverSocket = new ServerSocket(Constants.COMMAND_PORT);
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * Constants.POOL_SIZE);
    }

    /**
     * 启动服务，监听客户端连接并分发到线程池
     */
    public void service() {
        stdout.println("服务器在 " + serverSocket.getLocalSocketAddress() + " 启动,开始监听客户端连接");
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                executor.execute(new ServiceHandler(socket, rootDir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 处理主程序输入参数，并验证合法
     */
    public static void main(String[] args) {
        String workDir = null;
        if (args.length == 0) {
            stderr.println(Constants.USAGE_STRING);
            return;
        } else {
            File inputDir = new File(args[0]);
            if (inputDir.exists() && inputDir.isDirectory()) {
                workDir = args[0];
            } else {
                stderr.println(Constants.ILLEGAL_DIR_STRING);
            }
        }
        try {
            FileServer server = new FileServer(workDir);
            server.service();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
