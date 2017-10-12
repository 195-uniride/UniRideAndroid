package com.sjsu.se195.uniride;

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
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.RideRequestPost;
import com.sjsu.se195.uniride.models.User;

import java.util.HashMap;
import java.util.Map;

public class NewPostActivity extends BaseActivity {

    private static final String TAG = "NewPostActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText mSourceField;
    private EditText mDestinationField;
    private FloatingActionButton mSubmitButton;
    private boolean postType = false; //true = driveOffer; false = rideRequest

    private EditText mpassengerCount;

    private EditText mpickupPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postType = getIntent().getExtras().getBoolean("driveOffer");

        if(postType){
            setContentView(R.layout.activity_drive_offer_post);
        }
        else{
            setContentView(R.layout.activity_ride_request_post);
        }

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mSourceField = (EditText) findViewById(R.id.field_source);
        mDestinationField = (EditText) findViewById(R.id.field_destination);
        mSubmitButton = (FloatingActionButton) findViewById(R.id.fab_submit_post);

        if(postType){
            mpassengerCount = (EditText) findViewById(R.id.passengerCount);
        }
        else{
            mpickupPoint = (EditText) findViewById(R.id.pickupPoint);
        }

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private void submitPost() {
        final String source = mSourceField.getText().toString();
        final String destination = mDestinationField.getText().toString();

        String pickupPoint_temp = "nil";
        int passengerCount_temp = 0;
        if(postType && !mpassengerCount.getText().toString().equals("")){
            passengerCount_temp = Integer.parseInt(mpassengerCount.getText().toString());
        }
        else if(postType && mpassengerCount.getText().toString().equals("")){
            mpassengerCount.setError(REQUIRED);
            return;
        }
        else{
            pickupPoint_temp = mpickupPoint.getText().toString();
        }

        final int passengerCount = passengerCount_temp;
        final String pickupPoint = pickupPoint_temp;

        //if drive offer post and passenger count empty
        if(postType && passengerCount_temp==0) {
            mpassengerCount.setError("Must be greater than 0.");
            return;
        }

        //if ride request post and pickup point empty
        if (!postType && TextUtils.isEmpty(pickupPoint_temp)) {
            mpickupPoint.setError(REQUIRED);
            return;
        }

        // Title is required
        if (TextUtils.isEmpty(source)) {
            mSourceField.setError(REQUIRED);
            return;
        }

        // Body is required
        if (TextUtils.isEmpty(destination)) {
            mDestinationField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Posting...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewPostActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                            if(postType) {
                                writeNewDriveOfferPost(userId, user.username, source, destination, passengerCount);
                            }
                            else{
                                writeNewRideRequestPost(userId, user.username, source, destination, pickupPoint);
                            }
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

    private void setEditingEnabled(boolean enabled) {
        mSourceField.setEnabled(enabled);
        mDestinationField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]

    //creating a drive offer
    private void writeNewDriveOfferPost(String userId, String username, String source, String destination, int count) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").child("driveOffers").push().getKey();

        DriverOfferPost driverPost = new DriverOfferPost(userId, username, source, destination, count);
        Map<String, Object> postValues = driverPost.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/driveOffers/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/driveOffers/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    //creating a ride request
    private void writeNewRideRequestPost(String userId, String username, String source, String destination, String pickupPoint){
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").child("rideRequests").push().getKey();

        RideRequestPost rideRequest = new RideRequestPost(userId, username, source, destination, pickupPoint);
        Map<String, Object> postValues = rideRequest.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/rideRequests/" + key, postValues);
        childUpdates.put("/user-posts/" + userId + "/rideRequests/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]
}
