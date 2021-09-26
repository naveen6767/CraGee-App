package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Groups.GroupInformationActivity;
import com.crageeApp.appbesocial.Models.ModelGroups;
import com.crageeApp.appbesocial.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterGroupsForEarning extends RecyclerView.Adapter<AdapterGroupsForEarning.MyHolder>{


    private Context context;
    private List<ModelGroups> groupsList;
    private DatabaseReference groupsRef;
    public static final String TAG = "Adapter group earnings";

    //constructor
    public AdapterGroupsForEarning(Context context, List<ModelGroups> groupsList) {
        this.context = context;
        this.groupsList = groupsList;

    }
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the layout row_groups.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_groups_earning, parent, false);
        return new MyHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //get data
        final String gImage= groupsList.get(position).getImageUrl();
        final String gName= groupsList.get(position).getName();
        final String gCreator= groupsList.get(position).getGroupCreator();
        final String gId= groupsList.get(position).getGroupId();
        final String gPrivacy= groupsList.get(position).getPrivacy();
        final String gCategory= groupsList.get(position).getCategory();
        final String gAbout= groupsList.get(position).getStatus();
        final String gChatKey= groupsList.get(position).getGroupChatKey();
        String gEarnings=groupsList.get(position).getEarning();
        Log.i(TAG, "onBindViewHolder:the value of group earnings are "+gEarnings);
        groupsRef= FirebaseDatabase.getInstance().getReference("Groups");

        //set data
        holder.groupName.setText(gName);
        switch (gPrivacy){
            case "Open Group":
                holder.groupPrivacy.setText(gPrivacy);
                holder.groupPrivacy.setTextColor(ContextCompat.getColor(context, R.color.green));
                break;
            case "Public Group":
                holder.groupPrivacy.setText(gPrivacy);
                holder.groupPrivacy.setTextColor(Color.BLUE);
                break;
            case "Private Group":
                holder.groupPrivacy.setText(gPrivacy);
                holder.groupPrivacy.setTextColor(Color.RED);
                break;
            default:
                break;
        }

        holder.groupEarnings.setText(gEarnings+".00");
        //get the group  cover image through the picasso
        try {
            Picasso.get().load(gImage).resize(60, 60)
                    .centerCrop().placeholder(R.drawable.profile_image)
                    .into(holder.requestGroupCoverImage);
        }
        catch (Exception e)
        {
            Toast.makeText(context, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //handle the item click listener
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToGroupInfo = new Intent(context, GroupInformationActivity.class);
                intentToGroupInfo.putExtra("groupName",gName);
                intentToGroupInfo.putExtra("groupId",gId);
                intentToGroupInfo.putExtra("groupAdmin",gCreator);
                intentToGroupInfo.putExtra("groupImage",gImage);
                intentToGroupInfo.putExtra("groupPrivacy",gPrivacy);
                intentToGroupInfo.putExtra("groupCategory",gCategory);
                intentToGroupInfo.putExtra("groupAbout",gAbout);
                intentToGroupInfo.putExtra("groupChatKey",gChatKey);
                context.startActivity(intentToGroupInfo);
            }
        });
    }
    @Override
    public int getItemCount() {
        return groupsList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{
        //define all the views in the user row_groups layout
        ImageView requestGroupCoverImage;
        TextView groupName,groupPrivacy,groupEarnings;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init all the views here
            requestGroupCoverImage=itemView.findViewById(R.id.group_cover_image);
            groupName=itemView.findViewById(R.id.group_name);
            groupPrivacy=itemView.findViewById(R.id.group_privacy);
            groupEarnings=itemView.findViewById(R.id.group_earning);
        }
    }
}

