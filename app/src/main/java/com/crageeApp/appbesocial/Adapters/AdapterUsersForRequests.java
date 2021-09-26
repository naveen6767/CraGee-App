package com.crageeApp.appbesocial.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.AccountProfile.AccountProfileActivity;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsersForRequests extends RecyclerView.Adapter<AdapterUsersForRequests.MyHolder>{

    private Context context;
    private List<ModelUsers> usersList;
    private String chat_Id;
    private String chat_key;
    private FirebaseUser currentUser;
    private String currentUserId,clickedUserId,currentGroupId;
    public static final String TAG = "Constraints";
    //constructor
    public AdapterUsersForRequests(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
    }
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the userdisplay layout.xml
        View view =LayoutInflater.from(context).inflate(R.layout.users_display_layout, parent, false);
        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        //retrieving the current user and the user that has been clicked in the user list
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        currentUserId= FirebaseAuth.getInstance().getUid();
        clickedUserId=usersList.get(position).getUid();

        //get data
        final String hisUid =usersList.get(position).getUid();
        String userImage =usersList.get(position).getImage();
        String userName =usersList.get(position).getName();

        //getting the group id from the group information activity
        Intent intent = ((Activity) context).getIntent();
        currentGroupId= intent.getStringExtra("GroupId");
        Log.i(TAG, "onBindViewHolder: current group id "+currentGroupId);
        //set data
        holder.uNameTv.setText(userName);
        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.profile_image)
                    .into(holder.avatarIv);
        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image)
                    .into(holder.avatarIv);
        }


        /*first check if the visit_user_id is friend of current user id
        * if friends,invisible the add friend button
        * visibility of these button also depends upon the fact that the
        * item view can be clicked from messages activity,search activity,etc
         */
        //turning on the visibility of accept and cancel button
        holder.acceptBtn.setVisibility(View.VISIBLE);
        holder.cancelBtn.setVisibility(View.VISIBLE);

        //handle the accept button here
        holder.acceptBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                 Log.i(TAG, " Accept button clicked ");
                DatabaseReference groupsRef= FirebaseDatabase.getInstance().getReference("Groups");
                final DatabaseReference usersRef= FirebaseDatabase.getInstance().getReference("Users");
                /*
                 * before accepting the join request check the request in the current time
                 */

                //making a group member after accepting the group join request
                groupsRef
                        .child(currentGroupId)
                        .child("Group Members")
                        .child(hisUid)
                        .setValue(true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "onSuccess: group member added successfully");
                                //after addition to group members
                                //add it to the sender id as group joined
                                usersRef.child(hisUid).child("Group Joined")
                                        .child(currentGroupId).child("groupID")
                                        .setValue(currentGroupId)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i(TAG, "onSuccess: Group added in the group joined ");
                                                // remove the join request from the database
                                                final DatabaseReference groupReqRef= FirebaseDatabase
                                                        .getInstance()
                                                        .getReference("Group Requests");
                                                groupReqRef.child(hisUid).child(currentGroupId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            groupReqRef.child(currentGroupId)
                                                                    .child(hisUid)
                                                                    .removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            holder.itemView.setVisibility(View.GONE);
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.i(TAG, "failed"+e.getMessage());
                                                                }
                                                            });

                                                        }
                                                        else {
                                                            Log.i(TAG, "group request removal failed");
                                                            Log.i(TAG, "group request removal failed"+task.getException().getMessage());
                                                        }
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.i(TAG, "onFailure: group Joined failed "+e.getMessage());
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: group member updation failed "+e.getMessage());

                    }
                });
            }
        });

        holder.cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference groupReqRef= FirebaseDatabase.getInstance().getReference("Group Requests");
                groupReqRef.child(hisUid).child(currentGroupId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            groupReqRef.child(currentGroupId).child(hisUid).removeValue();
                            holder.itemView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                click user from the user list to start chatting to the users
                Start activity for chat by putting the UID of receiver
                we will use that uid to identify the user we are gonna chat
                 */
                Intent intent =new Intent(context, AccountProfileActivity.class);
                intent.putExtra("visit_user_id",hisUid);
                context.startActivity(intent);
            }
        });
    }



    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //define all the views in userDisplay layout
        CircleImageView avatarIv;
        TextView uNameTv;
        Button acceptBtn,cancelBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //initialize all the views
            avatarIv=itemView.findViewById(R.id.user_profile_image);
            uNameTv=itemView.findViewById(R.id.user_profile_name);
            acceptBtn=itemView.findViewById(R.id.requestAcceptButton);
            cancelBtn=itemView.findViewById(R.id.cancelButton);

        }
    }
}
