package com.sjsu.se195.uniride;

/**
 * Created by timhdavis on 10/8/17.
 */


import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.Organization;

import java.util.HashMap;
import java.util.Map;

public class OrganizationDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "OrganizationDetailActivity";
    private static final String REQUIRED = "Required";

    public static final String EXTRA_ORGANIZATION_KEY = "organization_key";

    private DatabaseReference mDatabase;
    private DatabaseReference mOrganizationReference;
    private String mOrganizationKey;

    private TextView mOrganizationNameView;
    private EditText mOrganizationEmailField;
    private Button mJoinButton;
    // TODO...add other fields...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_detail);

        // Set Joined button to visible only if not joined this organization before:
        //TODO

        // Get organization key from intent
        mOrganizationKey = getIntent().getStringExtra(EXTRA_ORGANIZATION_KEY);
        if (mOrganizationKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_ORGANIZATION_KEY");
        }

        // Initialize Database:

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mOrganizationReference = FirebaseDatabase.getInstance().getReference()
                .child("organizations").child(mOrganizationKey);
        // [END initialize_database_ref]


        // Initialize Views // TODO: all...vvv
        mOrganizationNameView = (TextView) findViewById(R.id.organization_name);


        mOrganizationEmailField = (EditText) findViewById(R.id.field_enter_email_text);
        mJoinButton = (Button) findViewById(R.id.button_join_organization);

        mJoinButton.setOnClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();

        mOrganizationReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Organization object and use the values to update the UI
                Organization organization = dataSnapshot.getValue(Organization.class);
                // [START_EXCLUDE]
                mOrganizationNameView.setText(organization.name);
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Organization failed, log a message
                Log.w(TAG, "loadOrganization:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(OrganizationDetailActivity.this, "Failed to load organization.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_join_organization) {
            joinOrganization();
        }
    }


    private void joinOrganization() {
        final String organizationEmail = mOrganizationEmailField.getText().toString();

        // User's organization email is required
        if (TextUtils.isEmpty(organizationEmail)) {
            mOrganizationEmailField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Joining Organization...", Toast.LENGTH_SHORT).show();

        // add new organization to database:
        addUserToOrganization(organizationEmail);
        Toast.makeText(this, "Joined " + mOrganizationNameView.getText(), Toast.LENGTH_SHORT).show();

        // Finish this Activity, back to the stream
        setEditingEnabled(true);
        finish();
    }

    private void setEditingEnabled(boolean enabled) {
        mOrganizationEmailField.setEnabled(enabled);

        if (enabled) {
            mJoinButton.setVisibility(View.VISIBLE);
        } else {
            mJoinButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void addUserToOrganization(String userOrganizationEmail) {
        // Link user with the organization, and include the user's organization email:
        String key = mDatabase.child("user-organizations").push().getKey();

        Map<String, Object> userOrganizationValues = getMap("userOrganizationEmail",userOrganizationEmail);

        Map<String, Object> childUpdates = new HashMap<>();
//        childUpdates.put("/user-organizations/" + getUid() + "/" + mOrganizationKey + "/" + key, userOrganizationValues);//TODO: is 'key' unnecessary here?
        childUpdates.put("/user-organizations/" + getUid() + "/" + mOrganizationKey, userOrganizationValues);//TODO: is 'key' unnecessary here?


        mDatabase.updateChildren(childUpdates);
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> getMap(String fieldName, Object object) {
        HashMap<String, Object> result = new HashMap<>();
        result.put(fieldName, object);

        return result;
    }
    // [END post_to_map]

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}
