package com.crageeApp.appbesocial.Models;

public class ModelUsers {
    //use same as in firebase database
    private String name, image, uid, typingTo, accountCategory,celebrityCategory;



    public ModelUsers() {

    }

    public ModelUsers(String name, String image, String uid, String typingTo, String accountCategory, String celebrityCategory) {
        this.name = name;
        this.image = image;
        this.uid = uid;
        this.typingTo = typingTo;
        this.accountCategory = accountCategory;
        this.celebrityCategory = celebrityCategory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getAccountCategory() {
        return accountCategory;
    }

    public void setAccountCategory(String accountCategory) {
        this.accountCategory = accountCategory;
    }

    public String getCelebrityCategory() {
        return celebrityCategory;
    }

    public void setCelebrityCategory(String celebrityCategory) {
        this.celebrityCategory = celebrityCategory;
    }
}