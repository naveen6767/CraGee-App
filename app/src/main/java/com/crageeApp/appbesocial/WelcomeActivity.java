package com.crageeApp.appbesocial;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.crageeApp.appbesocial.AccountChoice.AccountChoiceActivity;
import com.crageeApp.appbesocial.Home.HomeActivity;
import com.crageeApp.appbesocial.Login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WelcomeActivity extends AppCompatActivity {
    private DatabaseReference userRef;
    private String currentUserID;
    public static final String TAG = "Constraints";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //initializing the Fire base object
        currentUserID= FirebaseAuth.getInstance().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        // handler class for the welcome activity

        Handler handlerWelcome = new Handler();
        handlerWelcome.postDelayed(new Runnable() {
            @Override
            public void run() {
               checkCurrentUser();
                /*Intent intentLoginActivity = new Intent(WelcomeActivity.this, LoginActivity.class);


                startActivity(intentLoginActivity);
                finish();

                 */
            }
        },2500);
        Toast.makeText(this, "Welcome activity", Toast.LENGTH_SHORT).show();
    }

    public void checkCurrentUser() {
        // [START check_current_user]
        if (currentUserID != null) {
            Log.i(TAG, "checkCurrentUser: current user exist"+currentUserID);
            Log.i(TAG, "checkCurrentUser: verifying the user existence");
            verifyUserExistence();
        } else {
            // No user is signed in
            Log.i(TAG, "checkCurrentUser: no user is signed in");
            Intent intentLogin =new Intent(WelcomeActivity.this, LoginActivity.class);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            startActivity(intentLogin);
            finish();
        }
        // [END check_current_user]
    }

    private void verifyUserExistence() {
        userRef
                .child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.child("Account Choice").exists()){
                                Log.i(TAG, "onDataChange: user has set the account choice");
                                Log.i(TAG, "onDataChange: checking for the name of personal account or page");
                                if ((dataSnapshot.child("name").exists()))
                                {
                                    Log.i(TAG, "onDataChange:current user  name exists ");
                                    // User is signed in and have name
                                    //send user to home
                                    sendUserToHome();
                                }
//                                else {
//                                    Log.i(TAG, "onDataChange: current user has not set their name");
//                                    sendUserToAccountInfoActivity();
//                                }
                            }
                            else {
                                Log.i(TAG, "onDataChange: user has not set the account choice");
                                sendUserToAccountChoice();
                            }
                        }
                        else {
                            Intent intentLogin =new Intent(WelcomeActivity.this, LoginActivity.class);
                            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                            startActivity(intentLogin);
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendUserToHome() {
        Intent intentHome = new Intent(WelcomeActivity.this, HomeActivity.class);
        intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        startActivity(intentHome);
        finish();
    }
    private void sendUserToAccountChoice() {
        Intent intentToAccountChoice = new Intent(WelcomeActivity.this, AccountChoiceActivity.class);
        intentToAccountChoice.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentToAccountChoice);
        finish();
    }
//    private void sendUserToAccountInfoActivity() {
//        Intent intentToProfile = new Intent(WelcomeActivity.this, AccountInformationActivity.class);
//        intentToProfile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intentToProfile);
//        finish();
//    }
}
