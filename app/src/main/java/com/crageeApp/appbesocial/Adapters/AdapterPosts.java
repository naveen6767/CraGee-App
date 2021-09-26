package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.AccountProfile.AccountProfileActivity;
import com.crageeApp.appbesocial.Chat.Get_Time_ago;
import com.crageeApp.appbesocial.Models.ModelPosts;
import com.crageeApp.appbesocial.Posts.CommentsActivity;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AdapterPosts  extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    private Context context;
    private List<ModelPosts> postsList;
    private String myUid,publisherId;
    private DatabaseReference likesRef;  //for likes database node
    private DatabaseReference postsRef,allPostsRef;  //for posts database node
    private boolean mProcessLike =false;

    public AdapterPosts(Context context, List<ModelPosts> postsList) {
        this.context = context;
        this.postsList = postsList;

        //initialize the fire base here
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //likesRef =FirebaseDatabase.getInstance().getReference("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference("Users");
        allPostsRef = FirebaseDatabase.getInstance().getReference("Posts");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout allpostlayout.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.allposts_layout,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        //get data
        publisherId =postsList.get(position).getUid();
        String userName=postsList.get(position).getUserName();
        String userDp =postsList.get(position).getUserDp();
        String date =postsList.get(position).getDate();
        String time =postsList.get(position).getTime();
        final String postImage =postsList.get(position).getPostImage();
        String textPost =postsList.get(position).getTextPost();
        String pTime =postsList.get(position).getpTime();
        final String pId =postsList.get(position).getpId();
        String pLikes =postsList.get(position).getpLikes(); //contains total no of likes for a post

        Get_Time_ago getTimeAgo=new Get_Time_ago();
        long lastTime=Long.parseLong(pTime);

        String last_seen_time=getTimeAgo.getTimeAgo(lastTime,context);
        holder.pCurrentTime.setText(last_seen_time);
        //set data
        holder.pUserName.setText(userName);
        holder.pText.setText(textPost);
       // holder.pCurrentTime.setText(postCreateTime);
        holder.pTotalLikes.setText(pLikes+"Likes");   //e.g  100likes

        //set likes for each post
        setLikes(holder,pId,publisherId);
        //set user Dp
        try {
            Picasso.get().load(userDp).placeholder(R.drawable.profile_image).into(holder.postProfileImage);

        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.postProfileImage);
        }
        //set post image
        //if there is no image i.e.  pImage.equals("noImage") then hide the image view
        if (postImage.equals("noImage"))
        {
            //hide the image view
            holder.pImage.setVisibility(View.GONE);
        }
        else {
            //show the image view
            holder.pImage.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(postImage).into(holder.pImage);

            }catch (Exception e)
            {
                Picasso.get().load(R.drawable.profile_image).into(holder.pImage);
            }
        }
        //handle the button clicks
        holder.postOptionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // showMoreOptionsDialog(holder.postOptionsBtn,publisherId,myUid,pId,postImage);
            }
        });
        holder.postProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FirebaseAuth.getInstance().getUid().equals(publisherId)){
                    sendUserToAccountProfile();
                }
                else {
                    holder.postProfileImage.setEnabled(false);
                }
            }
        });
        holder.pUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FirebaseAuth.getInstance().getUid().equals(publisherId)){
                    sendUserToAccountProfile();
                }
                else {
                    holder.pUserName.setEnabled(false);
                }
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //here get the total no of likes for the post,whose like button has been clicked
                //if currently signed in user has not liked in before
                //increase value by 1 otherwise decrease by 1
                Log.i(TAG, "onClick: like button clicked");
                Log.i(TAG, "onClick: clicking the mProcess like"+mProcessLike);

                final int pLikes = Integer.parseInt(postsList.get(position).getpLikes());
                Log.i(TAG, "onClick: PLikes just after clicking the like button"+pLikes);
                mProcessLike=true;
                //get id of the post clicked
                final String postId=postsList.get(position).getpId();
                final String publisherID=postsList.get(position).getUid();
                postsRef
                        .child(publisherID)
                        .child("Posts")
                        .child(postId)
                        .child("Likes")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.i(TAG, "onDataChange: deciding whether like or not ");
                                if (mProcessLike) {
                                    Log.i(TAG, "onDataChange: currently the value of mProcessLike"+ mProcessLike);
                                    if (dataSnapshot.hasChild(myUid)) {
                                        //already liked so remove like
                                        Log.i(TAG, "already liked so remove like ");
                                        Log.i(TAG, "onDataChange: current pLikes"+pLikes);
                                        postsRef.child(publisherID).child("Posts").child(postId)
                                                .child("pLikes").setValue("" + (pLikes - 1));
                                        postsRef.child(publisherID).child("Posts").child(postId)
                                                .child("Likes").child(myUid).removeValue();
                                        postsList.get(position).setpLikes(String.valueOf(pLikes-1));
                                        Log.i(TAG, "onDataChange: current pLikes after setting value"+pLikes);

                                        mProcessLike = false;
                                        postsRef.child(publisherID).child("Posts").child(postId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild("pLikes")){
                                                            String postLikes =(String) dataSnapshot.child("pLikes").getValue();
                                                            Log.i(TAG, "onDataChange: posts likes"+postLikes);
                                                            holder.pTotalLikes.setText(postLikes+"Likes");
                                                            Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Log.i(TAG, "onCancelled: "+databaseError.getMessage());
                                                    }
                                                });
                                    } else {
                                        Log.i(TAG, "not liked like it ");
                                        Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);
                                        //not liked like it
                                        postsRef
                                                .child(publisherID)
                                                .child("Posts")
                                                .child(postId)
                                                .child("pLikes")
                                                .setValue("" + (pLikes + 1));
                                        postsRef.child(publisherID).child("Posts").child(postId)
                                                .child("Likes").child(myUid)
                                                .setValue("Liked");
                                        // don't generate notifications if the publisher id is equal to the current  user id
                                        if (publisherID.equals(FirebaseAuth.getInstance().getUid())){
                                            Log.i(TAG, "onDataChange: both the publisher and the current user is same");
                                            Log.i(TAG, "onDataChange: not generating the notifications");
                                        }
                                        else {
                                            Log.i(TAG, "onDataChange:  both the publisher and the current user is  not same");
                                            Log.i(TAG, "onDataChange: generate the notifications");

                                            //generate notifications here
                                            generateLikeNotification(publisherID);
                                            final String notification;
                                            notification="liked your post";
                                            final String timeStamp =String.valueOf(System.currentTimeMillis());
                                            postsRef.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    String currentUserName=(String) dataSnapshot.child("name").getValue();
                                                    String currentUserDp=(String) dataSnapshot.child("image").getValue();
                                                    HashMap<String, Object> notificationHashmap = new HashMap<>();
                                                    notificationHashmap.put("timeStamp",timeStamp);
                                                    notificationHashmap.put("notifierName",currentUserName);
                                                    notificationHashmap.put("notifierDp",currentUserDp);
                                                    notificationHashmap.put("notifierUid",myUid);
                                                    notificationHashmap.put("notification",notification);
                                                    notificationHashmap.put("postImage",postImage);
                                                    notificationHashmap.put("notifiedPostId",pId);
                                                    notificationHashmap.put("notifiedPostPublisher",publisherID);
                                                    Log.i(TAG, "onSuccess: publisher id"+publisherID);
                                                    Log.i(TAG, "onSuccess: timeStamp"+timeStamp);
                                                    postsRef.child(publisherID).child("Notifications")
                                                            .child(timeStamp)
                                                            .setValue(notificationHashmap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.i(TAG, "onSuccess: notifications updated successfully");
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.i(TAG, "onFailure: notification update failed "+e.getMessage());
                                                        }
                                                    });
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }

                                        postsList.get(position).setpLikes(String.valueOf(pLikes+1));
                                        mProcessLike = false;
                                        postsRef.child(publisherID).child("Posts").child(postId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild("pLikes")){
                                                            String postLikes =(String) dataSnapshot.child("pLikes").getValue();
                                                            Log.i(TAG, "onDataChange: posts likes"+postLikes);
                                                            holder.pTotalLikes.setText(postLikes+"Likes");
                                                            Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                mProcessLike=true;


                allPostsRef
                        .child(postId)
                        .child("Likes")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (mProcessLike){
                                    if (dataSnapshot.hasChild(myUid)) {
                                        //already liked so remove like
                                        Log.i(TAG, "already liked so remove like ");
                                        Log.i(TAG, "onDataChange: current pLikes"+pLikes);
                                        allPostsRef.child(postId)
                                                .child("pLikes").setValue("" + (pLikes - 1));
                                        allPostsRef.child(postId)
                                                .child("Likes").child(myUid).removeValue();
                                        postsList.get(position).setpLikes(String.valueOf(pLikes-1));
                                        Log.i(TAG, "onDataChange: current pLikes after setting value"+pLikes);
                                        mProcessLike = false;
                                        allPostsRef.child(postId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild("pLikes")){
                                                            String postLikes =(String) dataSnapshot.child("pLikes").getValue();
                                                            Log.i(TAG, "onDataChange: posts likes"+postLikes);
                                                            holder.pTotalLikes.setText(postLikes+"Likes");
                                                            Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Log.i(TAG, "onCancelled: "+databaseError.getMessage());
                                                    }
                                                });
                                    }
                                    else {

                                        Log.i(TAG, "not liked like it ");
                                        Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);
                                        //not liked like it
                                        allPostsRef
                                                .child(postId)
                                                .child("pLikes")
                                                .setValue("" + (pLikes + 1));
                                        allPostsRef
                                                .child(postId)
                                                .child("Likes").child(myUid)
                                                .setValue("Liked");
                                        // don't generate notifications if the publisher id is equal to the current  user id
                                        if (publisherID.equals(FirebaseAuth.getInstance().getUid())){
                                            Log.i(TAG, "onDataChange: both the publisher and the current user is same");
                                            Log.i(TAG, "onDataChange: not generating the notifications");
                                        }
                                        else {
                                            Log.i(TAG, "onDataChange:  both the publisher and the current user is  not same");
                                            Log.i(TAG, "onDataChange: generate the notifications");

                                            //generate notifications here
                                            generateLikeNotification(publisherID);
                                            final String notification;
                                            notification="liked your post";
                                            final String timeStamp =String.valueOf(System.currentTimeMillis());
                                            postsRef.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    String currentUserName=(String) dataSnapshot.child("name").getValue();
                                                    String currentUserDp=(String) dataSnapshot.child("image").getValue();
                                                    HashMap<String, Object> notificationHashmap = new HashMap<>();
                                                    notificationHashmap.put("timeStamp",timeStamp);
                                                    notificationHashmap.put("notifierName",currentUserName);
                                                    notificationHashmap.put("notifierDp",currentUserDp);
                                                    notificationHashmap.put("notifierUid",myUid);
                                                    notificationHashmap.put("notification",notification);
                                                    notificationHashmap.put("postImage",postImage);
                                                    notificationHashmap.put("notifiedPostId",pId);
                                                    notificationHashmap.put("notifiedPostPublisher",publisherID);
                                                    Log.i(TAG, "onSuccess: publisher id"+publisherID);
                                                    Log.i(TAG, "onSuccess: timeStamp"+timeStamp);
                                                    postsRef.child(publisherID).child("Notifications")
                                                            .child(timeStamp)
                                                            .setValue(notificationHashmap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Log.i(TAG, "onSuccess: notifications updated successfully");
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.i(TAG, "onFailure: notification update failed "+e.getMessage());
                                                        }
                                                    });
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                        postsList.get(position).setpLikes(String.valueOf(pLikes+1));
                                        mProcessLike = false;
                                        allPostsRef
                                                .child(postId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild("pLikes")){
                                                            String postLikes =(String) dataSnapshot.child("pLikes").getValue();
                                                            Log.i(TAG, "onDataChange: posts likes"+postLikes);
                                                            holder.pTotalLikes.setText(postLikes+"Likes");
                                                            Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);

                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

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

            private void generateLikeNotification(final String publisherID) {
                final String notification;
                notification="liked your post";
                final String timeStamp =String.valueOf(System.currentTimeMillis());
                postsRef.child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String currentUserName=(String) dataSnapshot.child("name").getValue();
                        String currentUserDp=(String) dataSnapshot.child("image").getValue();
                        HashMap<String, Object> notificationHashmap = new HashMap<>();
                        notificationHashmap.put("timeStamp",timeStamp);
                        notificationHashmap.put("notifierName",currentUserName);
                        notificationHashmap.put("notifierDp",currentUserDp);
                        notificationHashmap.put("notifierUid",myUid);
                        notificationHashmap.put("notification",notification);
                        notificationHashmap.put("postImage",postImage);
                        notificationHashmap.put("notifiedPostId",pId);
                        notificationHashmap.put("notifiedPostPublisher",publisherID);
                        Log.i(TAG, "onSuccess: publisher id"+publisherID);
                        Log.i(TAG, "onSuccess: timeStamp"+timeStamp);
                        postsRef.child(publisherID).child("Notifications")
                                .child(timeStamp)
                                .setValue(notificationHashmap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i(TAG, "onSuccess: notifications updated successfully");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "onFailure: notification update failed "+e.getMessage());
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String postId=postsList.get(position).getpId();
                final String publisherID=postsList.get(position).getUid();
                //start post details activity
                Intent intentToComments = new Intent(context, CommentsActivity.class);
                intentToComments.putExtra("postId",postId); //will get the details of the post using this Id here id of the post that has been clicked
                intentToComments.putExtra("publisherId",publisherID); //will get the publisher id(publisherId) of the post using this Id here id of the user that has created the post
                context.startActivity(intentToComments);
            }
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will implement later
                Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendUserToAccountProfile() {
        Intent intentToUserProfile = new Intent(context, AccountProfileActivity.class);
        intentToUserProfile.putExtra("visit_user_id",publisherId);
        context.startActivity(intentToUserProfile);
    }

    private void setLikes(final MyHolder holder, final String postKey, final String uid) {
        postsRef
                .child(uid)
                .child("Posts")
                .child(postKey)
                .child("Likes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(myUid))
                        {
                            //user has liked this post
                    /*to indicate that the post is liked by this (signed in) user
                    change drawable left icon of the like button
                    change text of like button from like to liked
                     */
                            holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked_button,0,0,0);
                            //holder.likeBtn.setText("Liked");
                            Log.i(TAG, "user has liked the post ");



                        }
                        else {
                            //user has  not liked this post
                            //user has liked this post
                    /*
                    to indicate that the post is not liked by this (signed in) user
                    change drawable left icon of the like button
                    change text of like button from liked to like
                    */
                            holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_button,0,0,0);
                            //holder.likeBtn.setText("Like");
                            Log.i(TAG, "user has  not liked the post ");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptionsDialog(ImageButton postOptionsBtn, String uid, String myUid, final String pId, final String postimage) {
        //creating more option having delete option
        //other options will be added later
        PopupMenu popupMenu =new PopupMenu(context,postOptionsBtn,Gravity.CENTER);

        //show the delete options in posts to the currently signed in user
        if (uid.equals(myUid))
        {
            //add items to the menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete Post");
        }

        //apply click listener to the menu items
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id =item.getItemId();
                if (id==0)
                {
                    //delete is clicked
                    beginDelete(pId,postimage);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete(String pId, String postimage) {
        //post can be with or without image
        if (postimage.equals("noImage"))
        {
            //post is without image
            deleteWithoutImage(pId,postimage);
        }
        else {
            //post is with image
            deleteWithImage(pId,postimage);
        }
    }
    private void deleteWithImage(final String pId, String postimage) {



        /*Steps
        1. delete image using url
        2. delete from the data base using post id
         */
        StorageReference imageReference = FirebaseStorage.getInstance().getReferenceFromUrl(postimage);
        imageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //here image will be deleted from the storage
                        //now delete from the  database

                        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds:dataSnapshot.getChildren())
                                {
                                    ds.getRef().removeValue(); //remove values from fire base where pId matches

                                }
                                //deleted
                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed in deleting the post

                        Toast.makeText(context, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage(String pId, String postimage) {




        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ds.getRef().removeValue(); //remove values from fire base where pId matches

                }
                //deleted
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }
//view holder class

    class MyHolder extends RecyclerView.ViewHolder
    {
        //views from the allpostlayout.xml are defined here
        CircleImageView postProfileImage;
        ImageView pImage;
        TextView pUserName,pText,pCurrentTime,pTotalLikes;
        Button likeBtn,commentBtn,shareBtn;
        ImageButton postOptionsBtn;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //initialize all the views here
            postProfileImage=itemView.findViewById(R.id.postProfileImageValue);
            pImage=itemView.findViewById(R.id.postImage);
            pUserName=itemView.findViewById(R.id.postProfileName);
            pText=itemView.findViewById(R.id.postDescription);
            pCurrentTime=itemView.findViewById(R.id.currentPostTime);
            pTotalLikes=itemView.findViewById(R.id.totalPostLikes);
            likeBtn=itemView.findViewById(R.id.like_button);
            commentBtn=itemView.findViewById(R.id.comment_button);
            shareBtn=itemView.findViewById(R.id.share_button);
            postOptionsBtn=itemView.findViewById(R.id.postOptions);

        }
    }
}
