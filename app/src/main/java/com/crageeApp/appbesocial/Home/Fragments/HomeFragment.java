package com.crageeApp.appbesocial.Home.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterPostsAllPosts;
import com.crageeApp.appbesocial.Adapters.AdapterStories;
import com.crageeApp.appbesocial.Chat.AllMessagesActivity;
import com.crageeApp.appbesocial.Interfaces.interfaceHome;
import com.crageeApp.appbesocial.Models.ModelPosts;
import com.crageeApp.appbesocial.Models.ModelPrnslChatFragment;
import com.crageeApp.appbesocial.Models.ModelStory;
import com.crageeApp.appbesocial.R;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment implements interfaceHome, View.OnClickListener {
    private View HomeFragmentView;
    private Toolbar homeToolBar;
    private RecyclerView recyclerViewPosts;
    private List<ModelPosts> postsList;
    private AdapterPostsAllPosts AdapterPostsAllPosts;
    private DatabaseReference postRef,likesRef,postsRef,mRootRef;
    private FirebaseAuth userAuth;
    private String currentUserId;
    private String followingUserKey,oldestPostId;
    private Boolean isScrolling=false;
    private LinearLayoutManager manager,storiesManager;
    private int totalItems,currentItems,scrollOutItems;
    private ProgressBar progressBar;
//  private SwipeRefreshLayout mRefreshLayout;
    private static final int TOTAL_ITEMS_TO_LOAD=6;
    private int mCurrentPage=1;
    private int itemPos=0;
    private String mLastKey="";
    private String mPrevKey="";
    private boolean loading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private ImageView sendMessages,searchUsers,userNotifications,newMessageNotify,newNotificationNotify;
    private NestedScrollView homeNestedScroll;
    private RecyclerView recyclerView_story;
    private AdapterStories adapterStories;
    private List<ModelStory> storyList;
    private List<String> followingList;
    private ShimmerFrameLayout mShimmerPosts,mShimmerStories,mShimmerPostsSecond;
    private BottomSheetDialog bottomSheetDialog;
    private ImageView imageShare;
    private EditText shareText;
    private int position;
    private static final String TAG = "HomeFragment";
    private NavController navController;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //sethasoptinsmenu is used to display the menu options in the tool bar
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        HomeFragmentView =inflater.inflate(R.layout.fragment_home, container, false);
        navController = Navigation.findNavController(getActivity(),R.id.navigation_host_fragment);
        //initialize post list
        postsList =new ArrayList<>();
        //initialize the home tool bar  here
        //homeToolBar =HomeFragmentView.findViewById(R.id.tool_bar_home);
        progressBar =HomeFragmentView.findViewById(R.id.progressBarNewsFeed);
//      mRefreshLayout =HomeFragmentView.findViewById(R.id.swipeRefresh_layout_home);
        sendMessages =HomeFragmentView.findViewById(R.id.send_messages);
        searchUsers =HomeFragmentView.findViewById(R.id.search_users);
        userNotifications =HomeFragmentView.findViewById(R.id.user_notifications);
        newMessageNotify =HomeFragmentView.findViewById(R.id.new_messages_notifications);
        newNotificationNotify =HomeFragmentView.findViewById(R.id.new_notifications);
        recyclerView_story =HomeFragmentView.findViewById(R.id.storiesRecyclerView);
        homeNestedScroll =HomeFragmentView.findViewById(R.id.stories);
        mShimmerPosts = HomeFragmentView.findViewById(R.id.shimmer_view_container);
        mShimmerPostsSecond = HomeFragmentView.findViewById(R.id.timeline_posts_shimmer);
        mShimmerStories = HomeFragmentView.findViewById(R.id.shimmer_stories_container);
        //((AppCompatActivity)getActivity()).setSupportActionBar(homeToolBar);// Setting/replace toolbar as the ActionBar
        //initialize the fire base
        //path of all posts
        postRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postsRef.keepSynced(true);
        likesRef =FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.keepSynced(true);
        userAuth = FirebaseAuth.getInstance();
        currentUserId =userAuth.getCurrentUser().getUid();
        //adapter
        AdapterPostsAllPosts =new AdapterPostsAllPosts(getContext(),postsList, this);

        //recycler views and its properties
        recyclerViewPosts =HomeFragmentView.findViewById(R.id.postsRecyclerView);
        manager=new LinearLayoutManager(HomeFragmentView.getContext());
        recyclerViewPosts.setHasFixedSize(true);
        recyclerView_story.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false);
        recyclerView_story.setLayoutManager(linearLayoutManager);
        storyList=new ArrayList<>();
        adapterStories=new AdapterStories(getContext(),storyList);
        recyclerView_story.setAdapter(adapterStories);

        //set layout to recycler view
        recyclerViewPosts.setLayoutManager(manager);
        //manager.setStackFromEnd(true);
        //manager.setReverseLayout(true);

        loadUserPosts();
        sendMessages.setOnClickListener(this);
        searchUsers.setOnClickListener(this);
        userNotifications.setOnClickListener(this);
        //for the new message notifications

        DatabaseReference msgNotify=FirebaseDatabase.getInstance().getReference("chat").child(currentUserId);
        msgNotify.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unRead=0;
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelPrnslChatFragment modelPrnslChatFragment=snapshot.getValue(ModelPrnslChatFragment.class);

                    if (!modelPrnslChatFragment.seen){
                        Log.d(TAG, "onDataChange: seen is true");
                        unRead++;
                        break;
                    }
                }
                Log.d(TAG, "onDataChange: the value of unread messages is"+unRead);

                if (unRead==0){
                    newMessageNotify.setVisibility(View.GONE);

                }
                else {
                    newMessageNotify.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //for the new notifications
        mRootRef
                .child("new_like_notifications")
                .child(currentUserId)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unRead=0;
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelPrnslChatFragment modelPrnslChatFragment=snapshot.getValue(ModelPrnslChatFragment.class);

                    if (modelPrnslChatFragment != null && !modelPrnslChatFragment.seen) {
                        Log.d(TAG, "onDataChange: seen is true");
                        unRead++;
                        break;
                    }
                }
                Log.d(TAG, "onDataChange: the value of unread messages is"+unRead);

                if (unRead==0){
                    newNotificationNotify.setVisibility(View.GONE);

                }
                else {
                    newNotificationNotify.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

//        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//              //  mCurrentPage++;
//            //    itemPos=0;
//              // loadMorePosts();
//            }
//        });
//        recyclerViewPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//
//                if (newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
//                    isScrolling=true;
//                }
//
//
//            }
//
//            @Override
//
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//
//                currentItems=manager.getChildCount();
//                totalItems=manager.getItemCount();
//                scrollOutItems=manager.findFirstVisibleItemPosition();
//                Log.d(TAG, "onScrolled: current items is "+currentItems);
//                Log.d(TAG, "onScrolled: total items is "+totalItems);
//                Log.d(TAG, "onScrolled: scrolled items"+scrollOutItems);
//
//                if (isScrolling&&(currentItems+scrollOutItems==totalItems)){
//                    //data fetch
//                    isScrolling=false;
//                    mCurrentPage++;
//                    loadMorePosts();
//                }
//            }
//        });
        homeNestedScroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if(v.getChildAt(v.getChildCount() - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {
                    //code to fetch more data for endless scrolling
                    Log.d(TAG, "onCreateView: fetching the data");
                    mCurrentPage++;
                    loadMorePosts();
                }
            }
        });

        return HomeFragmentView;

    }
    private void readStory(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timeCurrent=System.currentTimeMillis();
                storyList.clear();
                storyList.add(new ModelStory("","",FirebaseAuth.getInstance().getCurrentUser().getUid(),0,0));

                 for (String id:followingList){
                     int countStory=0;
                     ModelStory modelStory=null;
                     Log.d(TAG, "onDataChange: the value of datasnapshot is "+dataSnapshot);
                     Log.d(TAG, "onDataChange: the value of datasnapshot child is "+dataSnapshot.child(id));
                     for (DataSnapshot snapshot:dataSnapshot.child(id).getChildren()){
                         Log.d(TAG, "onDataChange: the valus od snapshot is "+snapshot);
                         modelStory=snapshot.getValue(ModelStory.class);
                         Log.d(TAG, "onDataChange: thevalue extracted is "+modelStory.getTimeStart());
                         if (timeCurrent>modelStory.getTimeStart()&&timeCurrent<modelStory.getTimeEnd()){
                             countStory++;
                         }
                     }

                     if (countStory>0){
                         storyList.add(modelStory);
                     }

                 }

                 adapterStories.notifyDataSetChanged();
                mShimmerStories.stopShimmer();
                mShimmerStories.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkFollowing(){
        followingList=new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId)
                .child("following");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){

                    followingList.add(snapshot.getKey());
                    Log.d(TAG, "onDataChange: the value inside the following list is "+followingList);

                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        readStory();

    }

     private void loadMorePosts() {

        Log.d(TAG, "loadMorePosts: loading more posts");
//        progressBar.setVisibility(View.VISIBLE);
        mShimmerPostsSecond.startShimmer();
         Log.d(TAG, "loadMorePosts: the value of last key is "+mLastKey);

        Query nextPostsQuery =mRootRef
                .child("user_timeline")
                .child(currentUserId)
                .orderByKey()
                .endAt(mLastKey)
                .limitToLast(TOTAL_ITEMS_TO_LOAD);
        nextPostsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ModelPosts modelPosts =dataSnapshot.getValue(ModelPosts.class);
                String postsKey = dataSnapshot.getKey();

                Log.d(TAG, "onChildAdded:inside load more the current item pos is"+itemPos);
                Log.d(TAG, "onChildAdded:inside load more the postsKey is"+postsKey);
                if (!mPrevKey.equals(postsKey)){

                    postsList.add(itemPos++,modelPosts);
                    Log.d(TAG, "onChildAdded: the current item position is "+itemPos);
                }
                Log.d(TAG, "onChildAdded: the value of item pos is "+itemPos);
                Log.d(TAG, "onChildAdded: the value of current page"+mCurrentPage);
                Log.d(TAG, "onChildAdded: the value of total items and current page"+itemPos+"for item pos"+TOTAL_ITEMS_TO_LOAD*mCurrentPage);

                if(mCurrentPage==2){
                    if (itemPos==7){
                        mLastKey=postsKey;
                        Log.d(TAG, "onChildAdded: the value of last key for second page"+mLastKey);
                    }
                }else {
                     if (itemPos==TOTAL_ITEMS_TO_LOAD*(mCurrentPage-1)){
                        mLastKey=postsKey;
                        Log.d(TAG, "onChildAdded: the value of total items and current page"+TOTAL_ITEMS_TO_LOAD*mCurrentPage);
                        Log.d(TAG, "onChildAdded: the value of last key for"+mCurrentPage+"is"+mLastKey);
                    }
                }
                Log.d(TAG, "onChildAdded: the value of posts list is"+postsList);
               // Collections.reverse(postsList);
                Log.d(TAG, "onChildAdded: the current value of current page is"+mCurrentPage);
                Log.d(TAG, "onChildAdded: the current value of current item position is "+itemPos);
                Log.d(TAG, "onChildAdded: after adding the child the list size is "+postsList.size());
                //adapter

                AdapterPostsAllPosts.notifyDataSetChanged();
//                mRefreshLayout.setRefreshing(false);
//                progressBar.setVisibility(View.GONE);
                mShimmerPostsSecond.stopShimmer();


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

    private void loadUserPosts() {
        Query allPostsQuery=mRootRef
                .child("user_timeline")
                .child(currentUserId)
                .limitToLast(TOTAL_ITEMS_TO_LOAD);
        allPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){

                    navController.navigate(R.id.action_fragment_home_to_welcomeFragment);

                }
                else {
                    if (itemPos!=0){
                        //get all the data from the post reference i.e. postRef
                        allPostsQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                if(!dataSnapshot.exists()){

                                    Toast.makeText(getContext(), "You are not following to anyone", Toast.LENGTH_SHORT).show();

                                }else {
                                    ModelPosts modelPosts =dataSnapshot.getValue(ModelPosts.class);
                                    Log.d(TAG, "onChildAdded: "+dataSnapshot.getKey());
                                    Log.d(TAG, "onChildAdded: the current item position is "+itemPos);

                                    postsList.add(itemPos++,modelPosts);
                                    if (itemPos==1){
                                        mLastKey= dataSnapshot.getKey();
                                        mPrevKey= dataSnapshot.getKey();
                                    }
                                    Collections.reverse(postsList);
                                    Log.d(TAG, "onChildAdded: the value of item pos"+itemPos);
                                    Log.d(TAG, "onChildAdded:mLastKey "+mLastKey);
                                    Log.d(TAG, "onChildAdded:mPrevKey "+mPrevKey);
                                    Log.d(TAG, "onChildAdded: the value of current page"+mCurrentPage);

                                    //adapter
                                   // AdapterPostsAllPosts =new AdapterPostsAllPosts(getContext(),postsList);
                                    //set adapter to recycler view
                                    recyclerViewPosts.setAdapter(AdapterPostsAllPosts);

                                    // Stopping Shimmer Effect's animation after data is loaded to ListView
                                    mShimmerPosts.stopShimmer();
                                    mShimmerPosts.setVisibility(View.GONE);

                                    checkFollowing();}


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
                    }else {

                        //get all the data from the post reference i.e. postRef
                        allPostsQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                if(!dataSnapshot.exists()){

                                    Toast.makeText(getContext(), "You are not following to anyone", Toast.LENGTH_SHORT).show();

                                }else
                                {
                                    ModelPosts modelPosts =dataSnapshot.getValue(ModelPosts.class);
                                    Log.d(TAG, "onChildAdded: "+dataSnapshot.getKey());
                                    Log.d(TAG, "onChildAdded: the current item position is "+itemPos);

                                    postsList.add(itemPos++,modelPosts);
                                    if (itemPos==1){
                                        mLastKey= dataSnapshot.getKey();
                                        mPrevKey= dataSnapshot.getKey();
                                    }
                                    Collections.reverse(postsList);
                                    Log.d(TAG, "onChildAdded: the value of item pos"+itemPos);
                                    Log.d(TAG, "onChildAdded:mLastKey "+mLastKey);
                                    Log.d(TAG, "onChildAdded:mPrevKey "+mPrevKey);
                                    Log.d(TAG, "onChildAdded: the value of current page"+mCurrentPage);

                                    //adapter
                                   // AdapterPostsAllPosts =new AdapterPostsAllPosts(getContext(),postsList);
                                    //set adapter to recycler view
                                    recyclerViewPosts.setAdapter(AdapterPostsAllPosts);

                                    // Stopping Shimmer Effect's animation after data is loaded to ListView
                                    mShimmerPosts.stopShimmer();
                                    mShimmerPosts.setVisibility(View.GONE);

                                    checkFollowing();}


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

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_messages:
                Intent intentToMessages =new Intent(getContext(), AllMessagesActivity.class);
                startActivity(intentToMessages);
                getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;
            case R.id.search_users:
//                Intent intentToSearchUsers =new Intent(getContext(), SearchUsersActivity.class);
//                startActivity(intentToSearchUsers);
//                getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                Navigation.findNavController(v).navigate(R.id.action_fragment_home_to_searchUsersFragment);

                break;
            case R.id.user_notifications:
//                Intent intentToNotifications=new Intent(getContext(), NotificationsActivity.class);
//                startActivity(intentToNotifications);
//                getActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
//                FragmentManager fragmentManager= getActivity().getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
//                        .replace(R.id.fragmentContainerHome,new NotificationsFragment())
//                        .addToBackStack(null)
//                        .commit();
                Navigation.findNavController(v).navigate(R.id.action_fragment_home_to_notificationsFragment2);
                break;
            default:
                break;

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: started with item pos"+itemPos);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: distroyed");
        itemPos=0;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: paused");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: stopped");
        super.onStop();
        
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: view destroyed");
        itemPos=0;
        Log.d(TAG, "onDestroyView: view destroyed with item pos"+itemPos);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: detached");
    }

    @Override
    public void onItemClicked(int position, String userUid) {

        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id",userUid);
        navController.navigate(R.id.action_global_accountProfileFragment,bundle);

    }
}
