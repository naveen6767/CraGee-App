package com.crageeApp.appbesocial.Home.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.crageeApp.appbesocial.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomePostsFragment extends Fragment   {

    public HomePostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_posts, container, false);


    }
}
