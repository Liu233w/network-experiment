package com.liu233w.network.rmi.share.exceptions;

import com.liu233w.network.rmi.share.Meeting;

import java.util.List;

/**
 * 添加会议失败时抛出此异常，存储与当前会议冲突的会议和错误信息
 */
public class AddMeetingException extends Exception {
    private List<Meeting> confilctMeetings;

    public AddMeetingException(List<Meeting> confilctMeetings) {
        this.confilctMeetings = confilctMeetings;
    }

    /**
     * 获取和待申请的会议冲突的会议
     *
     * @return
     */
    public List<Meeting> getConfilctMeetings() {
        return confilctMeetings;
    }
}
