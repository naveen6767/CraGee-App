package com.crageeApp.appbesocial.Posts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

public class FollowersActivity extends AppCompatActivity {

    private Toolbar followersToolbar;
    private RecyclerView recyclerViewFollowers;
    private DatabaseReference userRef;
    private String currentUserId,visitingUserId;
    private AdapterUsers adapterUsers;
    private List<ModelUsers> usersList;
    private List<ModelFollowers> followersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        followersToolbar=findViewById(R.id.followers_toolbar);
        //set tool bar as the action bar
        setSupportActionBar(followersToolbar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        followersToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //initialize the recycler view
        recyclerViewFollowers=findViewById(R.id.recyclerView_followers);
        //setting the recycler view properties
        recyclerViewFollowers.setHasFixedSize(true);
        recyclerViewFollowers.setLayoutManager(new LinearLayoutManager(this));

        //init the user list
        usersList =new ArrayList<>();
        followersList =new ArrayList<>();

        //init the fire base here
        userRef= FirebaseDatabase.getInstance().getReference("Users");
        currentUserId= FirebaseAuth.getInstance().getUid();

        //getting the intent of user id passed by previous activity
        Intent intent=getIntent();
        visitingUserId=intent.getStringExtra("userId");
        getFollowersList();

    }

    private void getFollowersList() {
        userRef
                .child(visitingUserId)
                .child("followers")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followersList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelFollowers modelFollowers=snapshot.getValue(ModelFollowers.class);
                    followersList.add(modelFollowers);
                }
                getAllFollowers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void getAllFollowers() {
        usersList.clear();
        for (int i=0;i<followersList.size();i++){
            Log.i(TAG, "no of Child in following list: "+followersList.size());
            Log.i(TAG, "getUserPosts: started running for the "+i+"th time");
            final String followersKeys=followersList.get(i).getId();
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds:dataSnapshot.getChildren())
                    {
                        ModelUsers modelUsers =ds.getValue(ModelUsers.class);
                        if (modelUsers.getUid().equals(followersKeys)){
                            usersList.add(modelUsers);
                        }


                        //adapter
                        adapterUsers =new AdapterUsers(FollowersActivity.this,usersList);

                        //set adapter to the recycler view
                        recyclerViewFollowers.setAdapter(adapterUsers);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }


}
