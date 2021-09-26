package com.crageeApp.appbesocial.Models;

public class ModelPrnslChatFragment {


    public boolean seen;
    public long timestamp;

    public ModelPrnslChatFragment() {
    }

    public ModelPrnslChatFragment(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
