package space._2ndelement.ftp.client;

import space._2ndelement.ftp.PublicConstants;

/**
 * 客户端常量
 *
 * @author 2ndelement
 */
public class Constants extends PublicConstants {
    /**
     * 客户端无参数时的提示文本
     */
    public static final String USAGE_STRING = "usage: java FileClient <server_host>";
    /**
     * 客户端的结束命令
     */
    public static final String END_COMMAND = "bye";

    public static final String UNKNOWN_COMMAND_STRING = "unknown command";
    public static final String UI_THEME_1 = "\u001B[32m╭─\u001B[0m \u001B[36m<dir>\u001B[0m\n\u001B[32m╰─\u001B[0m ";
    public static final String UI_THEME_2 = "\u001B[32m➜\u001B[0m \u001B[36m<dir>\u001B[0m ";
    public static final String UI_THEME = UI_THEME_2;
    public static final String WELCOME_STRING = "连接到服务器成功";
}
