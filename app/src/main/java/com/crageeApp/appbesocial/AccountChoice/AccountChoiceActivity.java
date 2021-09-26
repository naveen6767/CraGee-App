package com.crageeApp.appbesocial.AccountChoice;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.crageeApp.appbesocial.R;
import com.google.firebase.database.DatabaseReference;

public class AccountChoiceActivity extends AppCompatActivity {
        private ImageView personalAccount,publicPage;
        private DatabaseReference usersRef;
        private String currentUserId;
        private static final String TAG = "AccountChoiceActivity";
        private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_choice);
         navController = Navigation.findNavController(this,R.id.login_navigation_host_fragment);


//        personalAccount =findViewById(R.id.imageViewPersonal);
//        publicPage   =findViewById(R.id.imageViewPublic);
//        usersRef= FirebaseDatabase.getInstance().getReference("Users");
//        currentUserId= FirebaseAuth.getInstance().getUid();
//
//
//        personalAccount.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "onClick: publicPage selected ");
//                Log.i(TAG, "onClick: updating the database about the choice");
//                HashMap<String, Object> verificationMap = new HashMap<>();
//                verificationMap.put("accountChoice","Personal Account");
//                usersRef.child(currentUserId)
//                        .updateChildren(verificationMap)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//
//                                sendUserToAccountInfoActivity(v);
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.i(TAG, "onCancelled: failure in updating the user choice");
//                        Log.i(TAG, "onCancelled: "+e.getMessage());
//                    }
//                });
//            }
//        });
//
//        publicPage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "onClick: personal account selected ");
//                Log.i(TAG, "onClick: updating the database about the choice");
//                HashMap<String, Object> verificationMap = new HashMap<>();
//                verificationMap.put("accountChoice","Public Page");
//                usersRef.child(currentUserId)
//                        .updateChildren(verificationMap)
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                sendUserToAccountInfoActivity(v);
//
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.i(TAG, "onCancelled: failure in updating the user choice");
//                        Log.i(TAG, "onCancelled: "+e.getMessage());
//                    }
//                });
//            }
//        });
    }

//    private void sendUserToAccountInfoActivity(View v) {
//        Navigation.findNavController(v).navigate(R.id.action_accountChoiceFragment_to_accountInformationFragment);
//
//    }


}
