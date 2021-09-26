package com.crageeApp.appbesocial.Chat;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;
    private static final int MSG_CATEGORY=2;
    private List<Messages> mMessageList;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser ;
    private Context context;
    private static final String TAG = "MessageAdapter";

    public MessageAdapter(List<Messages> mMessageList, Context context) {
        this.mMessageList = mMessageList;
        this.context = context;
    }
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        //inflate layouts
        //rowchatleft.xml for receiver
        //rowchat_right.xml for sender

        if (viewType==MSG_TYPE_LEFT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MessageViewHolder(view);
        }
        else if (viewType==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MessageViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right_profile_share, parent, false);
            return new MessageViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        /*

        fAuth = FirebaseAuth.getInstance();

        String current_user_id = fAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(position);
        String from_user = c.getFrom();
        String message_type=c.getType();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        if (from_user.equals(current_user_id)){

            holder.messageIn.setVisibility(View.GONE);
            holder.messageOut.setVisibility(View.VISIBLE);

            params.gravity = Gravity.RIGHT;
            holder.mainMessageLayout.setLayoutParams(params);

        } else {

            holder.messageIn.setVisibility(View.VISIBLE);
            holder.messageOut.setVisibility(View.GONE);


            params.gravity = Gravity.LEFT;
            holder.mainMessageLayout.setLayoutParams(params);

        }

        holder.messageTextIn.setText(c.getMessage());
        holder.messageTextOut.setText(c.getMessage());
        holder.itemView.setTag(mMessageList.get(position));

         */

        //get data
        String message=mMessageList.get(position).getMessage();
        String type =mMessageList.get(position).getType();
        String messageTime =mMessageList.get(position).getMessageTime().toLowerCase();



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

//        String lastMessage=mMessageList.get(mMessageList.size()-1).getMessage();
//
//        if (lastMessage.equals("Hi")){
//            //auto reply the user
////            holder.messageTv.setVisibility(View.VISIBLE);
////            holder.messageIv.setVisibility(View.GONE);
////            holder.messageTv.setText(message);
//        }







        //set seen /delivered status of the message

/*
        if (position== mMessageList.size()-1){
            if (mMessageList.get(position).isSeen()){
                holder.btnSendReceipt.setImageResource(R.drawable.icon_send_message_blue);
            }
            else {
                holder.btnSendReceipt.setImageResource(R.drawable.icon_send_message);
            }
        }
        else {
          //  holder.btnSendReceipt.setVisibility(View.GONE);
            Log.i(TAG, "onBindViewHolder: chat list size is 0");
            Log.i(TAG, "onBindViewHolder: no messages inside the chat list");
        }

 */

         if (mMessageList.size()>0){
            Log.i(TAG, "onBindViewHolder: setting the send receipt ");
            if (mMessageList.get(position).isSeen())
            {
                Log.i(TAG, "onBindViewHolder: "+mMessageList.get(position).isSeen());
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



    }
    public class MessageViewHolder extends RecyclerView.ViewHolder {

        // define all the views
        TextView messageTv,timeTv;
        ImageButton btnSendReceipt;
        ImageView messageIv;
        public MessageViewHolder(View view) {
            super(view);

            //init all the view defined
            messageTv =itemView.findViewById(R.id.messageTv);
            messageIv =itemView.findViewById(R.id.messageIv);
            timeTv =itemView.findViewById(R.id.timeTv);
            btnSendReceipt =itemView.findViewById(R.id.sendReceiptButton);

        }

    }
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mMessageList.get(position).getFrom().equals(fUser.getUid())){
            //means the message sender is the current user
            //check if the message is the sent text or some shared profile
            if (mMessageList.get(position).getMessageCategory().equals("profileShare")){

                return MSG_CATEGORY;

            }else {
                return MSG_TYPE_RIGHT;
            }

        }
        else {
            return MSG_TYPE_LEFT;

        }
    }

}
