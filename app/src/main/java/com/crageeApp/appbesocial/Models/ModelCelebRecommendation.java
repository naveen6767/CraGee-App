package com.crageeApp.appbesocial.Models;

public class ModelCelebRecommendation {

    private String image,name,uid;

    public ModelCelebRecommendation() {

    }

    public ModelCelebRecommendation(String image, String name, String uid) {
        this.image = image;
        this.name = name;
        this.uid = uid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
