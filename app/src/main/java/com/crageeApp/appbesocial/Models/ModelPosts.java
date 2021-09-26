package com.crageeApp.appbesocial.Models;

public class ModelPosts {

    //use same name as used for uploading the post from the share fragment
    private String uid,userName,userDp,date,time,postImage,textPost,pTime,pId,pLikes,userVerified;

    public ModelPosts() {

    }

    public ModelPosts(String uid, String userName, String userDp, String date, String time, String postImage, String textPost, String pTime, String pId, String pLikes, String userVerified) {
        this.uid = uid;
        this.userName = userName;
        this.userDp = userDp;
        this.date = date;
        this.time = time;
        this.postImage = postImage;
        this.textPost = textPost;
        this.pTime = pTime;
        this.pId = pId;
        this.pLikes = pLikes;
        this.userVerified = userVerified;
    }

    public String getUid() {


        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDp() {
        return userDp;
    }

    public void setUserDp(String userDp) {
        this.userDp = userDp;
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

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getTextPost() {
        return textPost;
    }

    public void setTextPost(String textPost) {
        this.textPost = textPost;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpLikes() {
        return pLikes;
    }

    public void setpLikes(String pLikes) {
        this.pLikes = pLikes;
    }

    public String getUserVerified() {
        return userVerified;
    }

    public void setUserVerified(String userVerified) {
        this.userVerified = userVerified;
    }
}
