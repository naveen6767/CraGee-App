package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Chat.UserChatActivity;
import com.crageeApp.appbesocial.Models.ModelChatlist;
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

import java.util.HashMap;
import java.util.List;

public class AdapterChatlist extends RecyclerView.Adapter<AdapterChatlist.MyHolder> {


    private Context context;
    private List<ModelUsers> userList;   //get user info
    private HashMap<String, String> lastMessageMap;
    private String chat_key;


    //constructor
    public AdapterChatlist(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap=new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the layout  rowChatlist.xml
        View view= LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        // get data
        final String hisUid =userList.get(position).getUid();
        String userName =userList.get(position).getName();
        String userImage =userList.get(position).getImage();
        String lastMessage=lastMessageMap.get(hisUid);
        //set data
        holder.nameTv.setText(userName);

        if (lastMessage==null||lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);

        }
        else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileIv);
        }
        catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.profileIv);
        }

        //set online status of other users in the chat list
        /*
        if (userList.get(position).getOnlineStatus().toString().equals("online"))
        {
            //online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);

        }
        else {
            //offline
            holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }

         */





        //handle click of the user in chat list
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference currentUserRef= FirebaseDatabase.getInstance().getReference("ChatList");
                currentUserRef
                        .child(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot:dataSnapshot.getChildren())
                                {
                                    ModelChatlist modelChatlist=snapshot.getValue(ModelChatlist.class);

                                    assert modelChatlist != null;
                                    if (modelChatlist.getId().equals(userList.get(position).getUid())){
                                        chat_key= modelChatlist.getChatId();
                                        //start chat activity with that user
                                        Intent intentToChat = new Intent(context, UserChatActivity.class);
                                        intentToChat.putExtra("hisUid",hisUid);
                                        intentToChat.putExtra("chatId",chat_key);
                                        context.startActivity(intentToChat);
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });




            }
        });


    }



    public void setLastMessageMap(String userId,String lastMessage){
        lastMessageMap.put(userId,lastMessage);
    }
    @Override
    public int getItemCount() {
        return userList.size();  //size of the list

    }

    public class MyHolder extends RecyclerView.ViewHolder{
        //views of rowChatlist.xml
        ImageView profileIv,onlineStatusIv;
        TextView nameTv,lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //initialize all the views
            nameTv=itemView.findViewById(R.id.nameTv);
            profileIv=itemView.findViewById(R.id.profileIv);
            onlineStatusIv=itemView.findViewById(R.id.onlineStatusIv);
            lastMessageTv=itemView.findViewById(R.id.lastMessageTv);
        }
    }

}
