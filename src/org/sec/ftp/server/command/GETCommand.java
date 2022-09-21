package org.sec.ftp.server.command;

import org.sec.ftp.server.Constants;
import org.sec.ftp.server.ServiceHandler;

import java.io.File;

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
        File file = new File(serviceHandler.getCurrentDir(), commandWithArgs[1]);
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
}
