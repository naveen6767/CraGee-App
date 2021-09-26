package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{


    //constructor
    private Context context;
    private List<ModelUsers> usersList;
    private String chat_Id;
    private String chat_key;
    private FirebaseUser currentUser;
    private String currentUserId;
    private String clickedUserId;
    private static final String TAG = "AdapterUsers";



    public AdapterUsers(Context context, List<ModelUsers> usersList) {
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
       // String userOnlineStatus=String.valueOf(usersList.get(position).getOnlineStatus());

        //get data
        final String hisUid =usersList.get(position).getUid();
        String userImage =usersList.get(position).getImage();
        String userName =usersList.get(position).getName();
        Log.d(TAG, "onBindViewHolder: "+userName);

        if (userName!=null){
            //set data
            holder.uNameTv.setText(userName);
        }else {
            holder.itemView.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.profile_image)
                    .into(holder.avatarIv);
        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.avatarIv);
        }


        isFollowing(clickedUserId,holder.followBtn);

        /*first check if the visit_user_id is friend of current user id
        * if friends,invisible the add friend button
        * visibility of these button also depends upon the fact that the
        * item view can be clicked from messages activity,search activity,etc
         */
        //turning on the visibility of add friend and direct message button
        holder.followBtn.setVisibility(View.INVISIBLE);
        holder.sendDirectMsgBtn.setVisibility(View.INVISIBLE);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                click user from the user list to start chatting to the users
                Start activity for chat by putting the UID of receiver
                we will use that uid to identify the user we are gonna chat
                 */


//                AppCompatActivity activity = (AppCompatActivity) holder.itemView .getContext();
//                Fragment myFragment = new AccountProfileFragment();
//                Bundle args = new Bundle();
//                args.putString("visit_user_id",hisUid);
//                myFragment.setArguments(args);
//                activity.getSupportFragmentManager()
//                        .beginTransaction()
//                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
//                        .replace(R.id.fragmentContainerHome, myFragment).addToBackStack(null).commit();
                Bundle bundle = new Bundle();
                bundle.putString("visit_user_id",hisUid);
                Navigation.findNavController(v).navigate(R.id.action_global_accountProfileFragment, bundle);
            }
        });
    }

    private void isFollowing(final String clickedUserId, final Button followBtn) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId)
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(clickedUserId).exists())
                {
                    followBtn.setText("Following");
                }
                else {
                    followBtn.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        Button followBtn,sendDirectMsgBtn;
        ImageView onlineStatus;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //initialize all the views
            avatarIv=itemView.findViewById(R.id.user_profile_image);
            uNameTv=itemView.findViewById(R.id.user_profile_name);
            followBtn=itemView.findViewById(R.id.followButton);
            sendDirectMsgBtn=itemView.findViewById(R.id.directMessage_button);
            onlineStatus=itemView.findViewById(R.id.user_online_status);
        }
    }
}
