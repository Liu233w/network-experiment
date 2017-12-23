package com.liu233w.network.exam2.share;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    private String creator;
    private String receiver;
    private Date createTime;
    private String content;

    public Message(String creator, String receiver, String content) {
        this.creator = creator;
        this.receiver = receiver;
        this.content = content;
        this.createTime = new Date();
    }

    /**
     * 消息的发送者
     *
     * @return
     */
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * 消息的接收者
     *
     * @return
     */
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * 消息的创建时间
     *
     * @return
     */
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 消息的内容
     *
     * @return
     */
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
