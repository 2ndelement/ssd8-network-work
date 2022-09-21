package org.sec.ftp.server;

import org.sec.ftp.PublicConstants;
/**
 * 服务端常量
 * @author 2ndelement
 */
public class Constants extends PublicConstants {
    public static final String USAGE_STRING = "usage: java FileServer <dir>";
    public static final String ILLEGAL_DIR_STRING = "illegal directory";
    public static final int POOL_SIZE = 10;
    public static final String LS_FILE_STRING = "<file>\t<length>\t<name>";
    public static final String LS_DIR_STRING = "<dir> \t<length>\t\033[36m<name>\033[0m";



}
