package com.crageeApp.appbesocial.Chat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.crageeApp.appbesocial.Login.LoginActivity;
import com.crageeApp.appbesocial.MessageNotifications.APIService;
import com.crageeApp.appbesocial.MessageNotifications.Client;
import com.crageeApp.appbesocial.MessageNotifications.Data;
import com.crageeApp.appbesocial.MessageNotifications.Response;
import com.crageeApp.appbesocial.MessageNotifications.Sender;
import com.crageeApp.appbesocial.MessageNotifications.Token;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class UserChatActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar userChatToolBar;
    private RecyclerView recyclerView;
    private ImageView profileIv;
    private TextView nameTv, userStatusTv;
    private EditText messageEt;
    private ImageButton sendBtn, chatOptions, btnAttach;
    private DatabaseReference userDbRef, chatListRef, specialRef, userRefForSeen,
            mRootref, mchatSeen, mSpecialchatSeen, chatReportsReference, chatWarningsRef;
    private String myUid, hisUid, chat_key, currentUserName, userProfileImage, userCategory, oldestMessageId;
    private String saveCurrentDate, saveCurrentTime, oldestUserId;
    private ValueEventListener seenListener;
    private final List<Messages> messagesList = new ArrayList<>();
    private MessageAdapter mAdapter;
    private SwipeRefreshLayout mRefreshlayout;
    private LinearLayout userNameLayout, msgSendLayout;
    private RelativeLayout chatLayout;
    private List<String> messageIds;
    private Boolean isScrolling = false;
    private LinearLayoutManager manager;
    private int totalItems, currentItems, scrollOutItems;
    private static final int TOTAL_ITEMS_TO_LOAD = 20;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    //for camera permission
    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    //image picked will be saved in this URi
    private Uri image_rui = null;

    //permission array
    private String[] cameraPermissions;
    private String[] storagePermissions;

    APIService apiService;
    boolean notify=false;

    private static final String TAG = "UserChatActivity";

    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);
        navController = Navigation.findNavController(this,R.id.navigation_host_fragment);

        //init the tool bar here
        userChatToolBar = findViewById(R.id.toolBarUserChat);
        userNameLayout = findViewById(R.id.nameLayout);
        chatLayout = findViewById(R.id.userChatLayout);
        msgSendLayout = findViewById(R.id.chatLayout);
        setSupportActionBar(userChatToolBar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //initializing the  permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //init all the views here
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        chatOptions = findViewById(R.id.OptionsUserChat);
        userStatusTv = findViewById(R.id.userStatusTv);
        btnAttach = findViewById(R.id.attach_button);

        //layout (Linear layout) for recycler view
        manager = new LinearLayoutManager(this);

        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);




        //recycler view properties
        mAdapter = new MessageAdapter(messagesList, UserChatActivity.this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mAdapter);
        mRefreshlayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_layout);
        messageIds = new ArrayList<>(0);

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        String timeStamp = String.valueOf(System.currentTimeMillis());

        /*
        on clicking the user from the users list we have passed the users UID  using intent
        so get that UID here to get the profile picture,name ,start chat with that user
         */
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");
        currentUserName = intent.getStringExtra("user_name");
        userProfileImage = intent.getStringExtra("userProfileImage");
        chat_key = intent.getStringExtra("chat_key");
        userCategory = intent.getStringExtra("accountCategory");
        Log.d(TAG, "onCreate: the value of account category is " + userCategory);
        // chat_key = intent.getStringExtra("chatId");

        //init the fire base auth
        myUid = FirebaseAuth.getInstance().getUid();
        userDbRef = FirebaseDatabase.getInstance().getReference("Users");
        userDbRef.keepSynced(true);
        chatListRef = FirebaseDatabase.getInstance().getReference("ChatList");
        chatListRef.keepSynced(true);
        specialRef = FirebaseDatabase.getInstance().getReference("Special");
        specialRef.keepSynced(true);
        mRootref = FirebaseDatabase.getInstance().getReference();
        mRootref.keepSynced(true);
        mchatSeen = FirebaseDatabase.getInstance().getReference().child("chat");
        mSpecialchatSeen = FirebaseDatabase.getInstance().getReference().child("Special Chat");
        mchatSeen.keepSynced(true);
        chatReportsReference = FirebaseDatabase.getInstance().getReference().child("Chat Reports");
        chatReportsReference.keepSynced(true);
        chatWarningsRef = FirebaseDatabase.getInstance().getReference().child("Chat Warnings");
        chatWarningsRef.keepSynced(true);

        //userChatBgTask userChatBgTask=new userChatBgTask();
        //userChatBgTask.execute();

        //setting the value from the personal chat fragment
        //set data
        nameTv.setText(currentUserName);
        try {
            //image received and set it to the tool bar
            Picasso.get().load(userProfileImage).placeholder(R.drawable.profile_image).into(profileIv);
        } catch (Exception e) {
            //there is exception in getting the picture
            //use a default picture
            Picasso.get().load(R.drawable.profile_image).into(profileIv);
        }
        seenMessages(hisUid,chat_key);
        loadMessages();
        mRootref
                .child("Users")
                .child(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String typingStatus = (String) dataSnapshot.child("typingTo").getValue();

                        //set the typing status of the user
                        if (typingStatus.equals(myUid)) {
                            userStatusTv.setText("typing...");
                        } else {
                            //get value of online status of the user
                            String onlineStatus = dataSnapshot.child("onlineStatus").getValue().toString();
                            if (onlineStatus.equals("online")) {
                                userStatusTv.setText(onlineStatus);
                            } else {
                                Get_Time_ago getTimeAgo = new Get_Time_ago();
                                long lastTime = Long.parseLong(onlineStatus);

                                String last_seen_time = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                                userStatusTv.setText("Last seen" + " " + last_seen_time);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        mRefreshlayout.setEnabled(true);
        btnAttach.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        userNameLayout.setOnClickListener(this);
        chatOptions.setOnClickListener(this);
        //check edit text change listener
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().trim().length() == 0) {
                    checkTypingStatus("noOne");
                } else {
                    checkTypingStatus(hisUid); //  uid of receiver
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        mRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;

                loadMoreMessages();

            }
        });


    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootref
                .child("messages")
                .child(chat_key);
        messageRef.keepSynced(true);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(20);
        messageQuery.keepSynced(true);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)) {
                    messagesList.add(itemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }
                if (itemPos == 1) {
                    mLastKey = messageKey;
                }

                mAdapter.notifyDataSetChanged();
                mRefreshlayout.setRefreshing(false);
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
    private void currentUser(String userId){
        SharedPreferences.Editor editor=getSharedPreferences("SP_USER",MODE_PRIVATE).edit();
        editor.putString("Current_USERID",userId);
        editor.apply();
    }


    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typingTo", typing);
        //update value of online status of current user
        dbRef.updateChildren(hashMap);
    }

    private void loadMessages() {
        Log.d(TAG, "loadMessages: the current date is " + saveCurrentDate);
        Log.d(TAG, "loadMessages: the value of chat key is" + chat_key);
        DatabaseReference messageRef = mRootref
                .child("messages")
                .child(chat_key);
        messageRef.keepSynced(true);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        messageQuery.keepSynced(true);
        messageQuery.orderByChild("time").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message1 = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos == 1) {
                    mLastKey = dataSnapshot.getKey();
                    mPrevKey = dataSnapshot.getKey();
                }
                messagesList.add(message1);
                mAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messagesList.size() - 1);
                mRefreshlayout.setRefreshing(false);

