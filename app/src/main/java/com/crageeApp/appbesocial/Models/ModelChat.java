package com.crageeApp.appbesocial.Models;

public class ModelChat {
     private String message,sender,receiver,timestamp,messageDate,messageTime,type,unSeenMsg;
     private boolean seen;

    public ModelChat() {
    }

    public ModelChat(String message, String sender, String receiver, String timestamp, String messageDate, String messageTime, String type, String unSeenMsg, boolean seen) {
        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.messageDate = messageDate;
        this.messageTime = messageTime;
        this.type = type;
        this.unSeenMsg = unSeenMsg;
        this.seen = seen;
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

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnSeenMsg() {
        return unSeenMsg;
    }

    public void setUnSeenMsg(String unSeenMsg) {
        this.unSeenMsg = unSeenMsg;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
