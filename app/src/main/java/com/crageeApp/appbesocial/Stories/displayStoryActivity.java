package com.crageeApp.appbesocial.Stories;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crageeApp.appbesocial.Models.ModelStory;
import com.crageeApp.appbesocial.Models.ModelUsers;
import com.crageeApp.appbesocial.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class displayStoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {


    int counter=0;
    long pressTime=0L;
    long limit=500L;
    StoriesProgressView storiesProgressView;
    ImageView image,story_photo;
    TextView story_username;
    List<String> images;
    List<String> storyIds;
    String userId;
    private static final String TAG = "displayStoryActivity";

    private View.OnTouchListener onTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    pressTime=System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;

                case MotionEvent.ACTION_UP:
                    long now=System.currentTimeMillis();
                    storiesProgressView.resume();
                   return limit < now -pressTime;

            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_story);
        storiesProgressView=findViewById(R.id.stories);
        image=findViewById(R.id.image);
        story_photo=findViewById(R.id.profilePhotoStories);
        story_username=findViewById(R.id.story_user_name);
        userId=getIntent().getStringExtra("userId");
        userInfo(userId);
        getStories(userId);
        View reverse=findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();

            }
        });
        reverse.setOnTouchListener(onTouchListener);
        View skip=findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();

            }
        });
        skip.setOnTouchListener(onTouchListener);

    }

    @Override
    public void onNext() {

    }

    @Override
    public void onPrev() {

    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();

    }

    private void getStories(String userId){
        images=new ArrayList<>();
        storyIds=new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storyIds.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelStory modelStory=snapshot.getValue(ModelStory.class);
                    long timeCurrent=System.currentTimeMillis();
                    if (timeCurrent>modelStory.getTimeStart() && timeCurrent< modelStory.getTimeEnd()){
                        images.add(modelStory.getImageUrl());
                        storyIds.add(modelStory.getStoryId());
                    }
                }
                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(displayStoryActivity.this);
                storiesProgressView.startStories(counter);
                Picasso.get().load(images.get(counter)).placeholder(R.drawable.profile_image).into(image);



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void userInfo(String userId){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: the datasnapshot is"+dataSnapshot);
                ModelUsers modelUsers=dataSnapshot.getValue(ModelUsers.class);
                Log.d(TAG, "onDataChange: the modelUsers.getName() is"+modelUsers.getName());

                Picasso.get().load(modelUsers.getImage()).placeholder(R.drawable.profile_image).into(story_photo);
                story_username.setText(modelUsers.getName());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
