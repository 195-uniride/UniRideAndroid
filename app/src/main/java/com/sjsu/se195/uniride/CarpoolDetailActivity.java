package com.sjsu.se195.uniride;

import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;

public class CarpoolDetailActivity extends AppCompatActivity {
    private static final String TAG = "CarpoolDetailActivity";
    private Carpool mCarpool;
    private String carpoolId;
    private Post mUserPostKey;
    private Post mUserPost;
    DriverOfferPost driver;
    private DatabaseReference mCarpoolReference;
    private ValueEventListener mCarpoolListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        carpoolId = getIntent().getExtras().getString("carpoolID");

        getPostReference(carpoolId);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool_detail);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                System.out.println("Driver post source: " + driver.source);
                System.out.println("Driver post is of type: " + driver.getClass().getName());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener carpoolListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                System.out.println(dataSnapshot.toString());
                Carpool carpool = dataSnapshot.getValue(Carpool.class);
                // [START_EXCLUDE]
                mCarpool = carpool;
                driver = mCarpool.getDriverPost();
                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(CarpoolDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mCarpoolReference.addValueEventListener(carpoolListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mCarpoolListener = carpoolListener;

        // Listen for comments
        // mAdapter = new CommentAdapter(this, mCommentsReference);
        // mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mCarpoolListener != null) {
            mCarpoolReference.removeEventListener(mCarpoolListener);
        }

        // // Clean up comments listener
        // mAdapter.cleanupListener();
    }

    private void getPostReference(String carpoolKey) {
      // Initialize Database // TODO: remove & change to just get carpool path. (do all of this in New Carpool Activity)
        mCarpoolReference = FirebaseDatabase.getInstance().getReference()
                .child("posts").child("carpools").child(carpoolKey);
    }


}
