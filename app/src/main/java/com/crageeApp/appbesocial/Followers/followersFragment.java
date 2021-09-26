package com.crageeApp.appbesocial.Followers;

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

public class followersFragment extends Fragment implements followersInterface {

    private View followersFragment;
    private RecyclerView recyclerViewFollowers;
    private DatabaseReference userRef,mRootRef;
    private String currentUserId,visitingUserId;
    private AdapterFollowers adapterFollowers;
    private List<ModelFollower> followersList;
    private ImageButton backButton;
    private NavController navController;
    private static final String TAG = "followersFragment";
    public followersFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        followersFragment= inflater.inflate(R.layout.fragment_followers, container, false);
        navController = Navigation.findNavController(getActivity(),R.id.navigation_host_fragment);


        recyclerViewFollowers=followersFragment.findViewById(R.id.recyclerView_followers);
//        backButton=followersFragment.findViewById(R.id.back_button_followers);
        //setting the recycler view properties
        recyclerViewFollowers.setHasFixedSize(true);
//
        LinearLayoutManager manager = new LinearLayoutManager(followersFragment.getContext());
        recyclerViewFollowers.setLayoutManager(manager);
        followersList =new ArrayList<>();

        //init the fire base here
        userRef= FirebaseDatabase.getInstance().getReference("Users");
        mRootRef= FirebaseDatabase.getInstance().getReference();
        currentUserId= FirebaseAuth.getInstance().getUid();

        adapterFollowers=new AdapterFollowers(getContext(),followersList,this);

//        //Retrieve the value
//        visitingUserId= getArguments().getString("userId");
//
//        getFollowersList();
//
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Fragment myFragment=new followersFragment();
//                 getActivity().getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragmentContainerHome, myFragment).addToBackStack(null).commit();
//            }
//        });

//        followersViewModel= ViewModelProviders.of(this).get(followersViewModel.class);
//        followersViewModel.init();
//        followersViewModel.getFollowers().observe(getViewLifecycleOwner(), new Observer<ArrayList<ModelUsers>>() {
//            @Override
//            public void onChanged(ArrayList<ModelUsers> modelUsers) {
//                adapterFollower.notifyDataSetChanged();
//            }
//        });
//        adapterFollower =new AdapterFollower(followersViewModel.getFollowers().getValue());
//        recyclerViewFollowers.setAdapter(adapterFollower);
        loadFollowers();

        return followersFragment;
    }

    private void loadFollowers() {
        mRootRef
                .child("user_followers")
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            Log.d(TAG, "onDataChange: "+ds);
                            ModelFollower modelFollower=ds.getValue(ModelFollower.class);
                            followersList.add(modelFollower);
                        }
                        adapterFollowers.notifyDataSetChanged();
                        recyclerViewFollowers.setAdapter(adapterFollowers);
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
//    private void getFollowersList() {
//
//        mRootRef
//                .child("user_followers")
//                .child(visitingUserId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        followersList.clear();
//                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
//                            ModelFollowers modelFollowers=snapshot.getValue(ModelFollowers.class);
//                            followersList.add(modelFollowers);
//                        }
//                        getAllFollowers();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//
//
//    }
//
//
//    private void getAllFollowers() {
//        usersList.clear();
//        for (int i=0;i<followersList.size();i++){
//            Log.i(TAG, "no of Child in following list: "+followersList.size());
//            Log.i(TAG, "getUserPosts: started running for the "+i+"th time");
//            final String followersKeys=followersList.get(i).getId();
//            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    for (DataSnapshot ds:dataSnapshot.getChildren())
//                    {
//                        ModelUsers modelUsers =ds.getValue(ModelUsers.class);
//                        if (modelUsers.getUid().equals(followersKeys)){
//                            usersList.add(modelUsers);
//                        }
//
//                        //adapter
//                        adapterFollower =new adapterFollower(followersFragment.getContext(),usersList);
//
//                        //set adapter to the recycler view
//                        recyclerViewFollowers.setAdapter(adapterFollower);
//
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