package com.sjsu.se195.uniride;

/**
 * Created by timhdavis on 10/23/17.
 */

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.Organization;
import com.sjsu.se195.uniride.models.User;

import java.util.HashMap;
import java.util.Map;

public class NewOrganizationActivity extends BaseActivity {

    private static final String TAG = "NewOrganizationActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText mNameField;
    //private EditText mBodyField;
    private FloatingActionButton mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_organization);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mNameField = (EditText) findViewById(R.id.field_name);
        //mBodyField = (EditText) findViewById(R.id.field_body);
        mSubmitButton = (FloatingActionButton) findViewById(R.id.fab_submit_organization);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    private void submit() {
        final String name = mNameField.getText().toString();
        //final String body = mBodyField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(name)) {
            mNameField.setError(REQUIRED);
            return;
        }

//        // Body is required
//        if (TextUtils.isEmpty(body)) {
//            mBodyField.setError(REQUIRED);
//            return;
//        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Adding Organization...", Toast.LENGTH_SHORT).show();

        // add new organization to database:
        addNewOrganization(name);

        // Finish this Activity, back to the stream
        setEditingEnabled(true);
        finish();
    }

    private void setEditingEnabled(boolean enabled) {
        mNameField.setEnabled(enabled);
//        mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void addNewOrganization(String name) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("organizations").push().getKey();
        Organization organization = new Organization(name);
        Map<String, Object> organizationValues = organization.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/organizations/" + key, organizationValues);
//        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]
}
