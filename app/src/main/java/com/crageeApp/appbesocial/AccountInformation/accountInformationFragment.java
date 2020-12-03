package com.crageeApp.appbesocial.AccountInformation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.crageeApp.appbesocial.AccountChoice.AccountChoiceActivity;
import com.crageeApp.appbesocial.Home.HomeActivity;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import static android.app.Activity.RESULT_OK;

public class accountInformationFragment extends Fragment {
    private View accountInformationFragment;
    private ImageView userProfile;
    private EditText firstName,lastName,textUserBio,pageDescription;
    private TextView selectDateOfBirth;
    private DatabaseReference myDatabaseReference,mRootRef;
    private RadioButton radio_Male,radio_Female;
    private String currentUserId;
    private StorageReference userProfileImagesRef,pageProfileImagesRef;
    private String setGender="";
    private String downloadUri;
    private String birthDate,saveTodaysDate;
    private DatePickerDialog datePickerDialog;
    private int year,month,dayOfMonth;
    private Calendar calendar;
    private LinearLayout pageLayout,personalAccountLayout;
    public static final String TAG = "AccountInformation";
    private View createProfile,createPageButton;


    //image picked will be saved in this URi
    private Uri image_rui=null;


    public accountInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        accountInformationFragment= inflater.inflate(R.layout.fragment_account_information, container, false);

        userProfile= accountInformationFragment.findViewById(R.id.user_ProfileImage);
        firstName =accountInformationFragment.findViewById(R.id.firstName);
        lastName =accountInformationFragment.findViewById(R.id.lastName);
        selectDateOfBirth=accountInformationFragment.findViewById(R.id.select_date_of_birth);
        textUserBio=accountInformationFragment.findViewById(R.id.txtUserBio);
        createProfile=accountInformationFragment.findViewById(R.id.buttonCompleteProfile);


        //init for the page
        pageDescription=accountInformationFragment.findViewById(R.id.eTDescription);
        pageLayout =accountInformationFragment.findViewById(R.id.about_page_layout);
        personalAccountLayout =accountInformationFragment.findViewById(R.id.personalDetailsLayout);
        createPageButton=accountInformationFragment.findViewById(R.id.create_page);

        //for radio buttons
        radio_Male =accountInformationFragment.findViewById(R.id.radio_Male);
        radio_Female=accountInformationFragment.findViewById(R.id.radio_Female);

        //initialize the progress dialog for the user information fragment


        //Initialize the fire base database reference
        myDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
        mRootRef=FirebaseDatabase.getInstance().getReference();

        currentUserId = FirebaseAuth.getInstance().getUid();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        pageProfileImagesRef = FirebaseStorage.getInstance().getReference().child("PageProfile Images");

