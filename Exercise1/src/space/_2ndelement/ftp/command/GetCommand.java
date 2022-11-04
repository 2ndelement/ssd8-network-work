package space._2ndelement.ftp.command;

import space._2ndelement.ftp.client.FileClient;
import space._2ndelement.ftp.server.Constants;
import space._2ndelement.ftp.server.ServiceHandler;

import java.io.File;
import java.io.IOException;

public class GetCommand extends Command {
    public GetCommand() {
        super("get");
    }

    /**
     * 服务端执行命令逻辑
     *
     * @param serviceHandler  单用户服务处理器 {@link ServiceHandler}
     * @param commandWithArgs 命令和参数
     */
    @Override
    public void serverExec(ServiceHandler serviceHandler, String[] commandWithArgs) {
        // 无参数给出提示
        if (commandWithArgs.length == 1) {
            serviceHandler.sendMessage("usage: get <file>");
            return;
        }

        for (int i = 1; i < commandWithArgs.length; i++) {
            String filename = commandWithArgs[i];
            File file = new File(serviceHandler.getCurrentDir(), filename);
            // 文件存在且是文件
            if (file.exists() && file.isFile()) {
                // 向客户端发送接受信号
                serviceHandler.sendMessage(Constants.CAN_GET_MARK);
                serviceHandler.sendFile(file);

            } else {
                // 文件不存在或者是目录
                serviceHandler.sendMessage("unknown file:" + filename);
            }
        }
        // 通知文件全部传输结束
        serviceHandler.sendMessage(Constants.SEND_FILES_END_MARK);
    }

    /**
     * 客户端执行命令逻辑
     *
     * @param client 客户端 {@link FileClient}
     * @param args   命令和参数
     */
    @Override
    public void clientExec(FileClient client, String[] args) {
        // 如果是GET命令开始进行与服务端协议的操作
        String info;
        try {
            while ((info = client.readMessage()) != null) {
                switch (info) {
                    case Constants.CAN_GET_MARK:
                        client.receiveFile();
                        break;
                    case Constants.SEND_FILES_END_MARK:
                        return;
                    default:
                        client.error(info);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

