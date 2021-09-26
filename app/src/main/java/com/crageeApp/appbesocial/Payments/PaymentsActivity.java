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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crageeApp.appbesocial.Groups.GroupInformationActivity;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PaymentsActivity extends AppCompatActivity {
    private Button send;
    private static final String TAG = "PaymentsActivity";
    final int UPI_PAYMENT = 0;
    private DatabaseReference usersReference,groupsRef;
    private String currentUserId;
    private TextView userAccountName;
    private CircleImageView userProfileImage;
    private String group_name,uniqueGroupId,groupAdminId,groupImage,groupPrivacy,groupAbout,groupChatKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);
        send = (Button) findViewById(R.id.send);

        userAccountName =(TextView) findViewById(R.id.payment_account_Name);
        userProfileImage =(CircleImageView) findViewById(R.id.payment_profile_image);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupsRef= FirebaseDatabase.getInstance().getReference().child("Groups");
        Intent intentFromGroupInfo=getIntent();
        group_name=intentFromGroupInfo.getStringExtra("groupName");
        uniqueGroupId=intentFromGroupInfo.getStringExtra("uniqueGroupId");
        groupAdminId=intentFromGroupInfo.getStringExtra("groupAdmin");
        groupImage=intentFromGroupInfo.getStringExtra("groupImage");
        groupPrivacy=intentFromGroupInfo.getStringExtra("groupPrivacy");
        groupAbout=intentFromGroupInfo.getStringExtra("groupAbout");
        groupChatKey=intentFromGroupInfo.getStringExtra("uniqueGroupChatKey");
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
                .appendQueryParameter("tn", group_name+" "+"group payment")
                .appendQueryParameter("am", "25")
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
            Toast.makeText(PaymentsActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
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
        if (isConnectionAvailable(PaymentsActivity.this)) {
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
                Toast.makeText(PaymentsActivity.this, "Transaction successful.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "payment successfull: "+approvalRefNo);

                /*
                *if the transaction is successful
                * make this user as a member of the group
                 */
                groupsRef
                        .child(uniqueGroupId)
                        .child("Group Members")
                        .child(currentUserId)
                        .setValue(true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "onSuccess: current user has become the member of the OPEN group successfully");
                                sendUserToGroupInfo();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: failure in making the current user to the member of the open group"+e.getMessage());
                    }
                });

            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {
                Toast.makeText(PaymentsActivity.this, "Payment cancelled by user.", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "Cancelled by user: "+approvalRefNo);
            }
            else {
                Toast.makeText(PaymentsActivity.this, "Transaction failed.Please try again", Toast.LENGTH_SHORT).show();
                Log.e("UPI", "failed payment: "+approvalRefNo);
            }
        } else {
            Log.e("UPI", "Internet issue: ");
            Toast.makeText(PaymentsActivity.this, "Internet connection is not available. Please check and try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendUserToGroupInfo() {
        Intent intentToGroupInfo = new Intent(PaymentsActivity.this, GroupInformationActivity.class);
        intentToGroupInfo.putExtra("groupName",group_name);
        intentToGroupInfo.putExtra("groupId",uniqueGroupId);
        intentToGroupInfo.putExtra("groupAdmin",groupAdminId);
        intentToGroupInfo.putExtra("groupImage",groupImage);
        intentToGroupInfo.putExtra("groupPrivacy",groupPrivacy);
        intentToGroupInfo.putExtra("groupAbout",groupAbout);
        intentToGroupInfo.putExtra("groupChatKey",groupChatKey);
        startActivity(intentToGroupInfo);
        finish();
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