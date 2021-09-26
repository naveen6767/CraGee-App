package com.crageeApp.appbesocial.Notifications;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class notificationsPagerAdapter extends FragmentStateAdapter {
    public notificationsPagerAdapter(@NonNull NotificationsFragment notificationsFragment) {
        super(notificationsFragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new LikesFragment();
            case 1:
                return new CommentsFragment();
            case 2:
                return new RequestsFragment();
            default:
                return new FollowsFragment();
        }

         }

    @Override
    public int getItemCount() {
        return 4;
    }
}
