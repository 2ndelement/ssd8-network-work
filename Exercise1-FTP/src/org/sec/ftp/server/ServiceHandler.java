package org.sec.ftp.server;

import org.sec.ftp.client.FileClient;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

/**
 * 单用户服务处理线程
 *
 * @author 2ndelement
 */
public class ServiceHandler implements Runnable {

    private PrintWriter pw;
    /**
     * 当前客户所在目录
     */
    private File currentDir;
    private BufferedReader br;
    private final File rootDir;
    private final Socket socket;
    private final DatagramSocket server;
    private final DatagramPacket packet;
    private final CommandHandler commandHandler;

    public ServiceHandler(Socket socket, String rootDir) throws IOException {
        this.socket = socket;
        this.rootDir = new File(rootDir);
        this.server = new DatagramSocket();
        this.currentDir = new File(rootDir);
        this.commandHandler = new CommandHandler(this);
        this.packet = new DatagramPacket(
                new byte[Constants.BUFFER_SIZE], Constants.BUFFER_SIZE,
                socket.getInetAddress(), Constants.DATA_PORT
        );
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

        } finally {
            try {
                FileServer.stdout.println(socket.getRemoteSocketAddress() + ">已断开");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 获取当前目录
     *
     * @return 当前客户所在目录
     */
    public File getCurrentDir() {
        return currentDir;
    }

    /**
     * 设置当前目录
     *
     * @param currentDir 当前客户所在目录
     */
    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    /**
     * 为{@link ServiceHandler}提供的向客户端发送数据的接口
     *
     * @param message 发送的消息
     */
    public void sendMessage(String message) {
        pw.println(message);
    }

    /**
     * 初始化输入流{@link  #br},输出流{@link #pw}
     */
    public void initStream() throws IOException {
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        pw = new PrintWriter(bw, true);
    }

    /**
     * 将命令传给处理器处理，处理完发送命令处理结束信号{@link Constants#END_MARK}
     *
     * @param command 待处理命令
     */
    public void handleCommand(String command) {
        FileServer.stdout.println(socket.getRemoteSocketAddress() + ">收到命令\033[36m" + command + "\033[0m");
        commandHandler.handleCommand(command);
        pw.println(Constants.END_MARK);

    }


    /**
     * 获取根目录
     *
     * @return 根目录
     */
    public File getRootDir() {
        return rootDir;
    }

    /**
     * 将当前目录返回到根目录
     */
    public void backRoot() {
        this.currentDir = rootDir;
    }

    /**
     * 将文件通过udp发送出去，预先将依次发送 文件名，文件长度
     * 然后再发送文件数据包
     * {@link FileClient#receiveFile()}
     *
     * @param file 待发送的文件
     */
    public void sendFile(File file) {
        try {
            pw.println(file.getName() + Constants.DELIM + file.length());
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
