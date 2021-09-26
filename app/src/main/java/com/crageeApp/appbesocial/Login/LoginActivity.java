package com.crageeApp.appbesocial.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crageeApp.appbesocial.AccountChoice.AccountChoiceActivity;
import com.crageeApp.appbesocial.Home.HomeActivity;
import com.crageeApp.appbesocial.R;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 456;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            this.finish();
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
                            .setLogo(R.drawable.logoo)      // Set logo drawable
                            .setTheme(R.style.loginTheme)      // Set theme
                            .setIsSmartLockEnabled(true)
                            .build(),
                    RC_SIGN_IN);
        }
    }



    // [START auth_fui_result]
    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d(TAG, "onActivityResult: inside response");

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                //check whether it is a new user or already signed in user
                FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG, "onActivityResult: "+user.getEmail());
                if (user.getMetadata().getCreationTimestamp()==user.getMetadata().getLastSignInTimestamp()){
                    Toast.makeText(this, "Welcome New User", Toast.LENGTH_SHORT).show();
                    //current user is the new user
                    registerNewUser(user.getUid());
                }
                else {
                    //this is a returning user
                    Toast.makeText(this, "Welcome back again", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

                    this.finish();
                }


            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Toast.makeText(this, "error"+response.getError().getErrorCode(), Toast.LENGTH_SHORT).show();
                IdpResponse firebaseResponse = IdpResponse.fromResultIntent(data);

                if (firebaseResponse==null){
                    Log.d(TAG, "onActivityResult: the user has cancelled the sign in request");

                }
                else{
                    Log.e(TAG, "onActivityResult: ",firebaseResponse.getError() );
                }

            }
        }
    }

    private void registerNewUser(final String uid) {
        Log.d(TAG, "registerNewUser: registering the new user details");
        Log.d(TAG, "registerNewUser: the current user is "+uid);

        //get user email and user id
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String mobile = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
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


                        sendOfficialMessage(uid);
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

    private void sendOfficialMessage(final String uid) {
        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm a");
        String saveCurrentTime = currentTime.format(calForTime.getTime());
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        String saveCurrentDate = currentDate.format(calForDate.getTime());
        final String officialSenderId="iqd8EN29gaOeuubUWaVHPSLDlzl1";

        String message="Welcome to CraGee App";
        HashMap<String, Object> messagemap = new HashMap<>();
        messagemap.put("message", message);
        messagemap.put("seen", false);
        messagemap.put("type", "text");
        messagemap.put("time", ServerValue.TIMESTAMP);
        messagemap.put("from", officialSenderId);
        messagemap.put("receiver", uid);
        messagemap.put("messageDate", saveCurrentDate);
        messagemap.put("messageTime", saveCurrentTime);
        //compare both the ids

        final String chat_key;
        if (officialSenderId.compareTo(uid)>0)
        {
            chat_key = officialSenderId + uid;
        }
        else{
            chat_key = uid+officialSenderId;
        }
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages")
                .child(chat_key);
        messagesRef
                .push()
                .updateChildren(messagemap);
//////////////////////////////////////////////Adding user in Chat database////////////////////////////////////////////////////////

        final DatabaseReference mchatSeen = FirebaseDatabase.getInstance().getReference().child("chat");
        final DatabaseReference mRootref = FirebaseDatabase.getInstance().getReference();
        mchatSeen.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(officialSenderId)) {
                    mRootref.child("chat").child(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.hasChild(officialSenderId)) {

                                HashMap<String, Object> chataddmap = new HashMap<>();
                                chataddmap.put("seen", false);
                                chataddmap.put("timestamp", ServerValue.TIMESTAMP);
                                HashMap<String, Object> chatUserMap = new HashMap<>();
                                chatUserMap.put("chat/" + uid + "/" + officialSenderId, chataddmap);
                                chatUserMap.put("chat/" + officialSenderId + "/" + uid, chataddmap);
                                mRootref.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                        }
                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void sendUserToAccountChoice() {
        Intent intentToAccountChoice = new Intent(LoginActivity.this, AccountChoiceActivity.class);
        intentToAccountChoice.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentToAccountChoice);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        finish();

    }


}