package com.crageeApp.appbesocial.Chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Groups.chatGroupActivity;
import com.crageeApp.appbesocial.Login.LoginActivity;
import com.crageeApp.appbesocial.Models.ModelPrnslChatFragment;
import com.crageeApp.appbesocial.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class groupsChatFragment extends Fragment {

    private RecyclerView mchatist;
    private DatabaseReference groupChatDatabase,mchatTime;
    private DatabaseReference groupsDatabase,currentUserRef;
    private FirebaseAuth userAuth;
    private TextView mtime;
    private LinearLayoutManager mlinearlayout;
    private FirebaseRecyclerAdapter<ModelPrnslChatFragment, groupChatViewHolder> groupChatRecyclerAdapter;
    private String currentUserId,saveCurrentDate;
    private View groupChatView;
    private Query query;
    private  DatabaseReference mRootref;
    private static final String TAG = "groupsChatFragment";
    public groupsChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupChatView= inflater.inflate(R.layout.fragment_groups_chat, container, false);

        mchatist = (RecyclerView) groupChatView.findViewById(R.id.groupsChatList);

        userAuth = FirebaseAuth.getInstance();
        if (userAuth.getCurrentUser()!=null)
        {
            currentUserId = userAuth.getCurrentUser().getUid();
        }
        else
        {
            startActivity(new Intent(getActivity(), LoginActivity.class));

        }
        groupChatDatabase = FirebaseDatabase.getInstance().getReference().child("Group Chats").child(currentUserId);
        groupChatDatabase.keepSynced(true);
        mchatTime=FirebaseDatabase.getInstance().getReference().child("group chat").child(currentUserId);
        mchatTime.keepSynced(true);
        query=mchatTime.orderByChild("timestamp");
        query.keepSynced(true);
        mRootref=FirebaseDatabase.getInstance().getReference();
        groupsDatabase= FirebaseDatabase.getInstance().getReference().child("Groups");
        groupsDatabase.keepSynced(true);
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        mchatist.setHasFixedSize(true);
        mlinearlayout=new LinearLayoutManager(getContext());
        mchatist.setLayoutManager(mlinearlayout);
        mlinearlayout.setStackFromEnd(true);
        mlinearlayout.setReverseLayout(true);
        return groupChatView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ModelPrnslChatFragment> options=
                new FirebaseRecyclerOptions.Builder<ModelPrnslChatFragment>()
                        .setQuery(query,ModelPrnslChatFragment.class)
                        .setLifecycleOwner(this)
                        .build();
        groupChatRecyclerAdapter=new FirebaseRecyclerAdapter<ModelPrnslChatFragment, groupChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final groupChatViewHolder holder, int position, @NonNull ModelPrnslChatFragment model) {
                final String groupID=getRef(position).getKey();

                groupsDatabase.child(groupID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                  String  groupChatKey=(String) dataSnapshot.child("groupChatKey").getValue();

                                  retrieveLastMessage(groupChatKey);
                                }
                            }

                            private void retrieveLastMessage(String groupChatKey) {
                                Log.d(TAG, "onBindViewHolder: the value of group id is "+groupID);
                                DatabaseReference groupMessagesRef=mRootref
                                        .child("Group Chats")
                                        .child(groupChatKey);

                                Query lastMessageQuery=groupMessagesRef.orderByChild("time").limitToLast(1);
                                lastMessageQuery.keepSynced(true);
                                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                        String message=(String) dataSnapshot.child("message").getValue();
                                        String from=(String) dataSnapshot.child("from").getValue();
                                        String type=(String) dataSnapshot.child("type").getValue();

                                        holder.setMessage(message,type);
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                groupsDatabase
                        .child(groupID)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                final String  groupName =(String) dataSnapshot.child("name").getValue();
                                final String  groupThumb_img = (String)dataSnapshot.child("imageUrl").getValue();
                                final String  groupChatKey = (String)dataSnapshot.child("groupChatKey").getValue();
                                holder.setName(groupName);
                                //viewHolder2.setStatus(userstatus);
                                holder.setThumb_image(groupThumb_img, getContext());


                                holder.mview2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intentToGroupChat=new Intent(getContext(), chatGroupActivity.class);
                                        intentToGroupChat.putExtra("uniqueGroupId",groupID);
                                        intentToGroupChat.putExtra("groupName",groupName);
                                        intentToGroupChat.putExtra("groupImage",groupThumb_img);
                                        intentToGroupChat.putExtra("groupChatKey",groupChatKey);
                                        startActivity(intentToGroupChat);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


            }

            @NonNull
            @Override
            public groupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_single,parent,false);
                return new groupChatViewHolder(mView);
            }
        };

        mchatist.setAdapter(groupChatRecyclerAdapter);
    }


    public static class groupChatViewHolder extends RecyclerView.ViewHolder{
        View mview2;
        public groupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            mview2=itemView;
        }


        public void setMessage(String message, String type) {
            TextView userMessage= mview2.findViewById(R.id.singlemessageview);

            if (type.equals("text")){
                userMessage.setText(message);
            }
            else {
                userMessage.setText(R.string.sent_photo);
            }
        }

        public void setName(String groupName) {
            TextView usernameview1= mview2.findViewById(R.id.singlemessagedisplayname);
            usernameview1.setText(groupName);
        }

        public void setThumb_image(String groupThumb_img, Context context) {
            CircleImageView circleImageView21=(CircleImageView)mview2.findViewById(R.id.singlemessageprofile_image);
            Picasso.get().load(groupThumb_img).placeholder(R.drawable.profile_image).into(circleImageView21);
        }
    }
}
