package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Interfaces.followersInterface;
import com.crageeApp.appbesocial.Models.ModelFollower;
import com.crageeApp.appbesocial.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFollowers extends RecyclerView.Adapter<AdapterFollowers.MyHolder>{


    private Context context;
    private List<ModelFollower> followersList;
    private followersInterface followersInterface;
    public AdapterFollowers(Context context, List<ModelFollower> followersList,followersInterface followersInterface) {
        this.context = context;
        this.followersList = followersList;
        this.followersInterface = followersInterface;
    }

    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the userdisplay layout.xml
        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        String userName=followersList.get(position).getName();
        String userDp =followersList.get(position).getImage();
        String userUid =followersList.get(position).getUid();


        if (userName!=null){
            //set data
            holder.uNameTv.setText(userName);
        }else {
            holder.itemView.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(userDp)
                    .placeholder(R.drawable.profile_image)
                    .into(holder.avatarIv);
        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.avatarIv);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                followersInterface.onItemClicked(position,userUid);


            }
        });
    }
    @Override
    public int getItemCount() {
        return followersList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //define all the views in userDisplay layout
        CircleImageView avatarIv;
        TextView uNameTv;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //initialize all the views
            avatarIv=itemView.findViewById(R.id.user_profile_image);
            uNameTv=itemView.findViewById(R.id.user_profile_name);


        }
    }
}
