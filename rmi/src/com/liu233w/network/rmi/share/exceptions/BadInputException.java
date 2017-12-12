package com.liu233w.network.rmi.share.exceptions;

/**
 * 输入参数错误时抛出的异常，跟 {@link IllegalArgumentException} 相比，
 * 由于不是 RuntimeException，必须要显式处理此异常
 */
public class BadInputException extends Exception {
    public BadInputException() {
        super();
    }

    public BadInputException(String message) {
        super(message);
    }

    public BadInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadInputException(Throwable cause) {
        super(cause);
    }

    protected BadInputException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
