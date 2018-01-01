package com.liu233w.network.webservice.client;

/**
 * 表示登陆失败的异常
 */
public class LoginFailedException extends Exception {
    public LoginFailedException(String message) {
        super(message);
    }
}
