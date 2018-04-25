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

      // get Intent data:
      postType = getIntent().getExtras().getBoolean("postType"); //postType of mSelectedPost

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

              if(getSelectedPostType()) {
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
              Intent intent = new Intent(NewCarpoolActivity.this, PostDetailActivity.class);
              intent.putExtra("typeOfPost", Post.PostType.CARPOOL.name());
              intent.putExtra("postType", false); // because required (incorrect, though).
              intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, carpool.postId);
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

      carpool.setCarpoolId(key);
      carpool.postId = key;

      Map<String, Object> carpoolValues = carpool.toMap();

      Map<String, Object> childUpdatesPosts = new HashMap<>();
      childUpdatesPosts.put("/posts/carpools/" + key, carpoolValues);

      mDatabase.updateChildren(childUpdatesPosts);

      Map<String, Object> childUpdatesOrganizationPosts = new HashMap<>();
      if (carpool.organizationId != null) {
          childUpdatesOrganizationPosts.put("/organization-posts/" + carpool.organizationId + "/driveOffers/" + key, carpoolValues);
      }
      mDatabase.updateChildren(childUpdatesOrganizationPosts);


      Map<String, Object> childUpdatesUserCarpool = new HashMap<>();
      Map<String, String> carpoolValuesUser = carpool.userToMap("driver");
      childUpdatesUserCarpool.put("/user-carpools/" + carpool.getDriverPost().uid + "/" + key, carpoolValuesUser);

      carpoolValuesUser = carpool.userToMap("rider");
      for (RideRequestPost post : carpool.getRiderPosts()) {
          childUpdatesUserCarpool.put("/user-carpools/" + post.uid + "/" + key, carpoolValuesUser);
      }

      mDatabase.updateChildren(childUpdatesUserCarpool);

      // TODO: childUpdates.put("/organization-posts/" + carpool.getDriverPost().orgID + "/rideRequests/" + key, postValues);

      // Also save the list of Riders:

      Map<String, Map<String, Object>> carpoolRiders = carpool.riderToMap();

      Map<String, Object> childUpdates_Posts_Riders = new HashMap<>();
      System.out.println("Saving CarpoolRiders to: " + "/posts/carpools/" + key + "/riderposts" + "...with carpoolRiders = " + carpoolRiders);
      childUpdates_Posts_Riders.put("/posts/carpools/" + key + "/riderposts", carpoolRiders);
      mDatabase.updateChildren(childUpdates_Posts_Riders);

      Map<String, Object> childUpdates_OrganizationPosts_Riders = new HashMap<>();
      System.out.println("Saving Carpool with organizationId = " + carpool.organizationId);
      if (carpool.organizationId != null) {
          System.out.println("Saving CarpoolRiders to : " + "/organization-posts/" + carpool.organizationId + "/driveOffers/" + key + "/riderposts");
          childUpdates_OrganizationPosts_Riders.put("/organization-posts/" + carpool.organizationId + "/driveOffers/" + key + "/riderposts", carpoolRiders);
      }
      mDatabase.updateChildren(childUpdates_OrganizationPosts_Riders);

  }

  private void addRider(Carpool carpool, RideRequestPost riderPost) {
      try {
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
