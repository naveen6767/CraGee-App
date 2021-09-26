package com.crageeApp.appbesocial.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.AccountProfile.AccountProfileFragment;
import com.crageeApp.appbesocial.Chat.Get_Time_ago;
import com.crageeApp.appbesocial.Models.ModelFollowers;
import com.crageeApp.appbesocial.Models.ModelPosts;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.Posts.CommentsActivity;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class AdapterPostsAllPosts extends RecyclerView.Adapter<AdapterPostsAllPosts.MyHolder> {

    private final Context context;
    private   List<ModelPosts> postsList;
    private final String myUid;
    private String publisherId;
    private final DatabaseReference mRootRef;  //for posts database node
    private boolean mProcessLike =false;
    private com.crageeApp.appbesocial.Interfaces.interfaceHome interfaceHome;

    public AdapterPostsAllPosts(Context context, List<ModelPosts> postsList, com.crageeApp.appbesocial.Interfaces.interfaceHome interfaceHome) {
        this.context = context;
        this.postsList = postsList;
        this.interfaceHome = interfaceHome;

        //initialize the fire base here
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //likesRef =FirebaseDatabase.getInstance().getReference("Likes");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        

    }
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout allpostlayout.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.allposts_layout,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  MyHolder holder,  int position) {
        //get data
        publisherId = postsList.get(position).getUid();
        String userName=postsList.get(position).getUserName();
        String userDp =postsList.get(position).getUserDp();
        String date =postsList.get(position).getDate();
        String time =postsList.get(position).getTime();
        final String postImage =postsList.get(position).getPostImage();
        String textPost =postsList.get(position).getTextPost();
        String pTime =postsList.get(position).getpTime();
        final String pId =postsList.get(position).getpId();
        final String userVerification =(String) postsList.get(position).getUserVerified();
        //String pLikes =postsList.get(position).getpLikes(); //contains total no of likes for a post

        Get_Time_ago getTimeAgo=new Get_Time_ago();
        long lastTime=Long.parseLong(pTime);

        String last_seen_time= Get_Time_ago.getTimeAgo(lastTime,context);
        holder.pCurrentTime.setText(last_seen_time);
        //set data
        if (!userName.isEmpty()){
            holder.pUserName.setText(userName);
        }

        if (textPost.isEmpty()){
            holder.pText.setVisibility(View.GONE);
        }else {
            holder.pText.setVisibility(View.VISIBLE);
            holder.pText.setText(textPost);
          //  holder.pText.setMovementMethod(LinkMovementMethod.getInstance());
//            if(holder.pText.isClickable()){
//                String link= String.valueOf(holder.pText.getLinksClickable());
//                holder.pText.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                         CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//                         builder.setToolbarColor(Color.BLUE);
//                         builder.addDefaultShareMenuItem();
////                        builder.setStartAnimations((HomeActivity)context, R.anim.slide_in_right, R.anim.slide_out_left);
////                        builder.setExitAnimations((HomeActivity)context, android.R.anim.slide_in_left,
////                                android.R.anim.slide_out_right);
////                        builder.setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.back_arrow));
//                        CustomTabsIntent customTabsIntent = builder.build();
//                        customTabsIntent.launchUrl((HomeActivity)context, Uri.parse(textPost));
//                    }
//                });
//            }

        }
        if(String.valueOf(userVerification).equals("Original")){
            holder.userBlueTick.setVisibility(View.VISIBLE);
        }else {
            holder.userBlueTick.setVisibility(View.GONE);

        }
       // holder.pCurrentTime.setText(postCreateTime);
//        if(Integer.parseInt(pLikes)==0){
//            holder.pTotalLikes.setVisibility(View.GONE);
//        }
//        else {
//            holder.pTotalLikes.setText(pLikes+" "+"Likes");   //e.g  100likes
//        }
        //get pLikes for each post
        mRootRef
                .child("Likes")
                .child(pId)
                .child("pLikes")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            int pLikes= Integer.parseInt(String.valueOf(snapshot.getValue()));
                            if(pLikes==0){
                                holder.pTotalLikes.setVisibility(View.GONE);
                            }
                            else {
                                holder.pTotalLikes.setText(pLikes+" "+"Likes");   //e.g  100likes
                            }
                        }else {
                            holder.pTotalLikes.setVisibility(View.GONE);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        //set likes for each post
        setLikes(holder,pId,publisherId);
        //set user Dp
        try {
            Picasso.get().load(userDp)
                    .placeholder(R.drawable.profile_image).into(holder.postProfileImage);

        }catch (Exception e)
        {
            Picasso.get().load(R.drawable.profile_image).into(holder.postProfileImage);
        }
        //set post image
        //if there is no image i.e.  pImage.equals("noImage") then hide the image view
        if (postImage.equals("noImage"))
        {
            //hide the image view
            holder.pImage.setVisibility(View.GONE);
        }
        else {
            //show the image view
            holder.pImage.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(postImage).into(holder.pImage);

            }catch (Exception e)
            {
                Picasso.get().load(R.drawable.profile_image).into(holder.pImage);
            }
        }
        //handle the button clicks
        holder.postOptionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (publisherId.equals(myUid)){
                    showPersonalPostOptions(publisherId,myUid,pId,postImage);
                }
                else {
                    showPostOptions();
                }
            }
        });

        holder.postProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interfaceHome.onItemClicked(position,publisherId);
            }
        });

