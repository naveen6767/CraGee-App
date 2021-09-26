package com.crageeApp.appbesocial.Settings;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.crageeApp.appbesocial.Home.Fragments.HomeFragment;
import com.crageeApp.appbesocial.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class SettingsActivity extends AppCompatActivity {


    public static FragmentManager fragmentManager;
    private DatabaseReference userRef;
    private static final String TAG = "SettingsActivity";
    private FirebaseAuth userAuth;
    private LinearLayout settingsLayout;
    private TextView aboutApp,rateApp,inviteFriends,writeToUs,termsOfService,dataPolicy,communityStandards,logoutCurrentUser,accountPrivacyText,
            accountType,accountEmail;
    private  String currentUserId;
    private LinearLayout blueTickLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Initialize the fragment manager
        fragmentManager = getSupportFragmentManager();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerSettings,new settingsFragment())
                .commit();

    }




}
