package com.crageeApp.appbesocial.Repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.crageeApp.appbesocial.Models.ModelUsers;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Repo {

    static Repo instance;
    private ArrayList<ModelUsers> modelUsers=new ArrayList<>();
    private  MutableLiveData<ArrayList<ModelUsers>> followers=new MutableLiveData<>();


    public static Repo getInstance( ){

        if (instance==null){
            instance=new Repo();
        }

        return instance;
    }

    public MutableLiveData<ArrayList<ModelUsers>> getFollowers(){

        if (modelUsers.size()==0){
            loadFollowers();
        }
        return followers;
    }

    private void loadFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query=reference.child("user_followers");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    modelUsers.add(dataSnapshot.getValue(ModelUsers.class));


                }
                followers.postValue(modelUsers);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
