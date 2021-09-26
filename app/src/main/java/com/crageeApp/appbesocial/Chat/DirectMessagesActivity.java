package com.crageeApp.appbesocial.Chat;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterUsersForMessage;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DirectMessagesActivity extends AppCompatActivity {
    private Toolbar directMessageToolbar;
    private SearchView searchUsers;
    private RecyclerView recyclerViewUsers;
    private AdapterUsersForMessage adapterUsersForMessage;
    private List<ModelUsers> usersList;
    private String oldestUserId;
    private static final String TAG = "DirectMessagesActivity";
    private Boolean isScrolling=false;
    private LinearLayoutManager manager;
    private int totalItems,currentItems,scrollOutItems;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_messages);

        //init the tool bar here
        directMessageToolbar=findViewById(R.id.toolBar_direct_message);
        progressBar=findViewById(R.id.progressBarDM);
        //set tool bar as the action bar
        setSupportActionBar(directMessageToolbar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //init the search view here
        searchUsers =findViewById(R.id.searchViewUsers);
        //initialize the recycler view
        recyclerViewUsers=findViewById(R.id.recyclerViewFriendList);
        //setting the recycler view properties
        recyclerViewUsers.setHasFixedSize(true);
        manager=new LinearLayoutManager(this);
        recyclerViewUsers.setLayoutManager(manager);
        //init the user list
        usersList =new ArrayList<>();
        //get all users
        getAllUsers();

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
                    //search text contains text,search it
                    searchQueriedUser(newText);

                }else {
                    //search text is empty
                }
                return false;
            }
        });


        recyclerViewUsers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScrolling=true;
                }

            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                currentItems=manager.getChildCount();
                totalItems=manager.getItemCount();
                scrollOutItems=manager.findFirstVisibleItemPosition();
                Log.d(TAG, "onScrolled: current items is "+currentItems);
                Log.d(TAG, "onScrolled: total items is "+totalItems);
                Log.d(TAG, "onScrolled: scrolled items"+scrollOutItems);

                if (isScrolling&&(currentItems+scrollOutItems==totalItems)){
                    //data fetch
                    isScrolling=false;
                    fetchData();
                }
            }
        });
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);
        //here we will assume that data will fetched after 5sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //get current user
                final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
                //get the path of the user database named"users"
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                //get all data from the path
                Log.d(TAG, "run: oldest user id"+oldestUserId);
                ref.orderByKey()
                        .startAt(oldestUserId)
                        .limitToFirst(8)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            Log.d(TAG, "onDataChange: fetching the values"+ds);
                            ModelUsers modelUsers =ds.getValue(ModelUsers.class);
                            Log.i(TAG, "inside the model users: "+modelUsers);
                            //get all the user except the user signed in
                            if (!modelUsers.getUid().equals(FirebaseAuth.getInstance().getUid())&&!modelUsers.getUid().equals(oldestUserId))
                            {
                                usersList.add(modelUsers);
                            }

                            oldestUserId=ds.getKey();
                            //adapter
                            adapterUsersForMessage = new AdapterUsersForMessage(DirectMessagesActivity.this,usersList);
                            adapterUsersForMessage.notifyDataSetChanged();
                        }

                        progressBar.setVisibility(View.GONE);



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
        },2000);
    }

    private void getAllUsers() {
        //get current user
        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get the path of the user database named"users"
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from the path

        //GETTING FIRST 10 RECORDS FROM THE FIREBASE HERE
        ref
                .limitToFirst(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ModelUsers modelUsers =ds.getValue(ModelUsers.class);
                    Log.i(TAG, "inside the model users: "+modelUsers);
                    //get all the user except the user signed in
                    if (!modelUsers.getUid().equals(FirebaseAuth.getInstance().getUid()))
                    {
                        usersList.add(modelUsers);
                    }

                    oldestUserId=ds.getKey();
                    Log.d(TAG, "onDataChange: oldest id "+oldestUserId);
                    //adapter
                    adapterUsersForMessage = new AdapterUsersForMessage(DirectMessagesActivity.this,usersList);

                    //set adapter to the recycler view
                    recyclerViewUsers.setAdapter(adapterUsersForMessage);
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
        //get all data from the path
        ref.addValueEventListener(new ValueEventListener() {
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
                    adapterUsersForMessage = new AdapterUsersForMessage(DirectMessagesActivity.this,usersList);


                    //refresh adapter
                    adapterUsersForMessage.notifyDataSetChanged();

                    //set adapter to the recycler view
                    recyclerViewUsers.setAdapter(adapterUsersForMessage);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
