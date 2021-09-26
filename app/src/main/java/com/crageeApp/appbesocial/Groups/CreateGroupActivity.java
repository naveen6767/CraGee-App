package com.crageeApp.appbesocial.Groups;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.crageeApp.appbesocial.ProgressButton;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private Toolbar createGroupToolBar;
    private EditText groupName,aboutGroup;
    private ImageView groupCoverImage;
    private DatabaseReference myDatabaseReference,userRef;
    private String currentUserId;
    private String setGroupName;
    private String setPrivacy;
    private String setGroupStatus;
    private View view;
    private Spinner spinnerGroup;
    private String setGroupCategory;
    private Uri imageUri;
    private String saveCurrentDate,saveCurrentTime,createdGroupId,createdGroupChatKey;
    private StorageReference groupCoverImageRef;
    private View btnCreateGroup;
    private ProgressButton progressButton;

    //for camera permission
    //permission constants
    private static final int CAMERA_REQUEST_CODE =100;
    private static final int STORAGE_REQUEST_CODE =200;
    //image pick constants
    private static final int IMAGE_PICK_CAMERA_CODE =300;
    private static final int IMAGE_PICK_GALLERY_CODE =400;

    //image picked will be saved in this URi
    private Uri image_rui=null;

    //permission array
    private String[] cameraPermissions;
    private String[] storagePermissions;
    private static final String TAG = "CreateGroupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        //initializing the  permission arrays
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        createGroupToolBar=findViewById(R.id.toolBar_create_group);
        //set tool bar as the action bar
        setSupportActionBar(createGroupToolBar);
        // add back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //initialize here
        btnCreateGroup =findViewById(R.id.create_group);
        groupName =findViewById(R.id.et_group_name);
        aboutGroup =findViewById(R.id.about_group_status);
        groupCoverImage =findViewById(R.id.groupCover);

        //for radio buttons
        view =new View(getApplicationContext());


        // initialize the fire base database
        myDatabaseReference = FirebaseDatabase.getInstance().getReference("Groups");
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        currentUserId = FirebaseAuth.getInstance().getUid();
        groupCoverImageRef = FirebaseStorage.getInstance().getReference().child("Group Images");

        //getting spinner value here
        getSpinnerValue();

        progressButton =new ProgressButton(CreateGroupActivity.this,btnCreateGroup);
        progressButton.buttonCreateGroup();
        //set on click listener for the group cover image
        groupCoverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //now show the image pick up dialog
                showImagePickDialog();
            }
        });
        //apply set on click listener for the button
        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGroup();


            }
        });

    }




    private void getSpinnerValue() {
        //initialize the spinner element
        spinnerGroup=findViewById(R.id.spinnerGroupCategories);
        //on click listener in spinner
        spinnerGroup.setOnItemSelectedListener(CreateGroupActivity.this);
    }


    //check for radio button regarding the privacy of the groups
    public void privacySelected(View view)
    {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_Public:
                if (checked)
                    setPrivacy="Public Group";
                break;
            case R.id.radio_Private:
                if (checked)
                    setPrivacy="Private Group";
                break;

            default:
                Log.i(TAG, "privacySelected: no privacy selected");
                break;
        }
    }

    private void createNewGroup() {
        setGroupName = groupName.getText().toString();
        setGroupStatus=aboutGroup.getText().toString();
        if (image_rui==null){
            Toast.makeText(this, "Please select the group cover photo", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(setGroupName))
        {
            groupName.setError("Please enter Group name");
        }
        else if (TextUtils.isEmpty(setPrivacy)){
            Toast.makeText(this, "Please select the privacy", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(setGroupStatus))
        {
            aboutGroup.setError("Please write about this group");
        }
        else {
            progressButton.buttonActivated();
            //create group and send the data to the fire base

            //creating a unique group id for that will be different for all the groups
            Calendar calForData =Calendar.getInstance();
            SimpleDateFormat currentDate =new SimpleDateFormat("dd-MM-yyyy");
            saveCurrentDate = currentDate.format(calForData.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime =new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calForTime.getTime());
            final String timeStamp =String.valueOf(System.currentTimeMillis());
            createdGroupId=currentUserId+saveCurrentDate+saveCurrentTime+timeStamp;
            createdGroupChatKey= setGroupName+createdGroupId;

            //first send the image to the storage and then the take the image url and send it to the database
            final StorageReference imageStorage =groupCoverImageRef
                                                                    .child(createdGroupId)
                                                                    .child(createdGroupId+".jpg");
            imageStorage.putFile(Uri.parse(String.valueOf(image_rui))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //image is uploaded to the fire base storage ,now get its url
                    Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    final String downloadUri= uriTask.getResult().toString();
                    if (uriTask.isSuccessful()){
                        HashMap<String,String> groupMap=new HashMap<>();
                        groupMap.put("name",setGroupName);
                        groupMap.put("category",setGroupCategory);
                        groupMap.put("privacy",setPrivacy);
                        groupMap.put("status",setGroupStatus);
                        groupMap.put("imageUrl", downloadUri);
                        groupMap.put("groupId", createdGroupId);
                        groupMap.put("groupCreator", currentUserId);
                        groupMap.put("groupChatKey", createdGroupChatKey);
                        groupMap.put("groupVerification","Pending");
                        groupMap.put("createdDate", saveCurrentDate);
                        groupMap.put("timeStamp", timeStamp);
                        groupMap.put("totalMessages", "0");
                        groupMap.put("joiningCredits", "10");
                        groupMap.put("Group Rating", "1");
                        myDatabaseReference
                                .child(createdGroupId)
                                .setValue(groupMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendGroupDetailsToCurrentUser(downloadUri,timeStamp);
                                        //add the group admin as the group member of the current group
                                        myDatabaseReference
                                                .child(createdGroupId)
                                                .child("Group Members")
                                                .child(currentUserId)
                                                .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i(TAG, "onSuccess: group admin added as group member");
                                                Log.i(TAG, "onSuccess: creating the bonus earning for creating the group");
                                                progressButton.buttonPostSaved();

                                                sendUserToGroupInformation(downloadUri);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "onFailure: failure in uploading to the database"+e.getMessage());
                                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                }
            });

        }
    }
    private void sendGroupDetailsToCurrentUser(String downloadUri, String timeStamp) {
        HashMap<String,String> userGroupMap=new HashMap<>();
        userGroupMap.put("name",setGroupName);
        userGroupMap.put("category",setGroupCategory);
        userGroupMap.put("privacy",setPrivacy);
        userGroupMap.put("status",setGroupStatus);
        userGroupMap.put("imageUrl",downloadUri);
        userGroupMap.put("groupId", createdGroupId);
        userGroupMap.put("groupCreator", currentUserId);
        userGroupMap.put("groupChatKey", createdGroupChatKey);
        userGroupMap.put("groupVerification","Pending");
        userGroupMap.put("createdDate", saveCurrentDate);
        userGroupMap.put("timeStamp", timeStamp);
        userGroupMap.put("totalMessages", "0");
        userGroupMap.put("joiningCredits", "10");
        userGroupMap.put("Group Rating", "1");
        userGroupMap.put("earning", "51");
        userRef
                .child(currentUserId)
                .child("Groups")
                .child(createdGroupId)
                .setValue(userGroupMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "onSuccess: data updated in the current user base");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: Your groups cant be updated"+e.getMessage());
                Toast.makeText(CreateGroupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void sendUserToGroupInformation(String downloadUri) {
        Intent intentToGroupInformation =new Intent(CreateGroupActivity.this, GroupInformationActivity.class);
        intentToGroupInformation.putExtra("group Category",setGroupCategory);
        intentToGroupInformation.putExtra("groupPrivacy",setPrivacy);
        intentToGroupInformation.putExtra("groupName",setGroupName);
        intentToGroupInformation.putExtra("groupAdmin",currentUserId);
        intentToGroupInformation.putExtra("groupImage",downloadUri);
        intentToGroupInformation.putExtra("groupAbout",setGroupStatus);
        intentToGroupInformation.putExtra("groupId",createdGroupId);
        intentToGroupInformation.putExtra("groupChatKey",createdGroupChatKey);
        //do not let the user go back to the sign up form after enter to the Basic profile activity
        // intentToGroupInformation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentToGroupInformation);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        finish();
    }

    private void showImagePickDialog() {
        //two options camera /gallery to be shown in the dialog
        String[] options ={"Camera","Gallery"};
        //dialog
        AlertDialog.Builder builder =new AlertDialog.Builder(CreateGroupActivity.this);
        builder.setTitle("Choose image from");
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //item click handle
                if (which==0)
                {
                    //camera clicked
                    //now we need to check the permission first
                    if (!checkCameraPermission())
                    {
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                if (which==1)
                {
                    //gallery clicked
                    if (!checkStoragePermission())
                    {
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }
    private void pickFromGallery() {
        //intent to pick image from the gallery
        Intent intent =new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        //intent to pick image from camera
        ContentValues cv =new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Desc");

        image_rui =CreateGroupActivity.this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        //check if the storage permission is enabled or not
        //return true if enabled
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(CreateGroupActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return  result;
    }
    private void requestStoragePermission(){
        //request run time storage permission
        ActivityCompat.requestPermissions(CreateGroupActivity.this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //check if the camera permission is enabled or not
        //return true if enabled
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(CreateGroupActivity.this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(CreateGroupActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return  result && result1;

    }
    private void requestCameraPermission(){
        //request run time camera permission
        ActivityCompat.requestPermissions(CreateGroupActivity.this,cameraPermissions,CAMERA_REQUEST_CODE);
    }


    //handle the permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method is called when user press allow or deny from  permission request dialog
        //here we will handle permission cases (allowed or denied)

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean cameraAccepted =grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted =grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted&&storageAccepted)
                    {
                        //both permission are granted
                        pickFromCamera();

                    }
                    else {
                        //if camera gallery both permission are denied
                        Toast.makeText(CreateGroupActivity.this, "Camera and storage permissions both are necessary", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean storageAccepted =grantResults[0] ==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted)
                    {

                        //storage permission are granted
                        pickFromGallery();

                    }
                    else {
                        //if  gallery  permission is denied
                        Toast.makeText(CreateGroupActivity.this, "storage permissions  is necessary", Toast.LENGTH_SHORT).show();
                    }

                }
                else {

                }

            }
            break;
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking the image from the camer or gallery
        if (resultCode== RESULT_OK)
        {
            if (requestCode==IMAGE_PICK_GALLERY_CODE)
            {
                //image is picked from the gallery get uri of the image
                image_rui =data.getData();
                //set the taken image to the image view
                groupCoverImage.setImageURI(image_rui);

            }
            else if (requestCode==IMAGE_PICK_CAMERA_CODE)
            {
                //image is picked from the camera get the uri of the image
                groupCoverImage.setImageURI(image_rui);

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        //sending the selected value in the setGroupCategory variable
        setGroupCategory=item;

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
