package com.crageeApp.appbesocial.Notifications;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterNotifications;
import com.crageeApp.appbesocial.Models.ModelNotifications;
import com.crageeApp.appbesocial.Posts.PostDetailsActivity;
import com.crageeApp.appbesocial.Posts.postDetailsFragment;
import com.crageeApp.appbesocial.Posts.postDetailsInterface;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LikesFragment extends Fragment implements postDetailsInterface {
    private View likesFragmentView;
    private RecyclerView notificationsRecycler;
    private AdapterNotifications adapterNotifications;
    private List<ModelNotifications> notificationsList;
    public static final String TAG = "Constraints";
    private String currentUserId;
    private FirebaseAuth userAuth;
    private DatabaseReference mRootRef;

    public LikesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        likesFragmentView=inflater.inflate(R.layout.fragment_likes, container, false);
        notificationsList=new ArrayList<>();
        userAuth= FirebaseAuth.getInstance();
        mRootRef=FirebaseDatabase.getInstance().getReference();
        currentUserId=userAuth.getUid();
        notificationsRecycler =likesFragmentView.findViewById(R.id.recyclerViewNotifications);
        notificationsRecycler.setHasFixedSize(true);
        notificationsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapterNotifications=new AdapterNotifications(getContext(), (ArrayList<ModelNotifications>) notificationsList,this);

        getAllNotifications();
        seenAllNotifications();
        return likesFragmentView;


    }

    private void seenAllNotifications() {
        Query notifySeenQuery=mRootRef
                                    .child("new_like_notifications")
                                    .child(currentUserId);
        notifySeenQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                    HashMap<String, Object> seenMap =new HashMap<>();
                    //put post info
                    seenMap.put("seen",true);

                    dataSnapshot
                            .getRef()
                            .updateChildren(seenMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {



                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAllNotifications() {

        Log.i(TAG, "getAllNotifications: getting all the notifications");

        mRootRef
                .child("like_notifications")
                .child(currentUserId)
                .orderByChild("timeStamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        notificationsList.clear();
                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                            Log.i(TAG, "onDataChange: "+snapshot);
                            //get data
                            ModelNotifications model=snapshot.getValue(ModelNotifications.class);
                            Log.i(TAG, "onDataChange: "+model.getNotifierName());
                            //add to list
                            notificationsList.add(model);
                            Log.i(TAG, "inside the notifications"+notificationsList);
                        }

                        Collections.reverse(notificationsList);
//                        //adapter
//                        adapterNotifications=new AdapterNotifications(getContext(), (ArrayList<ModelNotifications>) notificationsList,this);

                        //set adapter to the recycler view
                        adapterNotifications.notifyDataSetChanged();

                        notificationsRecycler.setAdapter(adapterNotifications);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @Override
    public void onItemClicked(int position) {


        postDetailsFragment fragment = new postDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userId","Naveen");
        bundle.putString("currentPostId","kumar");
        fragment.setArguments(bundle);
        Intent intentToPostDetails = new Intent(getContext(), PostDetailsActivity.class);
        intentToPostDetails.putExtra("userId",notificationsList.get(position).getNotifiedPostPublisher());
        intentToPostDetails.putExtra("currentPostId",notificationsList.get(position).getNotifiedPostId());
        getContext().startActivity(intentToPostDetails);

    }
}
