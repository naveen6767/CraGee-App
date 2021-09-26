 package com.crageeApp.appbesocial.Home.Fragments;

 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;

 import androidx.annotation.NonNull;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.appcompat.widget.Toolbar;
 import androidx.fragment.app.Fragment;
 import androidx.navigation.NavController;
 import androidx.navigation.Navigation;
 import androidx.navigation.ui.NavigationUI;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;

 import com.crageeApp.appbesocial.Adapters.AdapterPostsAllPosts;
 import com.crageeApp.appbesocial.Interfaces.interfaceHome;
 import com.crageeApp.appbesocial.Models.ModelPosts;
 import com.crageeApp.appbesocial.R;
 import com.facebook.shimmer.ShimmerFrameLayout;
 import com.google.android.gms.ads.AdRequest;
 import com.google.android.gms.ads.AdSize;
 import com.google.android.gms.ads.AdView;
 import com.google.android.gms.tasks.OnCompleteListener;
 import com.google.android.gms.tasks.Task;
 import com.google.android.material.floatingactionbutton.FloatingActionButton;
 import com.google.firebase.auth.FirebaseAuth;
 import com.google.firebase.database.DataSnapshot;
 import com.google.firebase.database.DatabaseError;
 import com.google.firebase.database.DatabaseReference;
 import com.google.firebase.database.FirebaseDatabase;
 import com.google.firebase.database.Query;
 import com.google.firebase.database.ValueEventListener;
 import com.google.firebase.dynamiclinks.DynamicLink;
 import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
 import com.google.firebase.dynamiclinks.ShortDynamicLink;
 import com.squareup.picasso.Picasso;

 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;

 import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment implements interfaceHome, View.OnClickListener {
    private View view;
    private Toolbar profileToolBar;
    private TextView account_Name, currentUsrPosts,
            currentUserFollowers, currentUserFollowing, currentAccountBio,account_userId,
            aboutPage, webPageAdd, emailPageAdd, pageAddressText, verification;
    private DatabaseReference usersReference, likesRef, current_usersReference,mRootRef;
    private String currentUserId;
    private CircleImageView userProfileImage;
    private FloatingActionButton floatingBtn;
    private RecyclerView rVCurrentUserPosts;
    private List<String> noFollowers;
    private List<String> noFollowing;
    private LinearLayout personalAccountLayout, pageAccountLayout, pageAddressLayout;
    private List<ModelPosts> postsList;
    private AdapterPostsAllPosts AdapterPostsAllPosts;
    private AdView adView;
    private FrameLayout adContainerView;
    private Context context;
    private String accountCategory, timeStamp, profileName, profileImage, receiverUserId, senderUserId, currentPrivacy, celebrityCategory,profileUserId;
    private ImageView accountTick;
    private String oldestPostId;
    private static final String TAG = "ProfileFragment";
    private Boolean isScrolling = false;
    private LinearLayoutManager manager;
    private int totalItems, currentItems, scrollOutItems;
    private ProgressBar progressBar;
    private ScrollView profileScroll;
    private static int count = 0;
    private Uri mInvitationUrl;
    private ShimmerFrameLayout profileShimmer;
    private NavController navController;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        count += 1;
        Log.d(TAG, "onCreateView: count" + count);
        //initialize post list
        postsList = new ArrayList<>();

        navController = Navigation.findNavController(getActivity(),R.id.navigation_host_fragment);

        //initialize all the views here
        userProfileImage = view.findViewById(R.id.visit_profile_image);
        currentAccountBio = view.findViewById(R.id.visit_user_status);
        personalAccountLayout = view.findViewById(R.id.layout_PersonalAccount);
        pageAccountLayout = view.findViewById(R.id.about_page_layout);
        pageAddressLayout = view.findViewById(R.id.layout_page_address);
        aboutPage = view.findViewById(R.id.eTDescription);
        webPageAdd = view.findViewById(R.id.page_website);
        pageAddressText = view.findViewById(R.id.addressOfPage);
        emailPageAdd = view.findViewById(R.id.page_email);
        floatingBtn = view.findViewById(R.id.floatingButton);
        account_Name = view.findViewById(R.id.account_Name);
        currentUsrPosts = view.findViewById(R.id.no_user_posts);
        currentUserFollowers = view.findViewById(R.id.no_user_followers);
        currentUserFollowing = view.findViewById(R.id.no_user_following);
        verification = view.findViewById(R.id.verifyUserProfile);
        adContainerView = view.findViewById(R.id.profileAdContainer);
        accountTick = view.findViewById(R.id.account_category_profile);
        progressBar = view.findViewById(R.id.progressBarProfile);
        profileScroll = view.findViewById(R.id.profileScroller);
        account_userId = view.findViewById(R.id.account_userId);
        profileShimmer = view.findViewById(R.id.profile_fragment_shimmer);
        //path of all posts
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        //initialize the progress dialog for the user information fragment
        adView = new AdView(getContext());
        adView.setAdUnitId(getString(R.string.profile_fragment_ad_unit));
        adContainerView.addView(adView);
//      loadBanner();
        noFollowers = new ArrayList<>();
        noFollowing = new ArrayList<>();
        //init the tool bar here
        profileToolBar = view.findViewById(R.id.profile_tool_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(profileToolBar);// replace toolbar as the ActionBar
        //setHasOptionsMenu is used to display the menu options in the tool bar
        setHasOptionsMenu(true);

        //recycler views and its properties
        rVCurrentUserPosts = view.findViewById(R.id.current_user_posts_list);
        manager = new LinearLayoutManager(getActivity());
        //show the newest post first ,for this load from last
        manager.setStackFromEnd(true);
        manager.setReverseLayout(true);

        //set layout to recycler view
        rVCurrentUserPosts.setLayoutManager(manager);
        rVCurrentUserPosts.setNestedScrollingEnabled(false);
        AdapterPostsAllPosts = new AdapterPostsAllPosts(getActivity(), postsList, this);


        //now we will retrieve the user info in the profile activity
        MyTask myTask = new MyTask();
        myTask.execute(currentUserId);
        countFollow(count);
        floatingBtn.setOnClickListener(this);
        verification.setOnClickListener(this);
        currentUserFollowers.setOnClickListener(this);
        currentUserFollowing.setOnClickListener(this);
        loadUserPosts();
        return view;
    }
    private void countFollow(int count) {
        if(count==1){
            Query currentUserQuery = usersReference.orderByKey().equalTo(currentUserId);
            currentUserQuery.keepSynced(true);
            currentUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String category = (String) snapshot.child(currentUserId).child("accountCategory").getValue();
                    if (category.equals("Normal")) {

                        countFollowing();
                        countFollowers();
                        Log.d(TAG, "countFollow: count of followers has been done " + count);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }



    }
    private void countFollowing() {
        Query countFollowingQuery = mRootRef.child("user_following").orderByKey().equalTo(currentUserId);
        countFollowingQuery.keepSynced(true);
        countFollowingQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: count following data snapshot"+dataSnapshot);
                String followingCount = String.valueOf(dataSnapshot.child(currentUserId).getChildrenCount());
                HashMap<String, Object> followingHashmap = new HashMap<>();
                followingHashmap.put("noFollowing", followingCount);
                usersReference.child(currentUserId).updateChildren(followingHashmap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void countFollowers() {
        Query countFollowersQuery =  mRootRef.child("user_followers").orderByKey().equalTo(currentUserId);
        countFollowersQuery.keepSynced(true);
        countFollowersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String followersCount = String.valueOf(dataSnapshot.child(currentUserId).getChildrenCount());
                HashMap<String, Object> followersHashmap = new HashMap<>();
                followersHashmap.put("noFollowers", followersCount);
                usersReference.child(currentUserId).updateChildren(followersHashmap);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadUserPosts() {
        //get all the data from the post reference i.e. postRef
        mRootRef
                .child("user_posts")
                .child(currentUserId)
                .orderByChild("pTime")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            postsList.clear();
                            Log.d(TAG, "onDataChange: the value of data snapshot in profile fragment is "+dataSnapshot);
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Log.d(TAG, "onDataChange: the value of ds is "+ds);
                                ModelPosts modelPosts = ds.getValue(ModelPosts.class);
                                Log.d(TAG, "onDataChange: the value of model posts"+modelPosts);
                                postsList.add(modelPosts);
                                Log.d(TAG, "onDataChange: inside postsList"+postsList);
                                //adapter
                             //   AdapterPostsAllPosts = new AdapterPostsAllPosts(getActivity(), postsList);
                                //set adapter to recycler view
                                rVCurrentUserPosts.setAdapter(AdapterPostsAllPosts);

                                profileShimmer.stopShimmer();
                                profileShimmer.setVisibility(View.GONE);
                            }
                        }else {
                            profileShimmer.stopShimmer();
                            profileShimmer.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //in case of error
                        Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_toolbar_menu, menu);
        menu.findItem(0);
        super.onCreateOptionsMenu(menu, inflater);
    }


    // Activity's override method used to perform click events on menu items
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            Intent intentToSettings = new Intent(getContext(), SettingsActivity.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//            }
//            startActivity(intentToSettings);
//            return true;
//        } else if (id == R.id.action_wallet) {
//
////               Intent intent=new Intent(Intent.ACTION_SEND);
////               intent.setType("text/plain");
////               intent.putExtra(Intent.EXTRA_TEXT,view.getContext().getResources().getString(R.string.share_app_link));
////               startActivity(intent.createChooser(intent,"Share"));
////               createlink();
//
//
//
//            WalletFragment fragment = new WalletFragment();
//            FragmentManager fragmentManager= getActivity().getSupportFragmentManager();
////            fragmentManager.beginTransaction()
////                    .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
////                    .replace(R.id.fragmentContainerHome,fragment)
////                    .addToBackStack(null)
////                    .commit();
//            NavigationUI.onNavDestinationSelected(item,)
//
//
//            return true;
//        }else if (id == R.id.action_refer){
//            Intent intentToRewards = new Intent(getContext(), RewardsActivity.class);
//            startActivity(intentToRewards);
//
//        }
//        return super.onOptionsItemSelected(item);
//    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        NavController navController = Navigation.findNavController(getActivity(), R.id.navigation_host_fragment);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.verifyUserProfile:
//                Intent intentToVerification = new Intent(getContext(), VerificationActivity.class);
//                intentToVerification.putExtra("userId", currentUserId);
//                startActivity(intentToVerification);
//                verificationFragment verificationFragment  = new verificationFragment ();
//                Bundle verificationArgs = new Bundle();
//                verificationArgs.putString("userId", currentUserId);
//                verificationFragment.setArguments(verificationArgs);
                //Inflate the fragment
//                if (getFragmentManager() != null) {
//                    getFragmentManager()
//                            .beginTransaction()
//                            .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
//                            .replace(R.id.fragmentContainerHome, verificationFragment).commit();
//                }
                Navigation.findNavController(v).navigate(R.id.action_fragment_profile_to_verificationFragment);
                break;
            case R.id.floatingButton:
                Navigation.findNavController(v).navigate(R.id.action_fragment_profile_to_accountInformationFragment);

                break;
            case R.id.no_user_followers:
                Navigation.findNavController(v).navigate(R.id.action_fragment_profile_to_followersFragment);
                break;
            case R.id.no_user_following:
                Navigation.findNavController(v).navigate(R.id.action_fragment_profile_to_followingFragment);
                break;

            default:
                break;
        }
    }

    private void loadBanner() {

        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getContext(), adWidth);
    }

    @Override
    public void onItemClicked(int position, String userUid) {
        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id",userUid);
        navController.navigate(R.id.action_global_accountProfileFragment,bundle);
    }

    class MyTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            Query currentUserQuery = usersReference.orderByKey().equalTo(currentUserId);
            currentUserQuery.keepSynced(true);
            currentUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String getAccountChoice = (String) dataSnapshot.child(currentUserId).child("accountChoice").getValue();
                    currentPrivacy = (String) dataSnapshot.child(currentUserId).child("accountPrivacy").getValue();
                    accountCategory = (String) dataSnapshot.child(currentUserId).child("accountCategory").getValue();
                    celebrityCategory = (String) dataSnapshot.child(currentUserId).child("celebrityCategory").getValue();
                    profileName = (String) dataSnapshot.child(currentUserId).child("name").getValue();
                    profileUserId = (String) dataSnapshot.child(currentUserId).child("uniUserId").getValue();
                    profileImage = (String) dataSnapshot.child(currentUserId).child("image").getValue();
                    String requestNoOfPosts = String.valueOf(dataSnapshot.child(currentUserId).child("noPosts").getValue());
                    String requestNoOfFollowers = String.valueOf(dataSnapshot.child(currentUserId).child("noFollowers").getValue());
                    String requestNoOfFollowing = String.valueOf(dataSnapshot.child(currentUserId).child("noFollowing").getValue());
                    if (currentUserFollowers != null) {
                        currentUserFollowers.setText(requestNoOfFollowers);
                    }
                    if (currentUserFollowing != null) {
                        currentUserFollowing.setText(requestNoOfFollowing);
                    }

                    checkVerificationStatus(dataSnapshot.child(currentUserId));
                    account_Name.setText(profileName);

                    account_userId.setText(view.getContext().getResources().getString(R.string.userId,profileUserId));
                    currentUsrPosts.setText(requestNoOfPosts);
                    try {
                        Picasso.get().load(R.drawable.post_image_blue).into(userProfileImage);
                        Picasso.get().load(profileImage).placeholder(R.drawable.post_image_blue).into(userProfileImage);

                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.post_image_blue).into(userProfileImage);
                    }
                    if (dataSnapshot.child(currentUserId).hasChild("userBio")) {
                        currentAccountBio.setVisibility(View.VISIBLE);
                        String userStatus = (String) dataSnapshot.child(currentUserId).child("userBio").getValue();
                        currentAccountBio.setText(userStatus);
                    } else {
                        currentAccountBio.setText("");
                        currentAccountBio.setVisibility(View.GONE);
                    }

                    if (celebrityCategory.equals("Original")) {
                        accountTick.setVisibility(View.VISIBLE);
                    } else {
                        accountTick.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: the value of s is" + s);
        }
    }

    private void checkVerificationStatus(DataSnapshot dataSnapshot) {

        if (dataSnapshot.child("Verification").hasChild("verification status")) {
            Log.d(TAG, "checkVerificationStatus: " + dataSnapshot);
            String userVerification = (String) dataSnapshot.child("Verification").child("verification status").getValue();
            Log.d(TAG, "checkVerificationStatus: user verification" + userVerification);
            if (userVerification != null && userVerification.equals("Verified")) {
                verification.setText(getResources().getString(R.string.verification_verified));
            } else if (userVerification != null && userVerification.equals("Rejected")) {
                verification.setText(getResources().getString(R.string.verification_rejected));
            } else {
                verification.setText(getResources().getString(R.string.verification_pending));
            }
        } else {
            Log.d(TAG, "checkVerificationStatus: the verification is pending");
            verification.setText("Verification Pending");

        }

    }

    public void createlink() {
        Log.e("main", "create link ");
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://play.google.com/store/apps/details?id=com.crageeApp.appbesocial"))
                .setDomainUriPrefix("https://crageeapp.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildDynamicLink();

        //click -- link -- google play store -- installed/ or not  ----
        Uri dynamicLinkUri = dynamicLink.getUri();
        Log.e("main", "  Long refer " + dynamicLink.getUri());
        //   https://referearnpro.page.link?apn=blueappsoftware.referearnpro&link=https%3A%2F%2Fwww.blueappsoftware.com%2F
        // apn  ibi link

        // manual link
        String shareLinkText = "https://crageeapp.page.link/?" +
                "link=https://play.google.com/store/apps/details?id=com.crageeApp.appbesocial/" +
                "&apn=" + view.getContext().getPackageName() +
                "&st=" + "My Refer Link" +
                "&sd=" + "Reward Money 21";
        // shorten the link
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                //.setLongLink(dynamicLink.getUri())
                .setLongLink(Uri.parse(shareLinkText))  // manually
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Log.e("main ", "short link " + shortLink.toString());
                            // share app dialog
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                            intent.setType("text/plain");
                            startActivity(intent);
                        } else {
                            // Error
                            // ...
                            Log.e("main", " error " + task.getException());

                        }
                    }
                });
    }
}
