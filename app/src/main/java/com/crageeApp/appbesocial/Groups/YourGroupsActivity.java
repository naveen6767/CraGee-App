package com.crageeApp.appbesocial.Groups;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterGroups;
import com.crageeApp.appbesocial.Models.ModelGroups;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YourGroupsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewYourGroups;
    private Toolbar toolbarYourGroups;
    private FirebaseAuth userAuth;
    private String currentUserId;
    private DatabaseReference userRef;

    private AdapterGroups adapterGroups;
    private List<ModelGroups> yourGroupsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_groups);

        //init the tool bar here
        toolbarYourGroups=findViewById(R.id.toolBarYrGroups);
        //set tool bar as the action bar
        setSupportActionBar(toolbarYourGroups);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //init the fire base here
        userAuth= FirebaseAuth.getInstance();
        currentUserId=userAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference("Users");

        //init the group requests list
        yourGroupsList=new ArrayList<>();

        //init the view here
        recyclerViewYourGroups=findViewById(R.id.createdGroupsList);

        //set the properties for the recycler view
        recyclerViewYourGroups.setHasFixedSize(true);
        recyclerViewYourGroups.setLayoutManager(new LinearLayoutManager(this));

        //get all your groups
        getYourGroups();

    }

    private void getYourGroups() {

        userRef.child(currentUserId).child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                yourGroupsList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ModelGroups modelGroups=ds.getValue(ModelGroups.class);
                    yourGroupsList.add(modelGroups);

                    //adapters
                    adapterGroups=new AdapterGroups(YourGroupsActivity.this,yourGroupsList);


                    //set adapter to the recycler view
                    recyclerViewYourGroups.setAdapter(adapterGroups);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
