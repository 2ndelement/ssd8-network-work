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
                    serviceHandler.sendMessage(Constants.ILLEGAL_MARK);
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (files == null) {
            return;
        }
        StringBuilder msg = new StringBuilder();
        for (File file : files) {
            msg.append(file.isFile() ? 'f' : 'd').append(Constants.DELIM).append(file.getName());
            msg.append(Constants.DELIM).append(file.length()).append(Constants.DELIM);
        }
        if (!"".equals(msg.toString())) {
            serviceHandler.sendMessage(msg.substring(0, msg.length() - 1));
        } else {
            serviceHandler.sendMessage(Constants.DELIM);
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
        try {
            String msg = client.readMessage();
            if ("".equals(msg) || msg == null) {
                return;
            }
            if (Constants.ILLEGAL_MARK.equals(msg)) {
                client.error("unknown dir");
            } else {
                String[] fileAndLengths = msg.split(Constants.DELIM);
                for (int i = 0; i < fileAndLengths.length; i += 3) {
                    if ("f".equals(fileAndLengths[i])) {
                        client.info(String.format(String.format(Constants.LS_FILE_STRING.replace("<name>", "%-60s"), fileAndLengths[i + 1])
                                .replace("<length>", "%12s"), fileAndLengths[i + 2]));
                    } else {
                        client.info(String.format(String.format(Constants.LS_DIR_STRING.replace("<name>", "%-60s"), fileAndLengths[i + 1])
                                .replace("<length>", "%12s"), fileAndLengths[i + 2]));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
