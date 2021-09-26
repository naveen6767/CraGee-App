package com.crageeApp.appbesocial.Groups;



public class groupMessages {
    private String message,type,messageDate,messageTime,messageSender,from;
    private boolean seen;
    private long time;

    public groupMessages() {
    }

    public groupMessages(String message, String type, String messageDate, String messageTime, String messageSender, String from, boolean seen, long time) {
        this.message = message;
        this.type = type;
        this.messageDate = messageDate;
        this.messageTime = messageTime;
        this.messageSender = messageSender;
        this.from = from;
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

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
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
