package com.crageeApp.appbesocial.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Models.ModelStory;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.crageeApp.appbesocial.Stories.addStoryActivity;
import com.crageeApp.appbesocial.Stories.displayStoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterStories  extends RecyclerView.Adapter<AdapterStories.ViewHolder>{
    private Context mContext;
    private List<ModelStory> mStoriesList;
    private static final String TAG = "AdapterStories";

    public AdapterStories(Context mContext, List<ModelStory> mStoriesList) {
        this.mContext = mContext;
        this.mStoriesList = mStoriesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==0){
            View view=LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false);
            return new AdapterStories.ViewHolder(view);
        }else {
            View view=LayoutInflater.from(mContext).inflate(R.layout.stories_item_layout, parent, false);
            return new AdapterStories.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        ModelStory modelStory=mStoriesList.get(position);
        userInfo(holder,modelStory.getUserId(),position);


        if (holder.getAdapterPosition()!=0){
            seenStory(holder,modelStory.getUserId());
        }
        if (holder.getAdapterPosition()==0){
            myStory(holder.addStory_text,holder.story_plus,false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.getAdapterPosition()==0){
                    myStory(holder.addStory_text,holder.story_plus,true);
                }else {
                    //TODO:go to story
                    Intent intent=new Intent(mContext, displayStoryActivity.class);
                    intent.putExtra("userId", modelStory.getUserId());
                    mContext.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mStoriesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView story_photo,story_plus,story_photo_seen;
        private TextView story_username,addStory_text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            story_photo=itemView.findViewById(R.id.story_photo_item);
            story_photo_seen=itemView.findViewById(R.id.story_photo_seen);
            story_plus=itemView.findViewById(R.id.story_plus);
            story_username=itemView.findViewById(R.id.story_user_name);
            addStory_text=itemView.findViewById(R.id.add_story_text);


        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position==0){
            return 0;
        }
        return 1;
    }

    private void userInfo(final ViewHolder viewHolder, String uid, final int pos){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUsers modelUsers=dataSnapshot.getValue(ModelUsers.class);
                Log.d(TAG, "onDataChange: the user info is "+dataSnapshot);



                Picasso
                        .get()
                        .load(modelUsers.getImage())
                        .placeholder(R.drawable.profile_image)
                        .into(viewHolder.story_photo);



                if (pos!=0){
                    Picasso.get().load(modelUsers.getImage()).into(viewHolder.story_photo_seen);
                    viewHolder.story_username.setText(modelUsers.getName());
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void myStory(final TextView textView, final ImageView imageView, final boolean click){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Story").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long timecurrent=System.currentTimeMillis();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelStory modelStory=snapshot.getValue(ModelStory.class);
                    if (timecurrent> modelStory.getTimeStart() && timecurrent <modelStory.getTimeEnd()){
                        count++;
                    }

                }
                if (click){
                    if(count>0){
                        AlertDialog alertDialog=new AlertDialog.Builder(mContext).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO:go to  view story
                                Intent intent=new Intent(mContext, displayStoryActivity.class);
                                intent.putExtra("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                mContext.startActivity(intent);
                                dialog.dismiss();

                            }
                        });

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent=new Intent(mContext, addStoryActivity.class);
                                mContext.startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        alertDialog.show();
                    }else {
                        Intent intent=new Intent(mContext, addStoryActivity.class);
                        mContext.startActivity(intent);
                    }


                }else {
                    if (count>0){
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);

                    }else {
                        textView.setText("Add story");
                        imageView.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenStory(final ViewHolder viewHolder, String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    if (!snapshot.child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()&&
                    System.currentTimeMillis()<snapshot.getValue(ModelStory.class).getTimeEnd()){
                        i++;

                    }

                }

                if (i>0){
                    viewHolder.story_photo.setVisibility(View.VISIBLE);
                    viewHolder.story_photo_seen.setVisibility(View.GONE);

                }else {
                    viewHolder.story_photo.setVisibility(View.VISIBLE);
                    viewHolder.story_photo_seen.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
