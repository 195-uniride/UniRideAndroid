package com.sjsu.se195.uniride;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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

              try {
                  writeNewCarpoolObject(carpool);
              } catch (ParseException e) {
                  e.printStackTrace();
              }

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

  private void writeNewCarpoolObject(Carpool carpool) throws ParseException {
      // Create new post at /user-posts/$userid/$postid and at
      // /posts/$postid simultaneously
      String key = mDatabase.child("posts").child("carpools").push().getKey();
      carpoolID = key;

      Map<String, Object> carpoolValues = carpool.toMap();

      Map<String, Object> childUpdates = new HashMap<>();
      childUpdates.put("/posts/carpools/" + key, carpoolValues);

      Map<String, String> carpoolValuesUser = carpool.userToMap("driver");
      childUpdates.put("/user-carpools/" + carpool.getDriverPost().uid + "/" + key, carpoolValuesUser);

      carpoolValuesUser = carpool.userToMap("rider");
      for (RideRequestPost post : carpool.getRiderPosts()) {
          childUpdates.put("/user-carpools/" + post.uid + "/" + key, carpoolValuesUser);
      }

      // TODO: childUpdates.put("/organization-posts/" + carpool.getDriverPost().orgID + "/rideRequests/" + key, postValues);

      mDatabase.updateChildren(childUpdates);

      Map<String, Object> childUpdates2 = new HashMap<>();

      Map<String, RideRequestPost> carpoolRiders = carpool.riderToMap();

      childUpdates2.put("/posts/carpools/" + key + "/riderposts", carpoolRiders);

      mDatabase.updateChildren(childUpdates2);

      //Now set the alarm for when the carpool is starting
      // lurker post is the that is the post of the current user
      System.out.println("This is the departure time: " + mLurkerPost.tripDate);

      AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

      //Getting the date and time for the alarm
      DateFormat date_format = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
      DateFormat time_format = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
      String date = Integer.toString(mLurkerPost.tripDate);
      String time = Integer.toString(mLurkerPost.departure_time);

      Date date_of_carpool = date_format.parse(date);
      Date time_of_carpool = time_format.parse(time);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date_of_carpool);
      System.out.println("***************************file - NewCarpoolActivity************************************");
      System.out.println("Month of carpool is: " + calendar.get(Calendar.MONTH));

      //Setting up the intent to start later for the alarm
      Intent intent = new Intent(NewCarpoolActivity.this, CarpoolActivity.class);

      //setting the up the intent to be called through the alarm
      PendingIntent pendingIntent = PendingIntent.getBroadcast(NewCarpoolActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      alarm.set(AlarmManager.RTC_WAKEUP, date_of_carpool.getTime() + time_of_carpool.getTime() , pendingIntent);

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
