package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.AccountProfile.AccountProfileActivity;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsersInShare extends RecyclerView.Adapter<AdapterUsersInShare.MyHolder>{


    //constructor
    private Context context;
    private List<ModelUsers> usersList;

    private FirebaseUser currentUser;
    private String currentUserId;
    private String clickedUserId;
    private static final String TAG = "AdapterUsers";
    public AdapterUsersInShare(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the user display layout.xml
        View view =LayoutInflater.from(context).inflate(R.layout.users_share_message, parent, false);
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
        holder.uNameTv.setOnClickListener(new View.OnClickListener() {
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


        holder.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();




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
        private CircleImageView avatarIv;
        private TextView uNameTv;
        private Button sendButton;



        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //initialize all the views
            avatarIv=itemView.findViewById(R.id.user_profile_image_share);
            uNameTv=itemView.findViewById(R.id.user_profile_name);
            sendButton=itemView.findViewById(R.id.send_message_button);

        }
    }
}
