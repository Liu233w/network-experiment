package com.liu233w.network.rmi.server;

import com.liu233w.network.rmi.share.Meeting;
import com.liu233w.network.rmi.share.MeetingService;
import com.liu233w.network.rmi.share.User;
import com.liu233w.network.rmi.share.exceptions.AddMeetingException;
import com.liu233w.network.rmi.share.exceptions.BadInputException;
import com.liu233w.network.rmi.share.exceptions.LoginFailedException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeetingServiceImpl extends UnicastRemoteObject implements MeetingService {

    private ArrayList<User> users;
    private ArrayList<Meeting> meetings;
    private int meetingIds;

    public MeetingServiceImpl() throws RemoteException {
        super();
        users = new ArrayList<>();
        meetings = new ArrayList<>();
        meetingIds = 1;
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
    public void add(Meeting meeting, String username, String password)
            throws RemoteException, AddMeetingException, LoginFailedException, BadInputException {
        ensureLogined(username, password);

        if (meeting.getStart().after(meeting.getEnd())) {
            throw new BadInputException("参数错误：会议的结束时间不能在开始时间之前。");
        }

        // 跟任意一个与会者时间冲突的会议
        Stream<Meeting> conflictMeetings = meetings.stream()
                .filter(item ->
                        // meeting.start 在 item 的时间中间
                        item.getStart().before(meeting.getStart()) && item.getEnd().after(meeting.getStart())
                                // meeting.end 在 item 的时间中间
                                || item.getStart().before(meeting.getEnd()) && item.getEnd().after(meeting.getEnd())
                                // item.start 和 end 在 meeting 的时间中间（防止 meeting 把 item 整个包进去的情况）
                                || meeting.getStart().before(item.getStart()) && meeting.getEnd().after(item.getStart())
                                || meeting.getStart().before(item.getEnd()) && meeting.getEnd().after(item.getEnd())
                                // item 正好等于 meeting（两端点重合时，且内部重合的情况；紧挨着的两个会议不算）
                                || meeting.getStart().equals(item.getStart()) && meeting.getEnd().equals(item.getEnd())
                );

        // 便于下文查询使用
        ArrayList<String> myMeetingUsers = new ArrayList<>();
        myMeetingUsers.add(meeting.getCreator());
        myMeetingUsers.add(meeting.getOtherUser());

        List<Meeting> conflictMeetingList = conflictMeetings.filter(item ->
                myMeetingUsers.contains(item.getCreator())
                        || myMeetingUsers.contains(item.getOtherUser()))
                .collect(Collectors.toList());

        if (conflictMeetingList.size() >= 1) {
            throw new AddMeetingException(conflictMeetingList);
        }

        meeting.setId(generateNewMeetingId());
        meetings.add(meeting);
    }

    @Override
    public List<Meeting> query(Date start, Date end, String username, String password) throws RemoteException, LoginFailedException {
        ensureLogined(username, password);
        return meetings.stream()
                .filter(item -> !item.getStart().before(start)
                        && !item.getEnd().after(end))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public void delete(int meetingId, String username, String password) throws RemoteException, LoginFailedException {
        ensureLogined(username, password);
        meetings.removeIf(item -> item.getId() == meetingId && item.getCreator().equals(username));
    }

    @Override
    public void clear(String username, String password) throws RemoteException, LoginFailedException {
        ensureLogined(username, password);
        meetings.removeIf(item -> item.getCreator().equals(username));
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

    /**
     * 获取一个新的 MeetingId，此id保证不与其他id冲突
     *
     * @return
     */
    private int generateNewMeetingId() {
        return meetingIds++;
    }
}
