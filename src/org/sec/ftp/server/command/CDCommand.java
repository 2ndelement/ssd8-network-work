package org.sec.ftp.server.command;

import org.sec.ftp.server.ServiceHandler;

import java.io.File;
import java.io.IOException;

public class CDCommand extends Command {

    public CDCommand() {
        super("cd");
    }

    @Override
    public void execute(ServiceHandler serviceHandler, String[] commandWithArgs) {
        // 无参数回到根目录
        if (commandWithArgs.length == 1) {
            serviceHandler.backRoot();
            serviceHandler.sendMessage("~ > ok");
        } else {
            // 有参数解析文件夹路径判断是否合法并跳转到文件夹
            File intoDir;
            String arg = commandWithArgs[1];
            arg = arg.replace("\\", "/");
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
                        serviceHandler.sendMessage(intoDir.getAbsolutePath().replace(serviceHandler.getRootDir().getCanonicalPath(), "~") + " > ok");
                    } else {
                        serviceHandler.backRoot();
                        serviceHandler.sendMessage("~ > ok");
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                serviceHandler.sendMessage("unknown dir");
            }
        }
    }
}
