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
    private EditText mClassificationField;
    private EditText mDescriptionField;
    private EditText mEmailPatternField;
    private EditText mWebsiteField;
    private FloatingActionButton mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_organization);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mNameField = (EditText) findViewById(R.id.field_name);
        mClassificationField = (EditText) findViewById(R.id.field_classification);
        mDescriptionField = (EditText) findViewById(R.id.field_description);
        mEmailPatternField = (EditText) findViewById(R.id.field_email_pattern);
        mWebsiteField = (EditText) findViewById(R.id.field_website);
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
        final String classification = mClassificationField.getText().toString();
        final String description = mDescriptionField.getText().toString();
        final String emailPattern = mEmailPatternField.getText().toString();
        final String website = mWebsiteField.getText().toString();

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

        addNewOrganization(new Organization(name, classification, description, emailPattern, website));

        // Finish this Activity, back to the stream
        setEditingEnabled(true);
        finish();
    }

    private void setEditingEnabled(boolean enabled) {
        mNameField.setEnabled(enabled);
        mClassificationField.setEnabled(enabled);
        mDescriptionField.setEnabled(enabled);
        mEmailPatternField.setEnabled(enabled);
        mWebsiteField.setEnabled(enabled);

        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void addNewOrganization(Organization organization) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("organizations").push().getKey();

        Map<String, Object> organizationValues = organization.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/organizations/" + key, organizationValues);
//        childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]
}
