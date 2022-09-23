package org.sec.ftp;

/**
 * 服务端客户端公共常量
 *
 * @author 2ndelement
 */
public class PublicConstants {
    /**
     * 服务端控制命令端口号
     */
    public static final int COMMAND_PORT = 2021;
    /**
     * UDP传输文件端口号
     */
    public static final int DATA_PORT = 2020;
    /**
     * udp单个数据包大小
     */
    public static final int BUFFER_SIZE = 1024 * 20;
    /**
     * 命令结束标志
     */
    public static final String END_MARK = "_ov";
    /**
     * GET时能开始传输文件的标志
     */
    public static final String CAN_GET_MARK = "_gok";
    /**
     * 文件全部传输完毕标志
     */
    public static final String SEND_FILES_END_MARK = "_sok";
}
