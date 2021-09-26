package com.crageeApp.appbesocial.Verification;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.crageeApp.appbesocial.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class VerificationActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener  {

    private TextView activityText,textListen,verifyStatus,verifyTextDisplay,listenTimer,verifyCreditStatus,aboutVerifyStatus,VerifiableText;
    private ImageButton record_button,play_button,listen_audio;
    private boolean isRecording=false;
    private String recordPermission= Manifest.permission.RECORD_AUDIO;
    private int PERMISSION_CODE=21;
    private String recordFile,recordPath,downloadUrl;
    private DatabaseReference verificationRef,verificationItemRef;
    private StorageReference verificationStorage;
    private Spinner languageSpinner;
    private Button verificationApply,watchVideoReward;
    private LinearLayout audioPlayLayout,readTextLayout,layoutVerify;
    private MediaPlayer mediaPlayer,mediaPlayerListener;
    private FrameLayout adContainerView;
    private CountDownTimer recordTimeCounter,playTimeCounter,voiceListenCounter;
    private static final String TAG = "VerificationActivity";
    private MediaRecorder recorder;
    private String currentUserId,verificationAudio,visitedUserId,verifyText;
    private int userCredits;
    private Uri uri;
    private AdView adView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        activityText=findViewById(R.id.currentActivityText);
        record_button=findViewById(R.id.recorder_mic);
        languageSpinner=findViewById(R.id.spinnerLanguages);
        textListen=findViewById(R.id.listen_text);
        play_button=findViewById(R.id.audio_play_mic);
        verificationApply=findViewById(R.id.apply_verification);
        audioPlayLayout=findViewById(R.id.audio_play_layout);
        readTextLayout=findViewById(R.id.textReadLayout);
        adContainerView=findViewById(R.id.verificationAdContainer);
        listen_audio=findViewById(R.id.audio_play_listen);
        layoutVerify=findViewById(R.id.verifyLayout);
        verifyStatus=findViewById(R.id.verificationStatus);
        verifyTextDisplay=findViewById(R.id.verifyTextDisplay);
        listenTimer=findViewById(R.id.listener_counter);
        verifyCreditStatus=findViewById(R.id.creditsStatusPrompt);
        watchVideoReward=findViewById(R.id.watch_videos_verify);
        aboutVerifyStatus=findViewById(R.id.textAboutVerification);
        VerifiableText=findViewById(R.id.verification_text_Read);

        currentUserId= FirebaseAuth.getInstance().getUid();
        verificationRef= FirebaseDatabase.getInstance().getReference("Users");
        verificationItemRef= FirebaseDatabase.getInstance().getReference("Verification Items");
        verificationStorage= FirebaseStorage.getInstance().getReference();
        //getting the intent passed by the account profile and profile fragment
        Intent intentFromPrevious = getIntent();
        visitedUserId=intentFromPrevious.getStringExtra("userId");
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.Verification_ad_unit));
        adContainerView.addView(adView);
