package com.sjsu.se195.uniride;

/**
 * Created by timhdavis on 10/8/17.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.Organization;
import com.sjsu.se195.uniride.models.User;
//import com.sjsu.se195.uniride.models.Comment;
//import com.sjsu.se195.uniride.models.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizationDetailActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "OrganizationDetailActivity";
    private static final String REQUIRED = "Required";

    public static final String EXTRA_ORGANIZATION_KEY = "organization_key";

    private DatabaseReference mDatabase;
//    private ValueEventListener mOrganizationListener;
    private String mOrganizationKey;
//    private CommentAdapter mAdapter;

    // TODO: change all...vvv
    private TextView mOrganizationNameView;
    private EditText mOrganizationEmailField;
    private Button mJoinButton;
    // TODO...add other fields...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_detail); // TODO

        // Get organization key from intent // TODO
        mOrganizationKey = getIntent().getStringExtra(EXTRA_ORGANIZATION_KEY);
        if (mOrganizationKey == null) { // TODO
            throw new IllegalArgumentException("Must pass EXTRA_ORGANIZATION_KEY");
        }

        // Initialize Database:

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]


        // Initialize Views // TODO: all...vvv
        mOrganizationNameView = (TextView) findViewById(R.id.organization_name);
//        mTitleView = (TextView) findViewById(R.id.post_title);
//        mBodyView = (TextView) findViewById(R.id.post_body);
        mOrganizationEmailField = (EditText) findViewById(R.id.field_enter_email_text);
        mJoinButton = (Button) findViewById(R.id.button_join_organization);
//        mCommentsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);

        mJoinButton.setOnClickListener(this);
//        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));

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
        //final String body = mBodyField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(organizationEmail)) {
            mOrganizationEmailField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Joining Organization...", Toast.LENGTH_SHORT).show();

        // add new organization to database:
        addUserToOrganization(organizationEmail);

        // Finish this Activity, back to the stream
        setEditingEnabled(true);
        finish();
    }

    private void setEditingEnabled(boolean enabled) {
        mOrganizationEmailField.setEnabled(enabled);
//        mBodyField.setEnabled(enabled);
        if (enabled) {
            mJoinButton.setVisibility(View.VISIBLE);
        } else {
            mJoinButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void addUserToOrganization(String userOrganizationEmail) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("user-organizations").push().getKey();
//        Organization organization = new Organization(name);
        Map<String, Object> userOrganizationValues = getMap("userOrganizationEmail",userOrganizationEmail);

        Map<String, Object> childUpdates = new HashMap<>();
//        childUpdates.put("/organizations/" + key, organizationValues);
        childUpdates.put("/user-organizations/" + getUid() + "/" + mOrganizationKey + "/" + key, userOrganizationValues); //TODO: need to add organization key in here.

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
