package com.sjsu.se195.uniride;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marta on 2/24/18.
 */

public class AddUserInformation extends BaseActivity implements View.OnClickListener{

    private DatabaseReference ref;
    private FirebaseUser currentUser;

    private String userID;

    private Button saveButton;
    private Button skipButton;

    private EditText firstName;
    private EditText lastName;
    private EditText dateOfBirth;
    private EditText phoneNumber;
    //DateOfBirth and PhoneNumber might have to be saved differently

    private Spinner orgSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_information);

        saveButton = findViewById(R.id.save_information);
        skipButton = findViewById(R.id.skip_this);

        firstName = (EditText)findViewById(R.id.first_name);
        lastName = (EditText) findViewById(R.id.last_name);
        dateOfBirth = (EditText) findViewById(R.id.date_of_birth);
        phoneNumber = (EditText) findViewById(R.id.phone_number);

        skipButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        fillOrganizations();
    }

    private void updateInformation(String first, String last, String dob, String phone) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = currentUser.getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        HashMap<String, String> userInformation = new HashMap<>();
        userInformation.put("first_name", first);
        userInformation.put("last_name", last);
        userInformation.put("date_of_birth", dob);
        userInformation.put("phone_number", phone);

        ref.push().setValue(userInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddUserInformation.this, "Information Saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddUserInformation.this, "Failed to Save Information", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fillOrganizations(){
        final Spinner orgSpinner = (Spinner) findViewById(R.id.chosen_organization);
        ref = FirebaseDatabase.getInstance().getReference().child("organizations");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> allOrganizations = new ArrayList<String>();
                for (DataSnapshot orgSnapshot: dataSnapshot.getChildren()) {
                    String orgName = orgSnapshot.child("name").getValue(String.class);
                    allOrganizations.add(orgName);
                }

                ArrayAdapter<String> orgAdapter = new ArrayAdapter<String>(AddUserInformation.this,
                        android.R.layout.simple_spinner_item, allOrganizations);
                orgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                orgSpinner.setAdapter(orgAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void onStart() {
        super.onStart();
        }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save_information:
                String theFirst = firstName.getText().toString();
                String theLast = lastName.getText().toString();
                String theDateOfBirth = dateOfBirth.getText().toString();
                String thePhoneNumber = phoneNumber.getText().toString();

                updateInformation(theFirst, theLast, theDateOfBirth, thePhoneNumber);

            case R.id.skip_this:
                startActivity(new Intent(AddUserInformation.this, MainActivity.class));
        }
    }
}
