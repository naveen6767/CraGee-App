package com.crageeApp.appbesocial.Groups;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.crageeApp.appbesocial.Adapters.AdapterGroupChat;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class chatGroupActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar groupToolbar;
    private RecyclerView groupRecyclerView;
    private ImageView groupProfileIv;
    private TextView groupNameTv,groupStatusTv;
    private EditText groupMessageEt;
    private ImageButton messageSendBtn,groupChatOptionsButton,nightModeBtnON,nightModeBtnOFF;
    private String currentUserId,groupName,groupUniqueChatKey,timeStamp,groupPicture;
    private FirebaseAuth userAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userDbRef,groupsRef,groupChatRef,mGroupChatSeen,mRootRef;
    private RelativeLayout groupChatLayout;
    private String saveCurrentDate,saveCurrentTime,currentUserName,groupId;
    private static final String TAG = "chatGroupActivity";
    private List<groupMessages> groupMessagesList;
    private AdapterGroupChat adapterGroupChat;
    private SwipeRefreshLayout mRefreshLayout;
    private static final int TOTAL_ITEMS_TO_LOAD=20;
    private int mCurrentPage=1;
    private int itemPos=0;
    private String mLastKey="";
    private String mPrevKey="";
    private LinearLayoutManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group);
        initializeViews();
        //set tool bar as the action bar
        setSupportActionBar(groupToolbar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Intent intent = getIntent();
        groupName=intent.getStringExtra("groupName");
        groupUniqueChatKey=intent.getStringExtra("groupChatKey");
        groupId=intent.getStringExtra("uniqueGroupId");
        groupPicture=intent.getStringExtra("groupImage");
        groupMessagesList=new ArrayList<>();
        //setting the group name
        groupNameTv.setText(groupName);
        try {
            //image received and set it to the tool bar
            Picasso.get().load(groupPicture).placeholder(R.drawable.profile_image).into(groupProfileIv);
        }
        catch (Exception e)
        {
            //there is exception in getting the picture
            //use a default picture
            Picasso.get().load(R.drawable.profile_image).into(groupProfileIv);
        }
        Calendar calForDate =Calendar.getInstance();
        SimpleDateFormat currentDate =new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        //layout (Linear layout) for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //recycler view properties
        adapterGroupChat=new AdapterGroupChat(chatGroupActivity.this, groupMessagesList);
        groupRecyclerView.setHasFixedSize(true);
        manager=new LinearLayoutManager(this);
      
        groupRecyclerView.setLayoutManager(manager);
        groupRecyclerView.setAdapter(adapterGroupChat);
        retrieveCurrentUserName();
        retrieveCurrentGroupInfo();
        loadMessages();
        mRefreshLayout.setEnabled(true);
        nightModeBtnON.setOnClickListener(this);
        nightModeBtnOFF.setOnClickListener(this);
        groupChatOptionsButton.setOnClickListener(this);
        messageSendBtn.setOnClickListener(this);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();

            }
        });

    }
    private void showOptions() {

        //show a alert dialog

        //options to show in the dialog box
        String[] options = {"Older chats", "Dark Mode","Report","Exit group"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(chatGroupActivity.this);
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog items click
                if (which==0){
                    //older chats clicked
                    //open the date picker dialog box
                    //open the date picker dialog box
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    final DatePickerDialog datePickerDialog = new DatePickerDialog(chatGroupActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                                    datePicker.setMaxDate(new Date().getTime());
                                    month = month + 1;
                                    String formattedMonth = "" + month;
                                    String formattedDayOfMonth = "" + dayOfMonth;
                                    if(month < 10){

                                        formattedMonth = "0" + month;
                                    }
                                    if(dayOfMonth < 10){

                                        formattedDayOfMonth = "0" + dayOfMonth;
                                    }
                                    String selectedDate = formattedDayOfMonth + "-" + formattedMonth + "-" + year;
                                    Log.d(TAG, "onDateSet: the selected date is"+selectedDate);

                                }
                            }, year, month, dayOfMonth);
                    datePickerDialog.show();

                }
                else if (which==1){

                    groupChatLayout.setBackgroundColor(ContextCompat.getColor(chatGroupActivity.this, R.color.group_chat_background));
                    nightModeBtnOFF.setVisibility(View.GONE);
                    nightModeBtnON.setVisibility(View.VISIBLE);
                }else if (which==2){
                    //Report clicked
                    String[] ReportOptions = {"It's spam", "It's inappropriate"};
                    AlertDialog.Builder ReportsBuilder = new AlertDialog.Builder(chatGroupActivity.this);
                    ReportsBuilder.setTitle("Why are you reporting this group?");
                    ReportsBuilder.setItems(ReportOptions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //handle dialog click of reports
                            if (which==0){
                                //It's spam is clicked
                                final AlertDialog.Builder spamBuilder = new AlertDialog.Builder(chatGroupActivity.this);
                                spamBuilder.setIcon(R.drawable.thank_icon);
                                spamBuilder.setTitle("Thanks for letting us know");
                                spamBuilder.setMessage("Your feedback is important in helping us keep the CraGee App community safe.");
                                groupsRef
                                        .child(groupId)
                                        .child("groupReports")
                                        .child("spam")
                                        .child(currentUserId).setValue(true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                spamBuilder.create().show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(chatGroupActivity.this, ""+e
                                                .getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else if (which==1){
                                //It's inappropriate is clicked
                                String[] inappropriateOptions = {"Report to Admin", "Report to CraGee App"};
                                AlertDialog.Builder inappropriateBuilder = new AlertDialog
                                        .Builder(chatGroupActivity.this);
                                inappropriateBuilder.setTitle("Please select your action");
                                inappropriateBuilder.setItems(inappropriateOptions,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //handle dialog click of reports
                                                if (which==0){
                                                    //Report to Admin clicked
                                                    final AlertDialog.Builder reportPostBuilder = new AlertDialog
                                                            .Builder(chatGroupActivity.this);
                                                    reportPostBuilder.setIcon(R.drawable.thank_icon);
                                                    reportPostBuilder.setTitle("Thank you for your response");
                                                    reportPostBuilder.setMessage("Group admin will take required actions");
                                                    /**
                                                     * code for the admin reports   is remaining
                                                     */
                                                    groupsRef
                                                            .child(groupId).child("adminReport")
                                                            .child(currentUserId).setValue(true)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    reportPostBuilder.create().show();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(chatGroupActivity.this, ""+e
                                                                    .getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                                }else if (which==1){
                                                    /**
                                                     * code for reporting to craGee app  is remaining
                                                     */
                                                    //Report to CraGee App clicked
                                                    String[] reportAccountOptions = {"It's sending content that shouldn't be on"+" "+groupName+"group",
                                                            "It's pretending to be someone else", "It may be under the age of 13"};
                                                    AlertDialog.Builder reportAccountBuilder = new AlertDialog
                                                            .Builder(chatGroupActivity.this);
                                                    reportAccountBuilder.setTitle("Why are you reporting this account?");
                                                    reportAccountBuilder.setItems(reportAccountOptions, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            //handle dialog click
                                                            if (which==0){
                                                                AlertDialog.Builder postContentBuilder = new AlertDialog.Builder(chatGroupActivity.this);
                                                                postContentBuilder.setIcon(R.drawable.thank_icon);
                                                                postContentBuilder.setTitle("Thanks for letting us know");
                                                                postContentBuilder.setMessage("Sent content by the user will be checked and removed if it violates group rules");
                                                                postContentBuilder.create().show();
                                                            }
                                                            else if (which==1){
                                                                AlertDialog.Builder pretendBuilder = new AlertDialog.Builder(chatGroupActivity.this);
                                                                pretendBuilder.setIcon(R.drawable.thank_icon);
                                                                pretendBuilder.setTitle("Thanks for letting us know");
                                                                pretendBuilder.setMessage("User's profile will be reviewed and verified");
                                                                pretendBuilder.create().show();
                                                            }
                                                            else if (which==2){
                                                                AlertDialog.Builder ageBuilder = new AlertDialog.Builder(chatGroupActivity.this);
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
                }
            }
        });
        //create and show the dialog box
        builder.create().show();
    }

    private void loadMoreMessages() {
        Log.d(TAG, "loadMoreMessages: fetching more messages");
        Log.d(TAG, "loadMoreMessages: the value of last key is"+mLastKey);
        DatabaseReference groupMessageRef=mRootRef
                .child("Group Chats")
                .child(groupUniqueChatKey);
        groupMessageRef.keepSynced(true);
        Query messageQuery=groupMessageRef.orderByKey().endAt(mLastKey).limitToLast(20);
        messageQuery.keepSynced(true);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                groupMessages message = dataSnapshot.getValue(groupMessages.class);
                String messageKey = dataSnapshot.getKey();
                Log.d(TAG, "onChildAdded: message key is "+messageKey);
                Log.d(TAG, "onChildAdded: item pos"+itemPos);

                if (!mPrevKey.equals(messageKey)){
                    groupMessagesList.add(itemPos++,message);
                }else {
                    mPrevKey=mLastKey;
                }
                if (itemPos==1){
                    mLastKey=messageKey;
                }

                adapterGroupChat.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {
        DatabaseReference groupMessageRef=mRootRef
                .child("Group Chats")
                .child(groupUniqueChatKey);
        groupMessageRef.keepSynced(true);
        Query messageQuery=groupMessageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);
        messageQuery.keepSynced(true);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded: datasnapshot"+dataSnapshot);
                groupMessages message = dataSnapshot.getValue(groupMessages.class);
                itemPos++;
                if (itemPos==1){
                    mLastKey= dataSnapshot.getKey();
                    mPrevKey= dataSnapshot.getKey();
                }




                groupMessagesList.add(message);
                adapterGroupChat.notifyDataSetChanged();
                groupRecyclerView.scrollToPosition(groupMessagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference db1=mGroupChatSeen;
        mGroupChatSeen
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(groupId)){
                    db1.child(currentUserId).child(groupId).child("seen").setValue(true);
               //     db1.child(groupUniqueChatKey).child(currentUserId).child("seen").setValue(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void retrieveCurrentGroupInfo() {

        groupsRef.child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("imageUrl")){
                    String requestGroupCoverImage =(String) dataSnapshot.child("imageUrl").getValue();
                    try {
                        Picasso.get().load(requestGroupCoverImage).placeholder(R.drawable.profile_image).into(groupProfileIv);

                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.profile_image).into(groupProfileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeViews() {
        //init the tool bar here
        groupToolbar =findViewById(R.id.toolBarGroupChat);
        //init all the views here
        groupRecyclerView =findViewById(R.id.group_chat_recyclerView);
        groupProfileIv =findViewById(R.id.groupProfileIv);
        groupNameTv =findViewById(R.id.groupNameTv);
        groupMessageEt =findViewById(R.id.groupMessageEt);
        messageSendBtn =findViewById(R.id.groupSMSSendBtn);
        groupChatOptionsButton =findViewById(R.id.groupChatOptions);
        groupStatusTv =findViewById(R.id.GroupUsersStatusTv);
        nightModeBtnON =findViewById(R.id.night_mode_button_on);
        nightModeBtnOFF =findViewById(R.id.night_mode_button_off);
        groupChatLayout =findViewById(R.id.group_chat_layout);
        mRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.groupSwipeRefresh_layout);

        //init the fire base here
        userAuth= FirebaseAuth.getInstance();
        currentUserId=userAuth.getUid();
        mRootRef= FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        userDbRef= FirebaseDatabase.getInstance().getReference("Users");
        groupsRef= FirebaseDatabase.getInstance().getReference().child("Groups");
        groupChatRef= FirebaseDatabase.getInstance().getReference().child("Group Chats");
        mGroupChatSeen=FirebaseDatabase.getInstance().getReference().child("group chat");
        mGroupChatSeen.keepSynced(true);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.night_mode_button_on:
                groupChatLayout.setBackgroundColor(Color.BLACK);
                nightModeBtnON.setVisibility(View.GONE);
                nightModeBtnOFF.setVisibility(View.VISIBLE);
                break;
            case R.id.night_mode_button_off:
                groupChatLayout.setBackgroundColor(ContextCompat.getColor(chatGroupActivity.this, R.color.group_chat_background));
                nightModeBtnOFF.setVisibility(View.GONE);
                nightModeBtnON.setVisibility(View.VISIBLE);

                break;
            case R.id.groupChatOptions:
                Log.i(TAG, "onClick: group chat options button clicked");
                Log.i(TAG, "onClick: showing group chat options");
                //showing the group chat options
                showOptions();
                break;
            case R.id.groupSMSSendBtn:
                //here get text from the edit text
                String message =groupMessageEt.getText().toString().trim();
                //here first check if the text is empty or not
                if (TextUtils.isEmpty(message))
                {
                    //text is empty
                    Toast.makeText(chatGroupActivity.this, "can not send the empty message", Toast.LENGTH_SHORT).show();
                }
                else {
                    //text is not empty
                    //send the message to the group
                    sendMessage(message);

                }
                //after sending the message to the friend
                //reset the editText for next message
                groupMessageEt.setText("");

                break;
            default:
                break;
        }
    }

    private void sendMessage(String message) {

        if (!TextUtils.isEmpty(message)) {
            // String current_user_ref="messages/" + myUid + "/" +hisUid;
            //  String chat_user_ref="messages/" + hisUid + "/" +myUid;

            //  DatabaseReference user_message_push=mRootref.child("messages").child(myUid).child(hisUid).push();

            // user_message_push.keepSynced(true);
            // String push_id=user_message_push.getKey();
            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
            saveCurrentTime = currentTime.format(calForTime.getTime());
            Calendar calForDate =Calendar.getInstance();
            SimpleDateFormat currentDate =new SimpleDateFormat("dd-MM-yyyy");
            saveCurrentDate = currentDate.format(calForDate.getTime());
            HashMap<String, Object> messagemap = new HashMap<>();
            messagemap.put("message", message);
            messagemap.put("seen", false);
            messagemap.put("type", "text");
            messagemap.put("time", ServerValue.TIMESTAMP);
            messagemap.put("from", currentUserId);
            messagemap.put("messageDate", saveCurrentDate);
            messagemap.put("messageTime", saveCurrentTime);
            messagemap.put("messageSender", currentUserName);
            HashMap<String, Object> message_user_map = new HashMap<>();
            //   message_user_map.put(current_user_ref +"/" + push_id,messagemap);
            //  message_user_map.put(chat_user_ref + "/ " + push_id,messagemap);

            DatabaseReference groupMessagesRef = FirebaseDatabase.getInstance().getReference("Group Chats")
                    .child(groupUniqueChatKey);
            groupMessagesRef
                    .push()
                    .updateChildren(messagemap);
            /*

                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isComplete()) {
                        Log.d(TAG, "onComplete: msg has been sent");
                        groupChatRef
                                .child(groupUniqueChatKey)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            int currentMessages;
                                            if (dataSnapshot.hasChild("All Messages")){
                                                currentMessages=Integer.parseInt(dataSnapshot.child("All Messages").getValue().toString());
                                                Log.i(TAG, "onDataChange: the value of current messages is "+currentMessages);
                                            }
                                            else {
                                                currentMessages=0;
                                            }
                                            int totalMessages=currentMessages+1;
                                            Log.i(TAG, "after increment to currentMessages"+totalMessages);
                                            HashMap<String, Object> messagesHashMap = new HashMap<>();
                                            messagesHashMap.put("All Messages",totalMessages);
                                            messagesHashMap.put("GroupId",groupId);
                                            mRootRef
                                                    .child("Group Chats")
                                                    .child(groupUniqueChatKey)
                                                    .updateChildren(messagesHashMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.i(TAG, "onSuccess: no of messages updated successfully");
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i(TAG, "onFailure: error in updating the messages");
                                                    Log.i(TAG, "onFailure: "+e.getMessage());
                                                }
                                            });
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.i(TAG, "onCancelled: error"+databaseError.getMessage());

                                    }
                                });
                    }
                }
            });
             */

//////////////////////////////////////////////Adding user in Chat database////////////////////////////////////////////////////////
            mGroupChatSeen.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(groupId)) {
                        mGroupChatSeen
                                .child(currentUserId)
                                .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (!dataSnapshot.hasChild(groupId)) {

                                    HashMap<String, Object> chataddmap = new HashMap<>();
                                    chataddmap.put("seen", false);
                                    chataddmap.put("timestamp", ServerValue.TIMESTAMP);

                                    HashMap<String, Object> chatUserMap = new HashMap<>();
                                    chatUserMap.put("group chat/" + currentUserId + "/" + groupId, chataddmap);

                                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                            }
                                        }
                                    });

                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
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
    private void retrieveCurrentUserName() {

        userDbRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("name")){
                    currentUserName=(String) dataSnapshot.child("name").getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
