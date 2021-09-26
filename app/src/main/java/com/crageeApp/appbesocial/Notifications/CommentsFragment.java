package com.crageeApp.appbesocial.Notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.crageeApp.appbesocial.R;


public class CommentsFragment extends Fragment {


    private View commentsFragmentView;


    public CommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        commentsFragmentView= inflater.inflate(R.layout.fragment_comments, container, false);

        return commentsFragmentView;



    }


}
