package com.crageeApp.appbesocial.Ratings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class RatingsActivity extends AppCompatActivity {



    private EditText reviews;
    private String groupId,currentUserId,currentUserName,currentUserImage,saveCurrentDate;
    private DatabaseReference ratingsRef,usersRef;

    public static final String TAG = "Ratings";
    private RatingBar ratingsBar;
    private float userRating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratings);

        ratingsBar=findViewById(R.id.rating_bar_button);
        Button postRatings = findViewById(R.id.btnPostRatings);


        Intent intent=getIntent();
        userRating=intent.getFloatExtra("rating", 0);
        groupId=intent.getStringExtra("current GroupId");

        reviews=findViewById(R.id.etReview);

        currentUserId= FirebaseAuth.getInstance().getUid();
        ratingsRef= FirebaseDatabase.getInstance().getReference("Groups");
        usersRef= FirebaseDatabase.getInstance().getReference("Users");
        Calendar calForDate =Calendar.getInstance();
        SimpleDateFormat currentDate =new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        ratingsBar.setRating(userRating);
        Log.i(TAG, "onCreate: user rating before the rating bar changed");
        retrieveCurrentUserInfo();

        ratingsBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float currentRating, boolean fromUser) {
                userRating=currentRating;
                Log.i(TAG, "onRatingChanged: the final ratings is"+userRating);
            }
        });
        postRatings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: post rating button clicked");
                postUserRatings();

            }
        });
    }
    private void postUserRatings() {

        final String userReviews=reviews.getText().toString();
        Log.i(TAG, "posting: rating from the group info activity"+userRating);
        String timeStamp =String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> ratingsMap = new HashMap<>();
        ratingsMap.put("rating",userRating);
        ratingsMap.put("review",userReviews);
        ratingsMap.put("timeStamp",timeStamp);
        ratingsMap.put("reviewerName",currentUserName);
        ratingsMap.put("reviewerImage",currentUserImage);
        ratingsMap.put("reviewDate",saveCurrentDate);
        ratingsMap.put("reviewerId", FirebaseAuth.getInstance().getUid());
        ratingsRef
                .child(groupId)
                .child("ReviewsAndRating")
                .child(currentUserId)
                .updateChildren(ratingsMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: rating and reviews are updated successfully"+userRating+userReviews);
                        ratingsRef
                                .child(groupId)
                                .child("All Ratings")
                                .child(currentUserId)
                                .setValue(userRating)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i(TAG, "onSuccess: successfully updated the all ratings");
                                        reviews.setText("");
                                        calculateAvgRating();

                                        onBackPressed();
                                        // sendUserToGroupInfo();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "onFailure: there is a exception error on updating the firebase in all ratings"+e.getMessage());
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: problem in updating the rating and reviews");

            }
        });
    }

    private void calculateAvgRating() {
        Log.i(TAG, "calculateAvgRating: calculating the average rating");
        ratingsRef
                .child(groupId)
                .child("All Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, "onDataChange: inside the reviews and ratings"+dataSnapshot);
                        int ratingsSum=0;
                        float totalRatings=0;
                        float avgRatings=0;
                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                            ratingsSum=ratingsSum+Integer.valueOf(snapshot.getValue().toString());
                            Log.i(TAG, "onDataChange: total"+ratingsSum);
                            totalRatings++;
                        }
                        if (totalRatings!=0){
                            avgRatings=ratingsSum/totalRatings;
                            Log.i(TAG, "onDataChange: the average rating of the current group is "+avgRatings);
                            ratingsRef
                                    .child(groupId)
                                    .child("Group Rating")
                                    .setValue(avgRatings)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i(TAG, "onSuccess: updating the group ratings");

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, "onFailure: error in updating the ratings"+e.getMessage());
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: error"+databaseError.getMessage());

                    }
                });
    }

    private void retrieveCurrentUserInfo() {
        usersRef.child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            currentUserName = (String) dataSnapshot.child("name").getValue();
                            currentUserImage = (String) dataSnapshot.child("image").getValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: error in retrieving the current user info"+databaseError.getMessage());
                        Log.i(TAG, "onCancelled:error in retrieving the current user info "+databaseError.getCode());
                        Log.i(TAG, "onCancelled: error in retrieving the current user info"+databaseError.getDetails());
                    }
                });
    }
}
