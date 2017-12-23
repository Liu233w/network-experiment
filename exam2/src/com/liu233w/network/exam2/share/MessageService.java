package com.liu233w.network.exam2.share;

import com.liu233w.network.exam2.share.exceptions.LoginFailedException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MessageService extends Remote {
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
     * 检查用户是否存在且密码正确
     *
     * @param username
     * @param password
     * @return 正确时返回 true，否则返回 false
     * @throws RemoteException
     */
    boolean checkUser(String username, String password)
            throws RemoteException;

    /**
     * 显示所有注册的用户
     *
     * @param username
     * @param password
     * @return
     * @throws RemoteException
     * @throws LoginFailedException 登陆失败时抛出
     */
    List<String> showUsers(String username, String password)
            throws RemoteException, LoginFailedException;

    /**
     * 该方法用于给其他用户留言
     *
     * @param username
     * @param password
     * @param receiverName
     * @param content      留言内容
     * @return 表示留言是否成功。如果不成功，返回 false
     * @throws RemoteException
     * @throws LoginFailedException 登陆失败时抛出
     */
    boolean leaveMessage(String username, String password,
                         String receiverName, String content)
            throws RemoteException, LoginFailedException;

    /**
     * 该方法打印用户的所有留言
     *
     * @param username
     * @param password
     * @return
     * @throws RemoteException
     * @throws LoginFailedException
     */
    List<Message> checkMessage(String username, String password)
            throws RemoteException, LoginFailedException;
}
