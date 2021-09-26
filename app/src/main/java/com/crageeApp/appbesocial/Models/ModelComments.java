package com.crageeApp.appbesocial.Models;

public class ModelComments {


    String userName,date,time,comment,image,cLikes,commenterId,cId,currentPostId,originalPostOwnerId,timeStamp;

    public ModelComments() {
    }

    public ModelComments(String userName, String date, String time, String comment, String image, String cLikes, String commenterId, String cId, String currentPostId, String originalPostOwnerId, String timeStamp) {
        this.userName = userName;
        this.date = date;
        this.time = time;
        this.comment = comment;
        this.image = image;
        this.cLikes = cLikes;
        this.commenterId = commenterId;
        this.cId = cId;
        this.currentPostId = currentPostId;
        this.originalPostOwnerId = originalPostOwnerId;
        this.timeStamp = timeStamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getcLikes() {
        return cLikes;
    }

    public void setcLikes(String cLikes) {
        this.cLikes = cLikes;
    }

    public String getCommenterId() {
        return commenterId;
    }

    public void setCommenterId(String commenterId) {
        this.commenterId = commenterId;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }

    public String getCurrentPostId() {
        return currentPostId;
    }

    public void setCurrentPostId(String currentPostId) {
        this.currentPostId = currentPostId;
    }

    public String getOriginalPostOwnerId() {
        return originalPostOwnerId;
    }

    public void setOriginalPostOwnerId(String originalPostOwnerId) {
        this.originalPostOwnerId = originalPostOwnerId;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
