package com.crageeApp.appbesocial.Posts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterPosts;
import com.crageeApp.appbesocial.Models.ModelPosts;
import com.crageeApp.appbesocial.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class postDetailsFragment extends Fragment {

    private RecyclerView recyclerViewPost;
    private DatabaseReference mRootRef;
    private List<ModelPosts> postsList;
    private AdapterPosts adapterPosts;
    private View postDetailsView;
    private static final String TAG = "postDetailsFragment";

    public postDetailsFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        postDetailsView= inflater.inflate(R.layout.fragment_post_details, container, false);

        recyclerViewPost=postDetailsView.findViewById(R.id.postRecyclerView);
        //setting the recycler view properties
        recyclerViewPost.setHasFixedSize(true);
        recyclerViewPost.setLayoutManager(new LinearLayoutManager(getActivity()));

        //get all the values passed by the notifications activity
        Bundle args=this.getArguments();


        if (args!=null){
            String publishedPostId=args.getString("currentPostId");
            String publisherId= args.getString("userId");
            Log.d(TAG, "onCreateView: the value of user id is "+publishedPostId);
            getNotifiedPost(publisherId,publishedPostId);
        }else {
            Toast.makeText(postDetailsView.getContext(), "args is null", Toast.LENGTH_SHORT).show();
        }

        //init the list here
        postsList =new ArrayList<>();
        //init the firebase here
        mRootRef= FirebaseDatabase.getInstance().getReference();
        //get the image from the user timeline

        return  postDetailsView;
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
                        //adapter
                        adapterPosts =new AdapterPosts(getActivity(),postsList);
                        adapterPosts.notifyDataSetChanged();
                        //set adapter to recycler view
                        recyclerViewPost.setAdapter(adapterPosts);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}