package org.sec.ftp.server.command;

import org.sec.ftp.server.ServiceHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 命令工厂，用于获取命令实例和管理命令注册
 */
public abstract class Command {
    private final String name;
    public static Map<String, Command> commandMap = new HashMap<>();

    // 注册命令，新命令在此注册即可运行
    static {
        register(new CDCommand());
        register(new GETCommand());
        register(new LSCommand());
    }

    /**
     * 执行命令
     *
     * @param serviceHandler  单用户服务处理器
     * @param commandWithArgs 命令和参数
     */
    public abstract void execute(ServiceHandler serviceHandler, String[] commandWithArgs);

    /**
     * 让jvm加载类文件，注册命令
     */
    public static void init() {
        // do nothing
    }

    /**
     * 定义命令执行方式
     *
     * @param name 命令名
     */
    public Command(String name) {
        this.name = name;
    }

    /**
     * 获取命令执行实例
     *
     * @param name 命令名
     */
    public static Command get(String name) {
        return commandMap.get(name);
    }

    public static boolean isLegalCommand(String command) {
        for (String legalCommand : commandMap.keySet()) {
            if (legalCommand.equals(command)) {
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    /**
     * 注册命令
     *
     * @param command 命令实例
     */
    public static void register(Command command) {
        commandMap.put(command.getName(), command);
        System.out.println("register command: " + command.getName());
    }
}
