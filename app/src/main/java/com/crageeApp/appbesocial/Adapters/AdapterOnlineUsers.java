package com.crageeApp.appbesocial.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterOnlineUsers extends RecyclerView.Adapter<AdapterOnlineUsers.MyHolder>{


    //constructor
    private Context context;
    private List<ModelUsers> usersList;
    private String chat_Id;
    private String chat_key;
    private FirebaseUser currentUser;
    private String currentUserId;
    private String clickedUserId;
    private static final String TAG = "AdapterUsers";
    public AdapterOnlineUsers(Context context, List<ModelUsers> usersList) {
        this.context = context;
        this.usersList = usersList;

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate the userdisplay layout.xml
        View view =LayoutInflater.from(context).inflate(R.layout.users_display_layout_online, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        //retrieving the current user and the user that has been clicked in the user list
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        currentUserId= FirebaseAuth.getInstance().getUid();
        clickedUserId=usersList.get(position).getUid();
       // String userOnlineStatus=String.valueOf(usersList.get(position).getOnlineStatus());

        //get data
        final String hisUid =usersList.get(position).getUid();
        String userImage =usersList.get(position).getImage();
        String userName =usersList.get(position).getName();
        String celebrityCategory=usersList.get(position).getCelebrityCategory();
        Log.d(TAG, "onBindViewHolder: "+userName);

        if (userName!=null){
            //set data
            holder.uNameTv.setText(userName);
        }else {
            holder.itemView.setVisibility(View.GONE);
        }

        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.profile_image)
                    .into(holder.avatarIv);
        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.avatarIv);
        }

        if (String.valueOf(celebrityCategory).equals("Original")){
            holder.blueTick.setVisibility(View.VISIBLE);
        }else {
            holder.blueTick.setVisibility(View.GONE);
        }





        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                click user from the user list to start chatting to the users
                Start activity for chat by putting the UID of receiver
                we will use that uid to identify the user we are gonna chat
                 */
//                Intent intent =new Intent(context, AccountProfileActivity.class);
//                intent.putExtra("visit_user_id",hisUid);
//                context.startActivity(intent);
                Bundle bundle = new Bundle();
                bundle.putString("visit_user_id",hisUid);
                Navigation.findNavController(v).navigate(R.id.action_global_accountProfileFragment, bundle);
            }
        });
    }



    @Override
    public int getItemCount() {
        return usersList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //define all the views in userDisplay layout
        CircleImageView avatarIv;
        TextView uNameTv;
        ImageView onlineStatus,blueTick;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //initialize all the views
            avatarIv=itemView.findViewById(R.id.user_profile_image);
            uNameTv=itemView.findViewById(R.id.user_profile_name);
            onlineStatus=itemView.findViewById(R.id.user_online_status);
            blueTick=itemView.findViewById(R.id.onlineUserBlueTick);
        }
    }
}
