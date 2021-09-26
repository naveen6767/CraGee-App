package com.crageeApp.appbesocial.Groups;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterReviews;
import com.crageeApp.appbesocial.Models.ModelReviews;
import com.crageeApp.appbesocial.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllReviewsActivity extends AppCompatActivity {
    private Toolbar reviewsToolbar;
    private RecyclerView recyclerViewReviews;
    private AdapterReviews adapterReviews;
    private String uniqueGroupId;
    private DatabaseReference reviewsRef;
    private RatingBar groupRatings;
    List<ModelReviews> reviewsList;
    public static final String TAG = "All Reviews";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reviews);

        //init the tool bar here
        reviewsToolbar=findViewById(R.id.toolBar_reviews);
        //set tool bar as the action bar
        setSupportActionBar(reviewsToolbar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerViewReviews=findViewById(R.id.rVReviews);
        groupRatings=findViewById(R.id.set_group_ratings);
        //setting the recycler view properties
        recyclerViewReviews.setHasFixedSize(true);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        //init the firebase here
        reviewsRef= FirebaseDatabase.getInstance().getReference("Groups");
        //init the user list
        reviewsList =new ArrayList<>();
        Intent intent = getIntent();
        uniqueGroupId=intent.getStringExtra("GroupId");
        setGroupRatings();
        //get all users
        getAllReviews();

    }

    private void setGroupRatings() {
        reviewsRef
                .child(uniqueGroupId)
                .child("Group Rating")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, "onDataChange: the value of group rating is "+dataSnapshot.getValue());
                        float groupRatingRetrieved= Float.parseFloat(String.valueOf(dataSnapshot.getValue()));
                        groupRatings.setRating(groupRatingRetrieved);
                        groupRatings.setIsIndicator(true);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: error in the database"+databaseError.getMessage());

                    }
                });
    }
    private void getAllReviews() {
        reviewsRef
                .child(uniqueGroupId)
                .child("ReviewsAndRating")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, "onDataChange:successfull in getting the reviews ");
                        reviewsList.clear();
                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                            Log.i(TAG, "onDataChange: n the for loop"+snapshot.getKey());
                            ModelReviews modelReviews=snapshot.getValue(ModelReviews.class);
                            reviewsList.add(modelReviews);
                            //adapter
                            adapterReviews = new AdapterReviews(AllReviewsActivity.this,reviewsList);

                            //set adapter to the recycler view
                            recyclerViewReviews.setAdapter(adapterReviews);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: "+databaseError.getMessage());
                        Log.i(TAG, "onCancelled: "+databaseError.getCode());
                        Log.i(TAG, "onCancelled: "+databaseError.getCode());

                    }
                });

    }
}
