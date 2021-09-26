package com.crageeApp.appbesocial.Models;

public class ModelChatlist {

    private String id,chatId,chatTime;  // we will need this id to get chatlist ,sender and receiver uid

    public ModelChatlist() {
    }

    public ModelChatlist(String id, String chatId, String chatTime) {
        this.id = id;
        this.chatId = chatId;
        this.chatTime = chatTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatTime() {
        return chatTime;
    }

    public void setChatTime(String chatTime) {
        this.chatTime = chatTime;
    }
}

