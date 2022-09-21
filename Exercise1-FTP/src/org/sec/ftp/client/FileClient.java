package org.sec.ftp.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileClient {
    public static PrintWriter stdout = new PrintWriter(System.out, true);
    public static PrintWriter stderr = new PrintWriter(System.err, true);
    public static Scanner stdin = new Scanner(System.in);
    private DatagramSocket datagramSocket;
    private DatagramPacket packet;
    private Socket socket;
    private String host;
    private BufferedReader br;
    private BufferedWriter bw;
    private PrintWriter pw;


    /**
     * 初始化流
     */
    public void initStream() throws IOException {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        pw = new PrintWriter(bw, true);
    }

    public FileClient(String host) {
        this.host = host;
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
                stdout.println(confirmMsg);
                String commandAndArgs;
                String command;
                String message;
                // 循环接受本地终端输入，使用不同的命令处理逻辑进行
                while (true) {
                    commandAndArgs = stdin.nextLine();
                    command = commandAndArgs.split(" ")[0];
                    if ("".equals(command)) {
                        continue;
                    }
                    // 如果是结束命令将退出循环关闭客户端，服务端会自动回收线程
                    if (Constants.END_COMMAND.equals(command)) {
                        break;
                    }
                    //发送命令
                    pw.println(commandAndArgs);

                    // 如果是GET命令开始进行与服务端协议的操作
                    if (Constants.GET_COMMAND.equals(command)) {
                        // 先读取是否能正常发送的信号并验证
                        String info = br.readLine();
                        if (Constants.CAN_GET_MARK.equals(info)) {
                            // 能发送，进入接受操作
                            receiveFile();
                        } else {
                            stderr.println(info);
                        }
                    }
                    // 等待服务端发送结束信号，输出信号前的所有消息
                    while (!Constants.END_MARK.equals(message = br.readLine())) {
                        stdout.println(message);
                    }

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

    /**
     * 通过udp协议接受文件
     */
    private void receiveFile() throws IOException {
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
}
