package com.crageeApp.appbesocial.Rewards;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

public class RewardsActivity extends AppCompatActivity {

    private Button refer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        refer=findViewById(R.id.refer);

        refer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();
                String link = "https://play.google.com/store/apps/details?id=com.crageeApp.appbesocial/";
                FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(link).buildUpon().appendQueryParameter("invitedby",uid).build())
                        .setDomainUriPrefix("https://crageeapp.page.link")
                        .setAndroidParameters(
                                new DynamicLink.AndroidParameters.Builder("com.crageeApp.appbesocial")
                                        .setMinimumVersion(125)
                                        .build())
                        .buildShortDynamicLink()
                        .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                            @Override
                            public void onSuccess(ShortDynamicLink shortDynamicLink) {
                                Uri mInvitationUrl = shortDynamicLink.getShortLink();
                                String referrerName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                                String subject = String.format("%s wants you to play MyExampleGame!", referrerName);
                                String invitationLink = mInvitationUrl.toString();
                                String msg = "Let's play MyExampleGame together! Use my referrer link: "
                                        + invitationLink;
                                String msgHtml = String.format("<p>Let's play MyExampleGame together! Use my "
                                        + "<a href=\"%s\">referrer link</a>!</p>", invitationLink);
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                                intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                                intent.putExtra(Intent.EXTRA_TEXT, msg);
                                intent.putExtra(Intent.EXTRA_HTML_TEXT, msgHtml);
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                            }

                        });

                 */

//                Intent intent=new Intent(Intent.ACTION_SEND);
//                intent.setType("text/plain");
//                intent.putExtra(Intent.EXTRA_TEXT,RewardsActivity.this.getResources().getString(R.string.share_app_link));
//                startActivity(intent.createChooser(intent,"Share and Earn"));
                createlink();
            }
        });
    }



    public void createlink() {
        Log.e("main", "create link ");

        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://play.google.com/store/apps/details?id=com.crageeApp.appbesocial"))
                .setDomainUriPrefix("https://crageeapp.page.link")
                // Open links with this app on Android
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .buildDynamicLink();

        //click -- link -- google play store -- installed/ or not  ----
        Uri dynamicLinkUri = dynamicLink.getUri();
        Log.e("main", "  Long refer " + dynamicLink.getUri());
        //   https://referearnpro.page.link?apn=blueappsoftware.referearnpro&link=https%3A%2F%2Fwww.blueappsoftware.com%2F
        // apn  ibi link

        // manual link
        String shareLinkText = "https://crageeapp.page.link/?" +
                "link=https://play.google.com/store/apps/details?id=com.crageeApp.appbesocial/" +
                "&apn=" + this.getPackageName() +
                "&st=" + "My Refer Link" +
                "&sd=" + "Reward Money 21";

        // shorten the link
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                //.setLongLink(dynamicLink.getUri())
                .setLongLink(Uri.parse(shareLinkText))  // manually
                .buildShortDynamicLink()
                .addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Uri flowchartLink = task.getResult().getPreviewLink();
                            Log.e("main ", "short link " + shortLink.toString());
                            // share app dialog
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                            intent.setType("text/plain");
                            startActivity(intent);
                        } else {
                            // Error
                            // ...
                            Log.e("main", " error " + task.getException());

                        }
                    }
                });


    }
}
