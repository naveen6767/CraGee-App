package com.crageeApp.appbesocial.Notifications;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class NotificationsActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar notificationsActivityToolbar;
    private static final String TAG = "NotificationsActivity";
    private ImageButton notifyRequests;
    private String currentUserId;
    private FirebaseAuth userAuth;
    public static FragmentManager fragmentManager;
    private DatabaseReference followRequestRef,usersRef;

    private ImageButton likesButton,commentsButton,followsButton,requestsButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        //init the buttons here
        initializeViews();

        //set tool bar as the action bar
        setSupportActionBar(notificationsActivityToolbar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        likesButton.setOnClickListener(this);
        commentsButton.setOnClickListener(this);
        followsButton.setOnClickListener(this);
        requestsButton.setOnClickListener(this);

        //Initialize the fragment manager
        fragmentManager = getSupportFragmentManager();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerNotifications,new LikesFragment()).commit();
        RequestsTask requestsTask=new RequestsTask();
        requestsTask.execute(currentUserId);

    }
    private void initializeViews() {
        //initialize the tool bar
        notificationsActivityToolbar =findViewById(R.id.toolBar_notifications);

        likesButton =findViewById(R.id.likes_show);
        commentsButton =findViewById(R.id.comments_show);
        followsButton =findViewById(R.id.follows_show);
        requestsButton =findViewById(R.id.requests_show);
        notifyRequests=findViewById(R.id.requests_show_notify);
        userAuth= FirebaseAuth.getInstance();
        currentUserId=userAuth.getUid();
        followRequestRef= FirebaseDatabase.getInstance().getReference("Follow Request");
        usersRef= FirebaseDatabase.getInstance().getReference("Users");




    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.likes_show:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerNotifications,new LikesFragment()).commit();

                break;
            case R.id.comments_show:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerNotifications,new CommentsFragment()).commit();

                break;
            case  R.id.follows_show:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerNotifications,new FollowsFragment()).commit();

                break;
            case R.id.requests_show:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerNotifications,new RequestsFragment()).commit();
                break;
            default:
                break;
        }

    }


    class RequestsTask extends AsyncTask<String,String,String>{


        @Override
        protected String doInBackground(String... strings) {
            String userId=strings[0];

            followRequestRef
                    .child(userId)
                    .orderByChild("request_type")
                    .equalTo("received")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if (dataSnapshot.exists()){
                               Log.d(TAG, "onDataChange: "+dataSnapshot);
                               notifyRequests.setVisibility(View.VISIBLE);
                           }
                           else {
                               notifyRequests.setVisibility(View.INVISIBLE);
                           }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
            return null;
        }
    }
}
