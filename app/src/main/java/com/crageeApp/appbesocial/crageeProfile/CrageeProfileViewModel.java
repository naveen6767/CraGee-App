package com.crageeApp.appbesocial.crageeProfile;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
public class CrageeProfileViewModel extends ViewModel {

    // TODO: Implement the ViewModel
    public MutableLiveData<ModelUsers> getUserProfile(){
        DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        MutableLiveData<ModelUsers> userProfileMutableLiveData=new MutableLiveData<>();
        MutableLiveData<String> name=new MutableLiveData<>();
        Query currentUserQuery = usersReference.orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentUserQuery.keepSynced(true);
        usersReference
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers modelUsers=snapshot.getValue(ModelUsers.class);
                userProfileMutableLiveData.postValue(modelUsers);

//              userProfileMutableLiveData.postValue(String.valueOf(snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("name").getValue()));


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return userProfileMutableLiveData;

    }

}