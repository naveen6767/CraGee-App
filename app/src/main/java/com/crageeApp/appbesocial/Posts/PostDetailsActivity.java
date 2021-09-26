package com.crageeApp.appbesocial.Posts;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterComments;
import com.crageeApp.appbesocial.Adapters.AdapterPostsAllPosts;
import com.crageeApp.appbesocial.Interfaces.interfaceHome;
import com.crageeApp.appbesocial.Models.ModelComments;
import com.crageeApp.appbesocial.Models.ModelPosts;
import com.crageeApp.appbesocial.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostDetailsActivity extends AppCompatActivity implements interfaceHome {

    private Toolbar postDetailsToolbar;
    private RecyclerView recyclerViewPost,recyclerViewComments;
    private DatabaseReference mRootRef;
    private List<ModelPosts> postsList;
    private AdapterPostsAllPosts adapterPosts;
    private List<ModelComments> commentsList;
    private AdapterComments adapterComments;
    public static final String TAG = "Post Details";
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        navController = Navigation.findNavController(this,R.id.navigation_host_fragment);

        //init the tool bar here
        postDetailsToolbar=findViewById(R.id.toolBar_post_details);
        recyclerViewPost=findViewById(R.id.postRecyclerView);

        //setting the recycler view properties
        recyclerViewPost.setHasFixedSize(true);
        recyclerViewPost.setLayoutManager(new LinearLayoutManager(this));

        //set tool bar as the action bar
        setSupportActionBar(postDetailsToolbar);


        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //adapter
        adapterPosts =new AdapterPostsAllPosts(PostDetailsActivity.this,postsList, this);
        //get all the values passed by the notifications activity
        Intent intent=getIntent();
        String publisherId=intent.getStringExtra("userId");
        String publishedPostId=intent.getStringExtra("currentPostId");

        //init the list here
        postsList =new ArrayList<>();
        //init the firebase here
        mRootRef= FirebaseDatabase.getInstance().getReference();
        //get the image from the user timeline
        getNotifiedPost(publisherId,publishedPostId);

    }
    private void getNotifiedPost(String publisherId, final String publishedPostId) {
        mRootRef
                .child("user_posts")
                .child(publisherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            Log.i(TAG, "onDataChange: getting the dataSnapshot for the posts");
                            Log.i(TAG, "now getting the posts: "+ds);

                            ModelPosts modelPosts =ds.getValue(ModelPosts.class);
                            Log.i(TAG, "getting the pid"+modelPosts.getpId());
                            Log.i(TAG, "getting the publishedPostId"+publishedPostId);
                            if (modelPosts.getpId().equals(publishedPostId)){
                                postsList.add(modelPosts);
                                Log.i(TAG, "inside the post list"+postsList);

                            }

                        }

                       // adapterPosts.notifyDataSetChanged();
                        //set adapter to recycler view
                        recyclerViewPost.setAdapter(adapterPosts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onItemClicked(int position, String userUid) {
        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id",userUid);
        navController.navigate(R.id.action_global_accountProfileFragment,bundle);

    }
}
