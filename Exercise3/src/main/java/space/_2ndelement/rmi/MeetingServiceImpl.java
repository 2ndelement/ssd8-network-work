package space._2ndelement.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @author 2ndElement
 * @version v1.0
 * @description
 * @date 2022/11/4 19:02
 */
public class MeetingServiceImpl extends UnicastRemoteObject implements MeetingService {
    /**
     * 会议列表
     */
    private ArrayList<Meeting> meetings;
    /**
     * 用户列表
     */
    private HashMap<String, User> userMap;
    /**
     * 日期格式工具
     */
    private DateFormat dateFormatter;
    /**
     * 会议号计数
     */
    private static int meetingCount = 0;

    public MeetingServiceImpl() throws RemoteException {
        super();
        meetings = new ArrayList<>();
        userMap = new HashMap<>();
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm");
    }

    /**
     * 判断用户是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    private boolean isUserExist(String username) {
        return userMap.containsKey(username);
    }

    /**
     * 判断用户是否有效
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户是否有效
     * @throws RemoteException
     */
    @Override
    public boolean verifyUser(String username, String password) {
        if (!isUserExist(username)) {
            return false;
        }
        return userMap.get(username).getPassword().equals(password);
    }

    /**
     * 判断时间是否可用
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 是否可用
     */
    private boolean isAvailableTime(Date start, Date end) {
        for (Meeting meeting : meetings) {
            // 当前时间段在某一会议的时间段内
            if (meeting.getStart().before(end) && meeting.getEnd().after(start)) {
                return false;
            }
            // 当前时间段包含某一会议的时间段
            if (start.before(meeting.getStart()) && end.after(meeting.getEnd())) {
                return false;
            }
            // 处理边界情况
            if (meeting.getStart().equals(start) || meeting.getEnd().equals(end)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 用户注册接口
     *
     * @param username 注册用户名
     * @param password 注册密码
     * @return 是否注册成功
     * @throws RemoteException
     */

    @Override
    public boolean registerUser(String username, String password) throws RemoteException {
        if (!isUserExist(username)) {
            userMap.put(username, new User(username, password));
            System.out.println("register user: " + "{" + username + "," + password + "}");
            return true;
        }
        return false;
    }

    /**
     * 添加会议
     *
     * @param username       创建者用户名
     * @param password       创建者密码
     * @param otherUserNames 其他参与用户用户名列表
     * @param start          会议开始时间
     * @param end            会议结束时间
     * @param title          会议标题
     * @return 是否创建成功
     * @throws RemoteException
     */
    @Override
    public String addMeeting(String username, String password, String[] otherUserNames, String start, String end, String title) throws RemoteException {
        if (!verifyUser(username, password)) {
            return Constant.INVALID_USER;
        }

        Date startTime;
        Date endTime;
        // 判断时间格式是否正确
        try {
            startTime = dateFormatter.parse(start);
            endTime = dateFormatter.parse(end);
        } catch (ParseException e) {
            return Constant.INVALID_TIME_FORMAT;
        }
        // 判断时间是否可用
        if (!isAvailableTime(startTime, endTime)) {
            return Constant.INVALID_TIME;
        }
        // 判断用户是否存在且数量合适
        if (otherUserNames.length < 1) {
            return Constant.INVALID_PARTICIPANT;
        }
        ArrayList<User> otherUsers = new ArrayList<>();
        for (String otherUserName : otherUserNames) {
            if (!isUserExist(otherUserName)) {
                return Constant.INVALID_USER;
            }
            otherUsers.add(userMap.get(otherUserName));
        }
        Meeting meeting = new Meeting(meetingCount++, title, userMap.get(username), otherUsers, startTime, endTime);
        meetings.add(meeting);
        System.out.println("meeting add: " + meeting);
        return Constant.SUCCESS_ADD_MEETING;
    }

    /**
     * 会议查询接口
     *
     * @param username 用户名
     * @param password 密码
     * @param start    开始时间
     * @param end      结束时间
     * @return 查询的会议信息
     * @throws RemoteException
     */
    @Override
    public String queryMeeting(String username, String password, String start, String end) throws RemoteException {
        if (!verifyUser(username, password)) {
            return Constant.INVALID_USER;
        }
        Date startTime;
        Date endTime;
        // 时间格式是否正确
        try {
            startTime = dateFormatter.parse(start);
            endTime = dateFormatter.parse(end);
        } catch (ParseException e) {
            return Constant.INVALID_TIME_FORMAT;
        }
        StringBuilder info = new StringBuilder();
        for (Meeting meeting : meetings) {
            if (meeting.getStart().after(startTime) && meeting.getEnd().before(endTime)) {
                info.append(meeting).append("\n");
            }
        }
        return info.toString().isEmpty() ? Constant.NO_MEETING : info.toString();
    }

    /**
     * 会议删除接口
     *
     * @param username  用户名
     * @param password  密码
     * @param meetingId 会议ID
     * @return 是否删除成功
     * @throws RemoteException
     */
    @Override
    public boolean deleteMeeting(String username, String password, int meetingId) throws RemoteException {
        if (!verifyUser(username, password)) {
            return false;
        }
        meetings.forEach(meeting -> {
            if (meeting.getId() == meetingId && meeting.getOrganizer().getUsername().equals(username)) {
                System.out.println("delete meeting: " + meeting);
            }
        });
        meetings.removeIf(meeting -> meeting.getId() == meetingId && meeting.getOrganizer().getUsername().equals(username));
        return true;
    }

    /**
     * 会议清除接口
     *
     * @param username 用户名
     * @param password 密码
     * @return 是否清除成功
     * @throws RemoteException
     */
    @Override
    public boolean clearMeeting(String username, String password) throws RemoteException {
        if (!verifyUser(username, password)) {
            return false;
        }
        meetings.forEach(meeting -> {
            if (meeting.getOrganizer().getUsername().equals(username)) {
                System.out.println("delete meeting: " + meeting);
            }
        });
        meetings.removeIf(meeting -> meeting.getOrganizer().getUsername().equals(username));
        return true;
    }

}
