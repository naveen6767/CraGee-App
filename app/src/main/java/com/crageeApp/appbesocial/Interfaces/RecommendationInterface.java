package com.crageeApp.appbesocial.Interfaces;

import android.widget.Button;

public interface RecommendationInterface {

    void onItemClicked(int position, String userUid);
    void onFollowClicked(int position, String userUid, String name, String image, Button followButton);

}
