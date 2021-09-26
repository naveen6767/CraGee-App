package com.crageeApp.appbesocial.Notifications;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.crageeApp.appbesocial.R;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class NotificationsFragment extends Fragment  {

    private View notificationsFragmentView;

    private String currentUserId;
    private FirebaseAuth userAuth;

    private DatabaseReference followRequestRef,usersRef;

    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private Context context;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        notificationsFragmentView= inflater.inflate(R.layout.fragment_notifications, container, false);
     


        viewPager2=notificationsFragmentView.findViewById(R.id.notification_pager);
        viewPager2.setAdapter(new notificationsPagerAdapter(this));
        tabLayout=notificationsFragmentView.findViewById(R.id.tab_layout);
        TabLayoutMediator tabLayoutMediator=new TabLayoutMediator(
                tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setIcon(R.drawable.notify_likes);
                        BadgeDrawable badgeDrawable=tab.getOrCreateBadge();
                       // badgeDrawable.setBackgroundColor(ContextCompat.getColor(context,R.color.red));
                        badgeDrawable.setVisible(true);
                        badgeDrawable.setNumber(10);

                        break;
                    case 1:
                        tab.setIcon(R.drawable.notify_comments);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.notify_requests);
                        break;
                    default:
                        tab.setIcon(R.drawable.notify_follows);
                        break;
                }

            }
        }
        );
        tabLayoutMediator.attach();

        userAuth= FirebaseAuth.getInstance();
        currentUserId=userAuth.getUid();
        followRequestRef= FirebaseDatabase.getInstance().getReference("Follow Request");
        usersRef= FirebaseDatabase.getInstance().getReference("Users");


        return notificationsFragmentView;
    }



}