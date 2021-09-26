package com.crageeApp.appbesocial.crageeProfile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class crageeProfileFragment extends Fragment {

    private CrageeProfileViewModel mViewModel;
    private View view;
    private Toolbar profileToolBar;
    private TextView account_Name, currentUsrPosts,
            currentUserFollowers, currentUserFollowing, currentAccountBio,account_userId,verification;
    private CircleImageView userProfileImage;
    private FloatingActionButton floatingBtn;
    private static final String TAG = "crageeProfileFragment";
    public static crageeProfileFragment newInstance() {
        return new crageeProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.cragee_profile_fragment, container, false);

        account_Name=view.findViewById(R.id.account_Name);
        userProfileImage=view.findViewById(R.id.visit_profile_image);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CrageeProfileViewModel.class);
        // TODO: Use the ViewModel
        mViewModel.getUserProfile().observe(getViewLifecycleOwner(), new Observer<ModelUsers>() {
            @Override
            public void onChanged(ModelUsers modelUsers) {
                Log.d(TAG, "onChanged: the value of model users is"+modelUsers.getName()+modelUsers.getUid());
                account_Name.setText(String.valueOf(modelUsers.getName()));
                try {
                    
                    Picasso.get().load(modelUsers.getImage()).placeholder(R.drawable.post_image_blue).into(userProfileImage);

                } catch (Exception e) {
                    Picasso.get().load(R.drawable.post_image_blue).into(userProfileImage);
                }
            }
        });
        
//        mViewModel.getUserProfile().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(String s) {
//                Log.d(TAG, "onChanged: the value of s is"+s);
//
//                account_Name.setText(String.valueOf(s));
//
//            }
//        });


    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: started again");
    }
}