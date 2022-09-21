package org.sec.ftp.command;

import org.sec.ftp.client.FileClient;
import org.sec.ftp.server.Constants;
import org.sec.ftp.server.ServiceHandler;

import java.io.File;
import java.io.IOException;

public class GETCommand extends Command {
    public GETCommand() {
        super("get");
    }

    @Override
    public void execute(ServiceHandler serviceHandler, String[] commandWithArgs) {
        // 无参数给出提示
        if (commandWithArgs.length == 1) {
            serviceHandler.sendMessage("usage: get <file>");
            return;
        }
        // 有参数解析文件合法性发出文件或者提示
        File file = new File(serviceHandler.getCurrentDir(), commandWithArgs[1].replace('_', ' ').trim());
        // 文件存在且是文件
        if (file.exists() && file.isFile()) {
            // 向客户端发送接受信号
            serviceHandler.sendMessage(Constants.CAN_GET_MARK);
            serviceHandler.sendFile(file);
            serviceHandler.sendMessage("%s get ok, total %d b".formatted(file.getName(), file.length()));
        } else {
            // 文件不存在或者是目录
            serviceHandler.sendMessage("unknown file");
        }
    }

    @Override
    public void handle(FileClient client, String[] args) {
        // 如果是GET命令开始进行与服务端协议的操作
        String info;
        try {
            info = client.readLine();
            if (Constants.CAN_GET_MARK.equals(info)) {
                // 能发送，进入接受操作
                client.receiveFile();
            } else {
                client.error(info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

