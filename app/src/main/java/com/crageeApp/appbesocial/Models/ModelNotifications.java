package com.crageeApp.appbesocial.Models;

public class ModelNotifications {

    private String notification,notifiedPostId,notifiedPostPublisher,notifierDp,notifierName,notifierUid,postImage,notificationId;
    private long timeStamp;
    public ModelNotifications() {
    }

    public ModelNotifications(String notification, String notifiedPostId, String notifiedPostPublisher, String notifierDp, String notifierName, String notifierUid, String postImage, String notificationId, long timeStamp) {
        this.notification = notification;
        this.notifiedPostId = notifiedPostId;
        this.notifiedPostPublisher = notifiedPostPublisher;
        this.notifierDp = notifierDp;
        this.notifierName = notifierName;
        this.notifierUid = notifierUid;
        this.postImage = postImage;
        this.notificationId = notificationId;
        this.timeStamp = timeStamp;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getNotifiedPostId() {
        return notifiedPostId;
    }

    public void setNotifiedPostId(String notifiedPostId) {
        this.notifiedPostId = notifiedPostId;
    }

    public String getNotifiedPostPublisher() {
        return notifiedPostPublisher;
    }

    public void setNotifiedPostPublisher(String notifiedPostPublisher) {
        this.notifiedPostPublisher = notifiedPostPublisher;
    }

    public String getNotifierDp() {
        return notifierDp;
    }

    public void setNotifierDp(String notifierDp) {
        this.notifierDp = notifierDp;
    }

    public String getNotifierName() {
        return notifierName;
    }

    public void setNotifierName(String notifierName) {
        this.notifierName = notifierName;
    }

    public String getNotifierUid() {
        return notifierUid;
    }

    public void setNotifierUid(String notifierUid) {
        this.notifierUid = notifierUid;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
