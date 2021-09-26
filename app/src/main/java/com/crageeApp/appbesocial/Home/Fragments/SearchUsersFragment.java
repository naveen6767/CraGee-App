package com.crageeApp.appbesocial.Home.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterOnlineUsers;
import com.crageeApp.appbesocial.Adapters.AdapterUsers;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SearchUsersFragment extends Fragment {



    private RecyclerView recyclerViewUsers,searchRecyclerViewUsers;
    private AdapterUsers adapterUsers;
    private AdapterOnlineUsers adapterOnlineUsers;
    private List<ModelUsers> usersList;
    private RelativeLayout onlineUsesLayout;
    private View searchUsersFragment;
    private EditText search_new_users;

    public SearchUsersFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        searchUsersFragment= inflater.inflate(R.layout.fragment_search_users, container, false);
        //initialize the tool bar

        search_new_users =searchUsersFragment.findViewById(R.id.search_new_users);
        onlineUsesLayout =searchUsersFragment.findViewById(R.id.onlineUsersLayout);



        //initialize the recycler view
        recyclerViewUsers=searchUsersFragment.findViewById(R.id.users_recycler_view);
        searchRecyclerViewUsers=searchUsersFragment.findViewById(R.id.search_users_rv);
        //setting the recycler view properties
        recyclerViewUsers.setHasFixedSize(true);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        searchRecyclerViewUsers.setHasFixedSize(true);
        searchRecyclerViewUsers.setLayoutManager(new LinearLayoutManager(getActivity()));


        //init the user list
        usersList =new ArrayList<>();



        //get all users
        getAllUsers();

        //search listener
        search_new_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString())){
                    onlineUsesLayout.setVisibility(View.GONE);
                    searchRecyclerViewUsers.setVisibility(View.VISIBLE);


                    //search text contains text,search it
                    searchQueriedUser(s.toString());

                }else {
                    //search text is empty
                    usersList.clear();

                }

            }
        });
//        search_new_users.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                //called when user presses search button from the keyboard
//                //if search query is not empty then search
//                if (!TextUtils.isEmpty(query.trim())){
//
//                    //search text contains text,search it
//                    searchQueriedUser(query);
//
//                }else {
//
//                    //search text is empty
//                }
//                return false;
//            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                //called whenever user presses any single letter through keyboard
//                //if search query is not empty then search
//                if (!TextUtils.isEmpty(newText.trim())){
//                    onlineUsesLayout.setVisibility(View.GONE);
//                    searchRecyclerViewUsers.setVisibility(View.VISIBLE);
//
//
//                    //search text contains text,search it
//                    searchQueriedUser(newText);
//
//                }else {
//                    //search text is empty
//                    usersList.clear();
//
//                }
//                return false;
//            }
//        });
        
        
        
        return searchUsersFragment;

    }



    private void getAllUsers() {
        //get current user
        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get the path of the user database named"users"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from the path
        Query onlineUsersQuery=ref.orderByChild("onlineStatus").equalTo("online");
        onlineUsersQuery
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        usersList.clear();
                        for (DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            ModelUsers modelUsers =ds.getValue(ModelUsers.class);

                            //get all the user except the user signed in
                            if (!modelUsers.getUid().equals(fUser.getUid()))
                            {
                                usersList.add(modelUsers);
                            }

                            //adapter
                            adapterOnlineUsers =new AdapterOnlineUsers(getActivity(),usersList);
                            //set adapter to the recycler view
                            recyclerViewUsers.setAdapter(adapterOnlineUsers);

                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    //searching users
    private void searchQueriedUser(final String newText) {
        //get current user
        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get the path of the user database named"users"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        Query searchUser=ref.limitToFirst(10);
        //get all data from the path
        searchUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ModelUsers modelUsers =ds.getValue(ModelUsers.class);

                    if (!modelUsers.getUid().equals(fUser.getUid()))
                    {
                        if (modelUsers.getName().toLowerCase().contains(newText.toLowerCase()))
                        {
                            usersList.add(modelUsers);
                        }
                    }
                    //adapter
                    adapterUsers =new AdapterUsers(getActivity(),usersList);

                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();

                    //set adapter to the recycler view
                    searchRecyclerViewUsers.setAdapter(adapterUsers);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}