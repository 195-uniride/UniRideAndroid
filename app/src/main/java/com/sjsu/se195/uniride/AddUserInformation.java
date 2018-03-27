package com.sjsu.se195.uniride;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import com.sjsu.se195.uniride.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Marta on 2/24/18.
 */

// EditPersonalInformationActivity:
public class AddUserInformation extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "AddUserInformation";

    private DatabaseReference mUserReference;
    private User currentUser;

    private String userID;

    private Button saveButton;
    private Button skipButton;
    private Button mJoinOrganizationButton;

    private HashMap<String, String> OrganizationNameIdMap;

    private EditText mFirstNameField;
    private EditText mLastNameField;
    private EditText mDateOfBirthField;
    private EditText mPhoneNumberField;
    //DateOfBirth and PhoneNumber might have to be saved differently

    private Spinner orgSpinner;
    private ArrayAdapter<String> orgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_information);

        saveButton = findViewById(R.id.save_information);
        skipButton = findViewById(R.id.skip_this);
        mJoinOrganizationButton = findViewById(R.id.join_an_organization);

        mFirstNameField = (EditText)findViewById(R.id.first_name);
        mLastNameField = (EditText) findViewById(R.id.last_name);
        mDateOfBirthField = (EditText) findViewById(R.id.date_of_birth);
        mPhoneNumberField = (EditText) findViewById(R.id.phone_number);

        orgSpinner = (Spinner) findViewById(R.id.chosen_organization);

        saveButton.setOnClickListener(this);
        skipButton.setOnClickListener(this);
        mJoinOrganizationButton.setOnClickListener(this);

        // If this activity was not started by SignUpActivity, change skipButton text to "Cancel" instead of "Skip This":
        if (getIntent().getExtras() == null ||
                !getIntent().getExtras().getString("callingActivity").equals("SignUpActivity")) {

            skipButton.setText("Cancel");
        }

        // fillOrganizations();
    }

    private void updateInformation(String first, String last, String dob, String phone, String defaultOrganizationId) {
        // TODO: continue WIP:
        // TODO: get user, update fields by doing user.updateEmail(newEmail), etc., then do ref.updateChildren after user.toMap
        HashMap<String, Object> userInformation = new HashMap<>();

//        userInformation.put("email", email);
//        userInformation.put("username", username);
        userInformation.put("firstName", first);
        userInformation.put("lastName", last);
        userInformation.put("phoneNumber", phone);
        userInformation.put("defaultOrganizationId", defaultOrganizationId);

        mUserReference.updateChildren(userInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    private void fillOrganizations() {
        OrganizationNameIdMap = new HashMap<>();

        DatabaseReference userOrgsRef = FirebaseDatabase.getInstance().getReference()
                .child("user-organizations").child(getUid());

        userOrgsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> allOrganizationNames = new ArrayList<String>();

                String defaultOrgName = "";
                // Gets the name of all organizations the user has joined:
                for (DataSnapshot orgSnapshot: dataSnapshot.getChildren()) {
                    // Change text if has already joined at least one organization:
                    if (!mJoinOrganizationButton.getText().equals("Join Another Organization")) {
                        mJoinOrganizationButton.setText("Join Another Organization");
                    }

                    String orgName = orgSnapshot.child("name").getValue(String.class);

                    allOrganizationNames.add(orgName);

                    // Link the organization name to its key (for later lookup):
                    String orgId = orgSnapshot.getKey();
                    OrganizationNameIdMap.put(orgName, orgId); // NOTE: This assumes all org names are unique.

                    // Get default org name to set as the selected item in the list:
                    if (currentUser.defaultOrganizationId.equals(orgId)) {
                        defaultOrgName = orgName;
                    }
                }

                //Fills spinner with organization names
                orgAdapter = new ArrayAdapter<String>(AddUserInformation.this,
                        android.R.layout.simple_spinner_item, allOrganizationNames);
                orgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                orgSpinner.setAdapter(orgAdapter);

                // Set the default org name as the initial selected item:
                orgSpinner.setSelection(orgAdapter.getPosition(defaultOrgName));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onStart() {
        super.onStart();

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = currentFirebaseUser.getUid();

        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Organization object and use the values to update the UI
                currentUser = dataSnapshot.getValue(User.class);
                // [START_EXCLUDE]
                mFirstNameField.setText(currentUser.firstName);
                mLastNameField.setText(currentUser.lastName);
                mLastNameField.setText(currentUser.lastName);
                mPhoneNumberField.setText(currentUser.phoneNumber);

                fillOrganizations();
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Organization failed, log a message
                Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(AddUserInformation.this, "Failed to load user.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save_information:
                String theFirst = mFirstNameField.getText().toString();
                String theLast = mLastNameField.getText().toString();
                String theDateOfBirth = mDateOfBirthField.getText().toString();
                String thePhoneNumber = mPhoneNumberField.getText().toString();
                String organizationName = orgSpinner.getSelectedItem().toString();

                String organizationId = OrganizationNameIdMap.get(organizationName);

                updateInformation(theFirst, theLast, theDateOfBirth, thePhoneNumber, organizationId);
                // falls through.

            case R.id.skip_this:
                startActivity(new Intent(AddUserInformation.this, MainActivity.class));
                break;

            case R.id.join_an_organization:
                startActivity(new Intent(AddUserInformation.this, ShowOrganizationsActivity.class));
                break;
        }
    }
}
