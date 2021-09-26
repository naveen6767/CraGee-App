package com.crageeApp.appbesocial.Models;

public class ModelStory {

    private String imageUrl,storyId,userId;
    private long timeStart,timeEnd;

    public ModelStory() {
    }

    public ModelStory(String imageUrl, String storyId, String userId, long timeStart, long timeEnd) {
        this.imageUrl = imageUrl;
        this.storyId = storyId;
        this.userId = userId;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }
}