//                for (Messages message:messagesList) {
//
//                }
                if (!messagesList.get(messagesList.size() - 1).getFrom().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    String lastMessage=messagesList.get(messagesList.size()-1).getMessage();
                    Toast.makeText(UserChatActivity.this, ""+lastMessage, Toast.LENGTH_SHORT).show();
                    if (lastMessage.contains("Hi...")){
                        sendMessage("hi there","personal");
                    }
                }



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

        /*
        messageQuery.orderByChild("time").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message1 = dataSnapshot.getValue(Messages.class);
                Log.d(TAG, "onChildAdded: the value of datasnapshot is "+dataSnapshot);
                Log.d(TAG, "onChildAdded: the value of s is "+s);
                String userid = dataSnapshot.getKey();
                messagesList.add(message1);
                mAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messagesList.size() - 1);
                mRefreshlayout.setRefreshing(false);

            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }

        });
         */

        final DatabaseReference db1 = mchatSeen;
        mchatSeen.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(hisUid)) {
                    db1.child(myUid).child(hisUid).child("seen").setValue(true);
                    db1.child(hisUid).child(myUid).child("seen").setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void seenMessages(final String userId, String chat_key){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("messages")
                .child(chat_key);
        Query seenQuery=reference.orderByChild("seen").equalTo(false);
        seenQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Messages messages=snapshot.getValue(Messages.class);
                    if (messages.getReceiver().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            &&messages.getFrom().equals(userId)){
                        HashMap<String, Object> seenMap = new HashMap<>();
                        seenMap.put("seen",true);
                        snapshot.getRef().updateChildren(seenMap);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.attach_button:
                //now show the image pick up dialog
                showImagePickDialog();

                break;
            case R.id.sendBtn:

                notify=true;
                //here get text from the edit text
                String message = messageEt.getText().toString().trim();
                /*
                after clicking the send button
                just start a new background async task
                 */
                if (TextUtils.isEmpty(message)) {
                    messageEt.setError("Please enter the message first");
                } else {
                    sendMsgTask sendMsgTask = new sendMsgTask();
                    sendMsgTask.execute(message, userCategory);
                }
                break;
            case R.id.nameLayout:
                sendUserToProfile();


                break;
            case R.id.OptionsUserChat:
                showOptions();
                break;
            default:
                break;
        }
    }

    private void sendUserToProfile() {
//        Intent intentToProfile = new Intent(UserChatActivity.this, AccountProfileActivity.class);
//        intentToProfile.putExtra("visit_user_id", hisUid);
//        startActivity(intentToProfile);
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//        finish();
        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id",hisUid);
        navController.navigate(R.id.action_global_accountProfileFragment,bundle);
    }

    private void showOptions() {

        //show a alert dialog

        //options to show in the dialog box
        String[] options = {"View contact", "Older chats", "Dark Mode", "Report", "Block", "Clear chat"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(UserChatActivity.this);
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog items click
                if (which == 0) {
                    //View Contact  clicked
                    sendUserToProfile();


                } else if (which == 1) {
                    //older chats clicked
                    //open the date picker dialog box
                    //open the date picker dialog box
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                    final DatePickerDialog datePickerDialog = new DatePickerDialog(UserChatActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                                    datePicker.setMaxDate(new Date().getTime());
                                    month = month + 1;
                                    String formattedMonth = "" + month;
                                    String formattedDayOfMonth = "" + dayOfMonth;
                                    if (month < 10) {

                                        formattedMonth = "0" + month;
                                    }
                                    if (dayOfMonth < 10) {

                                        formattedDayOfMonth = "0" + dayOfMonth;
                                    }
                                    String selectedDate = formattedDayOfMonth + "-" + formattedMonth + "-" + year;
                                    Log.d(TAG, "onDateSet: the selected date is" + selectedDate);
                                    if (!messagesList.isEmpty()) {
                                        messagesList.clear(); //The list for update recycle view
                                    }
                                    mAdapter.notifyDataSetChanged();
                                    loadMessages();
                                    msgSendLayout.setVisibility(View.GONE);

                                }
                            }, year, month, dayOfMonth);
                    datePickerDialog.show();

                } else if (which == 2) {

                    chatLayout.setBackgroundColor(getResources().getColor(R.color.black));

                } else if (which == 3) {
                    //Report clicked
                    String[] ReportOptions = {"It's spam", "It's inappropriate"};
                    AlertDialog.Builder ReportsBuilder = new AlertDialog.Builder(UserChatActivity.this);
                    ReportsBuilder.setTitle("Why are you reporting this account?");
                    ReportsBuilder.setItems(ReportOptions, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //handle dialog click of reports
                            if (which == 0) {
                                //It's spam is clicked
                                final AlertDialog.Builder spamBuilder = new AlertDialog.Builder(UserChatActivity.this);
                                spamBuilder.setIcon(R.drawable.thank_icon);
                                spamBuilder.setTitle("Thanks for letting us know");
                                spamBuilder.setMessage("Your feedback is important in helping us keep the CraGee App community safe.");
                                chatReportsReference.child(myUid).child("Reported")
                                        .child(hisUid).setValue(true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                spamBuilder.create().show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UserChatActivity.this, "" + e
                                                .getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else if (which == 1) {
                                //It's inappropriate is clicked
                                String[] inappropriateOptions = {"Give Warning", "Report Account"};
                                AlertDialog.Builder inappropriateBuilder = new AlertDialog
                                        .Builder(UserChatActivity.this);
                                inappropriateBuilder.setTitle("Please select your action");
                                inappropriateBuilder.setItems(inappropriateOptions,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //handle dialog click of reports
                                                if (which == 0) {
                                                    //Give Warning clicked
                                                    final AlertDialog.Builder reportPostBuilder = new AlertDialog
                                                            .Builder(UserChatActivity.this);
                                                    reportPostBuilder.setIcon(R.drawable.thank_icon);
                                                    reportPostBuilder.setTitle("Thank you for your response");
                                                    reportPostBuilder.setMessage("CraGee App will send a warning to user");
                                                    /**
                                                     * code for the waring to the user is remaining
                                                     */
                                                    chatWarningsRef.child(myUid).child("Warned")
                                                            .child(hisUid).setValue(true)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    reportPostBuilder.create().show();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(UserChatActivity.this, "" + e
                                                                    .getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                                } else if (which == 1) {
                                                    /**
                                                     * code for reporting the account is remaining
                                                     */
                                                    //Report Account clicked
                                                    String[] reportAccountOptions = {"It's sending content that shouldn't be on CraGee App",
                                                            "It's pretending to be someone else", "It may be under the age of 13"};
                                                    AlertDialog.Builder reportAccountBuilder = new AlertDialog
                                                            .Builder(UserChatActivity.this);
                                                    reportAccountBuilder.setTitle("Why are you reporting this account?");
                                                    reportAccountBuilder.setItems(reportAccountOptions, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            //handle dialog click
                                                            if (which == 0) {
                                                                AlertDialog.Builder postContentBuilder = new AlertDialog.Builder(UserChatActivity.this);
                                                                postContentBuilder.setIcon(R.drawable.thank_icon);
                                                                postContentBuilder.setTitle("Thanks for letting us know");
                                                                postContentBuilder.setMessage("Posted content by the user will be check and removed if it violates our policies");
                                                                postContentBuilder.create().show();
                                                            } else if (which == 1) {
                                                                AlertDialog.Builder pretendBuilder = new AlertDialog.Builder(UserChatActivity.this);
                                                                pretendBuilder.setIcon(R.drawable.thank_icon);
                                                                pretendBuilder.setTitle("Thanks for letting us know");
                                                                pretendBuilder.setMessage("User's profile will be reviewed and verified");
                                                                pretendBuilder.create().show();
                                                            } else if (which == 2) {
                                                                AlertDialog.Builder ageBuilder = new AlertDialog.Builder(UserChatActivity.this);
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

    private void sendMessage(String message, String accountCategory) {

        if (!TextUtils.isEmpty(message)) {
            // String current_user_ref="messages/" + myUid + "/" +hisUid;
            //  String chat_user_ref="messages/" + hisUid + "/" +myUid;

            //  DatabaseReference user_message_push=mRootref.child("messages").child(myUid).child(hisUid).push();

            // user_message_push.keepSynced(true);
            // String push_id=user_message_push.getKey();
            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
            saveCurrentTime = currentTime.format(calForTime.getTime());
            HashMap<String, Object> messagemap = new HashMap<>();
            messagemap.put("message", message);
            messagemap.put("seen", false);
            messagemap.put("type", "text");
            messagemap.put("time", ServerValue.TIMESTAMP);
            messagemap.put("from", myUid);
            messagemap.put("receiver", hisUid);
            messagemap.put("messageDate", saveCurrentDate);
            messagemap.put("messageTime", saveCurrentTime);
            messagemap.put("messageCategory", "personal");
            /*
            message category is personal for personal messaging
            message category is profileShare for profile sharing
             */
            HashMap<String, Object> message_user_map = new HashMap<>();
            //   message_user_map.put(current_user_ref +"/" + push_id,messagemap);
            //  message_user_map.put(chat_user_ref + "/ " + push_id,messagemap);

            DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages")
                    .child(chat_key);
            messagesRef
                    .push()
                    .updateChildren(messagemap);
//////////////////////////////////////////////Adding user in Chat database////////////////////////////////////////////////////////
            mchatSeen.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(hisUid)) {
                        mRootref.child("chat").child(myUid).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (!dataSnapshot.hasChild(hisUid)) {

                                    HashMap<String, Object> chataddmap = new HashMap<>();
                                    chataddmap.put("seen", false);
                                    chataddmap.put("timestamp", ServerValue.TIMESTAMP);

                                    HashMap<String, Object> chatUserMap = new HashMap<>();
                                    chatUserMap.put("chat/" + myUid + "/" + hisUid, chataddmap);
                                    chatUserMap.put("chat/" + hisUid + "/" + myUid, chataddmap);

                                    mRootref.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
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


            Log.d(TAG, "sendMessage: the account category is " + accountCategory);
            if (accountCategory.equals("Bot")) {
                /*
                 *for the special bots the current message will also be stored
                 * in the special messages node
                 * special chat node
                 */
                DatabaseReference specialMessagesRef = FirebaseDatabase.getInstance().getReference("Special Messages")
                        .child(chat_key);
                specialMessagesRef
                        .push()
                        .setValue(messagemap);
                //////////////////////////////////////////////Adding user in Special Chat database////////////////////////////////////////////////////////
                mSpecialchatSeen.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            HashMap<String, Object> chataddmap = new HashMap<>();
                            chataddmap.put("seen", false);
                            chataddmap.put("timestamp", ServerValue.TIMESTAMP);
                            chataddmap.put("chatId", chat_key);
                            chataddmap.put("senderId", myUid);
                            chataddmap.put("receiverId", hisUid);

                            HashMap<String, Object> chatUserMap = new HashMap<>();
                            chatUserMap.put("Special Chat/" + hisUid + "/" + myUid, chataddmap);
                            mRootref.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                    }
                                }
                            });
                        } else {
                            if (!dataSnapshot.hasChild(hisUid)) {
                                HashMap<String, Object> chataddmap = new HashMap<>();
                                chataddmap.put("seen", false);
                                chataddmap.put("timestamp", ServerValue.TIMESTAMP);
                                chataddmap.put("chatId", chat_key);
                                chataddmap.put("senderId", myUid);
                                chataddmap.put("receiverId", hisUid);
                                HashMap<String, Object> chatUserMap = new HashMap<>();
                                chatUserMap.put("Special Chat/" + hisUid + "/" + myUid, chataddmap);

                                mRootref.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                        }
                                    }
                                });

                            } else {
                                HashMap<String, Object> chataddmap = new HashMap<>();
                                chataddmap.put("seen", false);
                                chataddmap.put("timestamp", ServerValue.TIMESTAMP);
                                chataddmap.put("chatId", chat_key);
                                chataddmap.put("senderId", myUid);
                                chataddmap.put("receiverId", hisUid);
                                HashMap<String, Object> chatUserMap = new HashMap<>();
                                chatUserMap.put("Special Chat/" + hisUid + "/" + myUid, chataddmap);
                                mRootref.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                        }
                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            DatabaseReference newNotificationRef = mRootref.child("messages_notifications").child(hisUid).push();
            String newNotificationId = newNotificationRef.getKey();

            HashMap<String, String> notificationData = new HashMap<String, String>();
            notificationData.put("from", myUid);
            notificationData.put("seen", "false");
            notificationData.put("message", message);
            HashMap<String, Object> requestMap = new HashMap<>();
            requestMap.put("messages_notifications/" + hisUid + "/" + newNotificationId, notificationData);

            DatabaseReference chatref = FirebaseDatabase.getInstance().getReference().child("chat");
            chatref.child(myUid).child(hisUid).child("timestamp").setValue(ServerValue.TIMESTAMP);
            chatref.child(hisUid).child(myUid).child("timestamp").setValue(ServerValue.TIMESTAMP);
            chatref.child(myUid).child(hisUid).child("seen").setValue(true);
            chatref.child(hisUid).child(myUid).child("seen").setValue(false);

            mRootref.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                }
            });
            try {
                recyclerView.smoothScrollToPosition(messagesList.size() - 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mRootref.updateChildren(message_user_map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });


            //send message notifications
            DatabaseReference msgNotifications = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
            msgNotifications.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ModelUsers modelUsers= snapshot.getValue(ModelUsers.class);
                    if (notify){
                        sendNotifications(hisUid,modelUsers.getName(),modelUsers.getImage(),message);
                    }

                    notify=false;
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void sendNotifications(String hisUid, String name, String image, String message) {

        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    Token token=ds.getValue(Token.class);
                    Data data=new Data(myUid,name+":"+message,"New Message",hisUid,R.drawable.logo_transparent);
                    Sender sender=new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                        @Override
                        public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                             Log.d(TAG, "onResponse: the value of response is "+response);

                        }

                        @Override
                        public void onFailure(Call<Response> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {


        currentUserTask currentUserTask = new currentUserTask();
        currentUserTask.execute();
        super.onStart();
    }

    private void checkCurrentUser() {
        if (myUid != null) {
            Log.i(TAG, "checkCurrentUser: current user exist" + myUid);
            Log.i(TAG, "checkCurrentUser: verifying the user existence");

        } else {
            // No user is signed in
            Log.i(TAG, "checkCurrentUser: no user is signed in");
            Intent intentLogin = new Intent(UserChatActivity.this, LoginActivity.class);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            startActivity(intentLogin);
            finish();
        }

    }

    //creating a background thread
    public class userChatBgTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: ");

        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: ");
            //search user to get that user's info
            Query userQuery = userDbRef.orderByChild("uid").equalTo(hisUid);
            userQuery.keepSynced(true);
            //get the user picture and name
            userQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //check until required info is received or not
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        //get data
                        String name = "" + ds.child("name").getValue();
                        String hisImage = "" + ds.child("image").getValue();
                        String typingStatus = "" + ds.child("typingTo").getValue();

                        if (typingStatus.equals(myUid)) {
                            userStatusTv.setText("typing...");
                        } else {
                            //get value of online status of the user
                            String onlineStatus = "" + ds.child("onlineStatus").getValue();
                            if (onlineStatus.equals("online")) {
                                userStatusTv.setText(onlineStatus);
                            } else {
                                //convert time stamp to proper time  date
                                //convert timestamp to dd/MM/yyyy hh:mm am/pm
                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                cal.setTimeInMillis(Long.parseLong(onlineStatus));

                                String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                                if (dateTime == null) {
                                    userStatusTv.setVisibility(View.GONE);
                                } else {
                                    userStatusTv.setText("Last seen" + " " + dateTime);
                                }

                            }
                        }
                        //set data
                        nameTv.setText(name);
                        try {
                            //image received and set it to the tool bar
                            Picasso.get().load(hisImage).placeholder(R.drawable.profile_image).into(profileIv);
                        } catch (Exception e) {
                            //there is exception in getting the picture
                            //use a default picture
                            Picasso.get().load(R.drawable.profile_image).into(profileIv);
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
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public class currentUserTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: ");

        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: ");

            checkCurrentUser();
            //set online status
            //   checkOnlineStatus("online");

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

    public class sendMsgTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: ");

        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: ");

            String writtenMessage = strings[0];
            String accountCategory = strings[1];

            //here first check if the text is empty or not
            if (TextUtils.isEmpty(writtenMessage)) {
                //text is empty
                Toast.makeText(UserChatActivity.this, "can not send the empty message", Toast.LENGTH_SHORT).show();
            } else {
                //text is not empty
                //send the message to the person
                sendMessage(writtenMessage, accountCategory);

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //after sending the message to the friend
            //reset the editText for next message
            messageEt.setText("");

        }

    }

    private void showImagePickDialog() {
        //two options camera /gallery to be shown in the dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(UserChatActivity.this);
        builder.setTitle("Choose image from");
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //item click handle
                if (which == 0) {
                    //camera clicked
                    //now we need to check the permission first

                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }

                }
                if (which == 1) {
                    //gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }

                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void pickFromGallery() {

        //intent to pick image from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);

    }

    private void pickFromCamera() {

        //intent to pick image from camera
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Desc");

        image_rui = UserChatActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_rui);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        //check if the storage permission is enabled or not
        //return true if enabled
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(UserChatActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;

    }

    private void requestStoragePermission() {
        //request run time storage permission
        ActivityCompat.requestPermissions(UserChatActivity.this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //check if the camera permission is enabled or not
        //return true if enabled
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(UserChatActivity.this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(UserChatActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }

    private void requestCameraPermission() {
        //request run time camera permission
        ActivityCompat.requestPermissions(UserChatActivity.this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //handle the permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method is called when user press allow or deny from  permission request dialog
        //here we will handle permission cases (allowed or denied)

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //both permission are granted
                        pickFromCamera();

                    } else {
                        //if camera gallery both permission are denied
                        Toast.makeText(UserChatActivity.this, "Camera and storage permissions both are necessary", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {

                        //storage permission are granted
                        pickFromGallery();

                    } else {
                        //if  gallery  permission is denied
                        Toast.makeText(UserChatActivity.this, "storage permissions  is necessary", Toast.LENGTH_SHORT).show();
                    }

                } else {

                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking the image from the camera or gallery
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from the gallery get uri of the image
                image_rui = data.getData();
                //use this image uri to upload the image to the fire base database

                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //use this image uri to upload the image to the fire base database

                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_rui) throws IOException {
        final String current_user_ref = "messages/" + myUid + "/" + hisUid;
        final String chat_user_ref = "messages/" + hisUid + "/" + myUid;

        DatabaseReference user_message_push = mRootref.child("messages").child(myUid).child(hisUid)
                .push();
        user_message_push.keepSynced(true);
        final String push_id = user_message_push.getKey();
        String timeStamp = "" + System.currentTimeMillis();
        String fileNameAndPath = "ChatImages/" + chat_key + "/" + "post_" + timeStamp;
        /*
        chat nodes will be created that will contain all the  images sent via chat
         */
        //get bitmap from image_uri
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image_rui);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();  //convert image to bytes
        StorageReference referenceImage = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        referenceImage.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image uploaded

                        //get url of the image upload
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            //add uri task and other info to the database
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
                            Calendar calForTime = Calendar.getInstance();
                            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
                            saveCurrentTime = currentTime.format(calForTime.getTime());
                            //set up required data
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("message", downloadUri);
                            hashMap.put("seen", false);
                            hashMap.put("type", "image");
                            hashMap.put("time", ServerValue.TIMESTAMP);
                            hashMap.put("from", myUid);
                            hashMap.put("receiver", hisUid);
                            hashMap.put("messageDate", saveCurrentDate);
                            hashMap.put("messageTime", saveCurrentTime);
                            DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages")
                                    .child(chat_key);
                            messagesRef
                                    .push()
                                    .updateChildren(hashMap);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //failed
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser(myUid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentUser("None");
    }
}

