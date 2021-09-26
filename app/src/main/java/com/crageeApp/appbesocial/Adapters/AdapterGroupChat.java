package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.AccountProfile.AccountProfileActivity;
import com.crageeApp.appbesocial.Groups.groupMessages;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.MyHolder> {
    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;
    private Context context;
    private List<groupMessages> chatList;
    private FirebaseUser fUser ;
    public AdapterGroupChat(Context context, List<groupMessages> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layouts
        //rowchatleft.xml for receiver
        //rowchat_right.xml for sender

        if (viewType==MSG_TYPE_LEFT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_left, parent, false);
            return new AdapterGroupChat.MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_right, parent, false);
            return new AdapterGroupChat.MyHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
          //get data
          String message=chatList.get(position).getMessage();
          //String timeStamp =chatList.get(position).getTimestamp();
          String messageTime =chatList.get(position).getMessageTime();
          String sender=chatList.get(position).getMessageSender().toLowerCase();
          final String senderUid = chatList.get(position).getFrom();
            Log.i(TAG, "the value of user name: "+sender);
            Log.i(TAG, "the value of message: "+message);
            //set data
             holder.messageTv.setText(message);
            holder.timeTv.setText(messageTime);
            holder.tVUserName.setText(sender);
            holder.tVUserName.setTextColor(Color.RED);

        //open the accountProfile on clicking the user name
        holder.tVUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: sending user to accountProfileActivity");
                Intent intentToUserProfile=new Intent(context, AccountProfileActivity.class);
                intentToUserProfile.putExtra("visit_user_id",senderUid);
                context.startActivity(intentToUserProfile);
            }
        });
    }
    @Override
    public int getItemCount() {
        return chatList.size();
    }
    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getFrom().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }
    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        // define all the views
        TextView messageTv,timeTv,tVUserName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            messageTv =itemView.findViewById(R.id.messageTv);
            timeTv =itemView.findViewById(R.id.timeTv);
            tVUserName =itemView.findViewById(R.id.sender_user_name);
        }
    }


}
