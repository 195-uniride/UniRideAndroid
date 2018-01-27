package com.sjsu.se195.uniride;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.FirebaseDatabase;
import com.sjsu.se195.uniride.models.User;


/**
 * Created by Marta on 10/8/17.
 */

public class SignUpActivity extends Activity implements View.OnClickListener {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText FirstNameEditText, LastNameEditText, EmailEditText, PasswordEditText;
    private Button mSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        FirstNameEditText = (EditText) findViewById(R.id.FirstNameEditText);
        LastNameEditText = (EditText) findViewById(R.id.LastNameEditText);
        EmailEditText = (EditText) findViewById(R.id.EmailEditText);
        PasswordEditText = (EditText) findViewById(R.id.PasswordEditText);

        mSignUpButton = findViewById(R.id.SignUpButton);

        findViewById(R.id.SignUpButton).setOnClickListener(this);
    }

    public void onStart() {
        super.onStart();

        //Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    private void SignUp() {
        String email = EmailEditText.getText().toString().trim();
        String password = PasswordEditText.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    onAuthSuccess(task.getResult().getUser());
                } else {
                    Toast.makeText(SignUpActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void onAuthSuccess(FirebaseUser user) {
        String first = FirstNameEditText.getText().toString().trim();
        String last = LastNameEditText.getText().toString().trim();

        //Save user information to database
        writeNewUser(user.getUid(), first, last, user.getEmail());

        //Go to MainActivity
        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finish();
    }

    private void writeNewUser(String userId, String first, String last, String email) {
        UserInformation user = new UserInformation (first, last, email);

        mDatabase.child("users").child(userId).setValue(user);
    }


//    private void registerUser(){

//
//        if (first.isEmpty()){
//            FirstNameEditText.setError("First name is required.");
//            FirstNameEditText.requestFocus();
//            return;
//        } else if (last.isEmpty()){
//            LastNameEditText.setError("Last name is required.");
//            LastNameEditText.requestFocus();
//            return;
//        } else if (email.isEmpty()) {
//            EmailEditText.setError("Email is required.");
//            EmailEditText.requestFocus();
//            return;
//        } else if (password.isEmpty()) {
//            PasswordEditText.setError("Last name is required.");
//            PasswordEditText.requestFocus();
//            return;
//        }
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
//            EmailEditText.setError("PLease enter a valid email");
//            EmailEditText.requestFocus();
//            return;
//        }
//
//        if (password.length()<6){
//            PasswordEditText.setError("Mimimum length of password should be 6");
//            PasswordEditText.requestFocus();
//            return;
//        }
//
//        progressBar.setVisibility(View.VISIBLE);
//
//        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                progressBar.setVisibility(View.GONE);
//                if (task.isSuccessful()){
//                    Toast.makeText(SignUpActivity.this, "We've created your account!", Toast.LENGTH_SHORT).show();
//                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
//                } else {
//                    Toast.makeText(SignUpActivity.this, "An error has occurred", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }


//    private void createNewUser(FirebaseUser userFromRegistration) {
//        String username = "username";
//        String email = userFromRegistration.getEmail();
//        String userId = userFromRegistration.getUid();
//
//        User user = new User(username, email);
//
//        mDatabase.child("users").child(userId).setValue(user);
//    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.SignUpButton:
                SignUp();
                break;

//            case R.id. LoginButton:
//                startActivity(new Intent(this, SignInActivity.class));
//                break;
        }
    }
}