package space._2ndelement.ftp.client;

import space._2ndelement.ftp.command.Command;

import java.io.IOException;

/**
 * 客户端命令处理器
 */
public class CommandHandler {
    private final FileClient client;

    public CommandHandler(FileClient client) {
        this.client = client;
    }

    /**
     * 通过命令名从命令管理类中找到相应的处理器处理<br>
     * 会首先判断是否结束命令{@link Constants#END_COMMAND},如果是则结束客户端<br>
     * 然后向客户端发送该命令, 接着判断是否合法命令,如果是则执行客户端相应命令操作<br>
     * 最后结束服务端发来的消息,直到收到结束标志{@link Constants#END_MARK}
     *
     * @param originalCommand 处理一个命令
     */
    public void handleCommand(String originalCommand) throws IOException {
        if ("".equals(originalCommand.trim())) {
            return;
        }
        String[] commandWithArgs = Command.parseCommandString(originalCommand);
        if (commandWithArgs.length == 0) {
            return;
        }
        String command = commandWithArgs[0];
        // 如果是结束命令将关闭客户端，服务端会自动回收线程
        if (Constants.END_COMMAND.equals(command)) {
            System.exit(0);
        }

        //发送命令

        if (Command.isLegalCommand(command)) {
            client.sendMessage(originalCommand);
            Command.getCommand(command).clientExec(client, commandWithArgs);
        } else {
            client.error(Constants.UNKNOWN_COMMAND_STRING);
            return;
        }
        // 等待服务端发送结束信号，输出信号前的所有消息
        String message;
        while (!Constants.END_MARK.equals(message = client.readMessage())) {
            if (!"".equals(message) && message != null) {
                client.info(message);
            }
        }

    }
}
