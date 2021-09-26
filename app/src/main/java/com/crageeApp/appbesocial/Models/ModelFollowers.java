package com.crageeApp.appbesocial.Models;

public class ModelFollowers {

    private String id;  // we will need this id to get chatlist ,sender and receiver uid

    public ModelFollowers() {
    }

    public ModelFollowers(String id) {
        this.id = id;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
