package space._2ndelement.ftp.command;

import space._2ndelement.ftp.client.FileClient;
import space._2ndelement.ftp.server.ServiceHandler;

/**
 * @author 2ndElement
 * @version v1.0
 * @description 命令接口
 * @date 2022/10/22 00:02
 */
public interface CommandInterface {
    /**
     * 服务端执行本命令的逻辑方法
     *
     * @param serviceHandler  单用户服务处理器 {@link ServiceHandler}
     * @param commandWithArgs 命令和参数
     */
    void serverExec(ServiceHandler serviceHandler, String[] commandWithArgs);

    /**
     * 客户端执行本命令的逻辑方法
     *
     * @param client 客户端 {@link FileClient}
     * @param args   命令和参数
     */
    void clientExec(FileClient client, String[] args);
}
