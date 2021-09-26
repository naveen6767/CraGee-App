package com.crageeApp.appbesocial.Notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterUsers;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {


    private RecyclerView recyclerViewRequests;
    private String currentUserId;
    private DatabaseReference followRequestRef,usersRef;
    private AdapterUsers adapterUsers;
    private List<ModelUsers> usersList;
    private View view;
    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_requests, container, false);


        //init the recycler view
        recyclerViewRequests=view.findViewById(R.id.friendRequestsList);
        //init the fire base here
        currentUserId= FirebaseAuth.getInstance().getUid();
        followRequestRef= FirebaseDatabase.getInstance().getReference("Follow Request");
        usersRef= FirebaseDatabase.getInstance().getReference("Users");
        //init the group requests list
        usersList=new ArrayList<>();
        //setting the recycler view properties
        recyclerViewRequests.setHasFixedSize(true);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(getContext()));


        //get all group requests
        getAllFriendRequests();

        return view;
    }


    private void getAllFriendRequests() {

        followRequestRef
                .child(currentUserId)
                .orderByChild("request_type")
                .equalTo("received")
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
                            usersRef
                                    .child(requestSenderKey)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists())
                                    {
                                        ModelUsers modelUsers =dataSnapshot.getValue(ModelUsers.class);
                                        usersList.add(modelUsers);

                                        //adapter
                                        adapterUsers =new AdapterUsers(getContext(),usersList);

                                        //set adapter to the recycler view
                                        recyclerViewRequests.setAdapter(adapterUsers);
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
