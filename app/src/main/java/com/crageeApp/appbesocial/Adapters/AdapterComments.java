package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.AccountProfile.AccountProfileActivity;
import com.crageeApp.appbesocial.Chat.Get_Time_ago;
import com.crageeApp.appbesocial.Models.ModelComments;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder>{


    private Context context;
    private List<ModelComments> commentsList;
    private boolean mProcessLike =false;

    private String myUid;
    private DatabaseReference likesRef;  //for likes database node
    private DatabaseReference postsRef;  //for posts database node
    private DatabaseReference mRootRef;  //for  database node
    public AdapterComments(Context context, List<ModelComments> commentsList) {
        this.context = context;
        this.commentsList = commentsList;


        //initialize the fire base here
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //likesRef =FirebaseDatabase.getInstance().getReference("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference("Users");
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout all_comments_layout.xml
        View view = LayoutInflater.from(context)
                .inflate(R.layout.all_comments_layout,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        //get data
        final String commentPublisherId =commentsList.get(position).getCommenterId();
        String commentedText=commentsList.get(position).getComment();
        String commentedUserName=commentsList.get(position).getUserName();
        String commentedUserImage=commentsList.get(position).getImage();
        String cLikes =commentsList.get(position).getcLikes();
        String cId =commentsList.get(position).getcId();
        String cTime=commentsList.get(position).getTimeStamp();
        final String commentId=commentsList.get(position).getcId();
        final String originalPostID=commentsList.get(position).getCurrentPostId();
        final String postOwner=commentsList.get(position).getOriginalPostOwnerId();

        Get_Time_ago getTimeAgo=new Get_Time_ago();
        long lastTime=Long.parseLong(cTime);
        String last_seen_time=getTimeAgo.getTimeAgo(lastTime,context);
        holder.date.setText(last_seen_time);
        //set data
        holder.userName.setText(commentedUserName);
        holder.comment.setText(commentedText);
        holder.tCommentLikes.setText(cLikes+" "+"Likes");
        //set user Dp
        try {
            Picasso.get().load(commentedUserImage)
                    .placeholder(R.drawable.profile_image).into(holder.userProfile);

        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.userProfile);
        }

        //set likes for each post
        setLikes(holder,cId,commentPublisherId,originalPostID,postOwner);
        //profile opening
        holder.userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToUserProfile = new Intent(context, AccountProfileActivity.class);
                intentToUserProfile.putExtra("visit_user_id",commentPublisherId);
                context.startActivity(intentToUserProfile);
            }
        });
        holder.userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToUserProfile = new Intent(context, AccountProfileActivity.class);
                intentToUserProfile.putExtra("visit_user_id",commentPublisherId);
                context.startActivity(intentToUserProfile);

            }
        });
        //like button in comment
        holder.commentLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //here get the total no of likes for the comment,whose like button has been clicked
                //if currently signed in user has not liked it before
                //increase value by 1 otherwise decrease by 1
                Log.i(TAG, "onClick: like button clicked");
                Log.i(TAG, "onClick: clicking the mProcess like"+mProcessLike);

                final int cLikes = Integer.parseInt(commentsList.get(position).getcLikes());
                Log.i(TAG, "onClick: PLikes just after clicking the like button"+cLikes);
                mProcessLike=true;
                //get id of the post clicked

                mRootRef
                        .child("post_Comments")
                        .child(originalPostID)
                        .child(commentId)
                        .child("commentLikes")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.i(TAG, "onDataChange: deciding whether like or not ");
                                if (mProcessLike){
                                    Log.i(TAG, "onDataChange: currently the value of mProcessLike"+mProcessLike);
                                    if (dataSnapshot.hasChild(myUid)){
                                        Log.i(TAG, "onDataChange: current user has already liked the comment");
                                        Log.i(TAG, "already liked so remove like ");
                                        Log.i(TAG, "onDataChange: current pLikes"+cLikes);
                                        mRootRef
                                                .child("post_Comments")
                                                .child(originalPostID)
                                                .child(commentId)
                                                .child("cLikes")
                                                .setValue("" + (cLikes - 1));

                                        mRootRef
                                                .child("post_Comments")
                                                .child(originalPostID)
                                                .child(commentId)
                                                .child("commentLikes")
                                                .child(myUid)
                                                .removeValue();
                                        commentsList.get(position).setcLikes(String.valueOf(cLikes-1));
                                        Log.i(TAG, "onDataChange: current cLikes after setting value"+cLikes);

                                        mProcessLike = false;
                                        mRootRef
                                                .child("post_Comments")
                                                .child(originalPostID)
                                                .child(commentId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild("cLikes")){
                                                            String commentLikes =(String) dataSnapshot.child("cLikes").getValue();
                                                            Log.i(TAG, "onDataChange: comment likes"+commentLikes);
                                                            holder.tCommentLikes.setText(commentLikes+"Likes");
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
                                        Log.i(TAG, "onDataChange: current user has not liked the comment");
                                        //not liked like the comment here

                                        mRootRef
                                                .child("post_Comments")
                                                .child(originalPostID)
                                                .child(commentId)
                                                .child("cLikes")
                                                .setValue("" + (cLikes + 1));


                                        mRootRef
                                                .child("post_Comments")
                                                .child(originalPostID)
                                                .child(commentId)
                                                .child("commentLikes")
                                                .child(myUid)
                                                .setValue("Liked");
                                        //now update the values and change the like heart
                                        commentsList.get(position).setcLikes(String.valueOf(cLikes+1));
                                        mProcessLike = false;
                                        mRootRef
                                                .child("post_Comments")
                                                .child(originalPostID)
                                                .child(commentId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild("cLikes")){
                                                            String commentLikes =(String) dataSnapshot.child("cLikes").getValue();
                                                            Log.i(TAG, "onDataChange:  comment Likes"+commentLikes);
                                                            //holder.tCommentLikes.setVisibility(View.VISIBLE);
                                                            holder.tCommentLikes.setText(commentLikes+"Likes");
                                                            Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Log.i(TAG, "onCancelled: error in setting likes");
                                                        Log.i(TAG, "onCancelled: "+databaseError.getMessage());

                                                    }
                                                });

                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.i(TAG, "onCancelled: "+databaseError);

                            }
                        });
            }
        });
    }

    private void setLikes(final MyHolder holder, String cId, String commentPublisherId, String originalPostID, String postOwner) {

        mRootRef
                .child("post_Comments")
                .child(originalPostID)
                .child(cId)
                .child("commentLikes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(myUid)){
                            //user has liked this comment
                                /*to indicate that the comment is liked by this (signed in) user
                                change drawable left icon of the like button
                                change text of like button from like to liked
                                 */
                            holder.commentLikeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like_icon,0,0,0);
                            Log.i(TAG, "user has liked the comment ");

                        }
                        else {
                            //user has  not liked the comment

                            /*
                            to indicate that the comment is not liked by this (signed in) user
                            change drawable left icon of the like button
                            change text of like button from liked to like
                            */
                            holder.commentLikeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_love_button,0,0,0);
                            Log.i(TAG, "user has  not liked the comment ");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        // define all the views
        TextView userName,date,comment,tCommentLikes;
        CircleImageView userProfile;
        Button commentLikeBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init all the view defined
            userName=itemView.findViewById(R.id.comment_user_name);
            date=itemView.findViewById(R.id.comment_date);
            comment=itemView.findViewById(R.id.comment_text);
            userProfile=itemView.findViewById(R.id.user_profile_Image_Comments);
            commentLikeBtn=itemView.findViewById(R.id.comment_like_button);
            tCommentLikes=itemView.findViewById(R.id.comment_likes);
        }
    }
}
