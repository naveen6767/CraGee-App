package com.crageeApp.appbesocial.Chat;


public class Messages {
    private String message,type,messageDate,messageTime,receiver,from,messageCategory;
    private boolean seen;
    private long time;

    public Messages() {
    }

    public Messages(String message, String type, String messageDate, String messageTime, String receiver, String from, String messageCategory, boolean seen, long time) {
        this.message = message;
        this.type = type;
        this.messageDate = messageDate;
        this.messageTime = messageTime;
        this.receiver = receiver;
        this.from = from;
        this.messageCategory = messageCategory;
        this.seen = seen;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(String messageDate) {
        this.messageDate = messageDate;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessageCategory() {
        return messageCategory;
    }

    public void setMessageCategory(String messageCategory) {
        this.messageCategory = messageCategory;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
