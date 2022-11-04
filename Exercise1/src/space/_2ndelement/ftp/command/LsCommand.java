package space._2ndelement.ftp.command;

import space._2ndelement.ftp.Utils;
import space._2ndelement.ftp.client.FileClient;
import space._2ndelement.ftp.server.Constants;
import space._2ndelement.ftp.server.ServiceHandler;

import java.io.File;
import java.io.IOException;

public class LsCommand extends Command {


    public LsCommand() {
        super("ls");
    }

    /**
     * 服务端执行命令逻辑, 服务端的处理发出的数据格式为：文件类型(文件夹或文件) + 分隔符 + 文件名 + 分隔符 + 文件大小 + 分隔符
     *
     * @param serviceHandler  单用户服务处理器 {@link ServiceHandler}
     * @param commandWithArgs 命令和参数
     */
    public void serverExec(ServiceHandler serviceHandler, String[] commandWithArgs) {
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
     * 客户端执行命令逻辑, 服务端的返回的数据格式为：文件类型(文件夹或文件) + 分隔符 + 文件名 + 分隔符 + 文件大小 + 分隔符
     *
     * @param client 客户端 {@link FileClient}
     * @param args   命令和参数
     */
    @Override
    public void clientExec(FileClient client, String[] args) {
        try {
            String msg = client.readMessage();
            if ("".equals(msg) || msg == null) {
                return;
            }
            if (Constants.ILLEGAL_MARK.equals(msg)) {
                client.error("unknown dir");
            } else {
                String[] filesAndLengths = msg.split(Constants.DELIM);
                for (int i = 0; i < filesAndLengths.length; i += 3) {
                    if ("f".equals(filesAndLengths[i])) {
                        client.info(String.format(String.format(Constants.LS_FILE_STRING.replace("<name>", "%-60s"), filesAndLengths[i + 1])
                                .replace("<length>", "%12s"), Utils.formatVolume(filesAndLengths[i + 2])));
                    } else {
                        client.info(String.format(String.format(Constants.LS_DIR_STRING.replace("<name>", "%-60s"), filesAndLengths[i + 1])
                                .replace("<length>", "%12s"), Utils.formatVolume(filesAndLengths[i + 2])));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
