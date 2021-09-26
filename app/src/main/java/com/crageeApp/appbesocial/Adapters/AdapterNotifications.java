package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Models.ModelNotifications;
import com.crageeApp.appbesocial.Posts.PostDetailsActivity;
import com.crageeApp.appbesocial.Posts.postDetailsInterface;
import com.crageeApp.appbesocial.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterNotifications extends RecyclerView.Adapter<AdapterNotifications.MyHolder> {

    private Context context;
    private ArrayList<ModelNotifications> notificationsList;
    private postDetailsInterface postDetailsInterface;


    public AdapterNotifications(Context context, ArrayList<ModelNotifications> notificationsList,postDetailsInterface postDetailsInterface) {
        this.context = context;
        this.notificationsList = notificationsList;
        this.postDetailsInterface=postDetailsInterface;
    }



    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate view row_notifications
        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications, parent, false);
        return new MyHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get and set data to the views
        //get data
        String requestNotifierUserName=notificationsList.get(position).getNotifierName();
        String requestNotifierDp=notificationsList.get(position).getNotifierDp();
        String requestNotifierText=notificationsList.get(position).getNotification();
        String requestNotifiedImage=notificationsList.get(position).getPostImage();
        String requestNotifiedTimeStamp= String.valueOf(notificationsList.get(position).getTimeStamp());
        String requestNotifierUserId=notificationsList.get(position).getNotifierUid();
        final String requestNotifierPostId=notificationsList.get(position).getNotifiedPostId();
        final String requestPostPublisherId=notificationsList.get(position).getNotifiedPostPublisher();


        //convert time stamp to dd/mm/yy hh/mm am/pm
        Calendar calendar =Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(requestNotifiedTimeStamp));
        String notificationTime= DateFormat.format("dd/MM/yy hh:mm aa",calendar).toString();
        //set data
        holder.notifierName.setText(requestNotifierUserName);
        holder.notificationText.setText(requestNotifierText);
        holder.tvDate.setText(notificationTime);
        try {
            Picasso.get().load(requestNotifierDp).placeholder(R.drawable.profile_image).into(holder.notifierDp);

        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.notifierDp);
        }

        if (requestNotifiedImage!=null){
            try {
                Picasso.get().load(requestNotifiedImage).placeholder(R.drawable.profile_image).into(holder.image_notify);

            }catch (Exception e)
            {
                Picasso.get().load(R.drawable.profile_image).into(holder.image_notify);
            }
        }else {
            holder.image_notify.setVisibility(View.GONE);
        }

        //handle the button clicks
        holder.image_notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String requestPostPublisherId=notificationsList.get(position).getNotifiedPostPublisher();
//                postDetailsFragment  fragment = new postDetailsFragment();
//                Bundle bundle = new Bundle();
//                bundle.putString("userId",requestPostPublisherId);
//                bundle.putString("currentPostId",requestNotifierPostId);
//                fragment.setArguments(bundle);
//                FragmentManager fragmentManager= ((AppCompatActivity)context).getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
//                        .replace(R.id.fragmentContainerHome,new postDetailsFragment())
//                        .addToBackStack(null)
//                        .commit();
                Intent intentToPostDetails = new Intent(context, PostDetailsActivity.class);
                intentToPostDetails.putExtra("userId",requestPostPublisherId);
                intentToPostDetails.putExtra("currentPostId",requestNotifierPostId);
                context.startActivity(intentToPostDetails);


            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToPostDetails = new Intent(context, PostDetailsActivity.class);
                intentToPostDetails.putExtra("userId",requestPostPublisherId);
                intentToPostDetails.putExtra("currentPostId",requestNotifierPostId);
                context.startActivity(intentToPostDetails);
            }
        });
        holder.notifierDp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("visit_user_id",requestNotifierUserId);
                Navigation.findNavController(v).navigate(R.id.action_global_accountProfileFragment, bundle);
            }
        });
    }
 
    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        //define all views from the row_notifications.xml
        CircleImageView notifierDp;
        TextView notifierName,notificationText,tvDate;
        ImageView image_notify;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            notifierDp=(CircleImageView) itemView.findViewById(R.id.user_profile_image);
            notifierName=(TextView) itemView.findViewById(R.id.notifyUserName);
            notificationText=(TextView) itemView.findViewById(R.id.dummyText);
            tvDate=(TextView) itemView.findViewById(R.id.dateTime);
            image_notify= itemView.findViewById(R.id.notifyImage);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postDetailsInterface.onItemClicked(getAdapterPosition());


                }
            });
        }
    }
}
