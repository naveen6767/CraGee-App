package com.crageeApp.appbesocial;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ProgressButton {

    private CardView cardView;
    private ConstraintLayout layout;
    private TextView textView;
    private ProgressBar progressBar;
    Animation fade_in;



//constructor
public ProgressButton(Context context, View view){

        cardView=view.findViewById(R.id.card_view);
        layout=view.findViewById(R.id.constraint_layout);
        textView=view.findViewById(R.id.button_text);
        progressBar=view.findViewById(R.id.progressBar);

    }
      public void buttonActivated(){
        progressBar.setVisibility(View.VISIBLE);
        textView.setText("Please wait...");
    }

    public void buttonFinished(){
        textView.setText("Saved");
        layout.setBackgroundColor(cardView.getResources().getColor(R.color.blue));
        progressBar.setVisibility(View.GONE);
    }
    public void closeButton(){
        progressBar.setVisibility(View.GONE);
    }
    public void buttonPostSaved(){
        textView.setText("Completed");
        progressBar.setVisibility(View.GONE);
    }

    public void buttonFollow(){
    textView.setText("Follow");
    textView.setEnabled(true);
    progressBar.setVisibility(View.GONE);
    }
    public void buttonFollowing(){
    textView.setText("Following");
        progressBar.setVisibility(View.GONE);
    }
    public void buttonUnfollow(){
    textView.setText("Unfollow");
    textView.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    public void buttonSendMessage(){
        textView.setText("Send Message");
    }
    public void buttonRequested(){
        textView.setText("Requested");
        textView.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }
    public void buttonUnblock(){
        textView.setText("Unblock");
        textView.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }
    public void buttonFollowBack(){
        textView.setText("Follow back");
        textView.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }
    public void buttonCompleteProfile(){
        textView.setText("Complete Profile");

    }
    public void buttonCreatePage(){
        textView.setText("Create Page");

    }
    public void buttonPost(){
        textView.setText("POST");


    }
    public void buttonWatchVideos(){
        textView.setText("Watch videos to earn Credits");
        progressBar.setVisibility(View.GONE);

    }
    public void buttonCreateGroup(){
        textView.setText("Create Group");
        progressBar.setVisibility(View.GONE);

    }
    public void buttonJoinGroup(){
        textView.setText("Join Group");
        progressBar.setVisibility(View.GONE);

    } public void buttonCancelRequest(){
        textView.setText("Cancel Request");
        progressBar.setVisibility(View.GONE);

    }
    public void buttonAllMembers(){
        textView.setText("All Members");
        progressBar.setVisibility(View.GONE);

    }
    public void buttonUpdateRules(){
        textView.setText("Update Rules");
        progressBar.setVisibility(View.GONE);

    }
    public void buttonPostSuggestions(){
        textView.setText("Post Suggestions");
        progressBar.setVisibility(View.GONE);


    }
}
