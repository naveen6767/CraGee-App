package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Models.ModelChat;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;


public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{


    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;
    private Context context;
    private List<ModelChat> chatList;
    private FirebaseUser fUser ;
    private static final String TAG = "AdapterChat";
    public AdapterChat(Context context, List<ModelChat> chatList) {
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
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String message=chatList.get(position).getMessage();
        String type =chatList.get(position).getType();
        String messageTime =chatList.get(position).getMessageTime().toLowerCase();



        //convert timestamp to dd/MM/yyyy hh:mm am/pm
        /*Calendar cal =Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp)*1000L);

        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
         */


        if (type.equals("text")){
                //text
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageIv.setVisibility(View.GONE);

            holder.messageTv.setText(message);
        }
        else {
            //image message
            holder.messageTv.setVisibility(View.GONE);
            holder.messageIv.setVisibility(View.VISIBLE);

            Picasso.get().load(message).placeholder(R.drawable.icon_image_chat).into(holder.messageIv);

        }
        //set data

        holder.timeTv.setText(messageTime);
        holder.timeTv.setTextColor(Color.BLACK);

        //set seen /delivered status of the message
        if (position== chatList.size()-1){
            if (chatList.get(position).isSeen()){
                holder.btnSendReceipt.setImageResource(R.drawable.icon_send_message_blue);
            }
            else {
                holder.btnSendReceipt.setImageResource(R.drawable.icon_send_message);
            }
        }
        else {
            Log.i(TAG, "onBindViewHolder: chat list size is 0");
            Log.i(TAG, "onBindViewHolder: no messages inside the chat list");
        }

        /*
         if (chatList.size()>0){
            Log.i(TAG, "onBindViewHolder: setting the send receipt ");
            if (chatList.get(position).getIsSeen().equals("true"))
            {
                Log.i(TAG, "onBindViewHolder: "+chatList.get(position).getIsSeen().equals("true"));
                Log.i(TAG, "onBindViewHolder: setting the text to seen");
                holder.btnSendReceipt.setImageResource(R.drawable.icon_send_message_blue);


            }
            else {

                Log.i(TAG, "onBindViewHolder: setting the text to delivered");
                holder.btnSendReceipt.setImageResource(R.drawable.icon_send_message);

            }

        }
        else {
            Log.i(TAG, "onBindViewHolder: chat list size is 0");
            Log.i(TAG, "onBindViewHolder: no messages inside the chat list");

        }
         */




    }
    @Override
    public int getItemCount() {
        return chatList.size();
    }
    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }
    //view holder class
     public class MyHolder extends RecyclerView.ViewHolder{

        // define all the views
        TextView messageTv,timeTv;
        ImageButton btnSendReceipt;
        ImageView messageIv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init all the view defined
            messageTv =itemView.findViewById(R.id.messageTv);
            messageIv =itemView.findViewById(R.id.messageIv);
            timeTv =itemView.findViewById(R.id.timeTv);
            btnSendReceipt =itemView.findViewById(R.id.sendReceiptButton);
        }
    }
}