        //get image from the camera or gallery on click
        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setOutputCompressQuality(50)
                        .start((Activity) accountInformationFragment.getContext());
            }
        });
        final ProgressButton progressButton =new ProgressButton(getContext(),createProfile);
        progressButton.buttonCompleteProfile();
        final ProgressButton progressButtonPage =new ProgressButton(getContext(),createPageButton);
        progressButtonPage.buttonCreatePage();
        //taking the dob of the user
        selectDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open the date picker dialog box
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
                saveTodaysDate = currentDate.format(calendar.getTime());

                final DatePickerDialog datePickerDialog = new DatePickerDialog(accountInformationFragment.getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                                datePicker.setMaxDate(new Date().getTime());
                                birthDate=dayOfMonth+"/"+(month+1)+"/"+year;
                                selectDateOfBirth.setText(birthDate);
                            }
                        }, year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });
        /*
         * Now applying the click listener to the next button
         * and the next button will send the data to the real time data base
         * other details about personal information will be taken from other activity
         * personal profile information
         */
        createProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get data i.e. text through edit text
                //for profile the user name is compulsory
                //check if the user has entered the user name or not
                final String enteredFirstName=firstName.getText().toString().trim();
                final String enteredLastName=lastName.getText().toString().trim();
                final String enteredUserBio=textUserBio.getText().toString().trim();
                String enteredUserDob=selectDateOfBirth.getText().toString().trim();
                if (radio_Male.isChecked())
                {
                    setGender="Male";
                }
                if (radio_Female.isChecked())
                {
                    setGender="Female";
                }
                /*after clicking the next button
                check if the user has entered the user name or not
                 */
                /*
                applying the condition for deciding whether only text or
                image or both will be uploaded as post
                 */
                if (TextUtils.isEmpty(enteredFirstName))
                {
                    firstName.setError("User name is required");
                }
                else if (setGender.isEmpty()){
                    Toast.makeText(accountInformationFragment.getContext(), "Please select the gender",
                            Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(enteredUserDob)){
                    Toast.makeText(accountInformationFragment.getContext(), "Please enter your date of birth",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    final ProgressButton progressButton =new ProgressButton(accountInformationFragment.getContext(),createProfile);
                    progressButton.buttonActivated();
                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            completeProfile(enteredFirstName,enteredUserBio,String.valueOf(image_rui),progressButton,enteredLastName);



                        }
                    },3000);

                }

            }
        });
        createPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //send the entered info to the database
                //get data i.e. text through edit text
                //for page the page name is compulsory
                //check if the user has entered the page name or not
                final String enteredFirstName=firstName.getText().toString().trim();
                final String enteredLastName=lastName.getText().toString().trim();
                final String pageDescriptionEntered=pageDescription.getText().toString().trim();
                String enteredUserDob=selectDateOfBirth.getText().toString().trim();

                if (radio_Male.isChecked())
                {
                    setGender="Male";
                }
                if (radio_Female.isChecked())
                {
                    setGender="Female";
                }
                /*after clicking the create page button
                check if the user has entered the  page name or not
                 */
                /*
                applying the condition for deciding whether only text or
                image or both will be uploaded as creation of page
                 */
                if (TextUtils.isEmpty(enteredFirstName))
                {
                    firstName.setError("Page name is required");
                }
                else if (setGender.isEmpty()){
                    Toast.makeText(accountInformationFragment.getContext(), "Please select the gender",
                            Toast.LENGTH_SHORT).show();
                }
                else if (TextUtils.isEmpty(enteredUserDob)){
                    Toast.makeText(accountInformationFragment.getContext(), "Please enter your date of birth",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    final ProgressButton progressButton =new ProgressButton(accountInformationFragment.getContext(),createPageButton);
                    progressButton.buttonActivated();

                    Handler handler=new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            createPage(enteredFirstName,pageDescriptionEntered,String.valueOf(image_rui),progressButton,enteredLastName);
                            progressButton.buttonFinished();

                        }
                    },3000);
                }
            }
        });
        /*
         *check if the current user has already filled the account info
         * if filled ,retrieve the pre existing data
         */
        retrievePreAccInfo();

        return accountInformationFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this method will be called after picking the image from the camera or gallery
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    image_rui= result.getUri();
                }
                userProfile.setImageURI(image_rui);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = null;
                if (result != null) {
                    error = result.getError();
                }
                if (error != null) {
                    Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void retrievePreAccInfo() {
        myDatabaseReference
                .child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("name")) {
                            //retrieve the data name and others
                            String userName = (String) dataSnapshot.child("name").getValue();
                            String userImage = (String) dataSnapshot.child("image").getValue();
                            String userGender = (String) dataSnapshot.child("Gender").getValue();
                            String userDob = (String) dataSnapshot.child("dateOfBirth").getValue();
                            String userBio = (String) dataSnapshot.child("userBio").getValue();

                                if (userGender != null) {
                                    if (userGender.equals("Male")){
                                        radio_Male.setChecked(true);
                                    }else {
                                        radio_Female.setChecked(true);
                                    }
                                }
                                selectDateOfBirth.setText(userDob);


                            if (userName != null) {
                                if (userName.equals("CraGee Name")){
                                    firstName.setEnabled(true);
                                }else {
                                    //set data
                                    firstName.setText(userName);
                                    firstName.setEnabled(false);}
                            }
                            firstName.setTextColor(getResources().getColor(R.color.black));
                            try {
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfile);
                            } catch (Exception e) {
                                Picasso.get().load(R.drawable.profile_image).into(userProfile);
                            }

                            textUserBio.setText(userBio);
                        }
}
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void completeProfile(String enteredFirstName, String enteredUserBio, String uri, final ProgressButton buttonProgress, String enteredLastName) {
        //create profile with image
        final StorageReference imageStorage =userProfileImagesRef.child(currentUserId+".jpg");
        imageStorage.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image is uploaded to the fire base storage ,now get its url
                Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());

                final String downloadUri= uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    String userName=enteredFirstName+" "+enteredLastName;

                      /*
        generate unique user id from first name

         */
                    // create instance of Random class
                    Random rand = new Random();

                    // Generate random integers in range 0 to 999
                    int rand_int  = rand.nextInt(1000);

                    String uniqueUserId="";
                    switch (enteredFirstName.length()) {
                        case 3:
                            int rand_index_1  = rand.nextInt(3);
                            int rand_index_2 = rand.nextInt(2);
                            uniqueUserId = enteredFirstName + "." +enteredFirstName.charAt(rand_index_1)+enteredFirstName.charAt(rand_index_2)+ rand_int;
                            break;
                        case 4:
                            int rand_index_3  = rand.nextInt(4);
                            int rand_index_4 = rand.nextInt(3);
                            uniqueUserId = enteredFirstName + "." +enteredFirstName.charAt(rand_index_3)+enteredFirstName.charAt(rand_index_4)+ rand_int;
                            break;
                        case 5:
                            int rand_index1  = rand.nextInt(5);
                            int rand_index2 = rand.nextInt(4);
                            uniqueUserId = enteredFirstName + "_" +enteredFirstName.charAt(rand_index1)+enteredFirstName.charAt(rand_index2)+ rand_int;
                            break;
                        case 6:
                            int rand_index3  = rand.nextInt(6);
                            int rand_index4 = rand.nextInt(5);
                            uniqueUserId = enteredFirstName + "_" +enteredFirstName.charAt(rand_index3)+enteredFirstName.charAt(rand_index4)+ rand_int;
                            break;
                        case 7:
                            int rand_index5  = rand.nextInt(7);
                            int rand_index6 = rand.nextInt(6);
                            uniqueUserId = enteredFirstName + "." +enteredFirstName.charAt(rand_index5)+enteredFirstName.charAt(rand_index6)+ rand_int;
                            break;
                        case 8:
                            int rand_index7  = rand.nextInt(8);
                            int rand_index8 = rand.nextInt(7);
                            uniqueUserId = enteredFirstName + "." +enteredFirstName.charAt(rand_index7)+enteredFirstName.charAt(rand_index8)+ rand_int;
                            break;
                        case 9:
                            int rand_index9  = rand.nextInt(9);
                            int rand_index10 = rand.nextInt(8);
                            uniqueUserId = enteredFirstName + "_" +enteredFirstName.charAt(rand_index9)+enteredFirstName.charAt(rand_index10)+ rand_int;
                            break;
                        case 10:
                            int rand_index11  = rand.nextInt(10);
                            int rand_index12 = rand.nextInt(8);
                            uniqueUserId = enteredFirstName + "_" +enteredFirstName.charAt(rand_index11)+enteredFirstName.charAt(rand_index12)+ rand_int;
                            break;
                        default:
                            uniqueUserId = enteredFirstName +rand_int;
                            break;
                    }

                    HashMap<String, Object> userDetailsMap = new HashMap<>();
                    userDetailsMap.put("name",userName);
                    userDetailsMap.put("uniUserId",uniqueUserId);
                    userDetailsMap.put("userBio",enteredUserBio);
                    userDetailsMap.put("Gender",setGender);
                    userDetailsMap.put("dateOfBirth",birthDate);
                    mRootRef
                            .child("Users")
                            .child(currentUserId)
                            .updateChildren(userDetailsMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "onSuccess: name and bio has been set successfully");
                                    //automatic following of the official pages
                                    DatabaseReference autoFollowingCraGeeApp = mRootRef
                                            .child("user_following")
                                            .child(currentUserId)
                                            .child("iqd8EN29gaOeuubUWaVHPSLDlzl1")
                                            .child("id");
                                    autoFollowingCraGeeApp
                                            .setValue("iqd8EN29gaOeuubUWaVHPSLDlzl1")
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    DatabaseReference autoFollowerCraGeeApp  = mRootRef
                                                            .child("user_followers")
                                                            .child("iqd8EN29gaOeuubUWaVHPSLDlzl1")
                                                            .child(currentUserId)
                                                            .child("id");
                                                    autoFollowerCraGeeApp.setValue(currentUserId);

                                                    //create user essential node with name and image
                                                    HashMap<String, Object> userEssentialsMap = new HashMap<>();
                                                    userEssentialsMap.put("name",userName);
                                                    userEssentialsMap.put("image",downloadUri);

                                                    mRootRef
                                                            .child("user_essentials")
                                                            .child(currentUserId)
                                                            .setValue(userEssentialsMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    buttonProgress.buttonFinished();
                                                                    sendUserToHome();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "onFailure: failure in setting the name");

                        }
                    });
                    HashMap<String, Object> pageProfileMap = new HashMap<>();
                    pageProfileMap.put("image", downloadUri);
                    myDatabaseReference
                            .child(currentUserId)
                            .updateChildren(pageProfileMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "onSuccess: image uploaded successfully");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "onFailure: problem in uploading the image");
                            Log.i(TAG, "onFailure: problem in uploading the image"+e.getMessage());
                        }
                    });
                }

            }
        });


    }

    private void sendUserToHome() {
        Intent intentToAccountChoice = new Intent(accountInformationFragment.getContext(), HomeActivity.class);
        intentToAccountChoice.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentToAccountChoice);
        requireActivity().overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
        this.requireActivity().finish();
    }

    private void createPage(String enteredFirstName, final String pageDescriptionEntered, String uri, final ProgressButton saveButtonProgress,String enteredLastName) {
        //create page with image
        final StorageReference imageStorage =pageProfileImagesRef.child(currentUserId+".jpg");
        imageStorage.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image is uploaded to the fire base storage ,now get its url
                Task<Uri> uriTask =taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                final String downloadUri= uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    String userName=enteredFirstName+" "+enteredLastName;
                    // create instance of Random class
                    Random rand = new Random();

                    // Generate random integers in range 0 to 999
                    int rand_int  = rand.nextInt(1000);

                    String uniqueUserId="";
                    switch (enteredFirstName.length()) {
                        case 3:
                            int rand_index_1  = rand.nextInt(3);
                            int rand_index_2 = rand.nextInt(2);
                            uniqueUserId = enteredFirstName + "." +enteredFirstName.charAt(rand_index_1)+enteredFirstName.charAt(rand_index_2)+ rand_int;
                            break;
                        case 4:
                            int rand_index_3  = rand.nextInt(4);
                            int rand_index_4 = rand.nextInt(3);
                            uniqueUserId = enteredFirstName + "." +enteredFirstName.charAt(rand_index_3)+enteredFirstName.charAt(rand_index_4)+ rand_int;
                            break;
                        case 5:
                            int rand_index1  = rand.nextInt(5);
                            int rand_index2 = rand.nextInt(4);
                            uniqueUserId = enteredFirstName + "_" +enteredFirstName.charAt(rand_index1)+enteredFirstName.charAt(rand_index2)+ rand_int;
                            break;
                        case 6:
                            int rand_index3  = rand.nextInt(6);
                            int rand_index4 = rand.nextInt(5);
                            uniqueUserId = enteredFirstName + "_" +enteredFirstName.charAt(rand_index3)+enteredFirstName.charAt(rand_index4)+ rand_int;
                            break;
                        case 7:
                            int rand_index5  = rand.nextInt(7);
                            int rand_index6 = rand.nextInt(6);
                            uniqueUserId = enteredFirstName + "." +enteredFirstName.charAt(rand_index5)+enteredFirstName.charAt(rand_index6)+ rand_int;
                            break;
                        case 8:
                            int rand_index7  = rand.nextInt(8);
                            int rand_index8 = rand.nextInt(7);
                            uniqueUserId = enteredFirstName + "." +enteredFirstName.charAt(rand_index7)+enteredFirstName.charAt(rand_index8)+ rand_int;
                            break;
                        case 9:
                            int rand_index9  = rand.nextInt(9);
                            int rand_index10 = rand.nextInt(8);
                            uniqueUserId = enteredFirstName + "_" +enteredFirstName.charAt(rand_index9)+enteredFirstName.charAt(rand_index10)+ rand_int;
                            break;
                        case 10:
                            int rand_index11  = rand.nextInt(10);
                            int rand_index12 = rand.nextInt(8);
                            uniqueUserId = enteredFirstName + "_" +enteredFirstName.charAt(rand_index11)+enteredFirstName.charAt(rand_index12)+ rand_int;
                            break;
                        default:
                            uniqueUserId = enteredFirstName +rand_int;
                            break;
                    }
                    HashMap<String, Object> userDetailsMap = new HashMap<>();
                    userDetailsMap.put("name",userName);
                    userDetailsMap.put("uniUserId",uniqueUserId);
                    userDetailsMap.put("userBio",pageDescriptionEntered);
                    userDetailsMap.put("Gender",setGender);
                    userDetailsMap.put("dateOfBirth",birthDate);
                    mRootRef
                            .child("Users")
                            .child(currentUserId)
                            .updateChildren(userDetailsMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "onSuccess: name and bio has been set successfully");
                                    //automatic following of the official pages
                                    DatabaseReference autoFollowingCraGeeApp = mRootRef
                                            .child("user_following")
                                            .child(currentUserId)
                                            .child("iqd8EN29gaOeuubUWaVHPSLDlzl1")
                                            .child("id");
                                    autoFollowingCraGeeApp
                                            .setValue("iqd8EN29gaOeuubUWaVHPSLDlzl1")
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    DatabaseReference autoFollowerCraGeeApp  = mRootRef
                                                            .child("user_followers")
                                                            .child("iqd8EN29gaOeuubUWaVHPSLDlzl1")
                                                            .child(currentUserId)
                                                            .child("id");
                                                    autoFollowerCraGeeApp.setValue(currentUserId);

                                                    //create user essential node with name and image
                                                    HashMap<String, Object> userEssentialsMap = new HashMap<>();
                                                    userEssentialsMap.put("name",userName);
                                                    userEssentialsMap.put("image",downloadUri);

                                                    mRootRef
                                                            .child("user_essentials")
                                                            .child(currentUserId)
                                                            .setValue(userEssentialsMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    saveButtonProgress.buttonFinished();
                                                                    sendUserToHome();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "onFailure: failure in setting the name");

                        }
                    });



                    HashMap<String, Object> pageProfileMap = new HashMap<>();
                    pageProfileMap.put("image", downloadUri);
                    mRootRef
                            .child("Users")
                            .child(currentUserId)
                            .updateChildren(pageProfileMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "onSuccess: page image uploaded successfully");
                                    Log.i(TAG, "onSuccess: uploading other details of the account");

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "onFailure: error in uploading the image "+e.getMessage());
                        }
                    });
                }
            }
        });

    }
    private void displayRequiredLayout() {

        Log.i(TAG, "displayRequiredLayout: showing the progress dialog");
        myDatabaseReference
                .child(currentUserId)
                .child("accountChoice")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            String getAccountChoice = (String) dataSnapshot.getValue();
                            assert getAccountChoice != null;
                            switch (getAccountChoice) {
                                case "Public Page":
                                    Log.i(TAG, "onDataChange: public page choice ");
                                    Log.i(TAG, "onDataChange: preparing public page");
                                    pageLayout.setVisibility(View.VISIBLE);
                                    personalAccountLayout.setVisibility(View.GONE);

                                    break;
                                case "Personal Account":
                                    Log.i(TAG, "onDataChange: required account by the user is Personal account");
                                    pageLayout.setVisibility(View.GONE);
                                    personalAccountLayout.setVisibility(View.VISIBLE);
                                    break;

                            }
                        }else {
                            Log.i(TAG, "onDataChange: data snapshot does not exist");
                            Log.i(TAG, "onDataChange: ");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "error"+databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendUserToAccountChoice() {
        Intent intentToAccountChoice = new Intent(accountInformationFragment.getContext(), AccountChoiceActivity.class);
        intentToAccountChoice.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentToAccountChoice);
        this.requireActivity().finish();
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: starting the account info activity");
        Log.i(TAG, "onStart: displayRequiredLayout");
        displayRequiredLayout();
    }

}