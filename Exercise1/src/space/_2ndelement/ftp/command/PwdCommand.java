package space._2ndelement.ftp.command;

import space._2ndelement.ftp.client.FileClient;
import space._2ndelement.ftp.server.ServiceHandler;

/**
 * @author 2ndElement
 * @version v1.0
 * @description
 * @date 2022/10/29 00:20
 */
public class PwdCommand extends Command {
    public PwdCommand() {
        super("pwd");
    }

    @Override
    public void serverExec(ServiceHandler serviceHandler, String[] commandWithArgs) {
        // do nothing
    }

    @Override
    public void clientExec(FileClient client, String[] args) {
        client.info(client.getCurrentDir());
    }
}
