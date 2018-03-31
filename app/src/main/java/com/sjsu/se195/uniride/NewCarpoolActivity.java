package com.sjsu.se195.uniride;

import android.content.Intent;
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
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class NewCarpoolActivity extends BaseActivity { //AppCompatActivity {
  private boolean postType; //false = driverpost ; true = riderequest
  private Post mSelectedPost;
  private Post mLurkerPost;
  private String mSelectedPostKey;
  private DatabaseReference mDatabase;
  private DatabaseReference mPostReference;
  String carpoolID;
  private ValueEventListener mPostListener;
  private static final String TAG = "NewCarpoolActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {

      System.out.println("NewCarpoolActivity started.");
      // get Intent data:
      postType = getIntent().getExtras().getBoolean("postType"); //postType of mSelectedPost
      System.out.println("NewCarpoolActivity line47 postType = " + postType);

      mSelectedPostKey = getIntent().getExtras().getString("postId");

      mDatabase = FirebaseDatabase.getInstance().getReference();
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
      bundle.putBoolean("postType", getLurkerPostType()); // TODO: change back to "isRiderPost" after reformat MyPostsForDateFragment //pushing postType of mlurkerpost
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

              if(getSelectedPostType()) {
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
    if(getSelectedPostType()) {
        mPostReference = mDatabase.child("posts").child("rideRequests").child(postKey);
        // mCommentsReference = FirebaseDatabase.getInstance().getReference()
        //         .child("post-comments").child(postKey);
    }else{
        mPostReference = mDatabase.child("posts").child("driveOffers").child(postKey);
        // mCommentsReference = FirebaseDatabase.getInstance().getReference()
        //         .child("post-comments").child(postKey);
    }
  }

  //Here we make the new carpool object and send that thing
  public void createCarpoolObject(DatabaseReference mLurkerPostReference){
      ValueEventListener postListener = new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
              // Get Post object and use the values to update the UI

              System.out.println("datasnapshot from setriderpostsandcreatecarpool" + dataSnapshot.toString());

              Carpool carpool;
              if (getSelectedPostType()) { // mselected = rider; lurker must be driver. postType = mselected.type (true)
                mLurkerPost = dataSnapshot.getValue(DriverOfferPost.class);
                carpool = new Carpool((DriverOfferPost) mLurkerPost);
                addRider(carpool, (RideRequestPost) mSelectedPost);
              }
              else {// mselected = driver; lurker must be rider
                mLurkerPost = dataSnapshot.getValue(RideRequestPost.class);
                carpool = new Carpool((DriverOfferPost) mSelectedPost);
                addRider(carpool, (RideRequestPost) mLurkerPost);
              }

              writeNewCarpoolObject(carpool);

              // Create the Carpool object:
              Intent intent = new Intent(NewCarpoolActivity.this, CarpoolDetailActivity.class);
              intent.putExtra("carpoolID", carpoolID);
              startActivity(intent);
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
              // Getting Post failed, log a message
              Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
          }
      };
      mLurkerPostReference.addValueEventListener(postListener);
  }

  private void writeNewCarpoolObject(Carpool carpool) {
      // Create new post at /user-posts/$userid/$postid and at
      // /posts/$postid simultaneously
      String key = mDatabase.child("posts").child("carpools").push().getKey();
      carpoolID = key;

      Map<String, Object> carpoolValues = carpool.toMap();

      Map<String, Object> childUpdates = new HashMap<>();
      childUpdates.put("/posts/carpools/" + key, carpoolValues);

      childUpdates.put("/user-carpools/" + carpool.getDriverPost().uid + "/" + key, carpoolValues);

      for (RideRequestPost post : carpool.getRiderPosts()) {
          childUpdates.put("/user-carpools/" + post.uid + "/" + key, carpoolValues);
      }

      // TODO: childUpdates.put("/organization-posts/" + carpool.getDriverPost().orgID + "/rideRequests/" + key, postValues);

      mDatabase.updateChildren(childUpdates);

      System.out.println("line227 NewCarpoolActivity");
      Map<String, Object> childUpdates2 = new HashMap<>();

      Map<String, RideRequestPost> carpoolRiders = carpool.riderToMap();

      System.out.println("carpoolRiders = " + carpoolRiders);

      childUpdates2.put("/posts/carpools/" + key + "/riderposts", carpoolRiders);
      childUpdates2.put("/user-carpools/" + carpool.getDriverPost().uid + "/" + key + "/riderposts", carpoolRiders);

      for (RideRequestPost post : carpool.getRiderPosts()) {
          childUpdates.put("/user-carpools/" + post.uid + "/riderposts" + key, carpoolRiders);
      }

      System.out.println("line236 NewCarpoolActivity");
      mDatabase.updateChildren(childUpdates2);

  }

  private void addRider(Carpool carpool, RideRequestPost riderPost) {
      try {
          System.out.println("adding rider ID = " + riderPost.uid);
          carpool.addRider(riderPost); // get from PostRef
      }
      catch (Carpool.OverPassengerLimitException ex) {
          // tell user can't join because carpool is full.
          System.out.println(ex.getMessage());
          ex.printStackTrace();
      }
  }

  private boolean getSelectedPostType() {
    return postType;
  }

  private boolean getLurkerPostType() {
    return !postType;
  }

}
