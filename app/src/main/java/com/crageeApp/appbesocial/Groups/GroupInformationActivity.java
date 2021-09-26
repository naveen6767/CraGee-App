package com.crageeApp.appbesocial.Groups;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.crageeApp.appbesocial.AccountProfile.AccountProfileActivity;
import com.crageeApp.appbesocial.Payments.PaymentsActivity;
import com.crageeApp.appbesocial.ProgressButton;
import com.crageeApp.appbesocial.R;
import com.crageeApp.appbesocial.Ratings.RatingsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupInformationActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener{
    private ImageView groupAvatar;
    private CircleImageView groupAdminProfile;
    private TextView gNameTv,gPrivacyTv,gAboutTv,promptText,txtGroupRatings,firstRuleText,secondRuleText,
            groupsCreated,groupsMessages,reviewEdit;
    private Button joinGroupChat,groupRequest,btnWatchVideos,groupStatusUpdate;
    private DatabaseReference usersReference,groupRequestsRef,groupsRef,groupChatsRef;
    private String groupName,groupAdminId,joinRequestSender,current_state,
            uniqueGroupId,uniqueGroupChatKey,groupPrivacy,groupImage,groupAbout;
    private ImageButton btnAllReviews,rulesEdit,groupStatusEdit;
    private LinearLayout rulesLayoutText,rulesLayoutEdit,txtPrivate,txtPublic,txtOpen;
    private EditText firstRuleEdit,secondRuleEdit,groupStatusEditText;
    private RatingBar ratingBarGive,ratingBarSet;
    private float ratingsGiven;
    public static final String TAG = "Group Information";
    private View joinGroupButton,groupMembers,btnUpdateRules;
    private ProgressButton pbJoinGroup,pbGroupMembers,pbRules;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_information);

        //here the current_user_id is joinRequestSender
        initializeViews();

        //here the join request sender is the current user
        joinRequestSender = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // getting the group information passed by the last activity
        Intent intent = getIntent();
        groupAdminId =intent.getStringExtra("groupAdmin");
        uniqueGroupId =intent.getStringExtra("groupId");
        groupImage =intent.getStringExtra("groupImage");
        groupName =intent.getStringExtra("groupName");
        groupPrivacy =intent.getStringExtra("groupPrivacy");
        groupAbout =intent.getStringExtra("groupAbout");
        uniqueGroupChatKey =intent.getStringExtra("groupChatKey");
        //setting the group info details
        gNameTv.setText(groupName);
        gPrivacyTv.setText(groupPrivacy);
        gAboutTv.setText(groupAbout);
        pbJoinGroup =new ProgressButton(GroupInformationActivity.this,joinGroupButton);
        pbJoinGroup.buttonJoinGroup();
        pbGroupMembers =new ProgressButton(GroupInformationActivity.this,groupMembers);
        pbGroupMembers.buttonAllMembers();
        pbRules =new ProgressButton(GroupInformationActivity.this,btnUpdateRules);
        pbRules.buttonUpdateRules();
        //here trying to take the cover image through intents
        if (FirebaseAuth.getInstance().getUid().equals(groupAdminId)){
            if (joinRequestSender.equals(groupAdminId)){
                groupsRef
                        .child(uniqueGroupId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("imageUrl")){
                                    String requestGroupCoverImage =(String) dataSnapshot.child("imageUrl").getValue();
                                    try {
                                        Picasso.get().load(requestGroupCoverImage)
                                                .placeholder(R.drawable.profile_image)
                                                .into(groupAvatar);
                                    }
                                    catch (Exception e){
                                        Picasso.get().load(R.drawable.profile_image)
                                                .into(groupAvatar);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        }
        else {
            Log.i(TAG, "onCreate: current user is not the group admin");
            try {
                Picasso.get().load(groupImage).placeholder(R.drawable.profile_image)
                        .into(groupAvatar);
            }
            catch (Exception e){
                Log.i(TAG, "onCreate: not getting the group cover image"+e.getMessage());
                Picasso.get().load(R.drawable.profile_image).into(groupAvatar);
            }
        }
        groupAdminProfile.setOnClickListener(this);
        groupMembers.setOnClickListener(this);
        btnAllReviews.setOnClickListener(this);
        rulesEdit.setOnClickListener(this);
        btnWatchVideos.setOnClickListener(this);
        joinGroupChat.setOnClickListener(this);
        groupRequest.setOnClickListener(this);
        btnUpdateRules.setOnClickListener(this);
        reviewEdit.setOnClickListener(this);
        groupStatusUpdate.setOnClickListener(this);
        groupStatusEdit.setOnClickListener(this);


        ratingBarGive.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                sendUserToRatingsActivity(rating);
            }
        });


        groupLayoutTask groupLayoutTask = new groupLayoutTask();
        groupLayoutTask.execute();

        memberExistenceTask memberExistenceTask=new memberExistenceTask();
        memberExistenceTask.execute();

        groupAdminTask groupAdminTask =new groupAdminTask();
        groupAdminTask.execute();
    }
    private void getGroupMessages() {
        groupChatsRef
                .child(uniqueGroupChatKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String messagesCounter = String.valueOf( dataSnapshot.getChildrenCount());
                            groupsMessages.setText(messagesCounter);
                            HashMap<String, Object> messagesMap = new HashMap<>();
                            messagesMap.put("totalMessages",messagesCounter);
                            groupsRef.child(uniqueGroupId).updateChildren(messagesMap);

                        }else {
                            groupsMessages.setText("0");
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: error in getting the value from the database"+databaseError.getMessage());

                    }
                });
    }
    private void sendUserToRatingsActivity(float rating) {
        Intent intentToRatings = new Intent(GroupInformationActivity.this, RatingsActivity.class);
        intentToRatings.putExtra("rating",rating);
        intentToRatings.putExtra("current GroupId",uniqueGroupId);
        startActivity(intentToRatings);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public class groupLayoutTask extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: ");
        }
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: ");
            setGroupLayout();
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
    private void setGroupLayout() {
        //setting the text about group
        switch (groupPrivacy){
            case "Public Group":
                txtPublic.setVisibility(View.VISIBLE);

                break;
            case "Private Group":

                txtPrivate.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        groupsRef
                .child(uniqueGroupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, "onDataChange: successfully getting the group info for layout"+dataSnapshot);

                        String firstRuleRetrieved = (String) dataSnapshot.child("Rules").child("firstRule").getValue();
                        String secondRuleRetrieved = (String) dataSnapshot.child("Rules").child("secondRule").getValue();
                        String groupCreationDate = (String) dataSnapshot.child("createdDate").getValue();
                        String allMessages = (String) dataSnapshot.child("totalMessages").getValue();

                        firstRuleText.setText(firstRuleRetrieved);
                        secondRuleText.setText(secondRuleRetrieved);
                        groupsCreated.setText(groupCreationDate);
                        groupsMessages.setText(allMessages);


                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: error in getting group layout info"+databaseError.getMessage());
                        Log.i(TAG, "onCancelled: error in getting group layout info"+databaseError.getCode());
                        Log.i(TAG, "onCancelled: error in getting group layout info"+databaseError.getDetails());

                    }
                });
    }


    public class groupAdminTask extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: ");
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: ");
            retrieveGroupAdminInfo();


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
    public class memberExistenceTask extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: ");
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: ");

            checkMemberExistence();
            getGroupMessages();
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


    private void sendUserToGroupMembers() {
        Intent intentToGroupMembers = new Intent(GroupInformationActivity.this,GroupMembersActivity.class);
        intentToGroupMembers.putExtra("GroupId",uniqueGroupId);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        startActivity(intentToGroupMembers);
    }

    private void sendUserToAccountProfile() {
        //send user to the group admin profile
        Intent intentToUserProfile = new Intent(GroupInformationActivity.this, AccountProfileActivity.class);
        intentToUserProfile.putExtra("visit_user_id",groupAdminId);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        startActivity(intentToUserProfile);
    }

    private void initializeViews() {
        groupAvatar=findViewById(R.id.gCImage);
        gNameTv =findViewById(R.id.gName);
        gPrivacyTv=findViewById(R.id.gPrivacy);
        joinGroupButton=findViewById(R.id.join_Group_button);
        joinGroupChat=findViewById(R.id.join_Group_chat);
        groupRequest=findViewById(R.id.all_group_requests);
        gAboutTv=findViewById(R.id.about_group);
        groupAdminProfile=findViewById(R.id.group_admin_profile_image);
        groupMembers=findViewById(R.id.group_all_members);
        btnAllReviews=findViewById(R.id.allReviewsButton);
        rulesEdit=findViewById(R.id.group_rules_edit);
        rulesLayoutText=findViewById(R.id.rules_layout_text);
        rulesLayoutEdit=findViewById(R.id.rules_layout_edit);
        promptText=findViewById(R.id.groupInfoPrompt);
        btnWatchVideos=findViewById(R.id.watch_videos_group_info);
        txtOpen=findViewById(R.id.tvAbout_open_group);
        txtPrivate=findViewById(R.id.tvAbout_private_group);
        txtPublic=findViewById(R.id.tvAbout_public_group);
        txtGroupRatings=findViewById(R.id.group_ratings_text);
        btnUpdateRules=findViewById(R.id.update_rules);
        firstRuleEdit=findViewById(R.id.group_rules_first_edit);
        secondRuleEdit=findViewById(R.id.group_rules_second_edit);
        firstRuleText=findViewById(R.id.Group_rules_first);
        secondRuleText=findViewById(R.id.Group_rules_second);
        groupsCreated=findViewById(R.id.Group_creation_date);
        groupsMessages=findViewById(R.id.Group_total_messages);
        ratingBarGive=findViewById(R.id.give_ratings);
        ratingBarSet=findViewById(R.id.set_ratings);
        reviewEdit=findViewById(R.id.edit_review);
        groupStatusEdit=findViewById(R.id.about_group_edit);
        groupStatusUpdate=findViewById(R.id.update_group_status);
        groupStatusEditText=findViewById(R.id.edit_group_status);
        current_state="new";
        //initialize the fire base  here
        usersReference= FirebaseDatabase.getInstance().getReference().child("Users");
        groupsRef= FirebaseDatabase.getInstance().getReference().child("Groups");
        groupRequestsRef= FirebaseDatabase.getInstance().getReference().child("Group Requests");
        groupChatsRef= FirebaseDatabase.getInstance().getReference().child("Group Chats");

    }

    private void checkMemberExistence() {
        //here check if the current user is the member of the current opened group
        groupsRef
                .child(uniqueGroupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("Group Members").hasChild(joinRequestSender)){
                            //current user is the member
                            /*
                             * if the current user is the member of the current opened group
                             * he can give the ratings and write reviews
                             * it is same for the admin and other members
                             * if the member has not given the rating it will show "Rate this group"
                             * if already given the text should be as "your review"
                             */
                            if (dataSnapshot.child("ReviewsAndRating").hasChild(joinRequestSender)){
                                Log.i(TAG, "onDataChange: current user has already rated the current group");
                                //change the text and
                                txtGroupRatings.setText(getResources().getString(R.string.Your_review));
                                reviewEdit.setVisibility(View.VISIBLE);
                                ratingsGiven = Float.parseFloat(dataSnapshot.child("ReviewsAndRating")
                                        .child(joinRequestSender)
                                        .child("rating")
                                        .getValue()
                                        .toString());
                                Log.i(TAG, "onDataChange: ratings given by the current user is"+ratingsGiven);
                                ratingBarSet.setVisibility(View.VISIBLE);
                                ratingBarSet.setRating(ratingsGiven);
                                ratingBarSet.setIsIndicator(true);
                            }
                            else {
                                Log.i(TAG, "onDataChange: current user is member but have not rated the group ");
                                //change the text and
                                txtGroupRatings.setText(getResources().getString(R.string.Rate_this_group));
                                ratingBarGive.setVisibility(View.VISIBLE);
                                ratingBarSet.setVisibility(View.GONE);
                            }
                        /*
                        check also whether the current user is also the group admin of the current opened group
                         */
                            if (joinRequestSender.equals(groupAdminId)){
                                Log.i(TAG, "onDataChange: current user is the owner of the current opened group");
                                joinGroupButton.setVisibility(View.GONE);
                                joinGroupChat.setVisibility(View.VISIBLE);
                                groupRequest.setVisibility(View.VISIBLE);

                                groupStatusEdit.setVisibility(View.VISIBLE);
                                //prompted text to show group admin
                                promptText.setVisibility(View.VISIBLE);
                                promptText.setText(getResources().getString(R.string.group_owner));
                                promptText.setTextColor(ContextCompat.getColor(GroupInformationActivity.this, R.color.blue));
                                rulesEdit.setVisibility(View.VISIBLE);

                            }
                            else {
                                Log.i(TAG, "onDataChange: current user is the member but not the owner of this group ");

                                //current user is the member but not the owner of this group
                                //current user has already joined the group
                                joinGroupButton.setVisibility(View.INVISIBLE);
                                joinGroupChat.setVisibility(View.VISIBLE);
                                // prompted text to show group member

                                promptText.setVisibility(View.VISIBLE);
                                promptText.setText(getResources().getString(R.string.group_member));
                                promptText.setTextColor(ContextCompat.getColor(GroupInformationActivity.this, R.color.green));
                            }
                        }else {
                            Log.i(TAG, "onDataChange:current user is not the member of current opened group ");
                            //current user is not the member of current opened group
                            //current user is not member nor owner
                            promptText.setVisibility(View.VISIBLE);
                            promptText.setText(getResources().getString(R.string.group_visitor));
                            promptText.setTextColor(ContextCompat.getColor(GroupInformationActivity.this, R.color.blue));
                            manageGroupRequest();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void sendUserToGroupRequests() {
        Intent intentToGroupRequests = new Intent(GroupInformationActivity.this,GroupRequestsActivity.class);
        intentToGroupRequests.putExtra("GroupId",uniqueGroupId);
        startActivity(intentToGroupRequests);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

    }

    private void sendUserToGroupChat() {
        Intent intentToGroupChat = new Intent(GroupInformationActivity.this, chatGroupActivity.class);
        intentToGroupChat.putExtra("groupName",groupName);
        intentToGroupChat.putExtra("groupImage",groupImage);
        intentToGroupChat.putExtra("uniqueGroupId",uniqueGroupId);
        intentToGroupChat.putExtra("groupChatKey",uniqueGroupChatKey);
        startActivity(intentToGroupChat);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    private void retrieveGroupAdminInfo() {
        //here retrieve the group admin profile image
        usersReference.child(groupAdminId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String requestGroupAdminImage =(String) dataSnapshot.child("image").getValue();
                try {
                    Picasso.get().load(requestGroupAdminImage).placeholder(R.drawable.profile_image).into(groupAdminProfile);

                }
                catch (Exception e){
                    Picasso.get().load(R.drawable.profile_image).into(groupAdminProfile);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        /*here validate if the current user is also the group admin
        if it is , hide the join button otherwise show this button
        and send the group join request
         */
    }
    private void manageGroupRequest() {
        //here try to retrieve the current group join request
        groupRequestsRef
                .child(joinRequestSender)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(uniqueGroupId))
                        {
                            String request_type=(String)dataSnapshot.child(uniqueGroupId).child("request_type").getValue();
                            if (request_type.equals("sent"))
                            {
                                current_state="request_sent";
                                pbJoinGroup.buttonCancelRequest();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: error in getting the current group join request"+databaseError.getMessage());

                    }
                });
        if (!joinRequestSender.equals(groupAdminId))
        {
            Log.i(TAG, "manageGroupRequest: current user is not the group admin");
            //here apply click listener and send join group request
            joinGroupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pbJoinGroup.buttonActivated();
                    if (current_state.equals("new"))
                    {
                        //both are new to each other i.e. current user is opening the group for the first time
                        /*
                         *here if the current group  is open for public i.e. open group
                         * if OPEN group,show the ad and hide the send request button
                         * show the group chat button
                         */
                        if (groupPrivacy.equals("Public Group")){
                            /*
                             *for the public group
                             * any user can join this group after the sending the group join request
                             * first interstitial ad will appear
                             */
                            sendGroupJoinRequest();
                        }
                        else {
                            Log.d(TAG, "onClick: current group is private group ");
                            sendUserToPayments();
                            pbJoinGroup.buttonJoinGroup();
                            /*
                             * for the private group
                             * open payments activity
                             * if user has paid the group joining fee
                             * then directly make them the member of the group
                             */
                        }
                    }
                    if (current_state.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                }
            });
        }
        else {
            joinGroupButton.setVisibility(View.INVISIBLE);
            joinGroupChat.setVisibility(View.VISIBLE);
            joinGroupChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentToGroupChat = new Intent(GroupInformationActivity.this,chatGroupActivity.class);
                    intentToGroupChat.putExtra("groupName",groupName);
                    intentToGroupChat.putExtra("uniqueGroupId",uniqueGroupId);
                    intentToGroupChat.putExtra("groupChatKey",uniqueGroupChatKey);
                    startActivity(intentToGroupChat);
                }
            });
            groupRequest.setVisibility(View.VISIBLE);
            groupRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentToGroupRequests = new Intent(GroupInformationActivity
                            .this,GroupRequestsActivity.class);
                    intentToGroupRequests.putExtra("GroupId",uniqueGroupId);
                    startActivity(intentToGroupRequests);
                }
            });

        }
    }

    private void sendUserToPayments() {
        Intent intentToPayments=new Intent(GroupInformationActivity.this,PaymentsActivity.class);
        intentToPayments.putExtra("groupName",groupName);
        intentToPayments.putExtra("uniqueGroupId",uniqueGroupId);
        intentToPayments.putExtra("groupChatKey",uniqueGroupChatKey);
        intentToPayments.putExtra("groupAdmin",groupAdminId);
        intentToPayments.putExtra("groupImage",groupImage);
        intentToPayments.putExtra("groupPrivacy",groupPrivacy);
        //intentToPayments.putExtra("groupCategory",);
        intentToPayments.putExtra("groupAbout",groupAbout);
        intentToPayments.putExtra("groupChatKey",uniqueGroupChatKey);
        startActivity(intentToPayments);
    }
    private void cancelChatRequest() {
        /*
         *first check whether the send chat request is accepted ot not
         * go to current group members and check the current user id as a child
         */
        groupsRef
                .child(uniqueGroupId)
                .child("Group Members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(joinRequestSender)) {
                            joinGroupButton.setVisibility(View.INVISIBLE);
                            joinGroupChat.setVisibility(View.VISIBLE);
                            joinGroupChat.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendUserToGroupChat();

                                }
                            });
                        }
                        else {
                            groupRequestsRef
                                    .child(joinRequestSender)
                                    .child(uniqueGroupId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {

                                                groupRequestsRef.child(uniqueGroupId).child(joinRequestSender).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful())
                                                        {   joinGroupButton.setEnabled(true);
                                                            current_state="new";
                                                            pbJoinGroup.buttonJoinGroup();
                                                        }


                                                    }
                                                });

                                            }
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void sendGroupJoinRequest() {
        groupRequestsRef
                .child(joinRequestSender)
                .child(uniqueGroupId)
                .child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            groupRequestsRef
                                    .child(uniqueGroupId)
                                    .child(joinRequestSender)
                                    .child("request_type")
                                    .setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                joinGroupButton.setEnabled(true);
                                                current_state="request_sent";
                                                 pbJoinGroup.buttonCancelRequest();
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.group_admin_profile_image:
                sendUserToAccountProfile();
                break;
            case R.id.group_all_members:
                pbGroupMembers.buttonActivated();
                sendUserToGroupMembers();
                pbGroupMembers.buttonAllMembers();
                break;
            case R.id.allReviewsButton:
                sendUserToAllReviews();
                break;
            case R.id.group_rules_edit:
                rulesLayoutText.setVisibility(View.GONE);
                rulesLayoutEdit.setVisibility(View.VISIBLE);
                break;
            case R.id.join_Group_chat:
                sendUserToGroupChat();
                break;
            case R.id.all_group_requests:
                sendUserToGroupRequests();
                break;
            case R.id.update_rules:
                updateRules();
                break;
            case R.id.edit_review:
                sendUserToRatingsActivity(ratingsGiven);
                break;
            case R.id.about_group_edit:
                gAboutTv.setVisibility(View.GONE);
                groupStatusEditText.setVisibility(View.VISIBLE);
                groupStatusUpdate.setVisibility(View.VISIBLE);
                break;
            case R.id.update_group_status:
                updateGroupStatus();

                break;
            default:
                break;
        }
    }

    private void updateGroupStatus() {


        String updatedStatus=groupStatusEditText.getText().toString();
        HashMap<String, Object> statusHashMap = new HashMap<>();
        statusHashMap.put("status",updatedStatus);
        groupsRef
                .child(uniqueGroupId)
                .updateChildren(statusHashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: successfully updated the status");
                        /*
                         *when the status get updated hide the edit layout
                         * and show the text layout of group status
                         */
                        groupStatusEditText.setVisibility(View.GONE);
                        groupStatusUpdate.setVisibility(View.GONE);
                        gAboutTv.setVisibility(View.VISIBLE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: error in uploading the status"+e.getMessage());

            }
        });

    }

    private void updateRules() {



        String firstRuleEntered=firstRuleEdit.getText().toString();
        String secondRuleEntered=secondRuleEdit.getText().toString();
        HashMap<String, String> rulesHashMap = new HashMap<>();
        rulesHashMap.put("firstRule",firstRuleEntered);
        rulesHashMap.put("secondRule",secondRuleEntered);
        groupsRef
                .child(uniqueGroupId)
                .child("Rules")
                .setValue(rulesHashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: successfully updated the rules");
                        /*
                         *when the rules get updated hide the edit layout
                         * and show the text layout of group rules
                         */
                        rulesLayoutEdit.setVisibility(View.GONE);
                        rulesLayoutText.setVisibility(View.VISIBLE);

                        //retrieve the rules set by the admin

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: error in uploading the rules"+e.getMessage());

            }
        });

    }

    private void sendUserToAllReviews() {
        Intent intentToAllReviews= new Intent(GroupInformationActivity.this, AllReviewsActivity.class);
        intentToAllReviews.putExtra("GroupId",uniqueGroupId);
        startActivity(intentToAllReviews);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

    }

    private void sendUserToRatingsActivity(String first, String s) {
        Intent intentToRatings = new Intent(GroupInformationActivity.this, RatingsActivity.class);
        intentToRatings.putExtra("rating",s);
        intentToRatings.putExtra("current GroupId",uniqueGroupId);
        startActivity(intentToRatings);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //when the item is selected from the spinner
        /*
         *if the admin select the value from the spinner
         * send this value to the database
         * this will update the minimum joining credit
         */
        String item = parent.getItemAtPosition(position).toString();

        groupsRef
                .child(uniqueGroupId)
                .child("joiningCredits")
                .setValue(item)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: successfully updated the joining credits");


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: error in updating the joining credits"+e.getMessage());

            }
        });
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
