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
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;

public class CarpoolDetailActivity extends AppCompatActivity {
    private static final String TAG = "CarpoolDetailActivity";
    private boolean isRiderPost;
    private Post mSelectedPost;
    private String mSelectedPostKey;
    private DatabaseReference mPostReference;
    private Post mUserPostKey;
    private Post mUserPost;
    private ValueEventListener mPostListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isRiderPost = getIntent().getExtras().getBoolean("isRiderPost");
        // if (isRiderPost) {
        //   mPost = (DriverOfferPost) getIntent().getExtras().get("post");
        // } else {
        //   mPost = (RideRequestPost) getIntent().getExtras().get("post");
        // }
        mSelectedPostKey = getIntent().getExtras().getString("postId");

        getPostReference(mSelectedPostKey, isRiderPost);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                System.out.println("Post source: " + mSelectedPost.source);
                System.out.println("Post dest: " + mSelectedPost.destination);
                System.out.println("Post is of type: " + mSelectedPost.getClass().getName());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                System.out.println(dataSnapshot.toString());
                if(isRiderPost){
                    RideRequestPost post = dataSnapshot.getValue(RideRequestPost.class);
                    // [START_EXCLUDE]
                    mSelectedPost = post;
                    // TODO: setup views.
                }
                else{
                    DriverOfferPost post = dataSnapshot.getValue(DriverOfferPost.class);
                    // [START_EXCLUDE]
                    mSelectedPost = post;
                    // TODO: setup views.
                }
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
        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        // mAdapter = new CommentAdapter(this, mCommentsReference);
        // mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // // Clean up comments listener
        // mAdapter.cleanupListener();
    }

    private void getPostReference(String postKey, boolean isRiderPost) {
      // Initialize Database // TODO: remove & change to just get carpool path. (do all of this in New Carpool Activity)
      if(isRiderPost){
          mPostReference = FirebaseDatabase.getInstance().getReference()
                  .child("posts").child("rideRequests").child(postKey);
          // mCommentsReference = FirebaseDatabase.getInstance().getReference()
          //         .child("post-comments").child(postKey);
      }else{
          mPostReference = FirebaseDatabase.getInstance().getReference()
                  .child("posts").child("driveOffers").child(postKey);
          // mCommentsReference = FirebaseDatabase.getInstance().getReference()
          //         .child("post-comments").child(postKey);
      }
    }


}
