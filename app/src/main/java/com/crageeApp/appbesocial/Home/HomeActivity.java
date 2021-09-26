package com.crageeApp.appbesocial.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.crageeApp.appbesocial.AccountChoice.AccountChoiceActivity;
import com.crageeApp.appbesocial.Login.LoginActivity;
import com.crageeApp.appbesocial.MessageNotifications.Token;
import com.crageeApp.appbesocial.Models.ModelToken;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;

public class HomeActivity extends AppCompatActivity {


    private DatabaseReference userRef;
    private String currentUserID,token;
    private static final String TAG = "HomeActivity";
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private BottomNavigationView bottomNavigation;
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        navController = Navigation.findNavController(this,R.id.navigation_host_fragment);
        bottomNavigation= findViewById(R.id.bottomNavViewBar);
        NavigationUI.setupWithNavController(bottomNavigation, navController);
        userRef= FirebaseDatabase.getInstance().getReference("Users");
        HashMap<String, Object> defaultsRate = new HashMap<>();
        defaultsRate.put("new_version_code", String.valueOf(getVersionCode()));

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(10) // change to 3600 on published app
                .build();

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(defaultsRate);
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    final String new_version_code = mFirebaseRemoteConfig.getString("new_version_code");

                    if(Integer.parseInt(new_version_code) > getVersionCode())
                        showTheDialog("com.crageeApp.appbesocial", new_version_code );
                }
                else Log.e("MYLOG", "mFirebaseRemoteConfig.fetchAndActivate() NOT Successful");

            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent =new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        }
        else{

            currentUserID=FirebaseAuth.getInstance().getUid();
            getToken();
            SharedPreferences sharedPreferences=getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor= sharedPreferences.edit();
            editor.putString("Current_USERID",currentUserID);
            editor.apply();
            //check whether the current user has set the user name image and other things
            //if not then send the user to account Information activity
            if (FirebaseAuth.getInstance().getUid()!=null){
                Log.d(TAG, "onCreate: there is  current user id");
                userRef
                        .child(FirebaseAuth.getInstance().getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    Log.d(TAG, "onDataChange: current user exist"+FirebaseAuth.getInstance().getUid());
                                    if (dataSnapshot.hasChild("accountChoice")){
                                        Log.d(TAG, "onDataChange: current user has already set the account choice");
                                        if (dataSnapshot.hasChild("name")){
                                            Log.d(TAG, "onDataChange: current user has already set the user name");
                                            //now set the current user is online
                                            //set online
                                          //  checkOnlineStatus("online");
                                            if(dataSnapshot.child("name").getValue().equals("CraGee Name")){
//                                                sendUserToAccountInfo();
                                                sendUserToAccountChoice();

                                            }
                                        }else {
                                            Log.d(TAG, "onDataChange: current user has not set the name ");
//                                            sendUserToAccountInfo();
                                            sendUserToAccountChoice();


                                        }
                                    }
                                    else {
                                        Log.d(TAG, "onDataChange: current user has not set the account choice");
                                        sendUserToAccountChoice();
                                    }
                                }
                                else {
                                    Log.d(TAG, "onDataChange: current user id does not exist");
                                    Log.d(TAG, "onDataChange: sending user to the account info activity");
                                    //current user is the new user

                                    registerNewUser(FirebaseAuth.getInstance().getUid());
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
            else {
                Log.d(TAG, "onCreate: there is no  current user id");
                sendUserToAccountInfo();
            }

            Log.d(TAG, "onCreate: "+FirebaseAuth.getInstance().getUid());
            /*Initialize the fragment manager
            fragmentManager = getSupportFragmentManager();
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavViewBar);
            bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerHome,new HomeFragment()).commit();
            
             */
        }

//        //Initialize the fragment manager
//        fragmentManager = getSupportFragmentManager();
//        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavViewBar);
//        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerHome,new HomeFragment()  )
//                .commit();

        /*
        get the  referrer's UID from the user who has installed this app
         */
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();

                            String referrerUid = deepLink.getQueryParameter("invitedby");
                            Log.d(TAG, "onSuccess: the value of referrer id is "+referrerUid);
                            Log.d(TAG, "onSuccess: the value of deep link  is "+deepLink);
                            DatabaseReference referralRef=FirebaseDatabase.getInstance().getReference("Referrals");

                            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("referredBy").setValue(referrerUid);
                            /*
                            get the device token and match with the database
                            if the device token matched with the existing tokens then
                            it will not be counted in the referral and no money will be given
                             */
                            getToken();
                            DatabaseReference tokenRef=FirebaseDatabase.getInstance().getReference("Tokens");
                            Query tokenQuery=tokenRef.orderByChild("token");
                            tokenQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Log.d(TAG, "onDataChange: the value of snapshot is "+snapshot);
                                    for (DataSnapshot ds:snapshot.getChildren()){
                                        ModelToken modelToken=ds.getValue(ModelToken.class);
                                        if (modelToken != null && modelToken.getToken().equals(token)) {
                                            Log.d(TAG, "onDataChange: user has already installed the app in the device");
                                            /*
                                             *user has already installed the app in the current device
                                             * don't add this id into referral program
                                             */
                                        }
                                        else {
                                            Log.d(TAG, "onDataChange: user is installing the first time so the current user is added in the referral program");
                                            //user is installing the first time so the current user is added in the referral program
                                            if (referrerUid != null) {
                                                Query currentUserQuery = userRef.orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                currentUserQuery.keepSynced(true);
                                                currentUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                       String profileName = (String) snapshot.child(currentUserID).child("name").getValue();
                                                        String  profileUserId = (String) snapshot.child(currentUserID).child("uniUserId").getValue();
                                                        String profileImage = (String) snapshot.child(currentUserID).child("image").getValue();
                                                        HashMap<String, Object> referralMap = new HashMap<>();
                                                        referralMap.put(profileName,profileName);
                                                        referralMap.put(profileUserId,profileUserId);
                                                        referralMap.put(profileImage,profileImage);
                                                        referralMap.put("referralMoney",21);
                                                        referralRef
                                                                .child(referrerUid)
                                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                .updateChildren(referralMap);
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });


                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    }
                });
    }
    private void getToken() {
        Log.d(TAG, "getToken: getting token");
        //update token
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                token=task.getResult().getToken();
                Log.d(TAG, "onComplete: the value of current user token is "+token);
                updateToken(token);
            }
        });


    }

    public void updateToken(String token){
        Log.d(TAG, "updateToken: updating token "+token);
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken=new Token(token);
        reference.child(currentUserID).setValue(mToken);
    }

    private void registerNewUser(String uid) {
        Log.d(TAG, "registerNewUser: registering the new user details");
        Log.d(TAG, "registerNewUser: the current user is "+uid);

        //get user email and user id
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String mobile=  FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        DatabaseReference userRef= FirebaseDatabase.getInstance().getReference("Users");
         /*
         user has made an account
         now saving the login info
         */
        HashMap<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("uid",uid);
        userInfoMap.put("email",email);
        userInfoMap.put("mobile",mobile);
        userInfoMap.put("name","CraGee Name");
        userInfoMap.put("onlineStatus","online");
        userInfoMap.put("typingTo","noOne");
        userInfoMap.put("noPosts","0");
        userInfoMap.put("noFollowers","0");
        userInfoMap.put("noFollowing","1");
        userInfoMap.put("Earnings","0");
        userInfoMap.put("Credits","100");
        userInfoMap.put("ratings","1");
        userInfoMap.put("accountCategory","Normal");
        userInfoMap.put("celebrityCategory","Normal");
        userInfoMap.put("accountChoice","Personal Account");
        userInfoMap.put("accountPrivacy","Public");
        //put data with hash map in the database
        userRef.child(uid)
                .setValue(userInfoMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //send user to update the account choice
                        sendUserToAccountChoice();
                        finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: error in updating the account details");

            }
        });

    }

    private void sendUserToAccountChoice() {
        Intent intentToAccountInfo=new Intent(HomeActivity.this, AccountChoiceActivity.class);
        startActivity(intentToAccountInfo);
        finish();
    }

    private void sendUserToAccountInfo() {
        Intent intentToAccountInfo=new Intent(HomeActivity.this, AccountChoiceActivity.class);
        startActivity(intentToAccountInfo);
        finish();

    }

