package com.crageeApp.appbesocial.Chat.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Chat.Get_Time_ago;
import com.crageeApp.appbesocial.Chat.UserChatActivity;
import com.crageeApp.appbesocial.Login.LoginActivity;
import com.crageeApp.appbesocial.Models.ModelPrnslChatFragment;
import com.crageeApp.appbesocial.R;
import com.facebook.shimmer.ShimmerFrameLayout;
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
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class PersonalChatFragment extends Fragment {

    private RecyclerView mchatist;
    private DatabaseReference mchatDatabase,mchatTime;
    private DatabaseReference userDatabase,currentUserRef;
    private FirebaseAuth userAuth;
    private TextView mtime;
    private LinearLayoutManager mlinearlayout;
    private FirebaseRecyclerAdapter<ModelPrnslChatFragment,chatviewholder> chatRecyclerAdapter;
    private String currentUserId,saveCurrentDate;
    private View personalChatView;
    private Query query;
    private  DatabaseReference mRootref;
    private static final String TAG = "PersonalChatFragment";
    private ShimmerFrameLayout mShimmerMessages;
    public PersonalChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        personalChatView =inflater.inflate(R.layout.fragment_personal_chat, container, false);
        mchatist = (RecyclerView) personalChatView.findViewById(R.id.chatlist);
        mShimmerMessages =   personalChatView.findViewById(R.id.all_messages_shimmer);

        userAuth = FirebaseAuth.getInstance();
        if (userAuth.getCurrentUser()!=null)
        {
            currentUserId = userAuth.getCurrentUser().getUid();
        }
        else
        {
            startActivity(new Intent(getActivity(), LoginActivity.class));

        }
        mchatDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(currentUserId);

        mchatDatabase.keepSynced(true);
        mchatTime=FirebaseDatabase.getInstance().getReference().child("chat").child(currentUserId);
        mchatTime.keepSynced(true);
        query=mchatTime.orderByChild("timestamp").limitToLast(5);
        query.keepSynced(true);
        mRootref=FirebaseDatabase.getInstance().getReference();
        userDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        userDatabase.keepSynced(true);
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        mchatist.setHasFixedSize(true);
        mlinearlayout=new LinearLayoutManager(getContext());
        mchatist.setLayoutManager(mlinearlayout);
        mlinearlayout.setStackFromEnd(true);
        mlinearlayout.setReverseLayout(true);
        return personalChatView;

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: personal chat fragment started");
        currentUserRef= FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userAuth.getCurrentUser().getUid());
        currentUserRef.keepSynced(true);
        FirebaseRecyclerOptions<ModelPrnslChatFragment> options=
                new FirebaseRecyclerOptions.Builder<ModelPrnslChatFragment>()
                        .setQuery(query,ModelPrnslChatFragment.class)
                        .setLifecycleOwner(this)
                        .build();
        chatRecyclerAdapter=new
                FirebaseRecyclerAdapter<ModelPrnslChatFragment, chatviewholder>(options) {
                    @NonNull
                    @Override
                    public chatviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_single,parent,false);
                        return new chatviewholder(mView);
                    }
                    @Override
                    protected void onBindViewHolder(@NonNull final chatviewholder holder, int position, @NonNull final ModelPrnslChatFragment model) {
                        final String receiverUserId = getRef(position).getKey();
                        Log.d(TAG, "onBindViewHolder: receiverUserId"+receiverUserId);
                        final String chat_Id;
                        if (currentUserId.compareTo(receiverUserId)>0)
                        {
                            chat_Id = currentUserId + receiverUserId;
                        }
                        else{
                            chat_Id = receiverUserId+currentUserId;
                        }
                        DatabaseReference messageRef=mRootref
                                .child("messages")
                                .child(chat_Id);
                        Log.d(TAG, "onBindViewHolder: the value of current date is "+saveCurrentDate);

                        Query lastMessageQuery=messageRef.orderByChild("time").limitToLast(1);
                        lastMessageQuery.keepSynced(true);
                        lastMessageQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                String message=(String) dataSnapshot.child("message").getValue();
                                String from=(String) dataSnapshot.child("from").getValue();
                                String type=(String) dataSnapshot.child("type").getValue();
                                if (from.equals(receiverUserId)) {
                                    holder.setMessage(message, model.isSeen(),type);

                                }
                                else if (from.equals(currentUserId)){
                                    holder.setMessage(message,true,type);
                                    mShimmerMessages.stopShimmer();
                                    mShimmerMessages.setVisibility(View.GONE);
                                }
                                else {
                                    holder.setMessage(message,true,type);
                                    mShimmerMessages.stopShimmer();
                                    mShimmerMessages.setVisibility(View.GONE);
                                }
                                String lastMsg_time= dataSnapshot.child("time").getValue().toString();
                                //String image=dataSnapshot.child("image").getValue().toString();
                                Get_Time_ago getTimeAgo=new Get_Time_ago();
                                long lastTime=Long.parseLong(lastMsg_time);
                                String last_message_time=getTimeAgo.getTimeAgo(lastTime,getContext());
                                //  mtime.setText(last_message_time);
                                holder.setTime(last_message_time);
                                holder.setUnSeenMessages( model.isSeen());


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

                        userDatabase
                                .child(receiverUserId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String username =(String) dataSnapshot.child("name").getValue();
                                        final String userThumb_img = (String)dataSnapshot.child("image").getValue();
                                        final String userCategory = (String)dataSnapshot.child("accountCategory").getValue();
                                        final String userOnlineStatus=dataSnapshot.child("onlineStatus").getValue().toString();
                                        if (dataSnapshot.hasChild("onlineStatus")){
                                            String userOnline=dataSnapshot.child("onlineStatus").getValue().toString();
                                            holder.setUserOnline(userOnline);
                                        }
                                        //  String userstatus = dataSnapshot.child("status").getValue().toString();
                                        holder.setName(username);
                                        //viewHolder2.setStatus(userstatus);
                                        holder.setThumb_image(userThumb_img, getContext());

                                        holder.mview2.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intentToUserChat = new Intent(getContext(), UserChatActivity.class);
                                                intentToUserChat.putExtra("hisUid", receiverUserId);
                                                intentToUserChat.putExtra("onlineStatus", userOnlineStatus);
                                                intentToUserChat.putExtra("user_name",username);
                                                intentToUserChat.putExtra("userProfileImage",userThumb_img);
                                                intentToUserChat.putExtra("chat_key",chat_Id);
                                                intentToUserChat.putExtra("accountCategory",userCategory);
                                                Log.d(TAG, "onClick: the value of account category is "+userCategory);
                                                startActivity(intentToUserChat);
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                    Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                                }

                                            }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                };
        mchatist.setAdapter(chatRecyclerAdapter);
        mShimmerMessages.stopShimmer();
        mShimmerMessages.setVisibility(View.GONE);
    }
    public static class chatviewholder extends RecyclerView.ViewHolder {
        View mview2;
        private chatviewholder(View itemView) {
            super(itemView);
            mview2=itemView;
        }
        public void setName(String name){
            TextView usernameview1= mview2.findViewById(R.id.singlemessagedisplayname);
            usernameview1.setText(name);
        }
        private void setMessage(String message,boolean isSeen,String type){
            TextView userMessage= mview2.findViewById(R.id.singlemessageview);
            Log.d(TAG, "setMessage: the value of message is "+message);
            Log.d(TAG, "setMessage: the value of messege seen  is "+isSeen);
            if (type.equals("text")){
                userMessage.setText(message);
            }
            else {
                userMessage.setText(R.string.sent_photo);
            }

            if(!isSeen){
                userMessage.setTypeface(userMessage.getTypeface(), Typeface.BOLD);
                final int res=R.dimen.emoji_size_default;
            //    userMessage.setEmojiSizeRes(res,true);
            }
            else {
                userMessage.setTypeface(userMessage.getTypeface(), Typeface.NORMAL);
                final  int res1=R.dimen.emoji_Normal_size;
               // userMessage.setEmojiSizeRes(res1,true);
            }
        }


        private void setThumb_image(String thumb_image, Context ctx){
            CircleImageView circleImageView21=(CircleImageView)mview2.findViewById(R.id.singlemessageprofile_image);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profile_image).into(circleImageView21);
        }
        private   void setUserOnline(String online_status){
            TextView useronline=(TextView) mview2.findViewById(R.id.online_messageicon);
            if (online_status.equals(R.string.online)){
                useronline.setVisibility(View.VISIBLE);
                //Toast.makeText(ChatFragment.this,)
            }
            else {
                useronline.setVisibility(View.INVISIBLE);
            }
        }
        public void setTime(String time){
            TextView timeview=(TextView) mview2.findViewById(R.id.time);
            timeview.setText(time);
        }

        private void setUnSeenMessages(boolean seen) {
            ImageView newMessageIcon=(ImageView) mview2.findViewById(R.id.new_message_icon);
            if(!seen){
                newMessageIcon.setVisibility(View.VISIBLE);
            }
            else {
                newMessageIcon.setVisibility(View.GONE);
            }
        }
    }
}
