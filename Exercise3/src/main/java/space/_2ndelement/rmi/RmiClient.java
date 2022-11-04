package space._2ndelement.rmi;

import java.rmi.Naming;
import java.util.Scanner;

/**
 * @author 2ndElement
 * @version v1.0
 * @description
 * @date 2022/11/4 20:10
 */
public class RmiClient {
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java RmiClient [options] [args...]");
            System.err.println("Type java RmiClient -help or -h for more information");
            return;
        }
        // 判断参数
        switch (args[0]) {
            case "-help":
            case "-h":
                help();
                break;
            case "-register":
            case "-r":
                register(args);
                break;
            case "-login":
            case "-l":
                interact(args);
                break;
            case "-interact":
            case "-i":
                interact(null);
                break;
            case "-add":
            case "-a":
                addMeeting(args);
                break;
            case "-query":
            case "-q":
                queryMeeting(args);
                break;
            case "-delete":
            case "-d":
                deleteMeeting(args);
                break;
            case "-clear":
            case "-c":
                clearMeeting(args);
                break;
            default:
        }
    }

    /**
     * 命令行模式清除会议
     *
     * @param args 参数 [host] [port] [username] [password]
     */
    private static void clearMeeting(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: java RmiClient -clear [host] [port] [username] [password]");
            return;
        }
        try {
            MeetingService meetingService = (MeetingService) Naming.lookup("rmi://" + args[1] + ":" + args[2] + "/meetingService");
            if (meetingService.clearMeeting(args[3], args[4])) {
                System.out.println("Clear meeting successfully.");
            } else {
                System.out.println("Clear meeting failed.");
            }
        } catch (Exception e) {
            System.err.println("Connection failed. Check if the server is running.");
        }
    }

    /**
     * 命令行模式删除会议
     *
     * @param args 参数 [host] [port] [username] [password] [meetingId]
     */
    private static void deleteMeeting(String[] args) {
        if (args.length != 6) {
            System.err.println("Usage: java RmiClient -delete [host] [port] [username] [password] [meetingId]");
            return;
        }
        try {
            MeetingService meetingService = (MeetingService) Naming.lookup("rmi://" + args[1] + ":" + args[2] + "/meetingService");
            boolean result = meetingService.deleteMeeting(args[3], args[4], Integer.parseInt(args[5]));
            System.out.println(result ? "Delete meeting successfully." : "Delete meeting failed.");
        } catch (NumberFormatException e) {
            System.err.println("Invalid meetingId.");
        } catch (Exception e) {
            System.err.println("Connection failed. Check if the server is running.");
        }
    }

    /**
     * 命令行查询会议
     *
     * @param args 参数 [host] [port] [username] [password] [start] [end]
     */
    private static void queryMeeting(String[] args) {
        if (args.length != 7) {
            System.err.println("Usage: java RmiClient -query [host] [port] [username] [password] [start] [end]");
            return;
        }
        try {
            MeetingService meetingService = (MeetingService) Naming.lookup("rmi://" + args[1] + ":" + args[2] + "/meetingService");
            String result = meetingService.queryMeeting(args[3], args[4], args[5], args[6]);
            System.out.println(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 命令行模式添加会议
     *
     * @param args host] [port] [username] [password] [otherUserNames] [start] [end]
     */
    private static void addMeeting(String[] args) {
        if (args.length != 9) {
            System.err.println("Usage: java RmiClient -add [host] [port] [username] [password] [otherUserNames] [start] [end] [title]");
            return;
        }
        try {
            MeetingService meetingService = (MeetingService) Naming.lookup("rmi://" + args[1] + ":" + args[2] + "/meetingService");
            String[] otherUserNames = args[5].split(",");
            String result = meetingService.addMeeting(args[3], args[4], otherUserNames, args[6], args[7], args[8]);
            System.out.println(result);
        } catch (Exception e) {
            System.err.println("Connection failed. Check if the server is running.");
        }
    }

    /**
     * 进入交互模式, 若args不为null则需要参数有效进行登录, 为null则直接进入交互模式不登录
     *
     * @param args [host] [port] [username] [password]
     */
    private static void interact(String[] args) {
        try {
            String info;
            // username和password为空时需要登录, 否则不能执行其他操作
            String username = null;
            String password = null;
            MeetingService meetingService = null;
            // args 为 null 不为空登录, 为空需要执行login命令登录, 若未执行login命令则无法进行其他操作
            if (args != null) {
                if (args.length != 5) {
                    System.err.println("Usage: java RmiClient -login [host] [port] [username] [password]");
                    return;
                }
                meetingService = (MeetingService) Naming.lookup("rmi://" + args[1] + ":" + args[2] + "/meetingService");
                if (meetingService.verifyUser(args[3], args[4])) {
                    username = args[3];
                    password = args[4];
                    System.out.println("Login as " + username + " successfully.");
                } else {
                    System.out.println("Login failed. Check your username and password.");
                    return;
                }
            }
            System.out.println("Now you are in Interactive Mode");
            System.out.println("Type help or h for more information");
            System.out.println("Type exit or e to exit");
            while (true) {
                // 交互模式 要求输入命令
                System.out.print("➜ ");
                System.out.flush();
                String command = scanner.nextLine();
                String[] commandArgs = command.split(" ");

                if (commandArgs.length == 0 || commandArgs[0].isEmpty()) {
                    continue;
                }
                // 交换模式命令选择
                switch (commandArgs[0]) {
                    case "help":
                    case "h":
                        interactiveHelp();
                        break;
                    case "exit":
                    case "e":
                        return;
                    case "add":
                    case "a":
                        if (username == null || password == null) {
                            System.out.println("Please login first.");
                            break;
                        }
                        if (commandArgs.length != 5) {
                            System.err.println("Usage: add [participants] [title] [start_time] [end_time]\n\tparticipants: username1,username2,username3...,time format: yyyy-MM-dd-HH:mm");
                            break;
                        }
                        String[] otherUserNames = commandArgs[1].split(",");
                        assert meetingService != null;
                        info = meetingService.addMeeting(username, password, otherUserNames, commandArgs[2], commandArgs[3], commandArgs[4]);
                        System.out.println(info);
                        break;
                    case "query":
                    case "q":
                        if (username == null || password == null) {
                            System.out.println("Please login first.");
                            break;
                        }
                        if (commandArgs.length != 3) {
                            System.err.println("Usage: query [start] [end]\n\ttime format: yyyy-MM-dd-HH:mm");
                            break;
                        }
                        assert meetingService != null;
                        info = meetingService.queryMeeting(username, password, commandArgs[1], commandArgs[2]);
                        System.out.println(info);
                        break;
                    case "delete":
                    case "d":
                        if (username == null || password == null) {
                            System.out.println("Please login first.");
                            break;
                        }
                        if (commandArgs.length != 2) {
                            System.err.println("Usage: delete [meeting_id]");
                            break;
                        }
                        int meetingId;
                        try {
                            meetingId = Integer.parseInt(commandArgs[1]);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid meeting id.");
                            break;
                        }
                        if (meetingService.deleteMeeting(username, password, meetingId)) {
                            System.out.println("Delete successfully.");
                        } else {
                            System.out.println("Delete failed.");
                        }
                        break;
                    case "clear":
                    case "c":
                        if (username == null || password == null) {
                            System.out.println("Please login first.");
                            break;
                        }
                        if (meetingService.clearMeeting(username, password)) {
                            System.out.println("Clear successfully.");
                        } else {
                            System.out.println("Clear failed.");
                        }
                        break;
                    case "login":
                    case "l":
                        if (meetingService == null) {
                            System.out.println("Please switch to a server first.");
                            break;
                        }
                        if (commandArgs.length != 3) {
                            System.out.println("Usage: login [username] [password]");
                            break;
                        }
                        if (meetingService.verifyUser(commandArgs[1], commandArgs[2])) {
                            username = commandArgs[1];
                            password = commandArgs[2];
                            System.out.println("Switch Login to " + commandArgs[1] + " successfully.");
                        } else {
                            System.out.println("Login failed. Check your username and password.");
                        }
                        break;
                    case "register":
                    case "r":
                        if (meetingService == null) {
                            System.out.println("Please switch to a server first.");
                            break;
                        }
                        if (commandArgs.length != 3) {
                            System.out.println("Usage: register [username] [password]");
                            break;
                        }
                        if (meetingService.registerUser(commandArgs[1], commandArgs[2])) {
                            System.out.println("Register successfully.");
                        } else {
                            System.out.println("Register failed. May be the username has been registered.");
                        }
                        break;
                    case "switch":
                    case "s":
                        if (commandArgs.length != 3) {
                            System.out.println("Usage: switch [host] [port]");
                            break;
                        }
                        try {
                            meetingService = (MeetingService) Naming.lookup("rmi://" + commandArgs[1] + ":" + commandArgs[2] + "/meetingService");
                            System.out.println("Switch to " + commandArgs[1] + ":" + commandArgs[2] + " successfully.");
                            if (username == null || password == null) {
                                System.out.println("Now you need to login again.");
                            } else if (meetingService.verifyUser(username, password)) {
                                System.out.println("Auto Login successfully.");
                            } else {
                                username = null;
                                password = null;
                                System.out.println("Current User auto Login failed. Because of the switch, you need to login again.");
                            }
                        } catch (Exception e) {
                            System.out.println("Switch failed. Check your host and port.");
                        }
                        break;
                    default:
                        System.out.print("Unknown command : " + commandArgs[0]);
                }
            }
        } catch (Exception e) {
            System.err.println("Connect failed. Check if the server is running.");
        }
    }

    /**
     * 显示交互模式的帮助
     */
    private static void interactiveHelp() {
        System.out.println("Usage: [operations] [args...]");
        System.out.println("Operations:");
        System.out.println("\thelp h\n\t\tshow this help message, host and port are not required");
        System.out.println("\tadd a [participants] [start_time] [end_time] [title]\n" + "\t\tadd a new meeting, time format: yyyy-MM-dd-HH:mm likes 2022-11-04 20:00, " + "participants format: username1,username2,username3");
        System.out.println("\tquery q [start_time] [end_time]\n" + "\t\tquery meetings in a time range, time format: yyyy-MM-dd-HH:mm likes 2022-11-04 20:00");
        System.out.println("\tdelete d [meeting_id]\n\t\tdelete a meeting by id");
        System.out.println("\tclear c \n\t\tclear all meetings of current user");
        System.out.println("\tlogin l [username] [password] \n\t\tlogin as another user");
        System.out.println("\tregister r [username] [password] \n\t\tregister a new user");
        System.out.println("\tswitch s [host] [port] \n\t\tswitch to another server");
        System.out.println("\texit e \n\t\texit interactive mode");
    }

    /**
     * 通过命令行参数注册用户
     *
     * @param args 命令行参数
     */
    private static void register(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: java RmiClient -register [host] [port] [username] [password]");
            return;
        }
        try {
            MeetingService meetingService = (MeetingService) Naming.lookup("rmi://" + args[1] + ":" + args[2] + "/meetingService");
            if (meetingService.registerUser(args[3], args[4])) {
                System.out.println("Register successfully.");
            } else {
                System.out.println("Register failed.");
            }
        } catch (Exception e) {
            System.err.println("Connection failed. Check if the server is running.");
        }
    }

    /**
     * 显示帮助文本
     */
    private static void help() {
        System.out.println("Usage: java RmiClient [options] [args...]");
        System.out.println("Options:");
        System.out.println("\t-h -help\n\t\tshow this help message, host and port are not required");
        System.out.println("\t-i -interact \n\t\tenter interactive mode, default mode if no operation is specified, host and port are not required");
        System.out.println("\t-r -register [host] [port] [username] [password]\n\t\tregister a new user");
        System.out.println("\t-l -login [host] [port] [username] [password]\n\t\tlogin a user and enter interactive mode");
        System.out.println("\t-a -add [host] [port] [username] [password] [participants] [start_time] [end_time] [title]\n" + "\t\tadd a new meeting, time format: yyyy-MM-dd-HH:mm likes 2022-11-04 20:00, " + "participants format: username1,username2,username3");
        System.out.println("\t-q -query [host] [port] [username] [password] [start_time] [end_time]\n" + "\t\tquery meetings in a time range, time format: yyyy-MM-dd-HH:mm likes 2022-11-04 20:00");
        System.out.println("\t-d -delete [host] [port] [username] [password] [meeting_id]\n\t\tdelete a meeting by id");
        System.out.println("\t-c -clear [host] [port] [username] [password]\n\t\tclear all meetings of a user");
    }
}
