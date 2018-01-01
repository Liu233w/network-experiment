package com.liu233w.network.webservice.server;

import com.liu233w.network.webservice.server.dto.QueryResult;
import com.liu233w.network.webservice.server.dto.TodoListResult;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

/**
 * 实现代办事项列表的类
 */
@WebService
public class TodoListService {

    private ArrayList<User> users;
    private ArrayList<Item> items;
    private int meetingIds;

    public TodoListService() {
        super();
        users = new ArrayList<>();
        items = new ArrayList<>();
        meetingIds = 1;
    }

    /**
     * 注册用户
     *
     * @param username
     * @param password
     * @return
     */
    @WebMethod
    public TodoListResult register(
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password) {
        User user = lookupUser(username);
        if (user != null) {
            return TodoListResult.failed("用户已经存在");
        }

        user = new User(username, password);
        users.add(user);
        return TodoListResult.success();
    }

    /**
     * 添加一条待办事项
     *
     * @param item     待办事项内容。不需要包含 id 和 creator，这个会自动设定
     * @param username
     * @param password
     * @return
     */
    @WebMethod
    public TodoListResult add(
            @WebParam(name = "item") Item item,
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password) {
        if (!login(username, password)) {
            return TodoListResult.authorizationFailed("用户名或密码错误");
        }

        if (item.getStart().after(item.getEnd())) {
            return TodoListResult.failed("参数错误：事项的结束时间不能在开始时间之前。");
        }

        item.setId(generateNewItemId());
        item.setCreator(username);
        items.add(item);

        return TodoListResult.success();
    }

    /**
     * 根据指定的起止时间来查询待办事项
     *
     * @param start
     * @param end
     * @param username
     * @param password
     * @return
     */
    @WebMethod
    public QueryResult query(
            @WebParam(name = "startDate") Date start,
            @WebParam(name = "endDate") Date end,
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password) {
        if (!login(username, password)) {
            return new QueryResult(TodoListResult.authorizationFailed("用户名或密码错误"));
        }

        Item[] results = this.items.stream()
                .filter(item -> !item.getStart().before(start)
                        && !item.getEnd().after(end))
                .toArray(Item[]::new);
        return new QueryResult(results);
    }

    /**
     * 根据id删除待办列表项。如果 id 不存在或不归该用户所有，会报错
     *
     * @param itemId
     * @param username 用户名，用来检查会议所有权
     * @param password
     * @return
     */
    @WebMethod
    public TodoListResult delete(
            @WebParam(name = "itemId") int itemId,
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password) {
        if (!login(username, password)) {
            return TodoListResult.authorizationFailed("用户名或密码错误");
        }

        Optional<Item> itemOptional = items.stream()
                .filter(item -> item.getId() == itemId)
                .findFirst();
        if (!itemOptional.isPresent() || !itemOptional.get().getCreator().equals(username)) {
            // 即使是不归用户所有也显示“不存在”，防止泄露其他用户的信息
            return TodoListResult.failed("该列表项不存在");
        }

        items.removeIf(item -> item.getId() == itemId && item.getCreator().equals(username));

        return TodoListResult.success();
    }

    /**
     * 删除该用户的所有待办事项
     *
     * @param username
     * @param password
     */
    @WebMethod
    public TodoListResult clear(
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password) {
        if (!login(username, password)) {
            return TodoListResult.authorizationFailed("用户名或密码错误");
        }

        items.removeIf(item -> item.getCreator().equals(username));

        return TodoListResult.success();
    }

    /**
     * 尝试登陆，检查用户名和密码是否正确
     *
     * @param username
     * @param password
     * @return
     */
    @WebMethod
    public TodoListResult checkUser(
            @WebParam(name = "username") String username,
            @WebParam(name = "password") String password) {
        if (login(username, password)) {
            return TodoListResult.success();
        } else {
            return TodoListResult.failed("用户名或密码错误");
        }
    }

    /**
     * 登陆。如果用户名和密码存在且正确，则返回true。否则返回false。
     *
     * @param username
     * @param password
     * @return
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
    private synchronized int generateNewItemId() {
        return meetingIds++;
    }
}