//        holder.postProfileImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!FirebaseAuth.getInstance().getUid().equals(publisherId)){
//       //           sendUserToAccountProfile(holder.postProfileImage);
////                    NavDirections directions= HomeFragmentDirections.actionFragmentHomeToAccountProfileFragment(publisherId);
////                    Navigation.findNavController(v).navigate(directions);
//
//                    Bundle bundle = new Bundle();
//                    bundle.putString("visit_user_id",publisherId);
//                    Navigation.findNavController(v).navigate(R.id.action_global_accountProfileFragment, bundle);
//
//                }
//                else {
//                    holder.postProfileImage.setEnabled(false);
//                }
//            }
//        });
        holder.pUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FirebaseAuth.getInstance().getUid().equals(publisherId)){
                    Bundle bundle = new Bundle();
                    bundle.putString("visit_user_id",publisherId);
                    Navigation.findNavController(v).navigate(R.id.action_global_accountProfileFragment, bundle);
                }
                else {
                    holder.pUserName.setEnabled(false);
                }
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //here get the total no of likes for the post,whose like button has been clicked
                //if currently signed in user has not liked in before
                //increase value by 1 otherwise decrease by 1
                Log.i(TAG, "onClick: like button clicked");
                Log.i(TAG, "onClick: clicking the mProcess like"+mProcessLike);

               // final int pLikes = Integer.parseInt(postsList.get(position).getpLikes());
                //get pLikes for liked post
                final int[] pLikes = new int[1];
                mRootRef
                        .child("Likes")
                        .child(pId)
                        .child("pLikes")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists())
                                {
                                    pLikes[0] =Integer.parseInt(String.valueOf(snapshot.getValue()));
                                }else {
                                    pLikes[0]=0;
                                }
                                
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                mProcessLike=true;
                //get id of the post clicked
                final String postId=postsList.get(position).getpId();
                final String publisherID=postsList.get(position).getUid();

                /*
                * before liking or disliking the post
                * check whether the post which is liked is available in the
                * all posts and users posts and current user timeline  in the database
                 */

                mRootRef
                        .child("Likes")
                        .child(postId)
                        .child("Likes")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (mProcessLike){
                                    if (dataSnapshot.hasChild(myUid)) {
                                        //already liked so remove like
                                        Log.i(TAG, "already liked so remove like ");
                                        Log.i(TAG, "onDataChange: current pLikes"+ pLikes[0]);
                                        mRootRef
                                                .child("Likes")
                                                .child(postId)
                                                .child("pLikes")
                                                .setValue("" + (pLikes[0] - 1));
                                        mRootRef
                                                .child("Likes")
                                                .child(postId)
                                                .child("Likes")
                                                .child(myUid)
                                                .removeValue();
                                        postsList.get(position).setpLikes(String.valueOf(pLikes[0] -1));
                                        Log.i(TAG, "onDataChange: current pLikes after setting value"+ pLikes[0]);
                                        mProcessLike = false;
                                        mRootRef
                                                .child("Likes")
                                                .child(postId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild("pLikes")){
                                                            String postLikes =(String) dataSnapshot.child("pLikes").getValue();
                                                            Log.i(TAG, "onDataChange: posts likes"+postLikes);
                                                            if(Integer.parseInt(postLikes)==0){
                                                                holder.pTotalLikes.setVisibility(View.GONE);

                                                            }
                                                            else {
                                                                holder.pTotalLikes.setVisibility(View.VISIBLE);
                                                                holder.pTotalLikes.setText(postLikes+" "+"Likes");
                                                            }


                                                            Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                        Log.i(TAG, "onCancelled: "+databaseError.getMessage());
                                                    }
                                                });

                                        /*
                                         *after removing like from the all posts in the database
                                         * remove like from the publisher personal database
                                         */
