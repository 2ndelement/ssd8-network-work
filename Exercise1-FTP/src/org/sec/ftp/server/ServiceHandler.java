package org.sec.ftp.server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * 单用户服务处理线程
 *
 * @author 2ndelement
 */
public class ServiceHandler implements Runnable {
    private DatagramSocket server;
    private DatagramPacket packet;
    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private PrintWriter pw;
    private File currentDir;
    private final File rootDir;
    private final CommandHandler commandHandler;

    public ServiceHandler(Socket socket, String rootDir) throws IOException {
        this.socket = socket;
        this.commandHandler = new CommandHandler(this);
        this.currentDir = new File(rootDir);
        this.rootDir = new File(rootDir);
        this.server = new DatagramSocket();
        this.packet = new DatagramPacket(new byte[Constants.BUFFER_SIZE], Constants.BUFFER_SIZE, socket.getInetAddress(), Constants.DATA_PORT);
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    public void sendMessage(String message) {
        pw.println(message);
    }

    public void initStream() throws IOException {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        pw = new PrintWriter(bw, true);
    }

    /**
     * 将命令传给处理器处理，处理完发送命令处理结束信号
     *
     * @param command 待处理命令
     */
    public void handleCommand(String command) {

        commandHandler.handleCommand(command);
        pw.println(Constants.END_MARK);
        FileServer.stdout.println(socket.getRemoteSocketAddress() + ">收到" + command);
    }

    /**
     * 用户服务线程内容，先向客户端发送成功信息，然后等待客户端命令输入并处理
     */
    @Override
    public void run() {
        String successInfo = socket.getRemoteSocketAddress() + ">连接成功";
        FileServer.stdout.println(successInfo);
        try {
            initStream();
            pw.println(successInfo);
            String command;
            while ((command = br.readLine()) != null) {
                handleCommand(command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    FileServer.stdout.println(socket.getRemoteSocketAddress() + ">已断开");
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    public File getRootDir() {
        return rootDir;
    }

    public void backRoot() {
        this.currentDir = rootDir;
    }

    /**
     * 将文件通过udp发送出去，预先将依次发送 文件名，文件长度
     * 然后再发送文件数据包
     *
     * @param file 待发送的文件
     */
    public void sendFile(File file) {
        try {
            pw.println(file.getName());
            pw.println(file.length());
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[Constants.BUFFER_SIZE];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                packet.setData(buffer, 0, len);
                server.send(packet);
                TimeUnit.MICROSECONDS.sleep(1);
            }

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
