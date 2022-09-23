package org.sec.ftp.command;

import org.sec.ftp.client.FileClient;
import org.sec.ftp.server.Constants;
import org.sec.ftp.server.ServiceHandler;

import java.io.File;
import java.io.IOException;

public class LSCommand extends Command {


    public LSCommand() {
        super("ls");
    }

    /**
     * 服务端执行命令逻辑
     *
     * @param serviceHandler  单用户服务处理器 {@link ServiceHandler}
     * @param commandWithArgs 命令和参数
     */
    public void execute(ServiceHandler serviceHandler, String[] commandWithArgs) {
        File[] files;
        if (commandWithArgs.length == 1) {
            files = serviceHandler.getCurrentDir().listFiles();
        } else {
            File tmpFile = new File(serviceHandler.getCurrentDir(), commandWithArgs[1]);
            try {
                if (tmpFile.exists() && tmpFile.isDirectory() && tmpFile.getCanonicalPath().startsWith(serviceHandler.getRootDir().getCanonicalPath())) {
                    files = tmpFile.listFiles();
                } else {
                    serviceHandler.sendMessage("unknown dir");
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                serviceHandler.sendMessage(Constants.LS_FILE_STRING.replace("<name>", "%-60s".formatted(file.getName())).replace("<length>", "%12d".formatted(file.length())));
            } else if (file.isDirectory()) {
                serviceHandler.sendMessage(Constants.LS_DIR_STRING.replace("<name>", "%-60s".formatted(file.getName())).replace("<length>", "%12d".formatted(file.length())));
            }
        }
    }

    /**
     * 客户端执行命令逻辑
     *
     * @param client 客户端 {@link FileClient}
     * @param args   命令和参数
     */
    @Override
    public void handle(FileClient client, String[] args) {
    }
}
