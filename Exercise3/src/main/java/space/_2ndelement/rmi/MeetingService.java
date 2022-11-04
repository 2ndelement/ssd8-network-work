package space._2ndelement.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author 2ndElement
 * @version v1.0
 * @description 会议服务接口
 * @date 2022/11/4 18:54
 */
public interface MeetingService extends Remote {
    /**
     * 用户注册接口
     *
     * @param username 注册用户名
     * @param password 注册密码
     * @return 是否注册成功
     * @throws RemoteException
     */
    public boolean registerUser(String username, String password) throws RemoteException;

    /**
     * 会议添加接口
     *
     * @param username       创建者用户名
     * @param password       创建者密码
     * @param otherUserNames 其他参与用户用户名列表
     * @param start          会议开始时间
     * @param end            会议结束时间
     * @param title          会议标题
     * @return 添加会议的信息
     * @throws RemoteException
     */
    public String addMeeting(String username, String password, String[] otherUserNames, String start, String end,
                             String title) throws RemoteException;

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
    public String queryMeeting(String username, String password, String start, String end) throws RemoteException;

    /**
     * 会议删除接口
     *
     * @param username  用户名
     * @param password  密码
     * @param meetingId 会议ID
     * @return 是否删除成功
     * @throws RemoteException
     */
    public boolean deleteMeeting(String username, String password, int meetingId) throws RemoteException;

    /**
     * 会议清除接口
     *
     * @param username 用户名
     * @param password 密码
     * @return 是否清除成功
     * @throws RemoteException
     */
    public boolean clearMeeting(String username, String password) throws RemoteException;

    /**
     * 判断用户是否有效
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户是否有效
     * @throws RemoteException
     */
    public boolean verifyUser(String username, String password) throws RemoteException;
}
