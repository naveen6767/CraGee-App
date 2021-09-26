package com.crageeApp.appbesocial.Home.Fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterCelebRecommendations;
import com.crageeApp.appbesocial.Interfaces.RecommendationInterface;
import com.crageeApp.appbesocial.Models.ModelCelebRecommendation;
import com.crageeApp.appbesocial.Models.ModelPosts;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class welcomeFragment extends Fragment implements RecommendationInterface {

    private View welcomeFragment;
    private RecyclerView recyclerViewRecommendations;
    private AdapterCelebRecommendations adapterCelebRecommendations;
    private List<ModelCelebRecommendation> celebRecommendationList;
    private NavController navController;
    private DatabaseReference mRootRef;
    private String currentUserId,currentUserName,currentUserImage;
    private static final String TAG = "welcomeFragment";
    public welcomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        welcomeFragment= inflater.inflate(R.layout.fragment_welcome, container, false);
        mRootRef=FirebaseDatabase.getInstance().getReference();
        currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        navController = Navigation.findNavController(getActivity(),R.id.navigation_host_fragment);
        celebRecommendationList=new ArrayList<>();
        recyclerViewRecommendations=(RecyclerView) welcomeFragment.findViewById(R.id.recommendations_RecyclerView);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        recyclerViewRecommendations.setLayoutManager(linearLayoutManager);
        adapterCelebRecommendations=new AdapterCelebRecommendations(getContext(),celebRecommendationList,
                this);
        loadRecommendations();
        retrieveUserDetails();
        return welcomeFragment;
    }

    private void retrieveUserDetails() {
        //here retrieve the user name and user profile image
        Query currentUserQuery=mRootRef.child("Users").orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentUserQuery.keepSynced(true);
        currentUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: datasnapshot from query"+dataSnapshot);

                currentUserName =(String) dataSnapshot.child(currentUserId).child("name").getValue();
                currentUserImage =(String) dataSnapshot.child(currentUserId).child("image").getValue();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadRecommendations() {

        mRootRef
                .child("Users")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: the snapshot is "+snapshot);

                for (DataSnapshot ds:snapshot.getChildren()){
                    Log.d(TAG, "onDataChange: "+ds);
                    ModelCelebRecommendation recommendation=ds.getValue(ModelCelebRecommendation.class);
                    celebRecommendationList.add(recommendation);
                }
                adapterCelebRecommendations.notifyDataSetChanged();
                recyclerViewRecommendations.setAdapter(adapterCelebRecommendations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    @Override
    public void onItemClicked(int position, String userUid) {
        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id",userUid);
        navController.navigate(R.id.action_global_accountProfileFragment,bundle);
    }

    @Override
    public void onFollowClicked(int position, String userUid, String name, String image, Button followButton) {
        HashMap<String, Object> followingMap = new HashMap<>();
        followingMap.put("name",name);
        followingMap.put("image",image);
        followingMap.put("uid",userUid);


        mRootRef
                .child("user_following")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(userUid)
                .setValue(followingMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        DatabaseReference followers_reference=FirebaseDatabase.getInstance().getReference();

                        HashMap<String, Object> followersMap = new HashMap<>();
                        followersMap.put("name",currentUserName);
                        followersMap.put("image",currentUserImage);
                        followersMap.put("uid",currentUserId);

                        mRootRef
                                .child("user_followers")
                                           .child(userUid)
                                            .child(currentUserId)
                                            .setValue(followersMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    followButton.setText("Following");
                                                    followButton.setBackgroundColor(Color.TRANSPARENT);
                                                    followButton.setTextColor(Color.BLACK);

                                                    //start a background thread
                                                    timelineThread timelineThread=new timelineThread();
                                                    timelineThread.execute(userUid);

                                                }
                                            });


                    }
                });


    }


    public class timelineThread extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: timeline thread is running ");
            String userId=strings[0];
            Log.d(TAG, "doInBackground: the value of user id is "+userId);
            updateTimeline(userId);
            return null;
        }
    }

    private void updateTimeline(String userId) {


        mRootRef
                .child("user_posts")
                .child(userId)
                .limitToLast(2)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            Log.d(TAG, "onDataChange: the value of ds is "+ds);
                            ModelPosts modelPosts=ds.getValue(ModelPosts.class);
                            HashMap<String, Object> timelineMap = new HashMap<>();
                            timelineMap.put("date",String.valueOf(modelPosts.getDate()));
                            timelineMap.put("pId",String.valueOf(modelPosts.getpId()));
                            timelineMap.put("pLikes",String.valueOf(modelPosts.getpLikes()));
                            timelineMap.put("pTime",String.valueOf(modelPosts.getpTime()));
                            timelineMap.put("postImage",String.valueOf(modelPosts.getPostImage()));
                            timelineMap.put("textPost",String.valueOf(modelPosts.getTextPost()));
                            timelineMap.put("time",String.valueOf(modelPosts.getTime()));
                            timelineMap.put("uid",String.valueOf(modelPosts.getUid()));
                            timelineMap.put("userName",String.valueOf(modelPosts.getUserName()));
                            timelineMap.put("userVerified",String.valueOf(modelPosts.getUserVerified()));
                            mRootRef
                                    .child("user_timeline")
                                    .child(currentUserId)
                                    .child(modelPosts.getpId())
                                    .setValue(timelineMap);
                        }
                    } 

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {



                    }
                });

    }
}