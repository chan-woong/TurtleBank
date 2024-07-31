package com.app.turtlebank;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoticeListRecords {
    private String id;
    private String title;
    private String userId;
    private String updatedAt;

    public NoticeListRecords(String id, String userId, String title, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.updatedAt = updatedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdatedAtFormatted() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date = dateFormat.parse(updatedAt);
            SimpleDateFormat newDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return newDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return updatedAt; // 형식이 맞지 않으면 그대로 반환
        }
    }

}