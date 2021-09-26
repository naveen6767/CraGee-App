package com.crageeApp.appbesocial.Groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterUsers;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupMembersActivity extends AppCompatActivity {


    private RecyclerView rVGroupMembers;
    private String currentGroupId;
    private AdapterUsers adapterUsersForMembers;
    private DatabaseReference groupMembersRef,usersRef;
    List<ModelUsers> usersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        //initialize here
        rVGroupMembers=findViewById(R.id.rVGroupMembersList);
        //setting the recycler view properties
        rVGroupMembers.setHasFixedSize(true);
        rVGroupMembers.setLayoutManager(new LinearLayoutManager(this));


        currentGroupId=getIntent().getStringExtra("GroupId");

        groupMembersRef= FirebaseDatabase.getInstance().getReference("Groups");
        usersRef= FirebaseDatabase.getInstance().getReference("Users");

        //init the group requests list
        usersList=new ArrayList<>();

        //get all group requests
        getAllGroupMembers();

    }

    private void getAllGroupMembers() {

        groupMembersRef.child(currentGroupId).child("Group Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    String groupMembersKey = ds.getKey();
                    //here try to retrieve the image and user name of the users
                    //who are the members of current group
                    usersRef
                            .child(groupMembersKey)
                            .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists())
                            {
                                ModelUsers modelUsers =dataSnapshot.getValue(ModelUsers.class);
                                usersList.add(modelUsers);

                                //adapter
                                adapterUsersForMembers =new AdapterUsers(GroupMembersActivity.this,usersList);

                                //set adapter to the recycler view
                                rVGroupMembers.setAdapter(adapterUsersForMembers);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
