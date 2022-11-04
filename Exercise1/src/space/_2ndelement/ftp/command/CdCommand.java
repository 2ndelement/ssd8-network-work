package space._2ndelement.ftp.command;

import space._2ndelement.ftp.client.FileClient;
import space._2ndelement.ftp.server.Constants;
import space._2ndelement.ftp.server.ServiceHandler;

import java.io.File;
import java.io.IOException;

public class CdCommand extends Command {

    public CdCommand() {
        super("cd", "cd..");
    }

    /**
     * 服务端执行命令逻辑
     *
     * @param serviceHandler  单用户服务处理器 {@link ServiceHandler}
     * @param commandWithArgs 命令和参数
     */
    @Override
    public void serverExec(ServiceHandler serviceHandler, String[] commandWithArgs) {
        // 无参数回到根目录
        if (commandWithArgs.length == 1) {
            // 特殊处理语法糖 cd.. 为返回上级
            if ("cd..".equals(commandWithArgs[0])) {
                String[] backCommand = new String[]{"cd", ".."};
                serverExec(serviceHandler, backCommand);
                return;
            }
            serviceHandler.backRoot();
            serviceHandler.sendMessage("~");
        } else {
            // 有参数解析文件夹路径判断是否合法并跳转到文件夹
            File intoDir;
            String arg = commandWithArgs[1];
            arg = arg.replace("\\", "/").replace("~", "/");
            // 是否相对根目录跳转
            if (arg.startsWith("/")) {
                intoDir = new File(serviceHandler.getRootDir(), arg);
            } else {
                intoDir = new File(serviceHandler.getCurrentDir(), arg);
            }
            // 判断文件是否存在且是目录
            if (intoDir.exists() && intoDir.isDirectory()) {
                try {
                    intoDir = intoDir.getCanonicalFile();
                    // 判断跳转目录是否在根目录下，不在则返回根目录
                    if (intoDir.getAbsolutePath().startsWith(serviceHandler.getRootDir().getCanonicalPath())) {
                        serviceHandler.setCurrentDir(intoDir);
                        String msg = intoDir.getAbsolutePath().replace(serviceHandler.getRootDir().getCanonicalPath(), "~");
                        if (msg.endsWith("\\") || msg.endsWith("/")) {
                            msg = msg.substring(0, msg.length() - 1);
                        }
                        serviceHandler.sendMessage(msg);
                    } else {
                        serviceHandler.backRoot();
                        serviceHandler.sendMessage("~");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                serviceHandler.sendMessage(Constants.ILLEGAL_MARK);
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
    public void clientExec(FileClient client, String[] args) {
        try {
            String msg = client.readMessage();
            if (Constants.ILLEGAL_MARK.equals(msg)) {
                client.error("unknown dir");
            } else {
                if (!client.getCurrentDir().equals(msg)) {
                    client.setCurrentDir(msg);
                    client.info("[36m" + msg + "[0m > ok");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
