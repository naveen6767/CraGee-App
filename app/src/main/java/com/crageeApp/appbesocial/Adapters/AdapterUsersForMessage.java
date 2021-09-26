package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Chat.UserChatActivity;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsersForMessage extends RecyclerView.Adapter<AdapterUsersForMessage.MyHolder> {

    private Context context;
    private List<ModelUsers> usersList;
    private String senderUserId;
    private static final String TAG = "AdapterUsersForMessage";

    //constructor
    public AdapterUsersForMessage(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;
        //initialize the progress dialog for the user information fragment
        senderUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the userdisplay layout.xml
        View view = LayoutInflater.from(context).inflate(R.layout.users_display_layout, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        //get data
        final String hisUID = usersList.get(position).getUid();
        final String userThumb_img = usersList.get(position).getImage();
        final String username = usersList.get(position).getName();
        String userName = usersList.get(position).getName();
        final String userCategory = usersList.get(position).getAccountCategory();
        //set data
        holder.uNameTv.setText(userName);
        try {
            Picasso.get().load(userThumb_img)
                    .placeholder(R.drawable.profile_image)
                    .into(holder.avatarIv);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.profile_image).into(holder.avatarIv);

        }

        holder.addFriendBtn.setVisibility(View.INVISIBLE);
        holder.sendDirectMsgBtn.setVisibility(View.INVISIBLE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String chat_Id;
                final String receiverUserId = usersList.get(position).getUid();
                if (senderUserId.compareTo(receiverUserId) > 0) {
                    chat_Id = senderUserId + receiverUserId;
                } else {
                    chat_Id = receiverUserId + senderUserId;
                }
                Intent intent = new Intent(context, UserChatActivity.class);
                intent.putExtra("hisUid", receiverUserId);
                // intent.putExtra("onlineStatus", userOnlineStatus);
                intent.putExtra("user_name", username);
                intent.putExtra("userProfileImage", userThumb_img);
                intent.putExtra("chat_key", chat_Id);
                intent.putExtra("accountCategory", userCategory);

                context.startActivity(intent);
                /*
                 * A unique chat id will be created and this will be the direct
                 * child of the Chats node in the data base
                 * All messages between two users will be stored in this node

                senderUserId= FirebaseAuth.getInstance().getUid();
                receiverUserId=usersList.get(position).getUid();

                if (senderUserId.compareTo(receiverUserId)>0)
                {
                    chat_Id = senderUserId + receiverUserId;
                }
                else{
                    chat_Id = receiverUserId+senderUserId;
                }
                HashMap<String, Object> messageCounterMap = new HashMap<>();
                messageCounterMap.put("All Messages","0");
                messageCounterMap.put("Unseen Messages","0");
                messageCounterMap.put("Sender",senderUserId);
                messageCounterMap.put("receiver",receiverUserId);

                 */

            }


        });

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder {

        //define all the views in userDisplay layout
        CircleImageView avatarIv;
        TextView uNameTv;
        Button addFriendBtn, sendDirectMsgBtn;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //initialize all the views
            avatarIv = itemView.findViewById(R.id.user_profile_image);
            uNameTv = itemView.findViewById(R.id.user_profile_name);
            addFriendBtn = itemView.findViewById(R.id.followButton);
            sendDirectMsgBtn = itemView.findViewById(R.id.cancelButton);
        }
    }
}