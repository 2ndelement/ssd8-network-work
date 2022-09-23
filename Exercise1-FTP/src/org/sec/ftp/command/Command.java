package org.sec.ftp.command;

import org.sec.ftp.client.FileClient;
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
        register(
                new CDCommand(),
                new LSCommand(),
                new GETCommand()
        );

    }

    /**
     * 定义命令执行方式
     *
     * @param name 命令名
     */
    public Command(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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

    /**
     * 注册命令
     *
     * @param aliases 命令实例
     */
    public static void register(Command... aliases) {
        StringBuilder info = new StringBuilder();
        for (Command alias : aliases) {
            commandMap.put(alias.getName(), alias);
            info.append('<').append(alias.getName()).append("> ");
        }
        System.out.println("加载命令: \033[36m" + info + "\033[0m");
    }

    /**
     * 服务端执行命令逻辑
     *
     * @param serviceHandler  单用户服务处理器 {@link ServiceHandler}
     * @param commandWithArgs 命令和参数
     */
    public abstract void execute(ServiceHandler serviceHandler, String[] commandWithArgs);

    /**
     * 客户端执行命令逻辑
     *
     * @param client 客户端 {@link FileClient}
     * @param args   命令和参数
     */
    public abstract void handle(FileClient client, String[] args);
}
