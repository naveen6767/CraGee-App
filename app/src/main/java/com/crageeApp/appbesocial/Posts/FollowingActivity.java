package com.crageeApp.appbesocial.Posts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterUsers;
import com.crageeApp.appbesocial.Models.ModelFollowers;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FollowingActivity extends AppCompatActivity {

    private Toolbar followingToolbar;
    private RecyclerView recyclerViewFollowing;
    private DatabaseReference userRef;
    private String currentUserId,visitingUserId;
    private AdapterUsers adapterUsers;
    private List<ModelUsers> usersList;
    private List<ModelFollowers> followingList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        followingToolbar=findViewById(R.id.following_toolbar);
        //set tool bar as the action bar
        setSupportActionBar(followingToolbar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //initialize the recycler view
        recyclerViewFollowing=findViewById(R.id.recyclerView_following);
        //setting the recycler view properties
        recyclerViewFollowing.setHasFixedSize(true);
        recyclerViewFollowing.setLayoutManager(new LinearLayoutManager(this));

        //init the user list
        usersList =new ArrayList<>();
        followingList =new ArrayList<>();

        //init the fire base here
        userRef= FirebaseDatabase.getInstance().getReference("Users");
        currentUserId= FirebaseAuth.getInstance().getUid();

        //getting the intent of user id passed by previous activity
        Intent intent=getIntent();
        visitingUserId=intent.getStringExtra("userId");

        getFollowingList();


    }

    private void getFollowingList() {
        userRef
                .child(visitingUserId)
                .child("following").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Log.i(TAG, "inside the following list"+snapshot);
                    ModelFollowers modelFollowing=snapshot.getValue(ModelFollowers.class);
                    Log.i(TAG, "inside the model followers"+modelFollowing.getId());
                    followingList.add(modelFollowing);
                }
                Log.i(TAG, "outside the for loop"+followingList.size());
                getAllFollowers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getAllFollowers() {
        //usersList.clear();
        for (int i=0;i<followingList.size();i++){
            Log.i(TAG, "no of Child in following list: "+followingList.size());
            Log.i(TAG, "getUserPosts: started running for the "+i+"th time");
            final String followersKeys=followingList.get(i).getId();
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds:dataSnapshot.getChildren())
                    {
                        Log.i(TAG, "inside the current user ds"+ds);
                        ModelUsers users =ds.getValue(ModelUsers.class);
                        Log.i(TAG, "inside the model users"+users);
                        Log.i(TAG, "accessing the model users"+users.getUid());
                        if (users.getUid().equals(followersKeys)){
                            usersList.add(users);
                        }


                        //adapter
                        adapterUsers =new AdapterUsers(FollowingActivity.this,usersList);
                        adapterUsers.notifyDataSetChanged();

                        //set adapter to the recycler view
                        recyclerViewFollowing.setAdapter(adapterUsers);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
