package com.crageeApp.appbesocial.Posts;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterComments;
import com.crageeApp.appbesocial.Home.Fragments.HomeFragment;
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


public class postCommentsFragment extends Fragment {

    private View postComments;

 

    private ImageView postCommentButton;
    private EditText commentInputText;
    private RecyclerView recyclerViewComments;
    private String published_post_id,currentUserId,postPublisherId,
            currentUserName,currentUserProfile,publishedPostImage,itemPosition;
    private DatabaseReference usersRef,postRef,mRootRef;
    private FirebaseAuth userAuth;
    private ImageButton backButton;
    private static final String TAG = "postCommentsFragment";

    private List<ModelComments> commentsList;
    private AdapterComments adapterComments;


    public postCommentsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        postComments= inflater.inflate(R.layout.fragment_post_comments, container, false);
        recyclerViewComments =postComments.findViewById(R.id.comments_list);



        commentInputText =postComments.findViewById(R.id.comment_input);
        postCommentButton =postComments.findViewById(R.id.post_comment_button);
        backButton =postComments.findViewById(R.id.back_button_comments);

        //init the list here
        commentsList=new ArrayList<>();



        //getting the intent from the previous activity
        Bundle bundle=this.getArguments();
        if (bundle != null) {
            Log.d(TAG, "onCreateView: bundle not null");
            published_post_id =  bundle.getString("postId");
            postPublisherId =bundle.getString("publisherId");
            publishedPostImage =bundle.getString("publishedPostImage");
            itemPosition =bundle.getString("itemPosition",itemPosition);
        }



        //initialize the fire base here
        userAuth = FirebaseAuth.getInstance();
        currentUserId =userAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef = FirebaseDatabase.getInstance().getReference();


        //layout (Linear layout) for recycler view

        LinearLayoutManager linearLayoutManager =new LinearLayoutManager(postComments.getContext());
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

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                getActivity().onBackPressed();
                Bundle bundle = new Bundle();
                bundle.putString("postId",published_post_id);
                bundle.putString("publisherId",postPublisherId);
                bundle.putString("publishedPostImage",publishedPostImage);
                bundle.putString("itemPosition",itemPosition);

                HomeFragment fragment = new HomeFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager= ((AppCompatActivity)postComments.getContext()).getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
//                        .replace(R.id.fragmentContainerHome,fragment)
//                        .addToBackStack(null)
//                        .commit();
            }
        });
        return postComments;
    }


    private void readComments() {
        // TODO: 27-10-2020 update the method for the comments like messages
        mRootRef
                .child("post_Comments")
                .child(published_post_id)
                .orderByChild("timeStamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        commentsList.clear();
                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                            ModelComments modelComments=snapshot.getValue(ModelComments.class);
                            commentsList.add(modelComments);
                            //adapter
                            adapterComments=new AdapterComments(postComments.getContext(),commentsList);
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
                    .child(published_post_id)
                    .child(random_comment_id)
                    .updateChildren(commentsMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "onSuccess: commented successfully");
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

}