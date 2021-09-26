package com.crageeApp.appbesocial.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.crageeApp.appbesocial.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class InviteFriendsFragment extends Fragment {

    private View view;
    private Button createLink;
    private static final String TAG = "InviteFriendsFragment";

    public InviteFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view= inflater.inflate(R.layout.fragment_invite_friends, container, false);


         createLink=view.findViewById(R.id.create_Link);



         createLink.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent=new Intent(Intent.ACTION_SEND);
                 intent.setType("text/plain");
                 intent.putExtra(Intent.EXTRA_TEXT,view.getContext().getResources().getString(R.string.share_app_link));
                 startActivity(intent.createChooser(intent,"Share"+"CraGee App"+"via"));

             }
         });

        return view;
    }


}
