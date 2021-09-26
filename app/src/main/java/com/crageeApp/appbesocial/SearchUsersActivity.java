package com.crageeApp.appbesocial;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterOnlineUsers;
import com.crageeApp.appbesocial.Adapters.AdapterUsers;
import com.crageeApp.appbesocial.Models.ModelUsers;
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

public class SearchUsersActivity extends AppCompatActivity {

    private Toolbar searchToolBar;
    private SearchView searchUsers;
    private RecyclerView recyclerViewUsers,searchRecyclerViewUsers;
    private AdapterUsers adapterUsers;
    private AdapterOnlineUsers adapterOnlineUsers;
    private List<ModelUsers> usersList;
    private RelativeLayout onlineUsesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        //initialize the tool bar
        searchToolBar =findViewById(R.id.tool_bar_search);

        searchUsers =findViewById(R.id.search_view_user);
        onlineUsesLayout =findViewById(R.id.onlineUsersLayout);


        //initialize the recycler view
        recyclerViewUsers=findViewById(R.id.users_recycler_view);
        searchRecyclerViewUsers=findViewById(R.id.search_users_rv);
        //setting the recycler view properties
        recyclerViewUsers.setHasFixedSize(true);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerViewUsers.setHasFixedSize(true);
        searchRecyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));


        //init the user list
        usersList =new ArrayList<>();



        //get all users
       getAllUsers();

        //set tool bar as the action bar
        setSupportActionBar(searchToolBar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        //search listener
        searchUsers.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user presses search button from the keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim())){

                    //search text contains text,search it
                    searchQueriedUser(query);

                }else {

                    //search text is empty
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                //called whenever user presses any single letter through keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(newText.trim())){
                    onlineUsesLayout.setVisibility(View.GONE);
                    searchRecyclerViewUsers.setVisibility(View.VISIBLE);


                    //search text contains text,search it
                    searchQueriedUser(newText);

                }else {
                    //search text is empty
                     usersList.clear();

                }
                return false;
            }
        });
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
                    adapterOnlineUsers =new AdapterOnlineUsers(SearchUsersActivity.this,usersList);
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
                    adapterUsers =new AdapterUsers(SearchUsersActivity.this,usersList);

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
