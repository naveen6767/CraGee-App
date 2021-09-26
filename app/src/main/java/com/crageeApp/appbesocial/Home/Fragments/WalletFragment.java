package com.crageeApp.appbesocial.Home.Fragments;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterGroupsForEarning;
import com.crageeApp.appbesocial.Models.ModelGroups;
import com.crageeApp.appbesocial.ProgressButton;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class WalletFragment extends Fragment {
    private View walletFragmentView;
    private RecyclerView recyclerViewGroupEarning;
    private Toolbar toolbarWallet;
    private TextView showEarnings,showCredits,showGroupEarnings;
    private FirebaseAuth userAuth;
    private String currentUserId;
    private DatabaseReference userRef;
    private AdapterGroupsForEarning adapterGroupsForEarning;
    private Button earningsUpdate;
    private List<ModelGroups> yourGroupsList;
    private AdView adView;
    private FrameLayout adContainerView;
    private ProgressButton progressButton;
    public static final String TAG = "Wallet Fragment";
    private View watchVideos;
    private ImageButton backButton;
    public WalletFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        walletFragmentView=inflater.inflate(R.layout.fragment_wallet, container, false);

        //init the tool bar here
        backButton=walletFragmentView.findViewById(R.id.back_button_wallet);
        adContainerView=walletFragmentView.findViewById(R.id.walletAdContainer);
        //init the fire base here
        userAuth=FirebaseAuth.getInstance();
        currentUserId=userAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference("Users");
        //init the view here
        recyclerViewGroupEarning=walletFragmentView.findViewById(R.id.groups_earning_list);
        //init other views
        showEarnings=walletFragmentView.findViewById(R.id.earnings_show);
        showCredits=walletFragmentView.findViewById(R.id.credits_show);
        showGroupEarnings=walletFragmentView.findViewById(R.id.total_group_earnings);
        earningsUpdate=walletFragmentView.findViewById(R.id.update_earnings);
        watchVideos=walletFragmentView.findViewById(R.id.btnWalletWatchVideos);

        //set tool bar as the action bar
        if (getActivity()!=null){
            ((AppCompatActivity)getActivity()).setSupportActionBar(toolbarWallet);// Setting/replace toolbar as the ActionBar

        }


        //init the group requests list
        yourGroupsList=new ArrayList<>();

        //set the properties for the recycler view
        recyclerViewGroupEarning.setHasFixedSize(true);
        recyclerViewGroupEarning.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGroupEarning.setNestedScrollingEnabled(false);
        adView = new AdView(getContext());
        adView.setAdUnitId(getString(R.string.wallet_fragment_ad_unit));
        adContainerView.addView(adView);
//         loadBanner();

        progressButton =new ProgressButton(getContext(),watchVideos);
        progressButton.buttonWatchVideos();

        //get all your groups
        getYourGroupsEarning();

        calculateEarnings();
        earningsUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateGroupEarnings();
            }
        });
        getEarnings();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment fragment = new ProfileFragment();
                FragmentManager fragmentManager= getActivity().getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left)
//                        .replace(R.id.fragmentContainerHome,fragment)
//                        .addToBackStack(null)
//                        .commit();
            }
        });

