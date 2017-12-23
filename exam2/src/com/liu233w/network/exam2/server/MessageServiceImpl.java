package com.liu233w.network.exam2.server;

import com.liu233w.network.exam2.share.Message;
import com.liu233w.network.exam2.share.MessageService;
import com.liu233w.network.exam2.share.User;
import com.liu233w.network.exam2.share.exceptions.LoginFailedException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MessageServiceImpl extends UnicastRemoteObject implements MessageService {

    /**
     * 用户列表
     */
    private ArrayList<User> users;

    /**
     * 消息列表
     */
    private ArrayList<Message> messages;

    public MessageServiceImpl() throws RemoteException {
        super();
        users = new ArrayList<>();
        messages = new ArrayList<>();
    }

    /**
     * 寻找该用户，找不到则返回 null
     *
     * @param username
     * @return
     */
    private User lookupUser(String username) {
        for (User item :
                users) {
            if (item.getUsername().equals(username)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean register(String username, String password) throws RemoteException {
        User user = lookupUser(username);
        if (user != null) {
            return false;
        }

        user = new User(username, password);
        users.add(user);
        return true;
    }

    @Override
    public boolean checkUser(String username, String password) throws RemoteException {
        return login(username, password);
    }

    /**
     * 进行登陆操作。登入失败时抛出异常。
     *
     * @param username
     * @param password
     * @throws LoginFailedException
     */
    private void ensureLogined(String username, String password) throws LoginFailedException {
        if (!login(username, password)) {
            throw new LoginFailedException();
        }
    }

    /**
     * 登陆。如果用户名和密码存在且正确，则返回true。否则返回false。
     *
     * @param username
     * @param password
     * @return
     * @throws RemoteException
     */
    private boolean login(String username, String password) {
        User user = lookupUser(username);
        return user != null && user.getPassword().equals(password);
    }

    @Override
    public List<String> showUsers(String username, String password) throws RemoteException, LoginFailedException {
        ensureLogined(username, password);
        return users.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
    }

    @Override
    public boolean leaveMessage(String username, String password, String receiverName, String content) throws RemoteException, LoginFailedException {
        ensureLogined(username, password);

        boolean receiverExists = users.stream().anyMatch(
                user -> user.getUsername().equals(receiverName));
        if (!receiverExists)
            return false;

        messages.add(new Message(username, receiverName, content));
        return true;
    }

    @Override
    public List<Message> checkMessage(String username, String password) throws RemoteException, LoginFailedException {
        ensureLogined(username, password);

        return messages.stream()
                .filter(item -> item.getReceiver().equals(username))
                .collect(Collectors.toList());
    }
}
