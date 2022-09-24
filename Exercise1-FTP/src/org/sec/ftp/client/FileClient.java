package org.sec.ftp.client;

import org.omg.CORBA.TIMEOUT;
import org.sec.ftp.command.Command;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class FileClient {
    private Socket socket;
    private PrintWriter pw;

    private String currentDir;
    private BufferedReader br;
    private final String host;
    private final DatagramPacket packet;
    private DatagramSocket datagramSocket;
    private final CommandHandler commandHandler;

    public static Scanner stdin = new Scanner(System.in);

    public static PrintWriter stdout = new PrintWriter(System.out, true);
    public static PrintWriter stderr = new PrintWriter(System.err, true);


    public FileClient(String host) {
        this.host = host;
        this.currentDir = "~";
        this.commandHandler = new CommandHandler(this);
        this.packet = new DatagramPacket(new byte[Constants.BUFFER_SIZE], Constants.BUFFER_SIZE);
        try {
            datagramSocket = new DatagramSocket(Constants.DATA_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    /**
     * 客户端启动连接服务器,收到服务器成功信息后开始接收命令
     */
    public void run() {
        try {
            socket = new Socket(host, Constants.COMMAND_PORT);
            initStream();

            // 接受第一条确认连接信息
            String confirmMsg = br.readLine();
            if (!Constants.CONNECT_SUC_MARK.equals(confirmMsg)) {
                stderr.println("连接异常,未收到服务端回应");
            } else {
                String command;
                stdout.println(Constants.WELCOME_STRING);
                try {
                    Class.forName(Command.class.getName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                // 循环接受本地终端输入，使用不同的命令处理逻辑进行
                while (true) {
                    try {
                        TimeUnit.MICROSECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    stdout.print(String.format(Constants.UI_THEME.replace("<dir>", currentDir)));
                    stdout.flush();
                    command = stdin.nextLine();
                    commandHandler.handleCommand(command);
                }
            }
        } catch (ConnectException e) {
            stderr.println("无法连接至主机 " + host);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getCurrentDir() {
        return this.currentDir;
    }

    public void setCurrentDir(String msg) {
        this.currentDir = msg;
    }

    public void info(String message) {
        stdout.println(message);
    }

    public void error(String message) {
        stdout.println("\u001B[31m" + message + "\u001B[0m");
    }

    /**
     * 初始化Socket输入流{@link #br}, 输出流{@link  #pw}
     */
    public void initStream() throws IOException {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        pw = new PrintWriter(bw, true);
    }

    /**
     * 为{@link org.sec.ftp.command.Command}提供的获取服务端消息的接口
     *
     * @return 从服务端接受的消息
     */
    public String readMessage() throws IOException {

        return br.readLine();
    }

    /**
     * 为{@link org.sec.ftp.command.Command}提供的消息发送接口
     *
     * @param message 发送的消息
     */
    public void sendMessage(String message) {
        pw.println(message);
    }

    /**
     * 按照和服务端约定的协议通过udp协议接受文件<br>
     * 首先接受文件名，然后接受文件大小，最后接受文件内容
     * {@link org.sec.ftp.server.ServiceHandler#sendFile}
     */
    public void receiveFile() throws IOException {
        // 获取文件名
        String[] filenameAndLength = br.readLine().split(Constants.DELIM);
        String filename = filenameAndLength[0];
        String length = filenameAndLength[1];
        // 获取文件大小
        int totalSize = Integer.parseInt(length);

        FileOutputStream fos = new FileOutputStream(filename);

        // 收数据并渲染进度条
        stdout.print("\u001B[36m" + filename + "\u001b[0m" + " 正在接收中... ");
        stdout.print("00.00%");
        int cnt = totalSize / Constants.BUFFER_SIZE + 1;
        int tmpSize = 0;
        for (int i = 0; i < cnt; i++) {
            datagramSocket.receive(packet);
            int len = packet.getLength();
            tmpSize += len;
            if (len > 0) {
                fos.write(packet.getData(), 0, len);
                stdout.printf("\b\b\b\b\b\b%05.2f%%", (float) (tmpSize) * 100 / totalSize);
                fos.flush();
            }

        }
        stdout.print("\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
        stdout.printf("接收完毕, 共 \u001b[35m%d\u001b[0m bytes\n", totalSize);
        fos.close();
    }

    public static void main(String[] args) {
        String hostname;

        if (args.length == 0) {
            stderr.println(Constants.USAGE_STRING);
            return;
        } else {
            hostname = args[0];
        }

        FileClient client = new FileClient(hostname);
        client.run();
    }


}
