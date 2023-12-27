package com.example.hprestaurant.Models.Request;

import java.util.Date;

public class ServerRequest {
    private String user_uid;
    private int table_id;
    private String day;
    private Date date;
    private int conversation_id;

    public ServerRequest(Date date, int conversation_id) {
        this.date = date;
        this.conversation_id = conversation_id;
    }

    public ServerRequest(int table_id, String day) {
        this.table_id = table_id;
        this.day = day;
    }

    public ServerRequest(String user_uid) {
        this.user_uid = user_uid;
    }

    public String getUser_uid() {
        return user_uid;
    }

    public void setUser_uid(String user_uid) {
        this.user_uid = user_uid;
    }
}
