package com.crageeApp.appbesocial;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crageeApp.appbesocial.AccountChoice.AccountChoiceActivity;
import com.crageeApp.appbesocial.Home.HomeActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class SplashScreen extends AppCompatActivity {
    private DatabaseReference userRef;
    private String currentUserID;
    private static final String TAG = "SplashScreen";
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         //startActivity(new Intent(this, HomeActivity.class));

        //initializing the Fire base object
        currentUserID = FirebaseAuth.getInstance().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.Users));


        splashScreenThread splashScreenThread=new splashScreenThread();
        splashScreenThread.execute();
    }
    public void checkCurrentUser() {
        // [START check_current_user]
        if (currentUserID != null) {

            //save uid of currently signed in user in shared preferences
            SharedPreferences sp =getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID",currentUserID);
            editor.apply();
            verifyUserExistence();

        } else {
            // No user is signed in
            Log.d(TAG, "checkCurrentUser: no user is signed in");
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.logo_transparent)      // Set logo drawable
                            .setTheme(R.style.loginTheme)      // Set theme
                            .build(),
                    RC_SIGN_IN);
        }
        // [END check_current_user]
    }

    private void verifyUserExistence() {

        userRef
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(currentUserID)){
                            Log.d(TAG, "onDataChange: the value of data snapshot is "+dataSnapshot.getKey());

                            if (dataSnapshot.child(currentUserID).child("Account Choice").exists()){
                                Log.d(TAG, "onDataChange: user has set the account choice");
                                Log.d(TAG, "onDataChange: checking for the name of personal account or page");
                                if ((dataSnapshot.child(currentUserID).child("name").exists()))
                                {
                                    Log.d(TAG, "onDataChange:current user  name exists ");
                                    // User is signed in and have name
                                    //send user to home
                                    sendUserToHome();
                                }
//                                else {
//                                    Log.d(TAG, "onDataChange: current user has not set their name");
//                                    sendUserToAccountInfoActivity();
//                                }
                            }
                            else {
                                Log.d(TAG, "onDataChange: user has not set the account choice");
                                sendUserToAccountChoice();
                            }
                        }
                        else {
                            // Choose authentication providers
                            List<AuthUI.IdpConfig> providers = Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.PhoneBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build());
                            // Create and launch sign-in intent
                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setAvailableProviders(providers)
                                            .setLogo(R.drawable.logo_transparent)      // Set logo drawable
                                            .setTheme(R.style.loginTheme)      // Set theme
                                            .build(),
                                    RC_SIGN_IN);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void sendUserToHome() {
        Intent intentHome = new Intent(SplashScreen.this, HomeActivity.class);
        intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        startActivity(intentHome);
        finish();
    }

    private void sendUserToAccountChoice() {
        Intent intentToAccountChoice = new Intent(SplashScreen.this, AccountChoiceActivity.class);
        intentToAccountChoice.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentToAccountChoice);
        finish();
    }

//    private void sendUserToAccountInfoActivity() {
//        Intent intentToProfile = new Intent(this, AccountInformationActivity.class);
//        intentToProfile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intentToProfile);
//        finish();
//    }


    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d(TAG, "onActivityResult: inside response");

            if (resultCode == RESULT_OK) {
                // Successfully signed in

                /*
                 *after successful login first check the current user is login for the first time
                 * if first time,
                 * Check the user choice send user to account choice
                 * after the account choice
                 * send user to the basic profile information activity
                 * else  send user to home activity
                 */
                final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onDataChange: data snapshot "+dataSnapshot);
                            if (dataSnapshot.hasChild(user.getUid())){
                                //current user id found in the database
                                //check for the account choice
                                if (dataSnapshot.child(user.getUid()).hasChild("Account Choice")){
                                    Log.d(TAG, "onDataChange: current user has account choice");
                                    if (dataSnapshot.child(user.getUid()).hasChild("name")){
                                        //current user is already using the app
                                        sendUserToHomeActivity();

                                    }
//                                    else {
//                                        //current  user has just made the user id
//                                        sendUserToAccountInformationActivity();
//                                    }

                                }
                                else {
                                    Log.d(TAG, "onDataChange: current user has not set account choice");
                                    sendUserToAccountChoice();
                                }

                            }
                            else {
                                Log.d(TAG, "onDataChange: user is logging in for the first time");
                                Log.d(TAG, "onDataChange: registering the login details");
                                registerNewUser();
                            }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Toast.makeText(this, "error"+response.getError().getErrorCode(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void registerNewUser() {
        Log.d(TAG, "registerNewUser: registering the new user details");

        //get user email and user id
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        final String uid =FirebaseAuth.getInstance().getCurrentUser().getUid();
        /*
         user has made an account
         now saving the login info
         */
        HashMap<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("uid",uid);
        userInfoMap.put("email",email);
        userInfoMap.put("onlineStatus","online");
        userInfoMap.put("typingTo","noOne");
        userInfoMap.put("noPosts","0");
        userInfoMap.put("Earnings","0");
        userInfoMap.put("Credits","100");
        userInfoMap.put("ratings","1");
        //put data with hash map in the database
        userRef.child(uid)
                .setValue(userInfoMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //send user to update the account choice
                        sendUserToAccountChoice();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: error in updating the account details");

            }
        });
    }

    private void sendUserToHomeActivity()
    {
        Intent intentToHome =new Intent(this, HomeActivity.class);
        startActivity(intentToHome);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        finish();
    }

//    private void sendUserToAccountInformationActivity()
//    {
//        Intent intentToBasicProfile= new Intent(this, AccountInformationActivity.class);
//        startActivity(intentToBasicProfile);
//        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
//        finish();
//    }

    //creating a background thread
    public class splashScreenThread extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: ");

        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: ");
            checkCurrentUser();

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
