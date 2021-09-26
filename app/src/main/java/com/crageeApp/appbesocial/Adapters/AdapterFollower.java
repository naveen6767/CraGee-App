package com.crageeApp.appbesocial.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterFollower extends RecyclerView.Adapter<AdapterFollower.ViewHolder> {


    private ArrayList<ModelUsers> modelUsers;

    public AdapterFollower() {
    }

    public AdapterFollower(ArrayList<ModelUsers> modelUsers) {
        this.modelUsers = modelUsers;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.itemView.setTag(modelUsers.get(position));
        final String hisUid =modelUsers.get(position).getUid();
        String userImage =modelUsers.get(position).getImage();
        String userName =modelUsers.get(position).getName();


        if (userName!=null){
            //set data
            holder.name.setText(userName);
        }else {
            holder.itemView.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.profile_image)
                    .into(holder.image);
        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.image);
        }


    }

    @Override
    public int getItemCount() {
        return modelUsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.user_profile_name);
            image=itemView.findViewById(R.id.user_profile_image);



        }
    }
}
