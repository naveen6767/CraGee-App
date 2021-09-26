package com.crageeApp.appbesocial.Settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class RateFragment extends Fragment {
    private View rateFragmentView;
    private TextView aboutRating,thanksRating;
    private RatingBar ratingsBar;
    private DatabaseReference mRootRef,usersRef;
    private String currentUserId,currentUserName,currentUserImage;

    public RateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rateFragmentView= inflater.inflate(R.layout.fragment_rate, container, false);

        aboutRating=rateFragmentView.findViewById(R.id.about_rating);
        ratingsBar=rateFragmentView.findViewById(R.id.rating_bar_app);
        thanksRating=rateFragmentView.findViewById(R.id.rating_thanks);
        currentUserId=FirebaseAuth.getInstance().getUid();
        mRootRef= FirebaseDatabase.getInstance().getReference();
        usersRef= mRootRef.child("Users");
        retrieveCurrentUserInfo();

        ratingsBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float currentRating, boolean fromUser) {

                postUserRatings(currentRating);

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + rateFragmentView.getContext().getPackageName())));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + rateFragmentView.getContext().getPackageName())));
                }

            }
        });
        return rateFragmentView;
    }

    private void postUserRatings(float currentRating) {
        String pushId=mRootRef
                .child("appRatings")
                .child(currentUserId).push().getKey();
        HashMap<String, Object> ratingsMap = new HashMap<>();
        ratingsMap.put("rating",currentRating);
        ratingsMap.put("timeStamp", ServerValue.TIMESTAMP);
        ratingsMap.put("reviewerName",currentUserName);
        ratingsMap.put("reviewerImage",currentUserImage);
        ratingsMap.put("reviewerId", FirebaseAuth.getInstance().getUid());
        if (pushId != null) {
            mRootRef
                    .child("appRatings")
                    .child(pushId)
                    .updateChildren(ratingsMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            thanksRating.setVisibility(View.VISIBLE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
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

                    }
                });
    }

}
