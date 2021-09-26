package com.crageeApp.appbesocial.Models;

public class ModelGroups {
    //here use same name as in fire base as all required items will be retrieved through the model class
    private String category,imageUrl,name,privacy,status,groupId,groupCreator,groupChatKey,earning,groupVerification;

    public ModelGroups() {

    }

    public ModelGroups(String category, String imageUrl, String name, String privacy, String status, String groupId, String groupCreator, String groupChatKey, String earning, String groupVerification) {
        this.category = category;
        this.imageUrl = imageUrl;
        this.name = name;
        this.privacy = privacy;
        this.status = status;
        this.groupId = groupId;
        this.groupCreator = groupCreator;
        this.groupChatKey = groupChatKey;
        this.earning = earning;
        this.groupVerification = groupVerification;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupCreator() {
        return groupCreator;
    }

    public void setGroupCreator(String groupCreator) {
        this.groupCreator = groupCreator;
    }

    public String getGroupChatKey() {
        return groupChatKey;
    }

    public void setGroupChatKey(String groupChatKey) {
        this.groupChatKey = groupChatKey;
    }

    public String getEarning() {
        return earning;
    }

    public void setEarning(String earning) {
        this.earning = earning;
    }

    public String getGroupVerification() {
        return groupVerification;
    }

    public void setGroupVerification(String groupVerification) {
        this.groupVerification = groupVerification;
    }
}

