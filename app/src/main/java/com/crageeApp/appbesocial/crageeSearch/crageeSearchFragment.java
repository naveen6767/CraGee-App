package com.crageeApp.appbesocial.crageeSearch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.crageeApp.appbesocial.R;

public class crageeSearchFragment extends Fragment {

    private CrageeSearchViewModel mViewModel;

    public static crageeSearchFragment newInstance() {
        return new crageeSearchFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cragee_search_fragment, container, false);




    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CrageeSearchViewModel.class);
        // TODO: Use the ViewModel







    }

}