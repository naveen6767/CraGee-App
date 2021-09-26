
package com.crageeApp.appbesocial.crageePosts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.crageeApp.appbesocial.ProgressButton;
import com.crageeApp.appbesocial.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class crageePostsFragment extends Fragment {
    private View PostsFragmentView;

    private CrageePostsViewModel mViewModel;
    private TextView currentUserNameShare;
    private CircleImageView userProfileImageShare;
    private EditText postUploadText;
    private View btnCreatePost;
    private ProgressButton progressButton;
    //image picked will be saved in this URi
    private Uri image_rui=null;
    private CropImageView cropImageView;
    private static final String TAG = "crageePostsFragment";
    public static crageePostsFragment newInstance() {
        return new crageePostsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        PostsFragmentView= inflater.inflate(R.layout.cragee_posts_fragment, container, false);
        currentUserNameShare=PostsFragmentView.findViewById(R.id.userNameShare);
        userProfileImageShare=PostsFragmentView.findViewById(R.id.userDpShare);

        cropImageView =PostsFragmentView.findViewById(R.id.cropImageView);
        postUploadText =PostsFragmentView.findViewById(R.id.user_post_text);
        btnCreatePost =PostsFragmentView.findViewById(R.id.buttonCreatePost);
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
        return PostsFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(CrageePostsViewModel.class);
        // TODO: Use the ViewModel




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

}