//    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//            Fragment selectedFragment =null;
//            switch (menuItem.getItemId())
//            {
//                case R.id.ic_home:
//                    selectedFragment = new HomeFragment();
//                    break;
//                case R.id.ic_find_friends:
//                    selectedFragment = new SearchFragment();
//                    break;
//                case R.id.ic_share:
//                    selectedFragment = new PostsFragment();
//                    break;
//                case R.id.icon_groups:
//                    selectedFragment = new GroupsFragment();
//                    break;
//                case R.id.ic_profile:
//                    selectedFragment = new ProfileFragment();
//                    break;
//            }
//            if (selectedFragment != null) {
//                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerHome,selectedFragment).commit();
//            }
//            return true;
//        }
//    };

    private PackageInfo pInfo;
    public int getVersionCode() {
        pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i("MYLOG", "NameNotFoundException: "+e.getMessage());
        }

        Log.d(TAG, "getVersionCode: the current version is "+ pInfo.versionCode);
        return pInfo.versionCode;
    }

    private void showTheDialog(final String appPackageName, String versionFromRemoteConfig) {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Update")
                .setMessage("This version is absolete, please update to version: " + versionFromRemoteConfig)
                .setPositiveButton("UPDATE", null)
                .show();

        dialog.setCancelable(false);

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });


    }

    private void manageConnections(){
        DatabaseReference infoConnected=FirebaseDatabase.getInstance().getReference(".info/connected");
        infoConnected.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: reading the connection");
                boolean connected=dataSnapshot.getValue(Boolean.class);
                if (connected){
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getUid());
                    HashMap<String, Object> hashMap=new HashMap<>();
                    hashMap.put("onlineStatus","online");
                    //update value of online status of current user
                    dbRef.updateChildren(hashMap);
                    HashMap<String, Object> lastSeenMap=new HashMap<>();
                    lastSeenMap.put("onlineStatus", ServerValue.TIMESTAMP);
                    dbRef.onDisconnect().updateChildren(lastSeenMap);
                }
                else {


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.d(TAG, "onCancelled: error with info connected"+databaseError.getMessage());

            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (FirebaseAuth.getInstance().getUid()!=null &&dataSnapshot.hasChild(FirebaseAuth.getInstance().getUid())){
                    Log.d(TAG, "onDataChange: current user has user id in the database");
                    manageConnections();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }
}
