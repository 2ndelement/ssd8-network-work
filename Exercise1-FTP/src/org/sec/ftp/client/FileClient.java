package org.sec.ftp.client;

import org.sec.ftp.command.Command;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    private Socket socket;
    private PrintWriter pw;
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
            if (confirmMsg == null || !confirmMsg.endsWith("连接成功")) {
                stderr.println("连接异常,未收到服务端回应");
            } else {
                String command;
                stdout.println(confirmMsg);
                // 循环接受本地终端输入，使用不同的命令处理逻辑进行
                while (true) {
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

    public void info(String message) {
        stdout.println(message);
    }

    public void error(String message) {
        stderr.println(message);
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
     */
    public void receiveFile() throws IOException {
        // 获取文件名
        String filename = br.readLine();

        // 获取文件大小
        int totalSize = Integer.parseInt(br.readLine());


        int tmpSize = totalSize;
        FileOutputStream fos = new FileOutputStream(filename);

        // 收数据并渲染进度条
        stdout.println("\u001b[36m" + filename + " 正在接收中..." + "\u001b[0m");
        stdout.print("00.00%");
        while (tmpSize > 0) {
            datagramSocket.receive(packet);
            int len = packet.getLength();
            if (len > 0) {
                fos.write(packet.getData(), 0, len);
                stdout.printf("\b\b\b\b\b\b%.2f%%", (float) (totalSize - tmpSize) * 100 / totalSize);
                fos.flush();
                tmpSize -= len;
            }
            stdout.print("\b\b\b\b\b\b\b\b\b");
        }
        info("\u001b[36m%s\u001b[0m 接受完毕, 共 \u001b[35m%d\u001b[0m bytes".formatted(filename, totalSize));
        fos.close();
    }

    public static void main(String[] args) {
        try {
            Class.forName(Command.class.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
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
