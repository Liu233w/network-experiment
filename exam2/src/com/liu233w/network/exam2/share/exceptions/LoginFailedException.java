package com.liu233w.network.exam2.share.exceptions;

/**
 * 表示登陆失败的异常
 */
public class LoginFailedException extends Exception {
    public LoginFailedException() {
        super("登陆失败，请检查用户名或密码是否正确");
    }
}
