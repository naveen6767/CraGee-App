package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Groups.chatGroupActivity;
import com.crageeApp.appbesocial.Models.ModelGroups;
import com.crageeApp.appbesocial.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterGroupChatlist extends RecyclerView.Adapter<AdapterGroupChatlist.MyHolder> {


    private Context context;
    private List<ModelGroups> groupsList;   //get user info
    private HashMap<String, String> lastMessageMap;
    public static final String TAG = "Constraints";

    //constructor
    public AdapterGroupChatlist(Context context, List<ModelGroups> groupsList) {
        this.context = context;
        this.groupsList = groupsList;
        lastMessageMap=new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the layout  row_group_chatlist.xml
        View view= LayoutInflater.from(context).inflate(R.layout.row_group_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        // get data
        String groupID=groupsList.get(position).getGroupId();
        String groupName = groupsList.get(position).getName();
        String groupImage = groupsList.get(position).getImageUrl();
        String lastMessage= lastMessageMap.get(groupID);

        Log.i(TAG, "onBindViewHolder:lastMessage "+""+lastMessage);

        //set data
        holder.group_name.setText(groupName);


        if (lastMessage==null||lastMessage.equals("default")){
            holder.lastMessage_Group.setVisibility(View.GONE);
        }
        else {
            holder.lastMessage_Group.setVisibility(View.VISIBLE);
            holder.lastMessage_Group.setText(lastMessage);
        }


        try {
            Picasso.get().load(groupImage).placeholder(R.drawable.profile_image).into(holder.groupCover);
        }
        catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.groupCover);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToGroupChat(position);
            }
        });
    }

    private void sendUserToGroupChat(int position) {
        String groupChatKEY=groupsList.get(position).getGroupChatKey();
        String groupUniqueID=groupsList.get(position).getGroupId();
        String groupUniqueName=groupsList.get(position).getName();
        Intent intentToGroupChat = new Intent(context, chatGroupActivity.class);
        intentToGroupChat.putExtra("groupName",groupUniqueName);
        intentToGroupChat.putExtra("groupChatKey",groupChatKEY);
        intentToGroupChat.putExtra("uniqueGroupId",groupUniqueID);
        context.startActivity(intentToGroupChat);

    }

    public void setLastMessageMap(String groupId,String lastMessage){
        lastMessageMap.put(groupId,lastMessage);
        Log.i(TAG, "setLastMessageMap:groupId "+""+lastMessageMap.get(groupId));
        Log.i(TAG, "setLastMessageMap: lastMessage"+""+lastMessageMap.get(lastMessage));
    }
    @Override
    public int getItemCount() {
        return groupsList.size();  //size of the list
    }
    class MyHolder extends RecyclerView.ViewHolder{
        //views of row_group_chatlist.xml
        ImageView groupCover;
        TextView group_name,lastMessage_Group;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //initialize all the views
            group_name=itemView.findViewById(R.id.group_nameTv);
            groupCover=itemView.findViewById(R.id.groupCoverIv);
            lastMessage_Group=itemView.findViewById(R.id.group_lastMessageTv);
        }
    }

}
