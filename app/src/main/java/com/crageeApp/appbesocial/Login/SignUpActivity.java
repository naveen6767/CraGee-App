package com.crageeApp.appbesocial.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crageeApp.appbesocial.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {


    private EditText eTPassword,eTEmail;
    private FirebaseAuth userAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        eTEmail = findViewById(R.id.txtEmailSignUp);
        eTPassword = findViewById(R.id.txtEmailPassword);
        Button  btnSignUp = findViewById(R.id.btnSignUp);
        // Initialize Fire base here
        userAuth=FirebaseAuth.getInstance();

        // apply set on click listener in the sign up button
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //take input from the user as email and password
                String userEmail=eTEmail.getText().toString().trim();
                String userPassword=eTPassword.getText().toString().trim();
                //here validate about the input
                if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
                {
                    eTEmail.setError("Please enter your email");
                    eTEmail.setFocusable(true);
                }
                else if (userPassword.length()<6)
                {
                    eTPassword.setError("Password length at least 6 character");
                    eTPassword.setFocusable(true);
                }
                else {
                    registerUser(userEmail,userPassword);
                }
            }
        });

    }

    private void registerUser(String userEmail, String userPassword) {
        //email and password pattern is valid

        //performing the sign up for the new users
        userAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success

                            Objects.requireNonNull(userAuth.getCurrentUser()).sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(SignUpActivity.this, "Registered successfully." +
                                                        "Please check your email for verification", Toast
                                                        .LENGTH_SHORT).show();
                                                eTEmail.setText("");
                                                eTPassword.setText("");
                                            }
                                            else {
                                                Toast.makeText(SignUpActivity.this, ""+ Objects.requireNonNull(task.getException())
                                                        .getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.

                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, " "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error ,dismiss the progress dialog  and  get and show the error message
                Toast.makeText(SignUpActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
    public void onSignUpClick(View view){
        startActivity(new Intent(this,LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

    }

}
