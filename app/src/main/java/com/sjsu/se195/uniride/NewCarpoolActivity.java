package com.sjsu.se195.uniride;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.fragment.MyPostsForDateFragment;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.Toast;

public class NewCarpoolActivity extends BaseActivity { //AppCompatActivity {
  private boolean postType; //true = driverpost ; false = riderequest
  private Post mSelectedPost;
  private String mSelectedPostKey;
  private DatabaseReference mPostReference;
  private Post mUserPostKey;
  private Post mUserPost;
  private ValueEventListener mPostListener;
  private static final String TAG = "NewCarpoolActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {

      System.out.println("NewCarpoolActivity started.");
      // get Intent data:
      postType = getIntent().getExtras().getBoolean("postType");

      mSelectedPostKey = getIntent().getExtras().getString("postId");

      // set post database path:
      getPostReference(mSelectedPostKey, postType);

      // setup views:
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_new_carpool);

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

  private void showUserPostList() {
      // show list of user's posts for this day:
      Bundle bundle = new Bundle();
      bundle.putBoolean("postType", postType); // TODO: change back to "isRiderPost" after reformat MyPostsForDateFragment //TODO: what?
      bundle.putInt("date", mSelectedPost.tripDate);
      bundle.putString("driverPostKey", mSelectedPostKey); //TODO: why driverpostkey? shouldnt it be either driver or rider?
      Fragment posts = new MyPostsForDateFragment(); // TODO: create other class to inherit from.
      posts.setArguments(bundle);
      System.out.println("About to show MyPostsForDateFragment...");
      //display the fragment:
      getSupportFragmentManager().beginTransaction().add(R.id.my_post_for_date_fragment_placeholder, posts).commit();
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
              //System.out.println(dataSnapshot.toString());

              System.out.println("About to lookup selected post in Firebase...");

              if(postType){
                  RideRequestPost post = dataSnapshot.getValue(RideRequestPost.class);
                  // [START_EXCLUDE]
                  mSelectedPost = post;
                  System.out.println("Found post in Firebase.");
                  // TODO: setup views.
              }
              else{
                  DriverOfferPost post = dataSnapshot.getValue(DriverOfferPost.class);
                  // [START_EXCLUDE]
                  mSelectedPost = post;
                  System.out.println("Found post in Firebase.");
                  // TODO: setup views.
              }

              System.out.println("Found post: KEY = " + mSelectedPostKey);
              showUserPostList(); // now that we have the post we can show the list of user posts with this post's date.
              // [END_EXCLUDE]
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
              // Getting Post failed, log a message
              Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
              // [START_EXCLUDE]
              Toast.makeText(NewCarpoolActivity.this, "Failed to load post.",
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
    // Initialize Database
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
