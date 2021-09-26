package com.crageeApp.appbesocial.Groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterUsersForRequests;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupRequestsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewGroupRequests;
    private String currentGroupId;
    private DatabaseReference groupRequestRef,requestSenderRef;
    private AdapterUsersForRequests adapterUsersForRequests;
    private List<ModelUsers> usersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_requests);

        initializeViews();
        //setting the recycler view properties
        recyclerViewGroupRequests.setHasFixedSize(true);
        recyclerViewGroupRequests.setLayoutManager(new LinearLayoutManager(this));

        //get passed values from the previous activities
        currentGroupId=getIntent().getStringExtra("GroupId");

        //get all group requests
        getAllGroupRequests();
    }

    private void initializeViews() {
        //init the recycler view
        recyclerViewGroupRequests=findViewById(R.id.groupRequestsList);
        //init the fire base here
        groupRequestRef= FirebaseDatabase.getInstance().getReference("Group Requests");
        requestSenderRef= FirebaseDatabase.getInstance().getReference("Users");
        //init the group requests list
        usersList=new ArrayList<>();

    }

    private void getAllGroupRequests() {

        groupRequestRef
                .child(currentGroupId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    final String requestSenderKey = ds.getKey();
                    //here try to retrieve the image and user name of the user
                    //who has sent the friend request
                    assert requestSenderKey != null;
                    requestSenderRef
                            .child(requestSenderKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists())
                            {
                                ModelUsers modelUsers =dataSnapshot.getValue(ModelUsers.class);
                                usersList.add(modelUsers);

                                //adapter
                                adapterUsersForRequests =new AdapterUsersForRequests(GroupRequestsActivity.this,usersList);

                                //set adapter to the recycler view
                                recyclerViewGroupRequests.setAdapter(adapterUsersForRequests);
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
