package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Interfaces.RecommendationInterface;
import com.crageeApp.appbesocial.Models.ModelCelebRecommendation;
import com.crageeApp.appbesocial.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class AdapterCelebRecommendations extends RecyclerView.Adapter<AdapterCelebRecommendations.celebsViewHolder>{
    private Context mContext;
    private List<ModelCelebRecommendation> celebRecommendationList;
    private RecommendationInterface recommendationInterface;

    public AdapterCelebRecommendations(Context mContext, List<ModelCelebRecommendation> celebRecommendationList, RecommendationInterface recommendationInterface) {
        this.mContext = mContext;
        this.celebRecommendationList = celebRecommendationList;
        this.recommendationInterface = recommendationInterface;
    }

    @NonNull
    @Override
    public celebsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.recommendation_profile, parent, false);
        return new celebsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull celebsViewHolder holder, int position) {
        String userName=celebRecommendationList.get(position).getName();
        String userDp =celebRecommendationList.get(position).getImage();
        String userUid =celebRecommendationList.get(position).getUid();

        holder.accountName.setText(userName);
        //set user Dp
        try {
            Picasso.get().load(userDp).placeholder(R.drawable.profile_image).into(holder.accountImage);

        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.accountImage);
        }
        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recommendationInterface.onFollowClicked(position,userUid,userName,userDp,holder.followButton);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recommendationInterface.onItemClicked(position,userUid);
            }
        });




    }

    @Override
    public int getItemCount() {
        return celebRecommendationList.size();
    }

    public class celebsViewHolder extends RecyclerView.ViewHolder{

        private TextView accountName;
        private ImageView accountTick,accountImage;
        private Button followButton;

        public celebsViewHolder(@NonNull View itemView) {
            super(itemView);

            accountImage=(ImageView) itemView.findViewById(R.id.account_image_recommendation);
            accountName=(TextView) itemView.findViewById(R.id.account_name_recommendation);
            accountTick=(ImageView) itemView.findViewById(R.id.account_tick_recommendation);
            followButton=(Button) itemView.findViewById(R.id.follow_button_recommendation);




        }
    }
}
