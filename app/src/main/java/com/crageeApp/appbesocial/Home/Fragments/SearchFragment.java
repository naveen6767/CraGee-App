package com.crageeApp.appbesocial.Home.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterPostsAllPosts;
import com.crageeApp.appbesocial.Interfaces.interfaceHome;
import com.crageeApp.appbesocial.Models.ModelPosts;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFragment extends Fragment implements interfaceHome {
    private View  SearchFragmentView;
    private RecyclerView recyclerViewPosts;
    private List<ModelPosts> postsList;
    private AdapterPostsAllPosts adapterPostsAllPosts;
    private DatabaseReference postRef,likesRef,postsRef,mRootRef;
    private FirebaseAuth userAuth;
    private String currentUserId;
    private String followingUserKey,oldestPostId;
    private Boolean isScrolling=false;
    private LinearLayoutManager manager,storiesManager;
    private int totalItems,currentItems,scrollOutItems;
    private ProgressBar progressBar;
    private static final int TOTAL_ITEMS_TO_LOAD=6;
    private int mCurrentPage=1;
    private int itemPos=0;
    private String mLastKey="";
    private String mPrevKey="";
    private boolean loading = true;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private ImageView sendMessages,searchUsers,userNotifications,newMessageNotify,newNotificationNotify;
    private ShimmerFrameLayout mShimmerPosts,mShimmerPostsSecond;
    private BottomSheetDialog bottomSheetDialog;
    private ImageView imageShare;
    private EditText shareText;
    private int position;
    private LinearLayout searchFriends;
    private static final String TAG = "SearchFragment";
    private NavController navController;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //initialize post list
        postsList =new ArrayList<>();
        navController = Navigation.findNavController(getActivity(),R.id.navigation_host_fragment);

        // Inflate the layout for this fragment

        SearchFragmentView =inflater.inflate(R.layout.fragment_search, container, false);
        recyclerViewPosts = SearchFragmentView.findViewById(R.id.all_postsRecyclerView);
        mShimmerPosts =  SearchFragmentView.findViewById(R.id.all_posts_shimmer);
//        mShimmerPostsSecond =  SearchFragmentView.findViewById(R.id.all_posts_shimmer_second);
        progressBar =  SearchFragmentView.findViewById(R.id.all_posts_pb_bar);
        searchFriends =  SearchFragmentView.findViewById(R.id.search_layout);
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
        manager=new LinearLayoutManager( SearchFragmentView.getContext());
        recyclerViewPosts.setHasFixedSize(true);

        //adapter
        adapterPostsAllPosts =new AdapterPostsAllPosts(getContext(),postsList, this);

        //set adapter to recycler view
        recyclerViewPosts.setAdapter(adapterPostsAllPosts);

        loadUserPosts();

        //set layout to recycler view
        recyclerViewPosts.setLayoutManager(manager);
        recyclerViewPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                 isScrolling=true;
                   }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                currentItems=manager.getChildCount();
                totalItems=manager.getItemCount();
                scrollOutItems=manager.findFirstVisibleItemPosition();
                Log.d(TAG, "onScrolled: current items is "+currentItems);
                Log.d(TAG, "onScrolled: total items is "+totalItems);
                Log.d(TAG, "onScrolled: scrolled items"+scrollOutItems);

                if (isScrolling&&(currentItems+scrollOutItems==totalItems)){
                    //data fetch
                    isScrolling=false;
                    mCurrentPage++;
                    loadMorePosts();
                }
            }
        });

        searchFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager= getActivity().getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
//                        .replace(R.id.fragmentContainerHome,new SearchUsersFragment())
//                        .addToBackStack(null)
//                        .commit();
                Navigation.findNavController(v).navigate(R.id.action_fragment_search_to_searchUsersFragment);



            }
        });



        return SearchFragmentView;
    }

    private void loadMorePosts() {

        Log.d(TAG, "loadMorePosts: loading more posts");
      //  progressBar.setVisibility(View.VISIBLE);
//        mShimmerPostsSecond.setVisibility(View.VISIBLE);
//        mShimmerPostsSecond.startShimmer();
        Log.d(TAG, "loadMorePosts: the value of last key is "+mLastKey);

        Query nextPostsQuery =postsRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);
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
                adapterPostsAllPosts.notifyDataSetChanged();
//                mRefreshLayout.setRefreshing(false);
              //  progressBar.setVisibility(View.GONE);
//                mShimmerPostsSecond.stopShimmer();
//                mShimmerPostsSecond.setVisibility(View.GONE);


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

        //get all the data from the post reference i.e. postRef
        Query allPostsQuery=postsRef.limitToLast(TOTAL_ITEMS_TO_LOAD);
        allPostsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ModelPosts modelPosts =dataSnapshot.getValue(ModelPosts.class);
                Log.d(TAG, "onChildAdded: "+dataSnapshot.getKey());


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
               // adapterPostsAllPosts =new AdapterPostsAllPosts(getContext(),postsList);


                // Stopping Shimmer Effect's animation after data is loaded to ListView
                mShimmerPosts.stopShimmer();
                mShimmerPosts.setVisibility(View.GONE);



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
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: view destroyed");
        itemPos=0;
        Log.d(TAG, "onDestroyView: view destroyed with item pos"+itemPos);
    }

    @Override
    public void onItemClicked(int position, String userUid) {
        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id",userUid);
        navController.navigate(R.id.action_global_accountProfileFragment,bundle);
    }
}
