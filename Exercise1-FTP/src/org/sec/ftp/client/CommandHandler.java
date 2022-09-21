package org.sec.ftp.client;

import org.sec.ftp.command.Command;

import java.io.IOException;

public class CommandHandler {
    private final FileClient client;

    public CommandHandler(FileClient client) {
        this.client = client;
    }

    /**
     * 通过命令名从命令管理类中找到相应的处理器处理
     *
     * @param originalCommand 处理一个命令
     */
    public void handleCommand(String originalCommand) throws IOException {
        String command = originalCommand.split(" ")[0];
        // 如果是结束命令将关闭客户端，服务端会自动回收线程
        if (Constants.END_COMMAND.equals(command)) {
            System.exit(0);
        }

        //发送命令
        client.sendMessage(originalCommand);
        if (Command.isLegalCommand(command)) {
            Command.get(command).handle(client, originalCommand.split(" "));
        }
        String message;
        // 等待服务端发送结束信号，输出信号前的所有消息
        while (!Constants.END_MARK.equals(message = client.readLine())) {
            client.info(message);
        }

    }
}
