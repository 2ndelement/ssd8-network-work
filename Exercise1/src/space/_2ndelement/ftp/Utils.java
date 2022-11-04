package space._2ndelement.ftp;

/**
 * @author 2ndElement
 * @version v1.0
 * @description
 * @date 2022/10/28 23:23
 */
public class Utils {
    // 对齐字符串
    public static String formatVolume(long volume) {
        if (volume < 1024) {
            return volume + "  B";
        } else if (volume < 1024 * 1024) {
            return volume / 1024 + " KB";
        } else if (volume < 1024 * 1024 * 1024) {
            return volume / 1024 / 1024 + " MB";
        } else {
            return volume / 1024 / 1024 / 1024 + " GB";
        }
    }

    public static String formatVolume(String volume) {
        return formatVolume(Long.parseLong(volume));
    }
}
