package org.sec.ftp;

/**
 * 服务端客户端公共常量
 * @author 2ndelement
 */
public class PublicConstants {
    public static final int COMMAND_PORT = 2021;
    public static final int DATA_PORT = 2020;
    public static final int BUFFER_SIZE = 1024 * 20;
    public static final String END_MARK = "_ov";
    public static final String GET_COMMAND = "get";
    public static final String CAN_GET_MARK = "_gok";
}