//                                        userPostsRef.child(publisherID).child(postId)
//                                                .child("pLikes").setValue("" + (pLikes - 1));
//                                        userPostsRef.child(publisherID).child(postId)
//                                                .child("Likes").child(myUid).removeValue();


                                        /*
                                         *after removing like from the publisher personal database
                                         * remove  like from the current user timeline
                                         */
//                                        mRootRef
//                                                .child("user_timeline")
//                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                                .child(postId)
//                                                .child("pLikes")
//                                                .setValue("" + (pLikes - 1));
//                                        mRootRef
//                                                .child("user_timeline")
//                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                                .child(postId)
//                                                .child("Likes")
//                                                .child(myUid)
//                                                .removeValue();
                                    }
                                    else {

                                        Log.i(TAG, "not liked like it ");
                                        Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);

                                        //not liked so like it after pressing heart button
                                        mRootRef
                                                .child("Likes")
                                                .child(postId)
                                                .child("pLikes")
                                                .setValue("" + (pLikes[0] + 1));
                                        mRootRef
                                                .child("Likes")
                                                .child(postId)
                                                .child("Likes")
                                                .child(myUid)
                                                .setValue("Liked");
                                        // don't generate notifications if the publisher id is equal to the current  user id
                                        if (publisherID.equals(FirebaseAuth.getInstance().getUid())){
                                            Log.i(TAG, "onDataChange: both the publisher and the current user is same");
                                            Log.i(TAG, "onDataChange: not generating the notifications");
                                        }
                                        else {
                                            Log.i(TAG, "onDataChange:  both the publisher and the current user is  not same");
                                            Log.i(TAG, "onDataChange: generate the notifications");

                                            //generate notifications here
                                            generateLikeNotification(publisherID);


                                        }
                                        postsList.get(position).setpLikes(String.valueOf(pLikes[0] +1));
                                        mProcessLike = false;
                                        mRootRef
                                                .child("Likes")
                                                .child(postId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild("pLikes")){
                                                            String postLikes =(String) dataSnapshot.child("pLikes").getValue();
                                                            Log.i(TAG, "onDataChange: posts likes"+postLikes);
                                                            if(Integer.parseInt(postLikes)==0){
                                                                holder.pTotalLikes.setVisibility(View.GONE);

                                                            }
                                                            else {
                                                                holder.pTotalLikes.setVisibility(View.VISIBLE);
                                                                holder.pTotalLikes.setText(postLikes+" "+"Likes");
                                                            }
                                                            Log.i(TAG, "onDataChange: now the mProcess"+mProcessLike);

                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                        /*
                                         *after liking the post from the all posts database
                                         * update likes in the publisher database
                                         */

//                                        //not liked like it for the current user database
//                                        mRootRef
//                                                .child("user_posts")
//                                                .child(publisherID)
//                                                .child(postId)
//                                                .child("pLikes")
//                                                .setValue("" + (pLikes + 1));
//                                        mRootRef
//                                                .child("user_posts")
//                                                .child(publisherID)
//                                                .child(postId)
//                                                .child("Likes")
//                                                .child(myUid)
//                                                .setValue("Liked");
//                                        //not liked like it for the current user timeline
//                                        mRootRef
//                                                .child("user_timeline")
//                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                                .child(postId)
//                                                .child("pLikes")
//                                                .setValue("" + (pLikes + 1));
//                                        mRootRef
//                                                .child("user_timeline")
//                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                                .child(postId)
//                                                .child("Likes")
//                                                .child(myUid)
//                                                .setValue("Liked");

                                    }

                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
            private void generateLikeNotification(final String publisherID) {
                final String notification;
                notification="liked your post";
                
                mRootRef
                        .child("Users")
                        .child(myUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String currentUserName=(String) dataSnapshot.child("name").getValue();
                        String currentUserDp=(String) dataSnapshot.child("image").getValue();
                        String new_notification_id= mRootRef
                                .child("like_notifications")
                                .child(publisherID)
                                .push()
                                .getKey();
                        HashMap<String, Object> notificationHashmap = new HashMap<>();
                        notificationHashmap.put("timeStamp", ServerValue.TIMESTAMP);
                        notificationHashmap.put("notifierName",currentUserName);
                        notificationHashmap.put("notifierDp",currentUserDp);
                        notificationHashmap.put("notifierUid",myUid);
                        notificationHashmap.put("notification",notification);
                        notificationHashmap.put("postImage",postImage);
                        notificationHashmap.put("notifiedPostId",pId);
                        notificationHashmap.put("notifiedPostPublisher",publisherID);
                        notificationHashmap.put("notificationId",new_notification_id);
                        Log.i(TAG, "onSuccess: publisher id"+publisherID);
                        

                        if (new_notification_id != null) {
                            mRootRef
                                    .child("like_notifications")
                                    .child(publisherID)
                                    .child(new_notification_id)
                                    .setValue(notificationHashmap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i(TAG, "onSuccess: notifications updated successfully");
                                            //update the notification in the new like notifications database
                                            HashMap<String, Object> notificationHashmap = new HashMap<>();
                                            notificationHashmap.put("timeStamp",ServerValue.TIMESTAMP);
                                            notificationHashmap.put("seen",false);
                                            mRootRef
                                                    .child("new_like_notifications")
                                                    .child(publisherID)
                                                    .child(new_notification_id)
                                                    .setValue(notificationHashmap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d(TAG, "onSuccess: notification updated successfully in the new like database");

                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure: failed to update the notifications in new like notifications ");
                                                }
                                            });


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, "onFailure: notification update failed "+e.getMessage());
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String postId=postsList.get(position).getpId();
                final String publisherID=postsList.get(position).getUid();
                final String publishedPostImage=postsList.get(position).getPostImage();
                //start the post comments fragment

//                Bundle bundle = new Bundle();
//                bundle.putString("postId",postId);
//                bundle.putString("publisherId",publisherID);
//                bundle.putString("publishedPostImage",publishedPostImage);

               Intent intent=new Intent(context, CommentsActivity.class);
               intent.putExtra("postId",postId);
               intent.putExtra("publisherId",publisherID);
               intent.putExtra("publishedPostImage",publishedPostImage);
               context.startActivity(intent);
//                String link="https://www.google.co.in/?gws_rd=cr&ei=c1TpUo6qGYmCrgfJnYGYCw";
//                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//                         builder.setToolbarColor(Color.BLUE);
//                         builder.addDefaultShareMenuItem();
////                        builder.setStartAnimations((HomeActivity)context, R.anim.slide_in_right, R.anim.slide_out_left);
////                        builder.setExitAnimations((HomeActivity)context, android.R.anim.slide_in_left,
////                                android.R.anim.slide_out_right);
////                        builder.setCloseButtonIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.back_arrow));
//                        CustomTabsIntent customTabsIntent = builder.build();
//                        customTabsIntent.launchUrl((Activity)v.getContext(), Uri.parse(link));

            }
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //share  name, profile image,post image, uid,text post
                final BottomSheetDialog bt=new BottomSheetDialog(context,R.style.BottomSheetDialogTheme);

                View view= LayoutInflater.from(context).inflate(R.layout.layout_dialog_share,null);
                ImageView imageView=view.findViewById(R.id.sharedImage);
                Picasso.get().load(postImage).placeholder(R.drawable.profile_image).into(imageView);
                EditText editText=view.findViewById(R.id.sharedMessage);
                EditText searchFollowers=view.findViewById(R.id.search_followers);
                bt.setContentView(view);
                bt.show();

                RecyclerView recyclerViewFollowers;
                DatabaseReference userRef;
                String currentUserId;

                List<ModelUsers> usersList;
                List<ModelFollowers> followersList;
                //initialize the recycler view
                recyclerViewFollowers=view.findViewById(R.id.recyclerView_followers_share);
                //setting the recycler view properties
                recyclerViewFollowers.setHasFixedSize(true);
                recyclerViewFollowers.setLayoutManager(new LinearLayoutManager(context));

                //init the user list
                usersList =new ArrayList<>();
                followersList =new ArrayList<>();

                //init the fire base here
                userRef= FirebaseDatabase.getInstance().getReference("Users");
                currentUserId= FirebaseAuth.getInstance().getUid();
                //adapter
                AdapterUsersInShare adapterUsers  =new AdapterUsersInShare(context,usersList);
                userRef
                        .child(currentUserId)
                        .child("followers")
                        .limitToLast(6)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                followersList.clear();
                                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                                    ModelFollowers modelFollowers=snapshot.getValue(ModelFollowers.class);
                                    followersList.add(modelFollowers);
                                }
                                usersList.clear();
                                for (int i=0;i<followersList.size();i++){
                                    Log.i(TAG, "no of Child in following list: "+followersList.size());
                                    Log.i(TAG, "getUserPosts: started running for the "+i+"th time");
                                    final String followersKeys=followersList.get(i).getId();
                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot ds:dataSnapshot.getChildren())
                                            {
                                                ModelUsers modelUsers =ds.getValue(ModelUsers.class);
                                                if (modelUsers.getUid().equals(followersKeys)){
                                                    usersList.add(modelUsers);

                                                }


                                                //set adapter to the recycler view
                                                recyclerViewFollowers.setAdapter(adapterUsers);


                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                //search listener
                searchFollowers.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {


                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //search text contains text,search it
                        Toast.makeText(context, "after text changed", Toast.LENGTH_SHORT).show();

                        //get current user
                        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
                        //get the path of the user database named"users"
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        Query searchUser=ref.limitToFirst(5);
                        //get all data from the path
                        searchUser
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        usersList.clear();
                                        for (DataSnapshot ds:dataSnapshot.getChildren())
                                        {
                                            ModelUsers modelUsers =ds.getValue(ModelUsers.class);

                                            if (!modelUsers.getUid().equals(fUser.getUid()))
                                            {
                                                if (modelUsers.getName().toLowerCase().contains(s.toString().toLowerCase()))
                                                {
                                                    usersList.add(modelUsers);
                                                }
                                            }
                                            //refresh adapter
                                            adapterUsers .notifyDataSetChanged();


                                            //set adapter to the recycler view
                                            recyclerViewFollowers.setAdapter(adapterUsers);
                                        }

                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                });




            }




        });
    }
    private void showPersonalPostOptions(final String publisherId, String myUid, final String pId, final String postImage) {
        //show a alert dialog

        //options to show in the dialog box
        String[] options = {"Edit","Delete", "Share to...", "Unfollow","Turn Off Commenting"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog items click
                if (which==0){
                    //edit clicked

                }else if (which==1){
                    //delete clicked
                    //delete is clicked
                 //   beginDelete(publisherId,pId,postImage);

                }else if (which==2){
                    //share clicked

                }else if (which==3){

                }else if (which==4){

                }
            }
        });
        builder.create().show();
    }

    private void showPostOptions() {

        //show a alert dialog

        //options to show in the dialog box
        String[] options = {"Report...", "Share to...", "Unfollow"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog items click
                if (which==0){

                }else if (which==1){

                }else if (which==2){

                }
            }
        });
        builder.create().show();
    }

    private void sendUserToAccountProfile(CircleImageView postProfileImage) {
//        Intent intentToUserProfile = new Intent(context, AccountProfileActivity.class);
//        intentToUserProfile.putExtra("visit_user_id",publisherId);
//        context.startActivity(intentToUserProfile);

//        FragmentActivity fragmentActivity = new FragmentActivity();
//        FragmentManager FR = fragmentActivity.getSupportFragmentManager();
//        FR.beginTransaction().replace(R.id.fragmentContainerHome,new AccountProfileFragment())
//                .commit();


        AppCompatActivity activity = (AppCompatActivity) postProfileImage .getContext();
        Fragment myFragment = new AccountProfileFragment();
        Bundle args = new Bundle();
        args.putString("visit_user_id",publisherId);
        myFragment.setArguments(args);
//        activity.getSupportFragmentManager()
//                .beginTransaction()
//                .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
//                .replace(R.id.fragmentContainerHome, myFragment).addToBackStack(null).commit();



    }

    private void setLikes(final MyHolder holder, final String postKey, final String uid) {
        mRootRef
                .child("Likes")
                .child(postKey)
                .child("Likes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(myUid))
                        {
                            //user has liked this post
                    /*to indicate that the post is liked by this (signed in) user
                    change drawable left icon of the like button
                    change text of like button from like to liked
                     */
                       //     holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked_button,0,0,0);
                            holder.likeBtn.setImageResource(R.drawable.ic_liked_button);
                            //holder.likeBtn.setText("Liked");
                            Log.i(TAG, "user has liked the post ");



                        }
                        else {
                            //user has  not liked this post
                            //user has liked this post
                    /*
                    to indicate that the post is not liked by this (signed in) user
                    change drawable left icon of the like button
                    change text of like button from liked to like
                    */
//                            holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_button,0,0,0);
                            holder.likeBtn.setImageResource(R.drawable.ic_like_button);

                            //holder.likeBtn.setText("Like");
                            Log.i(TAG, "user has  not liked the post ");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void beginDelete(String publisherId, String pId, String postimage) {
        //post can be with or without image
        if (postimage.equals("noImage"))
        {
            //post is without image
            deleteWithoutImage(publisherId,pId,postimage);
        }
        else {
            //post is with image
            deleteWithImage(publisherId,pId,postimage);
        }
    }
    private void deleteWithImage(final String publisherId, final String pId, String postimage) {

        /*Steps
        1. delete image using url
        2. delete from the data base using post id
         */
        StorageReference imageReference = FirebaseStorage.getInstance().getReferenceFromUrl(postimage);
        imageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        //here image will be deleted from the storage
                        //now delete from the  database

                        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds:dataSnapshot.getChildren())
                                {
                                    ds.getRef().removeValue(); //remove values from fire base where pId matches

                                }
                                //deleted
                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed in deleting the post

                        Toast.makeText(context, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        //now delete from the  database

                        DatabaseReference userPostsRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid).child("Posts");
                       Query userPostsQuery=userPostsRef.orderByChild("pId").equalTo(pId);
                        userPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds:dataSnapshot.getChildren())
                                {
                                    ds.getRef().removeValue(); //remove values from fire base where pId matches

                                }
                                //deleted
                                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });
    }

    private void deleteWithoutImage(String publisherId, String pId, String postimage) {
        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ds.getRef().removeValue(); //remove values from fire base where pId matches

                }
                //deleted
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return postsList.size();
    }



//view holder class

    class MyHolder extends RecyclerView.ViewHolder
    {
        //views from the allpostlayout.xml are defined here
        CircleImageView postProfileImage;
        ImageView pImage,userBlueTick;
        TextView pUserName,pText,pCurrentTime,pTotalLikes;
        ImageButton postOptionsBtn,likeBtn,commentBtn,shareBtn;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //initialize all the views here
            postProfileImage=itemView.findViewById(R.id.postProfileImageValue);
            pImage=itemView.findViewById(R.id.postImage);
            pUserName=itemView.findViewById(R.id.postProfileName);
            pText=itemView.findViewById(R.id.postDescription);
            pCurrentTime=itemView.findViewById(R.id.currentPostTime);
            pTotalLikes=itemView.findViewById(R.id.totalPostLikes);
            likeBtn=itemView.findViewById(R.id.like_button);
            commentBtn=itemView.findViewById(R.id.comment_button);
            shareBtn=itemView.findViewById(R.id.share_button);
            postOptionsBtn=itemView.findViewById(R.id.postOptions);
            userBlueTick=itemView.findViewById(R.id.userBlueTick);

        }
    }
}
