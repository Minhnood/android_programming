package com.example.bai1;

public class NotificationModel {
    private String id;
    private String title;
    private String message;
    private String time;
    private boolean read;

    public NotificationModel(String id, String title, String message, String time, boolean read) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
        this.read = read;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTime() { return time; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
