package com.crageeApp.appbesocial.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.Repository.Repo;

import java.util.ArrayList;

public class followersViewModel extends ViewModel {


    MutableLiveData<ArrayList<ModelUsers>> followers;

    public void init (){

        if (followers!=null){
            return;
        }

        followers= Repo.getInstance().getFollowers();

    }

    public LiveData<ArrayList<ModelUsers>> getFollowers(){
        return followers;
    }
}
