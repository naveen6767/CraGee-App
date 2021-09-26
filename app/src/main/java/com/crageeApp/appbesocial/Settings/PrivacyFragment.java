package com.crageeApp.appbesocial.Settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrivacyFragment extends Fragment {

    private DatabaseReference userRef;

    private Switch switchPrivacy;
    private String currentUserId;

    private static final String TAG = "PrivacyFragment";


    public PrivacyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View privacyFragmentView= inflater.inflate(R.layout.fragment_privacy, container, false);
        switchPrivacy=privacyFragmentView.findViewById(R.id.privacySwitch);
        currentUserId=FirebaseAuth.getInstance().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        switchPrivacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Log.d(TAG, "onCheckedChanged: the switch is enabled");
                    Log.d(TAG, "onCheckedChanged: user wants to make his account private");

                    userRef
                            .child( currentUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        HashMap<String,Object> privacyMap =new HashMap<>();
                                        privacyMap.put("accountPrivacy","Private");

                                        dataSnapshot.getRef().updateChildren(privacyMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getContext(), "Your account privacy has been updated to Private", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                    
                    
                } else {
                    // The toggle is disabled

                    userRef
                            .child( currentUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        HashMap<String,Object> privacyMap =new HashMap<>();
                                        privacyMap.put("accountPrivacy","Public");

                                        dataSnapshot.getRef().updateChildren(privacyMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(privacyFragmentView.getContext(), "Your account privacy has been updated to Public", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }
            }
        });

        return privacyFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //first retrieve the account privacy
        userRef
                .child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()&&dataSnapshot.hasChild("accountPrivacy")){
                            String accountPrivacy=(String) dataSnapshot.child("accountPrivacy").getValue();

                            if (accountPrivacy != null) {
                                if (accountPrivacy.equals("Public")){
                                    Log.d(TAG, "onDataChange: account privacy is public");
                                    switchPrivacy.setChecked(false);

                                }
                                else {
                                    Log.d(TAG, "onDataChange: account privacy is private");
                                    switchPrivacy.setChecked(true);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
