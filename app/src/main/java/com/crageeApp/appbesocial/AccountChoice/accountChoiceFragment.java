package com.crageeApp.appbesocial.AccountChoice;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class accountChoiceFragment extends Fragment {

    private ImageView personalAccount,publicPage;
    private DatabaseReference usersRef;
    private String currentUserId;
    private View accountChoiceFragmentView;
    private static final String TAG = "accountChoiceFragment";


    public accountChoiceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        accountChoiceFragmentView= inflater.inflate(R.layout.fragment_account_choice, container, false);


        personalAccount =accountChoiceFragmentView.findViewById(R.id.imageViewPersonal);
        publicPage   =accountChoiceFragmentView.findViewById(R.id.imageViewPublic);
        usersRef= FirebaseDatabase.getInstance().getReference("Users");
        currentUserId= FirebaseAuth.getInstance().getUid();
        personalAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: publicPage selected ");
                Log.i(TAG, "onClick: updating the database about the choice");
                HashMap<String, Object> verificationMap = new HashMap<>();
                verificationMap.put("accountChoice","Personal Account");
                usersRef
                        .child(currentUserId)
                        .updateChildren(verificationMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Bundle bundle=new Bundle();
                                bundle.putString("accountChoice","Personal Account");
                                Navigation.findNavController(v).navigate(R.id.action_accountChoiceFragment_to_accountInformationFragment,bundle);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onCancelled: failure in updating the user choice");
                        Log.i(TAG, "onCancelled: "+e.getMessage());
                    }
                });
            }
        });

        publicPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: personal account selected ");
                Log.i(TAG, "onClick: updating the database about the choice");
                HashMap<String, Object> verificationMap = new HashMap<>();
                verificationMap.put("accountChoice","Public Page");
                usersRef
                        .child(currentUserId)
                        .updateChildren(verificationMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Bundle bundle=new Bundle();
                                bundle.putString("accountChoice","Public Page");
                                Navigation.findNavController(v)
                                        .navigate(R.id.action_accountChoiceFragment_to_accountInformationFragment,bundle);



                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onCancelled: failure in updating the user choice");
                        Log.i(TAG, "onCancelled: "+e.getMessage());
                    }
                });
            }
        });
        return accountChoiceFragmentView;
    }


}