//        getActivity().onBackPressed();


        return walletFragmentView;
    }

    private void calculateGroupEarnings() {
        userRef
                .child(currentUserId)
                .child("Groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, "onDataChange: the no of children"+dataSnapshot.getChildrenCount());

                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                            ModelGroups modelGroups=snapshot.getValue(ModelGroups.class);
                            assert modelGroups != null;
                            String groupId=modelGroups.getGroupId();
                            updateCurrentGroupEarnings(groupId);

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {


                    }
                });
    }

    private void updateCurrentGroupEarnings(final String groupId) {
        DatabaseReference groupChatRef = FirebaseDatabase.getInstance().getReference("Groups");
        groupChatRef

                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(groupId)){
                            Log.i(TAG, "onDataChange: the value of datasnapshot"+dataSnapshot.getKey());
                            Log.i(TAG, "onDataChange: get the value of current group all messages");
                            int allMessagesNo= 0;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                allMessagesNo = Integer.parseInt(Objects.requireNonNull(dataSnapshot.child(groupId).child("totalMessages").getValue()).toString());
                            }
                            Log.i(TAG, "onDataChange: the value of all messages"+allMessagesNo);
                            Log.i(TAG, "onDataChange: the value of all messages"+Integer.parseInt(dataSnapshot.child(groupId).child("totalMessages").getValue().toString()));
                            if (allMessagesNo==0){
                                Log.i(TAG, "onDataChange: the value of all messages is "+allMessagesNo);
                            }
                            else {
                                int estimatedEarnings=allMessagesNo/10;
                                Log.i(TAG, "onDataChange: the current value of estimated earnings are "+estimatedEarnings);
                                sendEarningsToGroups(estimatedEarnings,groupId);
                            }
                        }
                        else {
                            Log.i(TAG, "onDataChange: current data snapshot does not have the requested group chat key");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: error in getting the all messages in the current group"+databaseError.getMessage());

                    }
                });
    }

    private void sendEarningsToGroups(int estimatedEarnings, String groupId) {
        Log.i(TAG, "sendEarningsToGroups: estimatedEarnings is"+estimatedEarnings);
        Log.i(TAG, "sendEarningsToGroups: groupId is"+groupId);
        Log.i(TAG, "sendEarningsToGroups: the final value i=of earnings is"+51+estimatedEarnings);
        HashMap<String, Object> updatedEarnings = new HashMap<>();
        updatedEarnings.put("earning",String.valueOf(51+estimatedEarnings));
        userRef
                .child(currentUserId)
                .child("Groups")
                .child(groupId)
                .updateChildren(updatedEarnings)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: updated earnings");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });



    }

    private void calculateEarnings() {
        userRef
                .child(currentUserId)
                .child("Groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, "onDataChange: the no of children"+dataSnapshot.getChildrenCount());
                        int groupEarning=0;
                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                            ModelGroups modelGroups=snapshot.getValue(ModelGroups.class);
                            groupEarning=groupEarning+Integer.parseInt(modelGroups.getEarning());
                            Log.i(TAG, "onDataChange: the total earning from the groups is "+" "+groupEarning);
                            Log.i(TAG, "onDataChange: the value of Integer.parseInt(modelGroups.getEarning()"+" "+Integer.parseInt(modelGroups.getEarning()));
                        }
                        Log.i(TAG, "onDataChange: current value of group earnings are"+groupEarning);
                        updateEarnings(String.valueOf(groupEarning));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void updateEarnings(String groupEarning) {
        userRef
                .child(currentUserId)
                .child("Earnings")
                .setValue(groupEarning)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: the earnings have been updated");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: error in updating the earnings"+e.getMessage());

            }
        });
    }
    private void getEarnings() {
        userRef
                .child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, "onDataChange: successfully getting the earnings");
                        String earningsRetrieved = (String) dataSnapshot.child("Earnings").getValue().toString();
                        String creditsRetrieved = (String) dataSnapshot.child("Credits").getValue().toString();
                      //  showEarnings.setText(earningsRetrieved+".00 INR");
                         showEarnings.setText(walletFragmentView.getContext().getResources().getString(R.string.rupees,earningsRetrieved));
                        showCredits.setText(creditsRetrieved);
                        showGroupEarnings.setText(walletFragmentView.getContext().getResources().getString(R.string.rupees,earningsRetrieved));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: error in getting the earnings"+databaseError.getMessage());

                    }
                });



    }


    private void getYourGroupsEarning() {
        userRef
                .child(currentUserId)
                .child("Groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i(TAG, "onDataChange: started getting the groups earnings"+dataSnapshot);
                        yourGroupsList.clear();
                        for (DataSnapshot ds:dataSnapshot.getChildren())
                        {
                            ModelGroups modelGroups=ds.getValue(ModelGroups.class);
                            yourGroupsList.add(modelGroups);
                            Log.i(TAG, "onDataChange: inside the modelGroups"+modelGroups.getEarning());

                            //adapters
                            adapterGroupsForEarning=new AdapterGroupsForEarning(getContext(),yourGroupsList);

                            //set adapter to the recycler view
                            recyclerViewGroupEarning.setAdapter(adapterGroupsForEarning);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.i(TAG, "onCancelled: error in getting the groups earnings"+databaseError.getMessage());

                    }
                });
    }

    private void loadBanner() {

        AdRequest adRequest =
                new AdRequest.Builder().build();

        AdSize adSize = getAdSize();

        adView.setAdSize(adSize);


        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {

        Display display =getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getContext(), adWidth);
    }

}
