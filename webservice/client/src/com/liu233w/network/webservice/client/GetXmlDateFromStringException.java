package com.liu233w.network.webservice.client;

/**
 * 只会被 {@link ClientHandler#getXmlDateFromString(String)} 抛出的异常，表示各种转换错误
 */
public class GetXmlDateFromStringException extends Exception {
    public GetXmlDateFromStringException(String message, Exception inner) {
        super(message, inner);
    }
}
