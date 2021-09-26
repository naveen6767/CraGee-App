package com.crageeApp.appbesocial.Groups;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterGroups;
import com.crageeApp.appbesocial.Models.ModelGroups;
import com.crageeApp.appbesocial.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SpecificGroupCategoryActivity extends AppCompatActivity {
    private RecyclerView recyclerViewGroups,recyclerViewGroupChats;

    private DatabaseReference groupsRef;
    private String specificCategoryChoice;
    private AdapterGroups adapterGroups;
    private List<ModelGroups> groupsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_group_category);


        //initialize the database reference
        groupsRef= FirebaseDatabase.getInstance().getReference("Groups");

        //init the recycler view and its properties
        recyclerViewGroups =findViewById(R.id.recyclerViewGroupsList);
        recyclerViewGroups.setNestedScrollingEnabled(false);
        recyclerViewGroups.setHasFixedSize(true);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(SpecificGroupCategoryActivity.this));

        Intent intent = getIntent();
        specificCategoryChoice=intent.getStringExtra("category choice");
        Toast.makeText(this, ""+specificCategoryChoice, Toast.LENGTH_SHORT).show();

        //init the groups list
        groupsList=new ArrayList<>();

        //get all the groups as suggestions to the user
        getAllGroups();

    }

    private void getAllGroups() {

        //get the groups data
        groupsRef
                .orderByChild("category")
                .equalTo(specificCategoryChoice)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        groupsList.clear();
                        for (DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            ModelGroups modelGroups=ds.getValue(ModelGroups.class);
                            groupsList.add(modelGroups);

                            //adapter
                            adapterGroups=new AdapterGroups(SpecificGroupCategoryActivity.this,groupsList);
                            //set adapter to recycler view
                            recyclerViewGroups.setAdapter(adapterGroups);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }
}
