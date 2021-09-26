package com.crageeApp.appbesocial.Home.Fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterGroups;
import com.crageeApp.appbesocial.Groups.CreateGroupActivity;
import com.crageeApp.appbesocial.Groups.GroupCategoriesActivity;
import com.crageeApp.appbesocial.Groups.YourGroupsActivity;
import com.crageeApp.appbesocial.Models.ModelGroups;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    private RecyclerView recyclerViewGroups;
    private DatabaseReference groupsRef;
    private AdapterGroups adapterGroups;
    private List<ModelGroups> groupsList;
    private AdView adView;
    private FrameLayout adContainerView;
    public static final String TAG = "Groups Fragment";
    public GroupsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_groups, container, false);

        //initialize the database reference
        groupsRef= FirebaseDatabase.getInstance().getReference("Groups");

        //initialization of multiple views

        Button btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        Button btnGroupCategories = view.findViewById(R.id.btnGroupCategories);
        Button btnYourGroups = view.findViewById(R.id.btnYourGroups);
        SearchView groupsSearch = view.findViewById(R.id.sVGroups);
        adContainerView= view.findViewById(R.id.groupsAdContainer);
        //init the recycler view and its properties
        recyclerViewGroups =view.findViewById(R.id.recyclerViewGroupsList);
        recyclerViewGroups.setNestedScrollingEnabled(false);
        recyclerViewGroups.setHasFixedSize(true);
        recyclerViewGroups.setLayoutManager(new LinearLayoutManager(getActivity()));
        adView = new AdView(getContext());
        adView.setAdUnitId(getString(R.string.groups_fragment_ad_unit));
        adContainerView.addView(adView);
//         loadBanner();
        //init the tool bar and set as action bar
        Toolbar groupsToolBar = view.findViewById(R.id.tool_bar_groups);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(groupsToolBar);// Setting/replace toolbar as the ActionBar
        }

        //init the groups list
        groupsList=new ArrayList<>();

        //get all the groups as suggestions to the user
        //getAllGroups();

        groupsBackgroundThread groupsBackgroundThread=new groupsBackgroundThread();
        groupsBackgroundThread.execute();

        /*
        here now we will search for the different groups
         */
        //search listener
        groupsSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user presses search button from the keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim())){
                    //search text contains text,search it
                    Log.i(TAG, "onQueryTextSubmit: search text contains text,search it");
                    searchQueriedGroup(query);
                }else {
                    Log.i(TAG, "onQueryTextSubmit: search text is empty");
                    //search text is empty
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                //called whenever user presses any single letter through keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(newText.trim())){
                    //search text contains text,search it
                    searchQueriedGroup(newText);

                }else {
                    Log.i(TAG, "onQueryTextChange: search text is empty");

                    //search text is empty
                }
                return false;
            }
        });

        //onclick listener for the buttons
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToCreteGroupActivity();
            }
        });
        btnGroupCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getContext(), "Still working", Toast.LENGTH_SHORT).show();
                sendUserToGroupCategoriesActivity();
            }
        });
        btnYourGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentTOYourGroups = new Intent(getContext(), YourGroupsActivity.class);
                startActivity(intentTOYourGroups);
            }
        });


        return view;

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

    private void searchQueriedGroup(final String newText) {
        //get the path of the user database named"users"
        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference("Groups");
        //get all data from the path
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupsList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ModelGroups modelGroups =ds.getValue(ModelGroups.class);
                    if (modelGroups.getName().toLowerCase().contains(newText.toLowerCase()))
                    {
                        groupsList.add(modelGroups);
                    }
                    //adapters
                    adapterGroups=new AdapterGroups(getContext(),groupsList);

                    //refresh adapter
                    adapterGroups.notifyDataSetChanged();
                    //set adapter to the recycler view
                    recyclerViewGroups.setAdapter(adapterGroups);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAllGroups() {

        //get the groups data
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupsList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren())
                {
                    ModelGroups modelGroups=ds.getValue(ModelGroups.class);
                    groupsList.add(modelGroups);

                    //adapter
                    adapterGroups=new AdapterGroups(getActivity(),groupsList);
                    //set adapter to recycler view
                    recyclerViewGroups.setAdapter(adapterGroups);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToGroupCategoriesActivity() {

        Intent intentCategoriesActivity =new Intent(getContext(), GroupCategoriesActivity.class);
        startActivity(intentCategoriesActivity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        }

    }

    private void sendUserToCreteGroupActivity() {

        Intent intentGroupActivity = new Intent(getContext(), CreateGroupActivity.class);
        startActivity(intentGroupActivity);
    }
    public class groupsBackgroundThread extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: groups background thread is running");
            //get all the groups as suggestions to the user
            getAllGroups();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }



}
