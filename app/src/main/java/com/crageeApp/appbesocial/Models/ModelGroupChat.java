package com.crageeApp.appbesocial.Models;

public class ModelGroupChat {
    private String message,sender,timestamp,messageSender,messageDate,messageTime;

    public ModelGroupChat() {
    }

    public ModelGroupChat(String message, String sender, String timestamp, String messageSender, String messageDate, String messageTime) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
        this.messageSender = messageSender;
        this.messageDate = messageDate;
        this.messageTime = messageTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
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

}
