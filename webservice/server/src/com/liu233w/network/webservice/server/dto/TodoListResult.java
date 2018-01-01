package com.liu233w.network.webservice.server.dto;

/**
 * 表示结果的类，Service中的每个方法都会返回本类或本类的子类
 */
public class TodoListResult {
    /**
     * 结果是否成功
     */
    private boolean success;

    /**
     * 是否因为验证原因而失败
     */
    private boolean authorizeFailed;

    /**
     * 携带的信息。一般在 success 为 false 时才有
     */
    private String message;

    /**
     * 工厂函数：构造表示成功的 Result 对象，没有携带额外信息
     *
     * @return
     */
    public static TodoListResult success() {
        return new TodoListResult(true, false, null);
    }

    /**
     * 工厂函数：构造表示因为某些原因而失败的 Result 对象
     *
     * @param reason 失败的原因
     * @return
     */
    public static TodoListResult failed(String reason) {
        return new TodoListResult(false, false, reason);
    }

    /**
     * 工厂函数：构造表示认证错误的 Result 对象
     *
     * @param reason 错误的具体原因
     * @return
     */
    public static TodoListResult authorizationFailed(String reason) {
        return new TodoListResult(false, true, reason);
    }

    /**
     * 使用另一个对象的内容来填充本对象中的 success authorizeFailed message 字段
     *
     * @param that
     */
    protected void setAs(TodoListResult that) {
        this.setSuccess(that.isSuccess());
        this.setAuthorizeFailed(that.isAuthorizeFailed());
        this.setMessage(that.getMessage());
    }

    protected TodoListResult(boolean success, boolean authorizeFailed, String message) {
        this.success = success;
        this.authorizeFailed = authorizeFailed;
        this.message = message;
    }

    /**
     * 默认构造函数
     */
    public TodoListResult() {
        this.success = false;
        this.authorizeFailed = false;
        this.message = null;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isAuthorizeFailed() {
        return authorizeFailed;
    }

    public void setAuthorizeFailed(boolean authorizeFailed) {
        this.authorizeFailed = authorizeFailed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
