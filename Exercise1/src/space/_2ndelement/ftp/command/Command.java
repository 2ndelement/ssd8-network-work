package space._2ndelement.ftp.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 命令工厂，用于获取命令实例和管理命令注册, 新建命令类继承实现该命令抽象基类并在此注册即可运行
 */
public abstract class Command implements CommandInterface {

    private final String name;
    private final String[] aliases;
    public static Map<String, Command> commandMap = new HashMap<>();

    // 注册命令，新命令在此注册即可运行
    static {
        register(
                new CdCommand(),
                new LsCommand(),
                new GetCommand(),
                new PwdCommand()
        );

    }


    /**
     * 定义命令执行方式
     *
     * @param name    命令名
     * @param aliases 命令的别名
     */
    public Command(String name, String... aliases) {
        this.name = name;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    /**
     * @param originalCommand 原始命令字符串
     * @return 处理完后的 [命令, 参数...] 字符串数组
     */
    public static String[] parseCommandString(String originalCommand) {
        // 正则解析引号包括的带空格的完整字符串为一条参数 如 get "1 2" 3 将被解析为 ["get", "1 2", "3"]
        String[] midProductArgs = originalCommand.split("\\s(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        ArrayList<String> commandWithArgsList = new ArrayList<>(midProductArgs.length);
        for (String midProductArg : midProductArgs) {
            if (!"".equals(midProductArg) && midProductArg != null) {
                commandWithArgsList.add(midProductArg.replaceAll("\"", "").trim());
            }
        }
        return commandWithArgsList.toArray(new String[0]);
    }


    /**
     * 通过命令名获取命令执行实例
     *
     * @param name 命令名
     */
    public static Command getCommand(String name) {
        return commandMap.get(name);
    }

    /**
     * 判断某条命令名是否合法, 即是否为注册过的命令名或别名
     *
     * @param command 命令名
     * @return 断某条命令名是否合法
     */
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
     * @param commands 命令实例
     */
    public static void register(Command... commands) {
        StringBuilder info = new StringBuilder();
        for (Command command : commands) {
            commandMap.put(command.getName(), command);
            info.append('<').append(command.getName()).append("> ");
            for (String alias : command.aliases) {
                commandMap.put(alias, command);
            }
        }
        System.out.println("加载命令: [36m" + info + "[0m");
    }
}
