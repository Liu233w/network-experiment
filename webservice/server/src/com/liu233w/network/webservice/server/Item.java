package com.liu233w.network.webservice.server;

import java.util.Date;

/**
 * 表示一条待办事项
 */
public class Item {
    private int id;
    private String creator;
    private String description;
    private Date start;
    private Date end;

    public Item(int id, String creator, String description, Date start, Date end) {
        this.id = id;
        this.creator = creator;
        this.description = description;
        this.start = start;
        this.end = end;
    }

    public Item() {
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
