package com.crageeApp.appbesocial.Following;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterFollowers;
import com.crageeApp.appbesocial.Interfaces.followersInterface;
import com.crageeApp.appbesocial.Models.ModelFollower;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class followingFragment extends Fragment implements followersInterface {


    private View followingFragment;
    private RecyclerView recyclerViewFollowing;
    private DatabaseReference userRef,mRootRef;
    private String currentUserId,visitingUserId;
    private AdapterFollowers adapterFollowing;
    private List<ModelFollower> followingList;

    private ImageButton backButton;
    private NavController navController;
    private static final String TAG = "followingFragment";


    public followingFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        followingFragment= inflater.inflate(R.layout.fragment_following, container, false);
        navController = Navigation.findNavController(getActivity(),R.id.navigation_host_fragment);
        recyclerViewFollowing=followingFragment.findViewById(R.id.recyclerView_following);
//        backButton=followersFragment.findViewById(R.id.back_button_followers);
        //setting the recycler view properties
        recyclerViewFollowing.setHasFixedSize(true);
//
        LinearLayoutManager manager = new LinearLayoutManager(followingFragment.getContext());
        recyclerViewFollowing.setLayoutManager(manager);
        followingList =new ArrayList<>();

        //init the fire base here
        userRef= FirebaseDatabase.getInstance().getReference("Users");
        mRootRef= FirebaseDatabase.getInstance().getReference();
        currentUserId= FirebaseAuth.getInstance().getUid();

        adapterFollowing=new AdapterFollowers(getContext(),followingList,this);
//
//
//        //initialize the recycler view
//        recyclerViewFollowing=followingFragment.findViewById(R.id.recyclerView_following);
//        backButton=followingFragment.findViewById(R.id.back_button_following);
//        //setting the recycler view properties
//        recyclerViewFollowing.setHasFixedSize(true);
//        recyclerViewFollowing.setLayoutManager(new LinearLayoutManager(followingFragment.getContext()));
//
//        //init the user list
//        usersList =new ArrayList<>();
//        followingList =new ArrayList<>();
//
//        //init the fire base here
//        userRef= FirebaseDatabase.getInstance().getReference("Users");
//        mRootRef= FirebaseDatabase.getInstance().getReference();
//        currentUserId= FirebaseAuth.getInstance().getUid();
//
//
//        //Retrieve the value
//        visitingUserId= getArguments().getString("userId");
//        getFollowingList();
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Fragment myFragment=new followingFragment();
////                getActivity().getSupportFragmentManager().beginTransaction()
////                        .replace(R.id.fragmentContainerHome, myFragment).addToBackStack(null).commit();
//            }
//        });
//
        loadFollowing();

        return followingFragment;
        
    }

    private void loadFollowing() {
        mRootRef
                .child("user_following")
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            Log.d(TAG, "onDataChange: "+ds);
                            ModelFollower modelFollowing=ds.getValue(ModelFollower.class);
                            followingList.add(modelFollowing);
                        }
                        adapterFollowing.notifyDataSetChanged();
                        recyclerViewFollowing.setAdapter(adapterFollowing);
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

//
//    private void getFollowingList() {
//        mRootRef
//                .child("user_following")
//                .child(visitingUserId)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                followingList.clear();
//                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
//                    Log.i(TAG, "inside the following list"+snapshot);
//                    ModelFollowers modelFollowing=snapshot.getValue(ModelFollowers.class);
//                    Log.i(TAG, "inside the model followers"+modelFollowing.getId());
//                    followingList.add(modelFollowing);
//                }
//                Log.i(TAG, "outside the for loop"+followingList.size());
//                getAllFollowers();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//
//    }

//    private void getAllFollowers() {
//        //usersList.clear();
//        for (int i=0;i<followingList.size();i++){
//            Log.i(TAG, "no of Child in following list: "+followingList.size());
//            Log.i(TAG, "getUserPosts: started running for the "+i+"th time");
//            final String followersKeys=followingList.get(i).getId();
//            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for (DataSnapshot ds:dataSnapshot.getChildren())
//                    {
//                        Log.i(TAG, "inside the current user ds"+ds);
//                        ModelUsers users =ds.getValue(ModelUsers.class);
//                        Log.i(TAG, "inside the model users"+users);
//                        Log.i(TAG, "accessing the model users"+users.getUid());
//                        if (users.getUid().equals(followersKeys)){
//                            usersList.add(users);
//                        }
//
//
//                        //adapter
//                        adapterUsers =new AdapterUsers(followingFragment.getContext(),usersList);
//                        adapterUsers.notifyDataSetChanged();
//
//                        //set adapter to the recycler view
//                        recyclerViewFollowing.setAdapter(adapterUsers);
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });
//        }
//    }


}