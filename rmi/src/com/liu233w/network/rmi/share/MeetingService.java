package com.liu233w.network.rmi.share;

import com.liu233w.network.rmi.share.exceptions.AddMeetingException;
import com.liu233w.network.rmi.share.exceptions.BadInputException;
import com.liu233w.network.rmi.share.exceptions.LoginFailedException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

/**
 * 表示会议的服务接口
 */
public interface MeetingService extends Remote {
    /**
     * 注册用户，注册成功时返回 true，否则（例如用户已经存在）返回 false
     *
     * @param username
     * @param password
     * @return 注册成功时返回 true，否则（例如用户已经存在）返回 false
     * @throws RemoteException
     */
    boolean register(String username, String password) throws RemoteException;

    /**
     * 添加会议，当添加失败时（和与会用户的其他会议冲突）抛出异常。
     * id可以是任何值，没有影响。
     *
     * @param meeting
     * @throws RemoteException
     * @throws AddMeetingException  添加会议失败时抛出的异常
     * @throws LoginFailedException 登陆失败时抛出
     * @throws BadInputException    Meeting 的 start 在 end 之后抛出的异常
     */
    void add(Meeting meeting, String username, String password)
            throws RemoteException, AddMeetingException, LoginFailedException, BadInputException;

    /**
     * 根据起止日期来查询会议
     *
     * @param start
     * @param end
     * @return 查询到的会议
     * @throws RemoteException
     * @throws LoginFailedException 登陆失败时抛出
     */
    List<Meeting> query(Date start, Date end, String username, String password)
            throws RemoteException, LoginFailedException;

    /**
     * 根据 id 删除用户创建的会议。如果 id 不存在或不为用户所有，则不发生任何事。
     *
     * @param meetingId
     * @param username  用户名，用来检查会议所有权
     * @throws RemoteException
     * @throws LoginFailedException 登陆失败时抛出
     */
    void delete(int meetingId, String username, String password)
            throws RemoteException, LoginFailedException;

    /**
     * 删除该用户创建的所有会议
     *
     * @param username
     * @throws RemoteException
     * @throws LoginFailedException 登陆失败时抛出
     */
    void clear(String username, String password)
            throws RemoteException, LoginFailedException;

    /**
     * 检查用户是否存在且密码正确
     *
     * @param username
     * @param password
     * @return 正确时返回 true，否则返回 false
     * @throws RemoteException
     */
    boolean checkUser(String username, String password)
            throws RemoteException;
}
