package com.crageeApp.appbesocial.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.crageeApp.appbesocial.Chat.fragments.PersonalChatFragment;
import com.crageeApp.appbesocial.Chat.fragments.groupsChatFragment;
import com.crageeApp.appbesocial.Home.HomeActivity;
import com.crageeApp.appbesocial.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AllMessagesActivity extends AppCompatActivity {
    public static FragmentManager fragmentManager;
    private ViewPager viewPager;
    private ImageButton directMessageBtn;
    private static final String TAG = "AllMessagesActivity";
    private TabLayout tabLayout;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_messages);
        //init the buttons here
        initializeViews();
        //Initialize the fragment manager
        fragmentManager = getSupportFragmentManager();
        setSupportActionBar(toolbar);
        // add back arrow to toolbar
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupViewPager(viewPager);
        String currentUserID=FirebaseAuth.getInstance().getUid();
        DatabaseReference chatUserRef= FirebaseDatabase.getInstance().getReference("Users");
        chatUserRef
                .child(currentUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.hasChild("name")){

                                String profileUserName =(String) dataSnapshot.child("name").getValue();
                                Log.d(TAG, "onDataChange:the name of the account is "+profileUserName);

                            }
//                            else {
////                                sendUserToAccountInfo();
//
//                            }
                        }
                        else {
                            Log.d(TAG, "onDataChange: current user does not exist");
                            sendUserToHome();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: the retrieve failed"+databaseError.getMessage());
                        Log.d(TAG, "onCancelled: the error is "+databaseError);
                        Log.d(TAG, "onCancelled: the error is "+databaseError.getDetails());

                    }
                });
        directMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentToDirectMessage = new Intent(AllMessagesActivity.this,DirectMessagesActivity.class);
                startActivity(intentToDirectMessage);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
        });


    }
    private void initializeViews() {
        //initialize the tool bar
        directMessageBtn =findViewById(R.id.directMessageButton);
        viewPager=(ViewPager) findViewById(R.id.allMessagesViewPager);
        toolbar=(Toolbar) findViewById(R.id.toolbarAllMessages);
    }
//    private void sendUserToAccountInfo() {
//        Intent intentToAccountInfo=new Intent(AllMessagesActivity.this, AccountInformationActivity.class);
//        startActivity(intentToAccountInfo);
//        finish();
//    }
    private void sendUserToHome() {
        Intent intentToHome=new Intent(AllMessagesActivity.this, HomeActivity.class);
        startActivity(intentToHome);
        finish();
    }
    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(fragmentManager);
        viewPagerAdapter.addFragment(new PersonalChatFragment(),"Personal Chat");
        viewPagerAdapter.addFragment(new groupsChatFragment(),"Groups Chat");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.profile_toolbar_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//
//
//    // Activity's override method used to perform click events on menu items
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//
//            Intent intentToSettings = new Intent(this, SettingsActivity.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                Objects.requireNonNull(this).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//            }
//            startActivity(intentToSettings);
//            return true;
//        } else if (id == R.id.action_share) {
//
////            Intent intent=new Intent(Intent.ACTION_SEND);
////            intent.setType("text/plain");
////            intent.putExtra(Intent.EXTRA_TEXT,view.getContext().getResources().getString(R.string.share_app_link));
////            startActivity(intent.createChooser(intent,"Share"));
//
//            startActivity(new Intent(this, RewardsActivity.class));
//
//            //  createlink();
//
//
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
}