//        loadBanner();
        mediaPlayer=new MediaPlayer();
        mediaPlayerListener=new MediaPlayer();
        record_button.setOnClickListener(this);
        play_button.setOnClickListener(this);
        listen_audio.setOnClickListener(this);
        verificationApply.setOnClickListener(this);
        watchVideoReward.setOnClickListener(this);

        setVerificationLayout();
        retrieveCurrentUserCredits();

        languageSpinner.setOnItemSelectedListener(this);
        recordTimeCounter=new CountDownTimer(20000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                activityText.setText("seconds remaining: " + millisUntilFinished / 1000);
                Log.i(TAG, "onTick: seconds remaining after:"+ millisUntilFinished / 1000);
            }
            @Override
            public void onFinish() {
                Log.i(TAG, "onFinish: timer stopped");
                stopRecording();
                isRecording=false;
                Log.i(TAG, "onFinish: setting the text to finish");
                activityText.setText("Please wait");
                uploadAudio();

            }
        };
        playTimeCounter=new CountDownTimer(20000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textListen.setText("seconds remaining: " + millisUntilFinished / 1000);
                Log.i(TAG, "onTick: seconds remaining after:"+ millisUntilFinished / 1000);
            }
            @Override
            public void onFinish() {
                Log.i(TAG, "onFinish: playTimeCounter stopped");
                mediaPlayer.stop();
                mediaPlayer.reset();
                Log.i(TAG, "onFinish: setting the text to finish");
                textListen.setText("done!");
            }
        };
        voiceListenCounter=new CountDownTimer(20000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                listenTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
                Log.i(TAG, "onTick: seconds remaining after:"+ millisUntilFinished / 1000);
            }
            @Override
            public void onFinish() {
                Log.i(TAG, "onFinish: voiceListenCounter stopped");
                mediaPlayer.stop();
                mediaPlayer.reset();
                Log.i(TAG, "onFinish: setting the text to finish");
                listenTimer.setText("done!");
            }
        };
    }

    private void retrieveVerificationText(final String selectedSpinnerChoice) {

        verificationItemRef
                .child(selectedSpinnerChoice)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Log.d(TAG, "onDataChange: the value of spinner is "+ selectedSpinnerChoice);
                            Log.d(TAG, "onDataChange: the value of the verification data snapshot is"+dataSnapshot);
                            int noOfTexts= (int) dataSnapshot.getChildrenCount();
                            Log.d(TAG, "onDataChange: the no of children"+noOfTexts);

                            int min=1;

                            final int random = new Random().nextInt((noOfTexts - min) + 1) + min;
                            Log.d(TAG, "onDataChange: the value of random is "+random);

                            String verificationNode="Verify Text"+" "+random;
                            Log.d(TAG, "onDataChange: verificationNode"+verificationNode);
                            if (dataSnapshot.hasChild(verificationNode)){
                                Log.d(TAG, "onDataChange: "+dataSnapshot.hasChild(verificationNode));
                                verifyText=(String) dataSnapshot.child(verificationNode).getValue();
                                Log.d(TAG, "onDataChange: the value of verify text is "+verifyText);

                                VerifiableText.setText(verifyText);

                            }
                            else {
                                Log.d(TAG, "onDataChange: error in setting the verify text");
                            }
                        }
                        else {
                            Toast.makeText(VerificationActivity.this, "no verification data", Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



    }


    private void setVerificationLayout() {
        verificationRef
                .child(visitedUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("Verification").child("verification status").exists()){
                            Log.i(TAG, "onDataChange: verification has been applied ");
                            String status = (String) dataSnapshot.child("Verification").child("verification status").getValue();
                            String verificationText = (String) dataSnapshot.child("Verification").child("text").getValue();
                            verificationAudio=(String) dataSnapshot.child("Verification").child("audio").getValue();
                            Log.i(TAG, "onDataChange: the verification audio is "+verificationAudio);
                            switch (status){
                                case "Pending":
                                    readTextLayout.setVisibility(View.GONE);
                                    audioPlayLayout.setVisibility(View.GONE);
                                    layoutVerify.setVisibility(View.VISIBLE);
                                    verifyStatus.setText(getResources().getString(R.string.verification_pending));
                                    aboutVerifyStatus.setText(getResources().getString(R.string.about_pending_verification));
                                    break;
                                case "Verified":
                                    readTextLayout.setVisibility(View.GONE);
                                    audioPlayLayout.setVisibility(View.GONE);
                                    layoutVerify.setVisibility(View.VISIBLE);
                                    verifyStatus.setText(getResources().getString(R.string.verification_verified));
                                    aboutVerifyStatus.setText(getResources().getString(R.string.about_verified_verification));
                                    break;
                                case "Rejected":
                                    readTextLayout.setVisibility(View.GONE);
                                    audioPlayLayout.setVisibility(View.GONE);
                                    layoutVerify.setVisibility(View.VISIBLE);
                                    verifyStatus.setText(getResources().getString(R.string.verification_rejected));
                                    aboutVerifyStatus.setText(getResources().getString(R.string.about_rejected_verification));
                                    break;
                                default:
                                    break;
                            }

                            //setting the text displayed
                            verifyTextDisplay.setText(verificationText);
                            //get the user credits of the current user


                        }else {
                            Log.i(TAG, "onDataChange: not applied for verification");
                            readTextLayout.setVisibility(View.VISIBLE);
                            //  audioPlayLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void retrieveCurrentUserCredits() {
        verificationRef
                .child(currentUserId)
                .child("Credits")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                userCredits= Integer.valueOf(String.valueOf(Objects.requireNonNull(dataSnapshot.getValue())));
                            }
                            Log.i(TAG, "onDataChange: current user has credits"+userCredits);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void uploadAudio() {
        StorageReference filePath=verificationStorage
                .child("Audio")
                .child(FirebaseAuth.getInstance().getUid())
                .child("verification_audio.3gp");
        uri=Uri.fromFile(new File(recordPath+"/"+recordFile));
        filePath
                .putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i(TAG, "onSuccess: successfully taken the audio to storage");

                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        downloadUrl= uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            activityText.setText("Recording Completed");
                            audioPlayLayout.setVisibility(View.VISIBLE);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: failure in uploading the file to storage");

            }
        });


    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.recorder_mic:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    audioPlayLayout.setVisibility(View.GONE);
                }
                else {
                    if (isRecording){
                        //stop recording
                        Log.i(TAG, "onClick: in the stop recording section"+isRecording);
                        stopRecording();
                        record_button.setImageResource(R.drawable.icon_mic_off);
                        isRecording=false;
                    }
                    else {
                        //start recording
                        if (checkPermission()) {
                            Log.i(TAG, "onClick: permission to use mic is given by user");
                            startRecording();
                            record_button.setImageResource(R.drawable.icon_mic_on);
                            isRecording = true;
                        }
                    }
                }
                break;
            case R.id.audio_play_mic:
                //play the recorded voice
                Log.i(TAG, "onClick:audio play button clicked");
                //if the media player is already running
                //stop it otherwise play recording
                if (mediaPlayer.isPlaying()){
                    Log.i(TAG, "onClick: media player is already playing");
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    playTimeCounter.cancel();
                }
                else {
                    Log.i(TAG, "onClick: media player is not running ");
                    playRecording();
                }
                break;
            case R.id.apply_verification:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    playTimeCounter.cancel();
                    completeVerification();
                }
                else {
                    completeVerification();
                }
                break;
            case R.id.audio_play_listen:
                if (mediaPlayerListener.isPlaying()){
                    Log.i(TAG, "onClick: media player listener is already playing");

                    mediaPlayerListener.stop();
                    mediaPlayerListener.reset();
                    voiceListenCounter.cancel();
                }
                else {
                    //here check if the current user has user credits enough for listening the voice.
                    if (userCredits>=100){
                        Log.i(TAG, "onClick: current user has enough credits ");
                        listenVoiceRecording();
                    }
                    else {
                        Log.i(TAG, "onClick: current user has not enough credits");
                        //show the status to user
                        listen_audio.setEnabled(false);
                        verifyCreditStatus.setVisibility(View.VISIBLE);
                        verifyCreditStatus.setText(getResources().getString(R.string.prompted_text_credits));
                        verifyCreditStatus.setTextColor(ContextCompat.getColor(VerificationActivity.this, R.color.red));
                        watchVideoReward.setVisibility(View.VISIBLE);
                    }

                }
                break;
            default:
                break;
        }
    }

    private void listenVoiceRecording() {
        try {
            mediaPlayerListener.setDataSource(verificationAudio);
            mediaPlayerListener.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    voiceListenCounter.start();
                    mp.start();
                    Log.i(TAG, "onPrepared: audio player start");
                 /*   mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Toast.makeText(VerificationActivity.this, "the audio has been played", Toast.LENGTH_SHORT).show();
                            mediaPlayer.stop();
                            Log.i(TAG, "onCompletion: media player is stopped");
                            mediaPlayer.reset();
                            Log.i(TAG, "onCompletion: media player is reset ");
                        }
                    });

                  */
                }
            });
            mediaPlayerListener.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void completeVerification() {
        //first check about the recording and text
        HashMap<String, Object> verifyAudioMap = new HashMap<>();
        verifyAudioMap.put("audio", downloadUrl);
        verifyAudioMap.put("verification status", "Pending");
        verifyAudioMap.put("verification text", verifyText);
        verificationRef
                .child(currentUserId)
                .child("Verification")
                .updateChildren(verifyAudioMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: verification status is updated successfully");
                        audioPlayLayout.setVisibility(View.VISIBLE);
                        textListen.setText("Verification Applied Successfully");
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: problem in updating the verification status"+e.getMessage());
            }
        });
    }

    private void playRecording() {
        try {
            Log.i(TAG, "playRecording: inside the try "+downloadUrl);
            mediaPlayer.setDataSource(downloadUrl);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    playTimeCounter.start();
                    mp.start();
                    Log.i(TAG, "onPrepared: audio player start");
                 /*   mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Toast.makeText(VerificationActivity.this, "the audio has been played", Toast.LENGTH_SHORT).show();
                            mediaPlayer.stop();
                            Log.i(TAG, "onCompletion: media player is stopped");
                            mediaPlayer.reset();
                            Log.i(TAG, "onCompletion: media player is reset ");
                        }
                    });

                  */
                }
            });
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void startRecording() {

        recordTimeCounter.start();
        Log.i(TAG, "startRecording:recording started ");

        recordPath= VerificationActivity.this.getExternalFilesDir("").getAbsolutePath();
        recordFile="filename.3gp";
        recorder=new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordPath+"/"+recordFile);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start();
    }
    private void stopRecording() {
        recordTimeCounter.cancel();
        Log.i(TAG, "stopRecording:recording stopped ");
        recorder.stop();
        recorder.release();
        recorder=null;
        record_button.setImageResource(R.drawable.icon_mic_off);
    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(VerificationActivity.this, recordPermission)== PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else {
            ActivityCompat.requestPermissions(VerificationActivity.this, new String[]{recordPermission},PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String selectedSpinnerChoice = parent.getItemAtPosition(position).toString();
        retrieveVerificationText(selectedSpinnerChoice);
        Toast.makeText(this, ""+selectedSpinnerChoice, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {


    }
    private void loadBanner() {

        AdRequest adRequest =
                new AdRequest.Builder().build();

        AdSize adSize = getAdSize();

        adView.setAdSize(adSize);


        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
}
