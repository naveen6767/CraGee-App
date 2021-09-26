package com.crageeApp.appbesocial.Payments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crageeApp.appbesocial.Home.HomeActivity;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class blueTickPaymentActivity extends AppCompatActivity {
    private Button send;
    private static final String TAG = "blueTickPaymentActivity";
    final int UPI_PAYMENT = 0;
    private DatabaseReference usersReference,mRootref;
    private String currentUserId;
    private TextView userAccountName;
    private CircleImageView userProfileImage;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_tick_payment);
        send = (Button) findViewById(R.id.send);
        userAccountName =(TextView) findViewById(R.id.payment_account_Name);
        userProfileImage =(CircleImageView) findViewById(R.id.payment_profile_image);
        progressBar = findViewById(R.id.pbPayment);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootref = FirebaseDatabase.getInstance().getReference();

        //now we will retrieve the user info in the profile activity
        retrieveAccountInfo();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payUsingUpi();
            }
        });
    }

    private void retrieveAccountInfo() {
        usersReference
                .child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.hasChild("name")){
                                String user_name=(String) dataSnapshot.child("name").getValue();
                                String user_image=(String) dataSnapshot.child("image").getValue();

                                userAccountName.setText(user_name);
                                try {
                                    Picasso.get().load(user_image).placeholder(R.drawable.profile_image).into(userProfileImage);

                                }catch (Exception e){
                                    Picasso.get().load(R.drawable.profile_image).into(userProfileImage);

                                }
                            }



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    void payUsingUpi() {
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", "8090597087@ybl")
                .appendQueryParameter("pn", "CraGee App")
                //.appendQueryParameter("mc", "")
                //.appendQueryParameter("tid", "02125412")
                //.appendQueryParameter("tr", "25584584")
                .appendQueryParameter("tn", "Verification Payment")
                .appendQueryParameter("am", "59")
                .appendQueryParameter("cu", "INR")
                //.appendQueryParameter("refUrl", "blueapp")
                .build();
        Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);
        // will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");
        // check if intent resolves
        if(null != chooser.resolveActivity(getPackageManager())) {
            startActivityForResult(chooser, UPI_PAYMENT);
        } else {
            Toast.makeText(blueTickPaymentActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("main ", "response "+resultCode );
        /*
       E/main: response -1
       E/UPI: onActivityResult: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPIPAY: upiPaymentDataOperation: txnId=AXI4a3428ee58654a938811812c72c0df45&responseCode=00&Status=SUCCESS&txnRef=922118921612
       E/UPI: payment successfull: 922118921612
         */
        switch (requestCode) {
            case UPI_PAYMENT:
                if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                    if (data != null) {
                        String trxt = data.getStringExtra("response");
                        Log.e("UPI", "onActivityResult: " + trxt);
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add(trxt);
                        upiPaymentDataOperation(dataList);
                    } else {
                        Log.e("UPI", "onActivityResult: " + "Return data is null");
                        ArrayList<String> dataList = new ArrayList<>();
                        dataList.add("nothing");
                        upiPaymentDataOperation(dataList);
                    }
                } else {
                    //when user simply back without payment
                    Log.e("UPI", "onActivityResult: " + "Return data is null");
                    ArrayList<String> dataList = new ArrayList<>();
                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }
                break;
        }
    }
    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(blueTickPaymentActivity.this)) {
            String str = data.get(0);
            Log.e("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.
                Toast.makeText(blueTickPaymentActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "payment successfull: "+approvalRefNo);

                /*
                 * if the transaction is successful
                 * make this user verified and give the verification badge to user
                 * change the celebrity category from norm
                 */
                progressBar.setVisibility(View.VISIBLE);
                HashMap<String, Object> userInfoMap = new HashMap<>();
                userInfoMap.put("celebrityCategory","Original");
                usersReference
                        .child(currentUserId)
                        .updateChildren(userInfoMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Query userPostsQuery=mRootref.child("Posts").orderByChild("uid").equalTo(currentUserId);
                        userPostsQuery.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                                    HashMap<String, Object> postsMap =new HashMap<>();
                                    //put post info
                                    postsMap.put("userVerified","Original");

                                    snapshot.getRef().updateChildren(postsMap);
                                }
                                progressBar.setVisibility(View.GONE);
                                //show dialog box with animation for successful payment
                                startActivity(new Intent(blueTickPaymentActivity.this, HomeActivity.class));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                });


            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(blueTickPaymentActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "Cancelled by user: "+approvalRefNo);
            }
            else {
                Toast.makeText(blueTickPaymentActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "failed payment: "+approvalRefNo);
            }
        } else {
            Log.e("UPI", "Internet issue: ");
            Toast.makeText(blueTickPaymentActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }


    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }
}