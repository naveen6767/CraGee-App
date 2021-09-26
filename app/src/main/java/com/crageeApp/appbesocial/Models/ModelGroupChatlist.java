package com.crageeApp.appbesocial.Models;

public class ModelGroupChatlist {

    private String groupChatTime,groupChatKey,groupID;  // we will need this id to get  group chatlist

    public ModelGroupChatlist() {
    }

    public ModelGroupChatlist(String groupChatTime, String groupChatKey, String groupID) {
        this.groupChatTime = groupChatTime;
        this.groupChatKey = groupChatKey;
        this.groupID = groupID;
    }

    public String getGroupChatTime() {
        return groupChatTime;
    }

    public void setGroupChatTime(String groupChatTime) {
        this.groupChatTime = groupChatTime;
    }

    public String getGroupChatKey() {
        return groupChatKey;
    }

    public void setGroupChatKey(String groupChatKey) {
        this.groupChatKey = groupChatKey;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }
}

