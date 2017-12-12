package com.liu233w.network.rmi.share;

import java.io.Serializable;
import java.util.Date;

/**
 * 表示一场会议
 */
public class Meeting implements Serializable, Comparable<Meeting> {
    private int id;
    private String creator;
    private String otherUser;
    private String title;
    private Date start;
    private Date end;

    public Meeting(int id, String creator, String otherUser, String title, Date start, Date end) {
        this.id = id;
        this.creator = creator;
        this.otherUser = otherUser;
        this.title = title;
        this.start = start;
        this.end = end;
    }

    public Meeting() {
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(String otherUser) {
        this.otherUser = otherUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * 比较大小，先按 start 比较大小，相同时再按照 end 比较大小
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Meeting o) {
        int cmpStart = this.getStart().compareTo(o.getStart());
        return cmpStart == 0
                ? this.getEnd().compareTo(o.getEnd())
                : cmpStart;
    }
}
