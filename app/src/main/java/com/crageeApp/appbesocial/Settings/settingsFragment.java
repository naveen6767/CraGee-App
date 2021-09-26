package com.crageeApp.appbesocial.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;

import com.crageeApp.appbesocial.Login.LoginActivity;
import com.crageeApp.appbesocial.MyApplication;
import com.crageeApp.appbesocial.Payments.blueTickPaymentActivity;
import com.crageeApp.appbesocial.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class settingsFragment extends Fragment implements View.OnClickListener {
    private View settingsFragmentView;
    private Toolbar settingsToolbar;
    public static FragmentManager fragmentManager;
    private DatabaseReference userRef;
    private static final String TAG = "settingsFragment";
    private FirebaseAuth userAuth;
    private LinearLayout settingsLayout;
    private TextView aboutApp,rateApp,inviteFriends,writeToUs,termsOfService,dataPolicy,communityStandards,logoutCurrentUser,accountPrivacyText,
            accountType,accountEmail;
    private  String currentUserId;
    private LinearLayout blueTickLayout;
    public settingsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        settingsFragmentView= inflater.inflate(R.layout.fragment_settings, container, false);

        //init the fire base here
        aboutApp= settingsFragmentView.findViewById(R.id.tVAboutApp);
        rateApp= settingsFragmentView.findViewById(R.id.tVRate);
        inviteFriends= settingsFragmentView.findViewById(R.id.tVInvite);
        writeToUs= settingsFragmentView.findViewById(R.id.tVWrite);
        termsOfService= settingsFragmentView.findViewById(R.id.tVTerms);
        dataPolicy= settingsFragmentView.findViewById(R.id.tVDataPolicy);
        communityStandards= settingsFragmentView.findViewById(R.id.tVCommunityStd);
        logoutCurrentUser= settingsFragmentView.findViewById(R.id.logoutUser);
        accountPrivacyText= settingsFragmentView.findViewById(R.id.account_privacy);
        accountType= settingsFragmentView.findViewById(R.id.account_type);
        settingsLayout= settingsFragmentView.findViewById(R.id.layout_settings);
        accountEmail= settingsFragmentView.findViewById(R.id.account_email);
        blueTickLayout= settingsFragmentView.findViewById(R.id.blueTickLayout);


        //init the fire base here
        userAuth= FirebaseAuth.getInstance();
        userRef= FirebaseDatabase.getInstance().getReference("Users");
        currentUserId=FirebaseAuth.getInstance().getUid();
        //init the progress dialog

        aboutApp.setOnClickListener(this);
        rateApp.setOnClickListener(this);
        inviteFriends.setOnClickListener(this);
        writeToUs.setOnClickListener(this);
        termsOfService.setOnClickListener(this);
        dataPolicy.setOnClickListener(this);
        communityStandards.setOnClickListener(this);
        logoutCurrentUser.setOnClickListener(this);
        accountPrivacyText.setOnClickListener(this);
        blueTickLayout.setOnClickListener(this);


        return settingsFragmentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tVAboutApp:
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_aboutFragment);
                break;
            case R.id.tVRate:
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_rateFragment);
                break;
            case R.id.tVInvite:
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_inviteFriendsFragment);
                break;
            case R.id.tVTerms:
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_termsOfServiceFragment);
                break;
            case R.id.tVWrite:
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_writeToUsFragment);
                break;
            case R.id.tVDataPolicy:
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_privacyPolicyFragment);
                break;
            case R.id.logoutUser:

                signOutUser();
                break;
            case R.id.tVCommunityStd:
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_communityFragment);
                break;
            case R.id.account_privacy:
                Navigation.findNavController(v).navigate(R.id.action_settingsFragment_to_privacyFragment);
                break;
            case R.id.blueTickLayout:
                startActivity(new Intent(getContext(), blueTickPaymentActivity.class));
                break;
            default:
                break;
        }


    }

    private void signOutUser() {
        MyApplication.getInstance().clearApplicationData();
        AuthUI.getInstance().signOut(getContext()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    sendUserToLoginActivity();
                }
                else {
                    Log.e(TAG, "onComplete: ",task.getException() );
                }
            }
        });


    }
    private void sendUserToLoginActivity() {
        Intent intentToLogin = new Intent(getContext(), LoginActivity.class);
        startActivity(intentToLogin);
        getActivity().overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        getActivity().finish();
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
                            String accountChoice=(String) dataSnapshot.child("accountChoice").getValue();
                            String email=(String) dataSnapshot.child("email").getValue();

                            accountPrivacyText.setText(accountPrivacy);
                            accountPrivacyText.setTextColor(settingsFragmentView.getContext().getResources().getColor(R.color.blue));
                            accountType.setText(accountChoice);
                            accountType.setTextColor(settingsFragmentView.getContext().getResources().getColor(R.color.red));
                            if (email != null && !email.isEmpty()) {
                                accountEmail.setText(email);
                                accountEmail.setTextColor(settingsFragmentView.getContext().getResources().getColor(R.color.blue));
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
