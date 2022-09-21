package org.sec.ftp.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    public static PrintWriter stdout = new PrintWriter(System.out, true);
    public static PrintWriter stderr = new PrintWriter(System.err, true);
    public static Scanner stdin = new Scanner(System.in);
    private DatagramSocket datagramSocket;
    private CommandHandler commandHandler;
    private DatagramPacket packet;
    private Socket socket;
    private String host;
    private BufferedReader br;
    private BufferedWriter bw;
    private PrintWriter pw;

    public FileClient(String host) {
        this.host = host;
        this.commandHandler = new CommandHandler(this);
        try {
            datagramSocket = new DatagramSocket(Constants.DATA_PORT);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        packet = new DatagramPacket(new byte[Constants.BUFFER_SIZE], Constants.BUFFER_SIZE);
    }

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
     * 初始化流
     */
    public void initStream() throws IOException {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        pw = new PrintWriter(bw, true);
    }

    public void sendMessage(String message) {
        pw.println(message);
    }

    /**
     * 通过udp协议接受文件
     */
    public void receiveFile() throws IOException {
        // 获取文件名
        String filename = br.readLine();
        // 获取文件长度
        int totalSize = Integer.parseInt(br.readLine());
        int tmpSize = totalSize;
        FileOutputStream fos = new FileOutputStream(filename);
        // 收数据并展示进度
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
            stdout.print("\b\b\b\b\b\b");
        }
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

    public String readLine() throws IOException {

        return br.readLine();
    }
}
