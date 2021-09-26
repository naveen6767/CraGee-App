package com.crageeApp.appbesocial.Posts;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterComments;
import com.crageeApp.appbesocial.Models.ModelComments;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {

    private ImageView postCommentButton;
    private EditText commentInputText;
    private RecyclerView recyclerViewComments;
    private String published_post_id,currentUserId,postPublisherId,currentUserName,currentUserProfile,publishedPostImage;
    private DatabaseReference usersRef,postRef,mRootRef;
    private FirebaseAuth userAuth;
    private Toolbar commentsToolbar;

    private List<ModelComments> commentsList;
    private AdapterComments adapterComments;
    public static final String TAG = "Comments Activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        initializeViews();

        //set tool bar as the action bar
        setSupportActionBar(commentsToolbar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //getting the intent from the previous activity
        published_post_id = getIntent().getStringExtra("postId");
        postPublisherId =getIntent().getStringExtra("publisherId");
        publishedPostImage =getIntent().getStringExtra("publishedPostImage");
        //initialize the fire base here
        userAuth = FirebaseAuth.getInstance();
        currentUserId =userAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        postRef= FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(postPublisherId)
                .child("Posts")
                .child(published_post_id)
                .child("Comment");

        //layout (Linear layout) for recycler view

        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(this);
        // linearLayoutManager.setStackFromEnd(true);

        //recycler view properties
        recyclerViewComments.setHasFixedSize(true);
        recyclerViewComments.setLayoutManager(linearLayoutManager);
        //retrieve current user for the comments
        retrieveCurrentUserInfo();
        readComments();

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
    }

    private void readComments() {
        mRootRef
                .child("post_Comments")
                .child(postPublisherId)
                .child(published_post_id)
                .orderByChild("timeStamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        commentsList.clear();
                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                            ModelComments modelComments=snapshot.getValue(ModelComments.class);
                            commentsList.add(modelComments);
                            //adapter
                            adapterComments=new AdapterComments(CommentsActivity.this,commentsList);
                            adapterComments.notifyDataSetChanged();
                            recyclerViewComments.smoothScrollToPosition(adapterComments.getItemCount());
                            recyclerViewComments.setAdapter(adapterComments);
                        }
                    }
                    @Override

                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: "+databaseError.getMessage());
                        Log.i(TAG, "onCancelled: problem in reading the comments");
                    }
                });
    }

    private void postComment() {
        String commentText =commentInputText.getText().toString();
        if (TextUtils.isEmpty(commentText))
        {
            commentInputText.setError("Please write comment to text....");
        }
        else {
            Calendar calForData =Calendar.getInstance();
            SimpleDateFormat currentDate =new SimpleDateFormat("dd-MM-yyyy");
            final  String saveCurrentDate = currentDate.format(calForData.getTime());

            Calendar calForTime =Calendar.getInstance();
            SimpleDateFormat currentTime =new SimpleDateFormat("HH:mm");
            final  String saveCurrentTime = currentTime.format(calForTime.getTime());
            final String timeStamp =String.valueOf(System.currentTimeMillis());

            final  String random_comment_id =currentUserId+saveCurrentDate+saveCurrentTime+timeStamp;

            //retrieve user profile image from the database

            HashMap<String, Object> commentsMap =new HashMap<>();
            commentsMap.put("commenterId",currentUserId);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("userName",currentUserName);
            commentsMap.put("image",currentUserProfile);
            commentsMap.put("cLikes","0");
            commentsMap.put("timeStamp",timeStamp);
            commentsMap.put("cId",random_comment_id);
            commentsMap.put("currentPostId",published_post_id);
            commentsMap.put("originalPostOwnerId",postPublisherId);

            mRootRef
                    .child("post_Comments")
                    .child(postPublisherId)
                    .child(published_post_id)
                    .child(random_comment_id)
                    .updateChildren(commentsMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "onSuccess: commented successfully");
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            overridePendingTransition(0, 0);
                            commentInputText.setText("");

                            //generate notifications for comment if the current user is not the post publisher
                            if(!postPublisherId.equals(currentUserId)){
                                Log.i(TAG, "onDataChange:  both the publisher and the current user is  not same");
                                Log.i(TAG, "onDataChange: generate the notifications");

                                //generate notifications here
                                generateCommentNotification(postPublisherId);


                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i(TAG, "onFailure: "+e.getMessage());
                }
            });
        }
    }
 
    private void generateCommentNotification(final String postPublisherId) {
        final String notification;
        notification="commented on your post";

        mRootRef
                .child("Users")
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String currentUserName=(String) dataSnapshot.child("name").getValue();
                        String currentUserDp=(String) dataSnapshot.child("image").getValue();
                        String new_notification_id= mRootRef
                                                            .child("comment_notifications")
                                                            .child(postPublisherId)
                                                            .push()
                                                            .getKey();
                        HashMap<String, Object> notificationHashmap = new HashMap<>();
                        notificationHashmap.put("timeStamp", ServerValue.TIMESTAMP);
                        notificationHashmap.put("notifierName",currentUserName);
                        notificationHashmap.put("notifierDp",currentUserDp);
                        notificationHashmap.put("notifierUid",currentUserId);
                        notificationHashmap.put("notification",notification);
                        notificationHashmap.put("notifiedPostImage",publishedPostImage);
                        notificationHashmap.put("notifiedPostId",published_post_id);
                        notificationHashmap.put("notifiedPostPublisher",postPublisherId);
                        notificationHashmap.put("notificationId",new_notification_id);
                        Log.i(TAG, "onSuccess: publisher id"+postPublisherId);


                        if (new_notification_id != null) {
                            mRootRef
                                    .child("comment_notifications")
                                    .child(postPublisherId)
                                    .child(new_notification_id)
                                    .setValue(notificationHashmap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i(TAG, "onSuccess: notifications updated successfully");
                                            //update the notification in the new like notifications database
                                            HashMap<String, Object> notificationHashmap = new HashMap<>();
                                            notificationHashmap.put("timeStamp",ServerValue.TIMESTAMP);
                                            notificationHashmap.put("seen",false);
                                            mRootRef
                                                    .child("new_comment_notifications")
                                                    .child(postPublisherId)
                                                    .child(new_notification_id)
                                                    .setValue(notificationHashmap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "onSuccess: notification updated successfully in the new comment database");

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: failed to update the notifications in new comment notifications ");
                                                }
                                            });


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, "onFailure: notification update failed "+e.getMessage());
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void retrieveCurrentUserInfo() {
        mRootRef
                .child("Users")
                .child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    Log.i(TAG, "onDataChange: retrieving the current user name and image");
                    currentUserName =(String) dataSnapshot.child("name").getValue() ;
                    currentUserProfile=(String) dataSnapshot.child("image").getValue() ;

                }
                else {
                    Log.i(TAG, "onDataChange: user not found");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeViews() {
        recyclerViewComments =findViewById(R.id.comments_list);

        //initialize the toolbar
        commentsToolbar=findViewById(R.id.commentsToolBar);



        commentInputText =findViewById(R.id.comment_input);
        postCommentButton =findViewById(R.id.post_comment_button);

        //init the list here
        commentsList=new ArrayList<>();

    }


}
