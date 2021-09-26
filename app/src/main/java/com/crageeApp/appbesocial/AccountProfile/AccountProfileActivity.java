package com.crageeApp.appbesocial.AccountProfile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterPosts;
import com.crageeApp.appbesocial.Chat.UserChatActivity;
import com.crageeApp.appbesocial.Models.ModelFollowers;
import com.crageeApp.appbesocial.Models.ModelPosts;
import com.crageeApp.appbesocial.Posts.FollowersActivity;
import com.crageeApp.appbesocial.Posts.FollowingActivity;
import com.crageeApp.appbesocial.ProgressButton;
import com.crageeApp.appbesocial.R;
import com.crageeApp.appbesocial.Ratings.ProfileRatingsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountProfileActivity extends AppCompatActivity implements View.OnClickListener   {
    private CircleImageView userProfileImage;
    private TextView account_Name,toolBarAccountName,currentAccountBio,currentUsrPosts,
            currentUserFollowers,currentUserFollowing,profileRatingsText, verification,editReview;
    private Button confirmBtn,deleteBtn;
    private ImageButton accountOptionsButton;
    private DatabaseReference usersReference,followRequestRef,contactsReference,reportsReference,specialRef,mRootRef;
    private FirebaseAuth userAuth;
    private Toolbar accountActivityToolbar;
    private List<String> noFollowers;
    private List<String> noFollowing;
    private LinearLayout requestShowLayout;
    private RecyclerView recyclerViewUsersPosts;
    private List<ModelPosts> postsList;
    private AdapterPosts adapterPosts;
    private String accountCategory,timeStamp,profileName,profileImage,
            receiverUserId,senderUserId,currentPrivacy,celebrityCategory;
    private static final String TAG = "AccountProfile";
    private RatingBar ratingBarGive,ratingBarSet;
    private float ratingsGiven;
    private View sendMessageBtn,followButton;
    private ImageView accountTick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_profile);

        //initialize all the views here
        initializeAllViews();
        userAuth = FirebaseAuth.getInstance();
        usersReference= FirebaseDatabase.getInstance().getReference().child("Users");
        usersReference.keepSynced(true);
        specialRef= FirebaseDatabase.getInstance().getReference().child("Special");
        mRootRef= FirebaseDatabase.getInstance().getReference();
        reportsReference= FirebaseDatabase.getInstance().getReference().child("Reports");
        followRequestRef= FirebaseDatabase.getInstance().getReference().child("Follow Request");
        contactsReference= FirebaseDatabase.getInstance().getReference().child("Contacts");
        //set tool bar as the action bar
        setSupportActionBar(accountActivityToolbar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getting the intent of the user id that has been sent to the account profile activity
        /*
         *here senderUserId is current user which is logged in
         * receiverUserId is another user whose profile is being opened
         */
        receiverUserId= getIntent().getStringExtra("visit_user_id");
        senderUserId =userAuth.getUid();
        LinearLayoutManager layoutManager=new LinearLayoutManager(AccountProfileActivity.this);
        //show the newest post first ,for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recycler view
        recyclerViewUsersPosts.setLayoutManager(layoutManager);
        recyclerViewUsersPosts.setNestedScrollingEnabled(false);
        timeStamp =String.valueOf(System.currentTimeMillis());

        //now we will retrieve the user info in the profile activity
       // retrieveAccountInfo();


        currentUserFollowers.setOnClickListener(this);
        currentUserFollowing.setOnClickListener(this);
        accountOptionsButton.setOnClickListener(this);
        followButton.setOnClickListener(this);
        sendMessageBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
        verification.setOnClickListener(this);
        editReview.setOnClickListener(this);
        ratingBarGive.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                sendUserToRatingsActivity(rating);
            }
        });

        ProgressButton progressButtonMessage =new ProgressButton(AccountProfileActivity.this,sendMessageBtn);
        progressButtonMessage.buttonSendMessage();
        ProgressButton progressButtonFollow =new ProgressButton(AccountProfileActivity.this,followButton);
      //  progressButtonFollow.buttonFollow();
        MyTask myTask=new MyTask();
        myTask.execute(receiverUserId);
        RatingTask ratingTask=new RatingTask();
        ratingTask.execute();
        checkRequestTask checkRequestTask=new checkRequestTask();
        checkRequestTask.execute();
        //check whether the current user is following the visiting user or not
        checkFollowingStatus(progressButtonFollow);
    }
    class MyTask extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... strings) {
            usersReference
                    .child(receiverUserId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG, "onDataChange: inside the current user data ");
                            if (dataSnapshot.exists()){
                                String getAccountChoice = (String) dataSnapshot.child("accountChoice").getValue();
                                currentPrivacy = (String) dataSnapshot.child("accountPrivacy").getValue();
                                accountCategory = (String) dataSnapshot.child("accountCategory").getValue();
                                celebrityCategory = (String) dataSnapshot.child("celebrityCategory").getValue();
                                profileName = (String) dataSnapshot.child("name").getValue();
                                profileImage = (String) dataSnapshot.child("image").getValue();
                                String requestNoOfPosts= String.valueOf(dataSnapshot.child("noPosts").getValue());
                                checkVerificationStatus(dataSnapshot);
                                toolBarAccountName.setText(profileName);
                                account_Name.setText(profileName);
                                currentUsrPosts.setText(requestNoOfPosts);
                                try {
                                    Picasso.get().load(profileImage).into(userProfileImage);

                                }catch (Exception e)
                                {
                                    Picasso.get().load(R.drawable.profile_image).into(userProfileImage);
                                }
                                if (dataSnapshot.hasChild("userBio")){
                                    currentAccountBio.setVisibility(View.VISIBLE);
                                    String userStatus =(String)  dataSnapshot.child("userBio").getValue();
                                    currentAccountBio.setText(userStatus);
                                }
                                else {
                                    currentAccountBio.setText("");
                                    currentAccountBio.setVisibility(View.GONE);
                                }
                                countFollowers();
                                countFollowing();
                                if (celebrityCategory.equals("Original")){
                                    accountTick.setVisibility(View.VISIBLE);
                                }
                                else {
                                    accountTick.setVisibility(View.GONE);
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


            return null;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: the value of s is"+s);
        }
    }
    class RatingTask extends AsyncTask<String,String,String>{

        @Override
        protected String doInBackground(String... strings) {

            setRatingsBar();
            return null;
        }

    }
    private void checkVerificationStatus(DataSnapshot dataSnapshot) {
        if (dataSnapshot.child("Verification").hasChild("verification status")){
            String userVerification = (String) dataSnapshot.child("Verification").child("verification status").getValue();
            if (userVerification != null && userVerification.equals("Verified")) {
                verification.setText(getResources().getString(R.string.verification_verified));
            }
            else if (userVerification != null && userVerification.equals("Rejected")){
                verification.setText(getResources().getString(R.string.verification_rejected));
            }
            else {
                verification.setText(getResources().getString(R.string.verification_pending));
            }
        }

    }
    private void setRatingsBar() {
        usersReference
                .child(receiverUserId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("followers").hasChild(senderUserId)){
                    Log.d(TAG, "onDataChange: the current user is following the other user");
                    /*here check if the sender user has already rated the receiver user
                     *if the current user has already rated the other user
                     * show the rating given by the current user and options to edit the user
                     * otherwise give the other text to rate the other user
                     */
                    usersReference.child(receiverUserId)
                            .child("All Ratings")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        if(dataSnapshot.hasChild(senderUserId)){
                                            Log.d(TAG, "onDataChange: current user has already rated the receiver user");
                                            //current user has already rated the receiver user
                                            ratingsGiven= Float.parseFloat(dataSnapshot.child(senderUserId).getValue().toString());
                                            ratingBarSet.setVisibility(View.VISIBLE);
                                            ratingBarSet.setRating(ratingsGiven);
                                            ratingBarSet.setIsIndicator(true);
                                            ratingBarGive.setVisibility(View.GONE);
                                            profileRatingsText.setText(getResources().getString(R.string.your_Ratings));
                                            editReview.setVisibility(View.VISIBLE);
                                        }
                                        else {
                                            profileRatingsText.setText(getResources().getString(R.string.rate_user));
                                            ratingBarGive.setVisibility(View.VISIBLE);
                                            ratingBarSet.setVisibility(View.GONE);
                                        }
                                    }
                                    else {
                                        profileRatingsText.setText(getResources().getString(R.string.rate_user));
                                        ratingBarGive.setVisibility(View.VISIBLE);
                                        ratingBarSet.setVisibility(View.GONE);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }
                else {
                    Log.d(TAG, "onDataChange: current user is not following the other user");
                    Query userQuery=usersReference.orderByChild("uid").equalTo(receiverUserId);
                    userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds:dataSnapshot.getChildren()){
                                float accountRatings = Float.parseFloat((ds.child("ratings").getValue()).toString());
                                ratingBarSet.setVisibility(View.VISIBLE);
                                ratingBarSet.setRating(accountRatings);
                                ratingBarSet.setIsIndicator(true);
                                ratingBarGive.setVisibility(View.GONE);
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
    private void sendUserToRatingsActivity(float rating) {
        Intent intentToRatings = new Intent(AccountProfileActivity.this, ProfileRatingsActivity.class);
        intentToRatings.putExtra("rating",rating);
        intentToRatings.putExtra("profile id",receiverUserId);
        startActivity(intentToRatings);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }
    private void countFollowing() {
        usersReference
                .child(receiverUserId)
                .child("following")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noFollowing.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelFollowers modelFollowers=snapshot.getValue(ModelFollowers.class);
                    noFollowing.add(modelFollowers.toString());
                    String nOOfFollowing=String.valueOf(noFollowing.size());
                    currentUserFollowing.setText(nOOfFollowing);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void countFollowers() {
        usersReference.child(receiverUserId).child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noFollowers.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelFollowers modelFollowers=snapshot.getValue(ModelFollowers.class);
                    noFollowers.add(modelFollowers.toString());
                    String nOOfFollowers=String.valueOf(noFollowers.size());
                    currentUserFollowers.setText(nOOfFollowers);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void checkRequest() {
        followRequestRef
                .child(senderUserId)
                .child(receiverUserId)
                .child("request_type")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()&&dataSnapshot.getValue().equals("received")){
                            Log.d(TAG, "onDataChange: other user has already sent the request");
                            requestShowLayout.setVisibility(View.VISIBLE);
                            Log.d(TAG, "onDataChange: request layout has been shown");

                        }
                        else {
                            Log.d(TAG, "onDataChange: current user has not received any request from the other user");
                            requestShowLayout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: error in getting the request from the other to the current user"+databaseError.getMessage()+databaseError.getDetails());

                    }
                });
    }

    private void checkFollowingStatus(final ProgressButton progressButtonFollow) {
        /*
         *here first check whether the current user has blocked the other user
         * if not,check whether the current user is following other user or not
         */
        usersReference
                .child(senderUserId)
                .child("Blocked Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId)){
                            Log.d(TAG, "onDataChange: current user has blocked the other user");
                             progressButtonFollow.buttonUnblock();
                        }else {
                            Log.d(TAG, "onDataChange: current user has not blocked the other user");
                            Log.d(TAG, "onDataChange:now checking if the current user is following the other user");

                            usersReference
                                    .child(senderUserId)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.child("following").hasChild(receiverUserId)&&dataSnapshot.child("followers").hasChild(receiverUserId)){
                                                Log.d(TAG, "onDataChange: current user and other user both are following each other");
                                                progressButtonFollow.buttonFollowing();
                                                loadUserPosts();
                                            }
                                            else{
                                                Log.d(TAG, "onDataChange: checking if the current user is following the other user");
                                                if (dataSnapshot.child("following").hasChild(receiverUserId)){
                                                    Log.d(TAG, "onDataChange: current user is following the other user");

                                                    progressButtonFollow.buttonFollowing();

                                                    Log.d(TAG, "onDataChange: as the current user is following the other user so current user can rate the other user ");

                                                    loadUserPosts();
                                                    Log.d(TAG, "onDataChange: now checking if the current user is followed by the other user");
                                                    Log.d(TAG, "onDataChange: other user is the follower of the current  user");

                                                }
                                                else {
                                                  //  recyclerViewUsersPosts.setVisibility(View.GONE);
                                                    Log.d(TAG, "onDataChange: current user is not following the other user");
                                                    /*
                                                     *current user is not following the other user
                                                     *now check whether the current user has already sent the follow request to other user
                                                     * if the current user has already sent the follow request
                                                     * change the text to "requested"
                                                     */
                                                    followRequestRef
                                                            .child(senderUserId)
                                                            .addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.exists()){
                                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                                            if (Objects.equals(dataSnapshot.child(receiverUserId)
                                                                                    .child("request_type")
                                                                                    .getValue(), "sent")){
                                                                                Log.d(TAG, "onDataChange: current user has sent the request");
                                                                                Log.d(TAG, "onDataChange: value of request type is " +dataSnapshot.getValue());
                                                                                progressButtonFollow.buttonRequested();
                                                                            }
                                                                            else {
                                                                                Log.d(TAG, "onDataChange: request type is not equal to sent");
                                                                                progressButtonFollow.buttonFollow();
                                                                            }
                                                                        }
                                                                    }
                                                                    else {
                                                                        Log.d(TAG, "onDataChange: data snapshot does not exist regarding the follow request");
                                                                        Log.d(TAG, "onDataChange: current user has not sent the request");
                                                                        progressButtonFollow.buttonFollow();
                                                                    }
                                                                }
                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                    Log.d(TAG, "onCancelled: database error"+databaseError.getMessage());
                                                                    Log.d(TAG, "onCancelled: database error"+databaseError.getCode());
                                                                    Log.d(TAG, "onCancelled: database error"+databaseError.getDetails());

                                                                }
                                                            });
                                                }
                                                Log.d(TAG, "onDataChange: checking if the current user is followed by the other user");
                                                if (dataSnapshot.child("followers").hasChild(receiverUserId)){
                                                    Log.d(TAG, "onDataChange: checking for the follow back");
                                                    Log.d(TAG, "onDataChange: current user is followed by the other user i.e. other user is follower of the current user");

                                                    progressButtonFollow.buttonFollowBack();

                                                }
                                                else {
                                                    Log.d(TAG, "onDataChange: the other user is not follower of the current user");
                                                    /*
                                                     *if the other user is not the follower of the current user
                                                     * check whether the other user has sent the follow request to the current user
                                                     * if yes,show the request to the current user
                                                     * button text can be determined by the ............
                                                     */
                                                    checkRequest();
                                                }
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

    private void loadUserPosts() {
        recyclerViewUsersPosts.setVisibility(View.VISIBLE);
        //get all the data from the post reference i.e. postRef
        mRootRef
                .child("Users Posts")
                .child(receiverUserId)
                .orderByChild("pTime")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                Log.d(TAG, "onDataChange: the value of data snapshot is fpr profile iss "+dataSnapshot);
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ModelPosts modelPosts =ds.getValue(ModelPosts.class);
                    postsList.add(modelPosts);

                    //adapter
                    adapterPosts =new AdapterPosts(AccountProfileActivity.this,postsList);
                    adapterPosts.notifyDataSetChanged();
                    //set adapter to recycler view
                    recyclerViewUsersPosts.setAdapter(adapterPosts);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error
                Toast.makeText(AccountProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void initializeAllViews() {
        userProfileImage=findViewById(R.id.visit_profile_image);
        toolBarAccountName=findViewById(R.id.accountNameToolBar);
        account_Name=findViewById(R.id.account_Name);
        currentAccountBio=findViewById(R.id.visit_user_status);
        sendMessageBtn=findViewById(R.id.sendMessageButton);
        currentUsrPosts=findViewById(R.id.no_user_posts);
        currentUserFollowers=findViewById(R.id.no_user_followers);
        currentUserFollowing=findViewById(R.id.no_user_following);
        accountActivityToolbar=findViewById(R.id.tool_bar_profile);
        accountOptionsButton=findViewById(R.id.accountOptions);
        requestShowLayout=findViewById(R.id.request_layout);
        confirmBtn=findViewById(R.id.btnConfirm);
        deleteBtn=findViewById(R.id.btnDelete);
        verification=findViewById(R.id.verifyUser);
        editReview=findViewById(R.id.edit_profile_review);
        ratingBarGive=findViewById(R.id.give_profile_ratings);
        ratingBarSet=findViewById(R.id.set_profile_ratings);
        profileRatingsText=findViewById(R.id.profile_ratings_text);
        followButton=findViewById(R.id.buttonFollowProfile);
        accountTick=findViewById(R.id.account_category);
        //initialize post list
        postsList =new ArrayList<>();
        noFollowers=new ArrayList<>();
        noFollowing=new ArrayList<>();

        //init the recycler view here
        recyclerViewUsersPosts=findViewById(R.id.rVUsersPosts);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.no_user_followers:
                //if the current user is following the other user
                sendUserToFollowerActivity();
                break;
            case R.id.no_user_following:
                //if the current user is following the other user
                sendUserToFollowingActivity();
                break;
            case R.id.buttonFollowProfile:
                final ProgressButton progressButtonFollow =new ProgressButton(AccountProfileActivity.this,followButton);
                progressButtonFollow.buttonActivated();
                /*
                *upon clicking the button, progress button will be activated
                * current state will be managed
                 */
                /*
                *first check whether the current user is following the other user or not
                * or requested to follow
                * if not following then follow as per privacy of the other user
                 */
                Query followQuery=usersReference
                        .child(receiverUserId)
                        .child("followers")
                        .orderByChild("id")
                        .equalTo(senderUserId);
                followQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Log.d(TAG, "onDataChange: the current user is follower of the other user");
                            /*
                            *here the current user is follower of the other user
                            * show a dialog box to unfollow or not
                             */
                            progressButtonFollow.closeButton();
                            //show a alert dialog
                            AlertDialog.Builder unFollowBuilder = new AlertDialog
                                    .Builder(AccountProfileActivity.this);
                            //views to set in the dialog
                            //set layout linear layout
                            LinearLayout linearLayout =new LinearLayout(AccountProfileActivity.this);
                            TextView dialogText = new TextView(AccountProfileActivity.this);
                            dialogText.setText(getResources().getString(R.string.unfollow_quote));
                            dialogText.setTextColor(ContextCompat.getColor(AccountProfileActivity.this, R.color.black));
                            linearLayout.addView(dialogText);
                            linearLayout.setPadding(5,5,5,5);
                            unFollowBuilder.setView(linearLayout);
                            unFollowBuilder.setPositiveButton(getResources().getString(R.string.unFollow), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "unFollowUser: ");
                                    usersReference
                                            .child(senderUserId)
                                            .child("following")
                                            .child(receiverUserId)
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    usersReference
                                                            .child(receiverUserId)
                                                            .child("followers")
                                                            .child(senderUserId)
                                                            .removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    //current user has unFollowed the  other user
                                                                    Log.d(TAG, "current user has unFollowed the  other user");
                                                                    progressButtonFollow.buttonFollow();
                                                                    recyclerViewUsersPosts.setVisibility(View.GONE);

                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "onFailure: "+e.getMessage());
                                                        }
                                                    });

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "onFailure: "+e.getMessage());
                                        }
                                    });

                                }
                            });
                            unFollowBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //dismiss the dialog
                                    dialog.dismiss();
                                }
                            });
                            unFollowBuilder.create().show();

                        }
                        else {
                            Log.d(TAG, "onDataChange: current user is not following the other user");
                            /*
                            * first check if the current user has sent the friend request
                            * if yes, on clicking the button cancel the friend request
                            send request if private account
                            follow if public account

                             */
                            followRequestRef
                                    .child(senderUserId)
                                    .child(receiverUserId)
                                    .child("request_type")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()&&dataSnapshot.getValue().equals("sent")){
                                                Log.d(TAG, "onDataChange:current user has requested other user to follow");
                                                /*
                                                now clicking the follow button will cancel the request

                                                 */
                                                    followRequestRef
                                                            .child(senderUserId)
                                                            .child(receiverUserId)
                                                            .child("request_type")
                                                            .removeValue()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.d(TAG, "onSuccess: follow request sent successfully");
                                                                    followRequestRef
                                                                            .child(receiverUserId)
                                                                            .child(senderUserId)
                                                                            .child("request_type")
                                                                            .removeValue()
                                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {
                                                                                    Log.d(TAG, "onSuccess: follow request received successfully");
                                                                                    progressButtonFollow.buttonFollow();
                                                                                }
                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Log.d(TAG, "onFailure: received request failed"+e.getMessage());
                                                                        }
                                                                    });
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d(TAG, "onFailure: sent request failed"+e.getMessage());
                                                        }
                                                    });


                                            }
                                            else {
                                                Log.d(TAG, "onDataChange:current user has not requested other user to follow");
                                                ProgressButton progressButtonFollow=new ProgressButton(AccountProfileActivity.this,followButton);
                                                followUser(progressButtonFollow);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.d(TAG, "onCancelled: error in getting the request from the other to the current user"+databaseError.getMessage()+databaseError.getDetails());

                                        }
                                    });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                break;
            case R.id.accountOptions:
                /*
                 *first check whether the current user has blocked the other user or not
                 * if already blocked then unblock and show unblock dialog
                 * if not blocked then show the block dialog
                 */
                usersReference
                        .child(senderUserId)
                        .child("Blocked Users")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(receiverUserId)){
                                    Log.d(TAG, "onDataChange: current user has already blocked the user");
                                    //show the unblock dialog to the user
                                    //show a alert dialog

                                    //options to show in the dialog box
                                    String[] options = {"Report...", "Unblock", "Restrict"};
                                    //alert dialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AccountProfileActivity.this);
                                    //set items to dialog
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //handle dialog items click
                                            if (which==0){
                                                //Report clicked
                                                String[] ReportOptions = {"It's spam", "It's inappropriate"};
                                                AlertDialog.Builder ReportsBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                ReportsBuilder.setTitle("Why are you reporting this account?");
                                                ReportsBuilder.setItems(ReportOptions, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        //handle dialog click of reports
                                                        if (which==0){
                                                            //It's spam is clicked
                                                            final AlertDialog.Builder spamBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                            spamBuilder.setIcon(R.drawable.thank_icon);
                                                            spamBuilder.setTitle("Thanks for letting us know");
                                                            spamBuilder.setMessage("Your feedback is important in helping us keep the CraGee App community safe.");
                                                            reportsReference.child(senderUserId).child("Reported")
                                                                    .child(receiverUserId).setValue(true)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            spamBuilder.create().show();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(AccountProfileActivity.this, ""+e
                                                                            .getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                        else if (which==1){
                                                            //It's inappropriate is clicked
                                                            String[] inappropriateOptions = {"Report Post Message or Comment", "Report Account"};
                                                            AlertDialog.Builder inappropriateBuilder = new AlertDialog
                                                                    .Builder(AccountProfileActivity.this);
                                                            inappropriateBuilder.setTitle("Why are you reporting this account?");
                                                            inappropriateBuilder.setItems(inappropriateOptions,
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            //handle dialog click of reports
                                                                            if (which==0){
                                                                                //Report Post clicked
                                                                                AlertDialog.Builder reportPostBuilder = new AlertDialog
                                                                                        .Builder(AccountProfileActivity.this);
                                                                                reportPostBuilder.setIcon(R.drawable.thank_icon);
                                                                                reportPostBuilder.setTitle("Thank you for your response");
                                                                                reportPostBuilder.setMessage("You can report any post from the post options Button");
                                                                                reportPostBuilder.create().show();

                                                                            }else if (which==1){
                                                                                //Report Account clicked
                                                                                String[] reportAccountOptions = {"It's posting content that shouldn't be on CraGee App",
                                                                                        "It's pretending to be someone else", "It may be under the age of 13"};
                                                                                AlertDialog.Builder reportAccountBuilder = new AlertDialog
                                                                                        .Builder(AccountProfileActivity.this);
                                                                                reportAccountBuilder.setTitle("Why are you reporting this account?");
                                                                                reportAccountBuilder.setItems(reportAccountOptions, new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        //handle dialog click
                                                                                        if (which==0){
                                                                                            AlertDialog.Builder postContentBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                                                            postContentBuilder.setIcon(R.drawable.thank_icon);
                                                                                            postContentBuilder.setTitle("Thanks for letting us know");
                                                                                            postContentBuilder.setMessage("Posted content by the user will be check and removed if it voilates our policies");
                                                                                            postContentBuilder.create().show();
                                                                                        }
                                                                                        else if (which==1){
                                                                                            AlertDialog.Builder pretendBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                                                            pretendBuilder.setIcon(R.drawable.thank_icon);
                                                                                            pretendBuilder.setTitle("Thanks for letting us know");
                                                                                            pretendBuilder.setMessage("User's profile will be reviewed and verified");
                                                                                            pretendBuilder.create().show();
                                                                                        }
                                                                                        else if (which==2){
                                                                                            AlertDialog.Builder ageBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                                                            ageBuilder.setIcon(R.drawable.thank_icon);
                                                                                            ageBuilder.setTitle("Thanks for letting us know");
                                                                                            ageBuilder.setMessage("User'age will be verified and removed if user's age is under 13");
                                                                                            ageBuilder.create().show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                                reportAccountBuilder.create().show();
                                                                            }
                                                                        }
                                                                    });
                                                            inappropriateBuilder.create().show();

                                                        }


                                                    }
                                                });
                                                //create and show the dialog box of Reports
                                                ReportsBuilder.create().show();

                                            }else if (which==1){
                                                //unblock clicked
                                                /*
                                                 *add the user id of user which is to be blocked in the database of current user node
                                                 */
                                                AlertDialog.Builder unblockBuilder = new AlertDialog
                                                        .Builder(AccountProfileActivity.this);
                                                //views to set in the dialog
                                                unblockBuilder.setTitle("Unblock User").setIcon(R.drawable.unblock_icon);
                                                //set layout linear layout
                                                LinearLayout linearLayout =new LinearLayout(AccountProfileActivity.this);

                                                TextView dialogText = new TextView(AccountProfileActivity.this);
                                                dialogText.setText(getResources().getString(R.string.unblock_text));
                                                dialogText.setTextColor(ContextCompat.getColor(AccountProfileActivity.this, R.color.dark_green));
                                                linearLayout.addView(dialogText);
                                                linearLayout.setPadding(5,5,5,5);
                                                unblockBuilder.setView(linearLayout);
                                                unblockBuilder.setPositiveButton("Unblock", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        usersReference.child(senderUserId)
                                                                .child("Blocked Users")
                                                                .child(receiverUserId)
                                                                .child("uid")
                                                                .removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d(TAG, "user unblocked");

                                                                        usersReference.child(receiverUserId)
                                                                                .child("Blocked By")
                                                                                .child(senderUserId)
                                                                                .child("uid")
                                                                                .removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.d(TAG, "user removed from the blocked by node of the user");

                                                                                        AlertDialog.Builder unblockSuccessBuilder = new AlertDialog
                                                                                                .Builder(AccountProfileActivity.this);
                                                                                        //views to set in the dialog
                                                                                        unblockSuccessBuilder.setTitle("User Blocked").setIcon(R.drawable.thank_icon);
                                                                                        //set layout linear layout
                                                                                        LinearLayout linearLayout =new LinearLayout(AccountProfileActivity.this);

                                                                                        TextView dialogText = new TextView(AccountProfileActivity.this);
                                                                                        dialogText.setText("You can block them anytime from their profile");
                                                                                        dialogText.setTextColor(ContextCompat.getColor(AccountProfileActivity.this, R.color.red));
                                                                                        linearLayout.addView(dialogText);
                                                                                        linearLayout.setPadding(5,5,5,5);
                                                                                        unblockSuccessBuilder.setView(linearLayout);
                                                                                        unblockSuccessBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(DialogInterface dialog, int which) {

                                                                                                Log.d(TAG, "onClick: user will   be able to see the current users posts");
                                                                                             /*   finish();
                                                                                                overridePendingTransition(0, 0);
                                                                                                startActivity(getIntent());
                                                                                                overridePendingTransition(0, 0);
                                                                                                dialog.dismiss();

                                                                                              */
                                                                                            }
                                                                                        });
                                                                                        unblockSuccessBuilder.create().show();

                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Log.d(TAG, "onFailure: failure in adding in the blocked by list"+e
                                                                                        .getMessage());


                                                                            }
                                                                        });


                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "User blocking Failed"+e.getMessage());
                                                                Toast.makeText(AccountProfileActivity.this, "User blocking Failed"+e
                                                                        .getMessage(), Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
                                                        });

                                                    }
                                                });
                                                // button cancel
                                                unblockBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //dismiss the dialog
                                                        dialog.dismiss();
                                                    }
                                                });
                                                unblockBuilder.create().show();
                                            }else if (which==2){
                                                //Restrict clicked
                                            }
                                        }
                                    });
                                    //create and show the dialog box
                                    builder.create().show();



                                }else {
                                    Log.d(TAG, "onDataChange: current user has not blocked the user yet");
                                    //show the blocked user data
                                    //show a alert dialog

                                    //options to show in the dialog box
                                    String[] options = {"Report...", "Block", "Restrict"};
                                    //alert dialog

                                    AlertDialog.Builder builder = new AlertDialog.Builder(AccountProfileActivity.this);
                                    //set items to dialog

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //handle dialog items click
                                            if (which==0){
                                                //Report clicked
                                                String[] ReportOptions = {"It's spam", "It's inappropriate"};
                                                AlertDialog.Builder ReportsBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                ReportsBuilder.setTitle("Why are you reporting this account?");
                                                ReportsBuilder.setItems(ReportOptions, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        //handle dialog click of reports
                                                        if (which==0){
                                                            //It's spam is clicked
                                                            final AlertDialog.Builder spamBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                            spamBuilder.setIcon(R.drawable.thank_icon);
                                                            spamBuilder.setTitle("Thanks for letting us know");
                                                            spamBuilder.setMessage("Your feedback is important in helping us keep the CraGee App community safe.");
                                                            reportsReference.child(senderUserId).child("Reported")
                                                                    .child(receiverUserId).setValue(true)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            spamBuilder.create().show();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(AccountProfileActivity.this, ""+e
                                                                            .getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                        }
                                                        else if (which==1){
                                                            //It's inappropriate is clicked
                                                            String[] inappropriateOptions = {"Report Post Message or Comment", "Report Account"};
                                                            AlertDialog.Builder inappropriateBuilder = new AlertDialog
                                                                    .Builder(AccountProfileActivity.this);
                                                            inappropriateBuilder.setTitle("Why are you reporting this account?");
                                                            inappropriateBuilder.setItems(inappropriateOptions,
                                                                    new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            //handle dialog click of reports
                                                                            if (which==0){
                                                                                //Report Post clicked
                                                                                AlertDialog.Builder reportPostBuilder = new AlertDialog
                                                                                        .Builder(AccountProfileActivity.this);
                                                                                reportPostBuilder.setIcon(R.drawable.thank_icon);
                                                                                reportPostBuilder.setTitle("Thank you for your response");
                                                                                reportPostBuilder.setMessage("You can report any post from the post options Button");
                                                                                reportPostBuilder.create().show();

                                                                            }else if (which==1){
                                                                                //Report Account clicked
                                                                                String[] reportAccountOptions = {"It's posting content that shouldn't be on CraGee App",
                                                                                        "It's pretending to be someone else", "It may be under the age of 13"};
                                                                                AlertDialog.Builder reportAccountBuilder = new AlertDialog
                                                                                        .Builder(AccountProfileActivity.this);
                                                                                reportAccountBuilder.setTitle("Why are you reporting this account?");
                                                                                reportAccountBuilder.setItems(reportAccountOptions, new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        //handle dialog click
                                                                                        if (which==0){
                                                                                            AlertDialog.Builder postContentBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                                                            postContentBuilder.setIcon(R.drawable.thank_icon);
                                                                                            postContentBuilder.setTitle("Thanks for letting us know");
                                                                                            postContentBuilder.setMessage("Posted content by the user will be check and removed if it voilates our policies");
                                                                                            postContentBuilder.create().show();
                                                                                        }
                                                                                        else if (which==1){
                                                                                            AlertDialog.Builder pretendBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                                                            pretendBuilder.setIcon(R.drawable.thank_icon);
                                                                                            pretendBuilder.setTitle("Thanks for letting us know");
                                                                                            pretendBuilder.setMessage("User's profile will be reviewed and verified");
                                                                                            pretendBuilder.create().show();
                                                                                        }
                                                                                        else if (which==2){
                                                                                            AlertDialog.Builder ageBuilder = new AlertDialog.Builder(AccountProfileActivity.this);
                                                                                            ageBuilder.setIcon(R.drawable.thank_icon);
                                                                                            ageBuilder.setTitle("Thanks for letting us know");
                                                                                            ageBuilder.setMessage("User'age will be verified and removed if user's age is under 13");
                                                                                            ageBuilder.create().show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                                reportAccountBuilder.create().show();
                                                                            }
                                                                        }
                                                                    });
                                                            inappropriateBuilder.create().show();

                                                        }


                                                    }
                                                });
                                                //create and show the dialog box of Reports
                                                ReportsBuilder.create().show();

                                            }else if (which==1){
                                                //Block clicked
                                                /*
                                                 *add the user id of user which is to be blocked in the database of current user node
                                                 */
                                                AlertDialog.Builder blockBuilder = new AlertDialog
                                                        .Builder(AccountProfileActivity.this);
                                                //views to set in the dialog
                                                blockBuilder.setTitle("Block User").setIcon(R.drawable.block_icon);
                                                //set layout linear layout
                                                LinearLayout linearLayout =new LinearLayout(AccountProfileActivity.this);

                                                TextView dialogText = new TextView(AccountProfileActivity.this);
                                                dialogText.setText(getResources().getString(R.string.block_clicked_text));
                                                dialogText.setTextColor(ContextCompat.getColor(AccountProfileActivity.this, R.color.blue));
                                                linearLayout.addView(dialogText);
                                                linearLayout.setPadding(5,5,5,5);
                                                blockBuilder.setView(linearLayout);
                                                blockBuilder.setPositiveButton("Block", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        usersReference
                                                                .child(senderUserId)
                                                                .child("Blocked Users")
                                                                .child(receiverUserId)
                                                                .child("uid").setValue(receiverUserId)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d(TAG, "user added in the blocked  users list");
                                                                        Log.d(TAG, "current user has blocked the other user successfully");


                                                                        usersReference
                                                                                .child(receiverUserId)
                                                                                .child("Blocked By")
                                                                                .child(senderUserId)
                                                                                .child("uid")
                                                                                .setValue(senderUserId)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Log.d(TAG, "user added in the blocked by list");
                                                                                          /*
                                                                                            current user will unfollow the other user
                                                                                            activity will refresh itself
                                                                                          */
                                                                                        AlertDialog.Builder blockSuccessBuilder = new AlertDialog
                                                                                                .Builder(AccountProfileActivity.this);
                                                                                        //views to set in the dialog
                                                                                        blockSuccessBuilder.setTitle("User Blocked").setIcon(R.drawable.thank_icon);
                                                                                        //set layout linear layout
                                                                                        LinearLayout linearLayout =new LinearLayout(AccountProfileActivity.this);

                                                                                        TextView dialogText = new TextView(AccountProfileActivity.this);
                                                                                        dialogText.setText(getResources().getString(R.string.after_block_text));
                                                                                        dialogText.setTextColor(ContextCompat.getColor(AccountProfileActivity.this, R.color.red));
                                                                                        linearLayout.addView(dialogText);
                                                                                        linearLayout.setPadding(5,5,5,5);
                                                                                        blockSuccessBuilder.setView(linearLayout);
                                                                                        blockSuccessBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                                            @Override
                                                                                            public void onClick(DialogInterface dialog, int which) {

                                                                                                Log.d(TAG, "onClick: user will not be able to see the current users posts");
                                                                                                /*
                                                                                                 *current user has blocked the other user successfully
                                                                                                 * now check if the current user is following the other user
                                                                                                 * if following ,then un follow the other user

                                                                                                 */
                                                                                                ProgressButton progressButtonFollow =new ProgressButton(AccountProfileActivity.this,followButton);
                                                                                               unFollowUser(progressButtonFollow);
                                                                                                dialog.dismiss();
                                                                                            }
                                                                                        });
                                                                                        blockSuccessBuilder.create().show();

                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Log.d(TAG, "onFailure: failure in adding in the blocked by list"+e
                                                                                        .getMessage());
                                                                            }
                                                                        });
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "User blocking Failed"+e.getMessage());
                                                                Toast.makeText(AccountProfileActivity.this, "User blocking Failed"+e
                                                                        .getMessage(), Toast.LENGTH_SHORT)
                                                                        .show();
                                                            }
                                                        });
                                                    }
                                                });
                                                // button cancel
                                                blockBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //dismiss the dialog
                                                        dialog.dismiss();
                                                    }
                                                });
                                                blockBuilder.create().show();
                                            }else if (which==2){
                                                //Restrict clicked
                                            }
                                        }
                                    });
                                    //create and show the dialog box
                                    builder.create().show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                break;
            case R.id.sendMessageButton:
                /*
                 * A unique chat id will be created and this will be the direct
                 * child of the Chats node in the data base
                 * All messages between two users will be stored in this node
                 */

                final String chat_Id;
                if (senderUserId.compareTo(receiverUserId)>0)
                {
                    chat_Id = senderUserId + receiverUserId;
                }
                else{
                    chat_Id = receiverUserId+senderUserId;
                }
                ProgressButton progressButton =new ProgressButton(AccountProfileActivity.this,sendMessageBtn);
                progressButton.buttonActivated();
                Intent intentToChat = new Intent(AccountProfileActivity.this, UserChatActivity.class);
                intentToChat.putExtra("hisUid",receiverUserId);
                intentToChat.putExtra("chat_key",chat_Id);
                intentToChat.putExtra("user_name",profileName);
                intentToChat.putExtra("userProfileImage",profileImage);
                intentToChat.putExtra("accountCategory",accountCategory);
                startActivity(intentToChat);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                finish();
                break;
            case R.id.btnConfirm:
                Log.d(TAG, "onClick: confirm button clicked");
                acceptRequest();
                break;
            case R.id.btnDelete:
                Log.d(TAG, "onClick: delete button clicked");
                deleteRequest();
                break;
            case R.id.verifyUser:
                Log.d(TAG, "onClick: verify user");
               // Intent intentToVerification=new Intent(AccountProfileActivity.this, VerificationActivity.class);
               // intentToVerification.putExtra("userId",receiverUserId);
              //  startActivity(intentToVerification);
                break;
            case R.id.edit_profile_review:
                sendUserToRatingsActivity(ratingsGiven);
                break;
            default:
                break;
        }
    }



    private void sendUserToFollowingActivity() {
        Intent intentToFollowing = new Intent(AccountProfileActivity.this, FollowingActivity.class);
        intentToFollowing.putExtra("userId",receiverUserId);
        startActivity(intentToFollowing);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
    private void sendUserToFollowerActivity() {
        Intent intentToFollowers = new Intent(AccountProfileActivity.this, FollowersActivity.class);
        intentToFollowers.putExtra("userId",receiverUserId);
        startActivity(intentToFollowers);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }


    class checkRequestTask extends AsyncTask<String,String,String>{


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            checkRequest();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    private void unFollowUser(final ProgressButton progressButtonFollow) {

    }


    private void followUser(final ProgressButton progressButtonFollow) {

        Log.d(TAG, "followUser: the current value of privacy is"+currentPrivacy);

        /*
         * if the privacy of the user is public then follow the user directly
         * if the privacy of the user is private then send follow request
         */
        switch (currentPrivacy){
            case "Public":
                Log.d(TAG, "followUser: current privacy is Public");
                Log.d(TAG, "followUser: the value of current privacy is"+currentPrivacy);
                usersReference
                        .child(senderUserId)
                        .child("following")
                        .child(receiverUserId)
                        .child("id")
                        .setValue(receiverUserId)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                usersReference
                                        .child(receiverUserId)
                                        .child("followers")
                                        .child(senderUserId)
                                        .child("id")
                                        .setValue(senderUserId)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //current user has started following other user
                                                Log.d(TAG, "current user has started following the other user: ");
                                                progressButtonFollow.buttonFollowing();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: "+e.getMessage());
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
                break;
            case "Private":
                Log.d(TAG, "followUser: current privacy is Private");
                Log.d(TAG, "followUser: the value of current privacy is"+currentPrivacy);
                followRequestRef
                        .child(senderUserId)
                        .child(receiverUserId)
                        .child("request_type")
                        .setValue("sent")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: follow request sent successfully");
                                followRequestRef
                                        .child(receiverUserId)
                                        .child(senderUserId)
                                        .child("request_type")
                                        .setValue("received")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: follow request received successfully");
                                                progressButtonFollow.buttonRequested();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: received request failed"+e.getMessage());
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: sent request failed"+e.getMessage());
                    }
                });
                break;
            default:
                break;
        }
    }

    private void acceptRequest() {
        Log.d(TAG, "acceptRequest: accepting the follow request");
        /*
         *when the request is accepted other user has become the follower of the current user
         * current user will be in the following list of the other user
         * after successfully accepting the request
         * remove the request from the database
         * hide the request layout
         *
         */
        usersReference
                .child(senderUserId)
                .child("followers")
                .child(receiverUserId)
                .child("id")
                .setValue(receiverUserId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: other user has been added successfully to the current user followers list");
                        Log.d(TAG, "onSuccess: now adding the current user to the following list of the other user");
                        usersReference
                                .child(receiverUserId)
                                .child("following")
                                .child(senderUserId)
                                .child("id")
                                .setValue(senderUserId)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: current user has been updated in the following list of the other user");
                                        /*
                                        remove the follow requests
                                         */
                                        followRequestRef
                                                .child(receiverUserId)
                                                .child(senderUserId)
                                                .removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        followRequestRef
                                                                .child(senderUserId)
                                                                .child(receiverUserId)
                                                                .removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d(TAG, "onSuccess: the follow request has been removed");
                                                                        //   followBtn.setText("Follow Back");
                                                                        requestShowLayout.setVisibility(View.GONE);
                                                                        finish();
                                                                        overridePendingTransition(0, 0);
                                                                        startActivity(getIntent());
                                                                        overridePendingTransition(0, 0);
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.d(TAG, "onFailure: "+e.getMessage());
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: "+e.getMessage());
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: failure in updating the following list of other user"+e.getMessage());
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: failure in updating the followers list of current user"+e.getMessage());
            }
        });
    }

    private void deleteRequest() {
        Log.d(TAG, "acceptRequest: deleting the follow request");
    /*
    delete the follow requests
     */
        followRequestRef
                .child(receiverUserId)
                .child(senderUserId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        followRequestRef
                                .child(senderUserId)
                                .child(receiverUserId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: the delete request has been removed");

                                        requestShowLayout.setVisibility(View.GONE);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: "+e.getMessage());

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: "+e.getMessage());

            }
        });
    }
}
