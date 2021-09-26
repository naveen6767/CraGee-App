package com.crageeApp.appbesocial.Settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.crageeApp.appbesocial.ProgressButton;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class WriteToUsFragment extends Fragment {

    private View writeToUsFragmentView,postSuggestions;
    private EditText suggestionsEdit;
    private ProgressButton progressButton;
    private DatabaseReference mRootRef;
    private String currentUserId;

    public WriteToUsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        writeToUsFragmentView = inflater.inflate(R.layout.fragment_write_to_us, container, false);
        suggestionsEdit=writeToUsFragmentView.findViewById(R.id.suggestions);
        postSuggestions=writeToUsFragmentView.findViewById(R.id.buttonPostSuggestions);
        currentUserId= FirebaseAuth.getInstance().getUid();
        mRootRef= FirebaseDatabase.getInstance().getReference();
        progressButton=new ProgressButton(getContext(),postSuggestions);
        progressButton.buttonPostSuggestions();

        postSuggestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToPost =suggestionsEdit.getText().toString().trim();

                if (!TextUtils.isEmpty(textToPost))
                {
                    uploadData(textToPost);
                }
                else {
                    Toast.makeText(getContext(), "Please write something", Toast.LENGTH_SHORT).show();
                }

            }
        });


        return writeToUsFragmentView;

    }

    private void uploadData(final String textToPost) {

        progressButton.buttonActivated();
        mRootRef
                .child("Users")
                .child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String profileUserName =(String) dataSnapshot.child("name").getValue();
                    String requestProfileImage =(String) dataSnapshot.child("image").getValue();
                    String userVerified =(String) dataSnapshot.child("celebrityCategory").getValue();
                    //post without image
                    //url is received
                    //upload post to fire base database
                    String pushId=mRootRef.child("Suggestions").push().getKey();
                    HashMap<String, Object> suggestionMap =new HashMap<>();
                    //put post info
                    suggestionMap.put("uid",currentUserId);
                    suggestionMap.put("userVerified",userVerified);
                    suggestionMap.put("userName",profileUserName);
                    suggestionMap.put("userDp",requestProfileImage);
                    suggestionMap.put("textPost",textToPost);
                    suggestionMap.put("sTime", ServerValue.TIMESTAMP);
                    suggestionMap.put("sId",pushId);
                    suggestionMap.put("sStatus","Pending");
                    sendSuggestions(suggestionMap,pushId);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendSuggestions(HashMap<String, Object> suggestionMap, String pushId) {

        mRootRef
                .child("Suggestions")
                .child(pushId)
                .updateChildren(suggestionMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Thank you for your suggestions", Toast.LENGTH_SHORT).show();
                        suggestionsEdit.setText(null);
                        progressButton.buttonPostSaved();
                        progressButton.buttonPostSuggestions();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }


}
