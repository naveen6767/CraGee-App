package com.crageeApp.appbesocial.Home.Fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crageeApp.appbesocial.ProgressButton;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {
    private TextView currentUserNameShare;
    private CircleImageView userProfileImageShare;
    private EditText postUploadText;
    private DatabaseReference usersRef,allPostsRef,usersPostsRef,mRootRef;
    private String currentUserId;
    private StorageReference postImagesRef;
    private String saveCurrentDate,saveCurrentTime,postRandomName,timeStamp;
    private View btnCreatePost;
    private int postCounter;
    private  ProgressButton progressButton;
    private CropImageView cropImageView;
    private static final String TAG = "PostsFragment";

    //image picked will be saved in this URi
    private Uri image_rui=null;

    private List<String> followersList;
    public PostsFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View PostsFragmentView = inflater.inflate(R.layout.fragment_posts, container, false);


        //initialize the fire base  here
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef= FirebaseDatabase.getInstance().getReference();
        allPostsRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        usersPostsRef= FirebaseDatabase.getInstance().getReference().child("user_posts");
        currentUserId = FirebaseAuth.getInstance().getUid();
        postImagesRef= FirebaseStorage.getInstance().getReference().child("post_images");

        //initialize the views here
        currentUserNameShare=PostsFragmentView.findViewById(R.id.userNameShare);
        userProfileImageShare=PostsFragmentView.findViewById(R.id.userDpShare);

        cropImageView =PostsFragmentView.findViewById(R.id.cropImageView);
        postUploadText =PostsFragmentView.findViewById(R.id.user_post_text);
        btnCreatePost =PostsFragmentView.findViewById(R.id.buttonCreatePost);
        //here retrieve the user profile image and name from the database
        //  retrieveUserDetails();
        retrieveUserBackgroundThread retrieveUserBackgroundThread =new retrieveUserBackgroundThread();
        retrieveUserBackgroundThread.execute();

        progressButton =new ProgressButton(getContext(),btnCreatePost);
        progressButton.buttonPost();
        //get image from the camera or gallery on click
        cropImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gallery clicked
                pickFromGallery();

            }
        });
        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data i.e. text through edit text
                String textToPost =postUploadText.getText().toString().trim();

                if (!TextUtils.isEmpty(textToPost)&&image_rui==null)
                {
                    //post without image
                    //means only text will be posted
                    Log.i(TAG, "only text will be posted ");
                    uploadData(textToPost,"noImage");
                }
                else if (!(image_rui ==null)){
                    //post with image
                    //text and image both can be posted from here
                    Log.i(TAG, "posting with the image");
                    uploadData(textToPost,String.valueOf(image_rui));
                }
                else {
                    Toast.makeText(getContext(), "Please post at least one of text or image", Toast.LENGTH_SHORT).show();
                }

            }
        });


        return PostsFragmentView;
    }

    private void uploadData(final String textToPost, String uri) {
        /*
         * here we will get the image upload date and time
         * data and time will be different fot different users
         */
        progressButton.buttonActivated();
        btnCreatePost.setEnabled(false);
        Calendar calForData =Calendar.getInstance();
        SimpleDateFormat currentDate =new SimpleDateFormat("dd-MM-yyyy");
        saveCurrentDate = currentDate.format(calForData.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime =new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());
        timeStamp =String.valueOf(System.currentTimeMillis());
        postRandomName =currentUserId+saveCurrentDate+saveCurrentTime+timeStamp;
        if (!uri.equals("noImage")){
            //post with image
            Log.i(TAG, "uploadData: posting with image");
            final StorageReference imageStorage =postImagesRef
                                                                .child(currentUserId)
                                                                .child(postRandomName+".jpg");
            imageStorage.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //image is uploaded to the fire base storage ,now get its url
                    Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    final String downloadUri= uriTask.getResult().toString();
                    if (uriTask.isSuccessful()){
                        //retrieve the user name and profile to save post information with these details
                        Log.i(TAG, "onSuccess: retrieving the data from the user profile");
                        usersRef
                                .child(currentUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String profileUserName = (String) dataSnapshot.child("name").getValue();
                                    String requestProfileImage =(String) dataSnapshot.child("image").getValue();
                                    String userVerified =(String) dataSnapshot.child("celebrityCategory").getValue();
                                    //url is received
                                    //upload post to fire base database
                                    Log.i(TAG, "onDataChange: uploading post to the database");
                                    String pushId=allPostsRef.push().getKey();
                                    HashMap<String, Object> hashMap =new HashMap<>();
                                    //put post info
                                    hashMap.put("uid",currentUserId);
                                    hashMap.put("userName",profileUserName);
                                    hashMap.put("userDp",requestProfileImage);
                                    hashMap.put("userVerified",userVerified);
                                    hashMap.put("time",saveCurrentTime);
                                    hashMap.put("date",saveCurrentDate);
                                    hashMap.put("postImage",downloadUri);
                                    hashMap.put("textPost",textToPost);
                                    hashMap.put("pTime",timeStamp);
                                    hashMap.put("pId",pushId);
                                    hashMap.put("pLikes","0");
                                    //path to store post data
                                    //put data in reference
                                    Log.i(TAG, "sending the hashmap data to the database");
                                    sendPostDataToDatabase(hashMap,pushId);//current user
                                    sendPostDataToAllDatabase(hashMap,pushId);//all users


                                    //update the followers timeline
                                    followersList=new ArrayList<>();
                                    Query followersQuery= mRootRef
                                            .child("user_followers")
                                            .child(currentUserId);
                                    followersQuery
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                    followersList.clear();
                                                    for (DataSnapshot dataSnapshot1:snapshot.getChildren()){

                                                        followersList.add(dataSnapshot1.getKey());

                                                    }
                                                    followersList.add(currentUserId);
                                                    Log.d(TAG, "onDataChange: the value of followers is inside the  "+followersList);

                                                    for (String followerId:followersList){
                                                        Query timeline_query= mRootRef
                                                                .child("user_timeline")
                                                                .orderByKey()
                                                                .equalTo(followerId);

                                                        timeline_query
                                                                .addValueEventListener(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                        snapshot.getRef().child(followerId).child(pushId).setValue(hashMap);
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                    }
                                                                });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

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
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed uploading the image

                    Toast.makeText(getContext(), "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }
        else {
            //retrieve the user name and profile to save post information with these details
            usersRef
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
                        String pushId=allPostsRef.push().getKey();
                        Log.d(TAG, "onDataChange: the push id is"+pushId);
                        HashMap<String, Object> withoutImageHashMap =new HashMap<>();
                        //put post info
                        withoutImageHashMap.put("uid",currentUserId);
                        withoutImageHashMap.put("userVerified",userVerified);
                        withoutImageHashMap.put("userName",profileUserName);
                        withoutImageHashMap.put("userDp",requestProfileImage);
                        withoutImageHashMap.put("time",saveCurrentTime);
                        withoutImageHashMap.put("date",saveCurrentDate);
                        withoutImageHashMap.put("postImage","noImage");
                        withoutImageHashMap.put("textPost",textToPost);
                        withoutImageHashMap.put("pTime",timeStamp);
                        withoutImageHashMap.put("pId",pushId);
                        withoutImageHashMap.put("pLikes","0");

                        //path to store post data
                        //put data in reference
                        sendPostDataToDatabaseWithoutImage(withoutImageHashMap,pushId);//current user
                        sendPostDataToAllDatabaseWithoutImage(withoutImageHashMap,pushId);//all users

                        //update the followers timeline
                        followersList=new ArrayList<>();
                        Query followersQuery= mRootRef
                                .child("user_followers")
                                .child(currentUserId);
                        followersQuery
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        followersList.clear();
                                        for (DataSnapshot dataSnapshot1:snapshot.getChildren()){
                                            followersList.add(dataSnapshot1.getKey());

                                        }
                                        followersList.add(currentUserId);
                                        Log.d(TAG, "onDataChange: the value of followers is "+followersList);



                                        for (String followerId:followersList){
                                            Query timeline_query= mRootRef
                                                    .child("user_timeline")
                                                    .orderByKey()
                                                    .equalTo(followerId);

                                            timeline_query
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            snapshot.getRef().child(followerId).child(pushId).setValue(withoutImageHashMap);
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

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

    private void sendPostDataToAllDatabaseWithoutImage(HashMap<String, Object> withoutImageHashMap, String pushId) {
        allPostsRef
                .child(pushId)
                .setValue(withoutImageHashMap);
    }

    private void sendPostDataToAllDatabase(HashMap<String, Object> hashMap, String pushId) {
        allPostsRef
                .child(pushId)
                .setValue(hashMap);

    }
    private void sendPostDataToDatabaseWithoutImage(HashMap<String, Object> withoutImageHashMap, String pushId) {
        usersPostsRef
                .child(currentUserId)
                .child(pushId)
                .setValue(withoutImageHashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            /*
                             *if the post is successfully added in the database
                             * increase the post counter by one
                             */
                            postCounter=postCounter+1;
                            Log.i(TAG, "after increment to post counter without image "+postCounter);
                            HashMap<String, Object> postsHashMap = new HashMap<>();
                            postsHashMap.put("noPosts",postCounter);
                            usersRef.child(currentUserId).updateChildren(postsHashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //added in the data base
                                    postUploadText.setText("");
                                    cropImageView.clearImage();
                                    progressButton.buttonPostSaved();
                                    progressButton.buttonPost();
                                    btnCreatePost.setEnabled(true);
                                    //sendUserToHome();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

    }
    private void sendPostDataToDatabase(HashMap<String, Object> hashMap, String pushId) {
        usersPostsRef
                .child(currentUserId)
                .child(pushId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Log.i(TAG, "current value of post counter"+postCounter);
                /*
                 *if the post is successfully added in the database
                 * increase the post counter by one
                 */
                postCounter=postCounter+1;
                Log.i(TAG, "after increment to post counter"+postCounter);
                HashMap<String, Object> postsHashMap = new HashMap<>();
                postsHashMap.put("noPosts",postCounter);
                usersRef
                        .child(currentUserId)
                        .updateChildren(postsHashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added in the data base

                         postUploadText.setText("");

                        cropImageView.clearImage();
                        progressButton.buttonPostSaved();
                        progressButton.buttonPost();
                        btnCreatePost.setEnabled(true);

                        //sendUserToHome();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //failed adding post to the data base

                Toast.makeText(getContext(), "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveUserDetails() {
        //here retrieve the user name and user profile image
        Query currentUserQuery=usersRef.orderByKey().equalTo(currentUserId);
        currentUserQuery.keepSynced(true);
        currentUserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: datasnapshot from query"+dataSnapshot);

                Log.d(TAG, "onDataChange: name"+dataSnapshot.child(currentUserId).child("name").getValue());
                String profileUserName =(String) dataSnapshot.child(currentUserId).child("name").getValue();
                String requestProfileImage =(String) dataSnapshot.child(currentUserId).child("image").getValue();
                postCounter =Integer.parseInt(String.valueOf(dataSnapshot.child(currentUserId).child("noPosts").getValue()));
                Log.i(TAG, "retrieve user post number"+postCounter);


                try {
                    Picasso.get().load(R.drawable.post_image_blue).into(userProfileImageShare);
                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.post_image_blue).into(userProfileImageShare);

                }catch (Exception e)
                {
                    Picasso.get().load(R.drawable.profile_image).into(userProfileImageShare);
                }
                currentUserNameShare.setText(profileUserName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void pickFromGallery() {


        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setOutputCompressQuality(50)
                .start(getContext(), this);

    }

    //handle the permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method is called when user press allow or deny from  permission request dialog
        //here we will handle permission cases (allowed or denied)
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
                Toast.makeText(getContext(), "storage permissions  is necessary", Toast.LENGTH_SHORT).show();
            }

        }
        else {

        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking the image from the camera or gallery
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    image_rui = result.getUri();
                    cropImageView.setImageUriAsync(image_rui);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Log.d(TAG, "onActivityResult: error occurred"+error.getMessage());
                }
            }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public class retrieveUserBackgroundThread extends AsyncTask<String,String,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground:posts retrieveUserBackgroundThread   is running");

            retrieveUserDetails();
            return null;
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }





}
