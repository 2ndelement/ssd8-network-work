package ssd8.socket.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @author 2ndElement
 * @version v1.0
 * @description
 * @date 2022/10/22 12:15
 */
public class HttpServer {
    /**
     * HTTP端口
     */
    public static final int PORT = 80;
    /**
     * 单核线程池大小
     */
    private static final int POOL_SIZE = 10;
    private static PrintWriter screen = new PrintWriter(System.out, true);
    private static BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
    /**
     * 工作目录记录
     */
    private File workDirectory;
    private ServerSocket serverSocket;
    /**
     * 线程池
     */
    private ExecutorService executor;

    /**
     * @param workDirectory 工作目录
     */
    public HttpServer(File workDirectory) {
        this.workDirectory = workDirectory;
        int poolSize = POOL_SIZE * Runtime.getRuntime().availableProcessors();
        this.executor = new ThreadPoolExecutor(poolSize, poolSize * 2, 1000, TimeUnit.MICROSECONDS, new SynchronousQueue<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            screen.println("Launched successful. Listening to new requests...");
            while (true) {
                Socket socket = serverSocket.accept();
                screen.println("Receive a new request form " + socket.getRemoteSocketAddress());
                executor.execute(new RequestService(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: HttpServer <dir>");
            System.exit(0);
        }
        File directory = new File(args[0]);
        if (!directory.exists() && !directory.isDirectory()) {
            System.err.println(args[0] + "is not a valid dir");
            System.exit(0);
        }
        new HttpServer(directory).run();
    }
}
