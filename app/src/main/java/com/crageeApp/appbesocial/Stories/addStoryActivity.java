package com.crageeApp.appbesocial.Stories;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.crageeApp.appbesocial.Home.HomeActivity;
import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class addStoryActivity extends AppCompatActivity {

    private Uri mImageUri;
    private String myUrl="";
    private StorageTask storageTask;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);
        storageReference=FirebaseStorage.getInstance().getReference("Story");
     //   CropImage.activity().setAspectRatio(9,16).start(addStoryActivity.this);
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(9,16)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setOutputCompressQuality(25)
                .start(addStoryActivity.this);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }


    private void publishStory(){

        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();
        if (mImageUri!=null){
            final StorageReference imageReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));

            storageTask =imageReference.putFile(mImageUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                  if (!task.isSuccessful()){
                      throw task.getException();
                  }

                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task <Uri>task) {
                    if (task.isSuccessful()){
                        Uri downloadUri=task.getResult();
                        myUrl=downloadUri.toString();
                        String myId= FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(myId);
                        String storyId=reference.push().getKey();
                        long timeEnd=System.currentTimeMillis()+86400000;   //1 day
                        HashMap<String, Object> hashMap=new HashMap<>();
                        hashMap.put("imageUrl",myUrl);
                        hashMap.put("timeStart", ServerValue.TIMESTAMP);
                        hashMap.put("timeEnd",timeEnd);
                        hashMap.put("storyId",storyId);
                        hashMap.put("userId",myId);


                        reference.child(storyId).setValue(hashMap);
                        pd.dismiss();
                        finish();


                    }else {
                        Toast.makeText(addStoryActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(addStoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        else {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE &&resultCode==RESULT_OK){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            mImageUri=result.getUri();
            publishStory();
        }else {
            Toast.makeText(this, "Something gone wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(addStoryActivity.this, HomeActivity.class));
            finish();
        }
    }
}
