package space._2ndelement.ftp.server;

import space._2ndelement.ftp.command.Command;


public class CommandHandler {
    private final ServiceHandler serviceHandler;

    public CommandHandler(ServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;

    }

    /**
     * 通过命令名从命令管理类中找到相应的处理器处理,并检验命令是否合法
     *
     * @param originalCommand 带参数待处理命令
     */
    public void handleCommand(String originalCommand) {
        String[] commandWithArgs = Command.parseCommandString(originalCommand);
        if (commandWithArgs.length == 0) {
            return;
        }
        if (Command.isLegalCommand(commandWithArgs[0])) {
            Command.getCommand(commandWithArgs[0]).serverExec(serviceHandler, commandWithArgs);
        }
    }
}

