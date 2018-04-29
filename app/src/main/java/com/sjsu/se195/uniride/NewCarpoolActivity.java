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
import com.sjsu.se195.uniride.fragment.PostListFragment;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class NewCarpoolActivity extends MainActivity
{
    private static final String TAG = "NewCarpoolActivity";
    public static final String EXTRA_POST_OBJECT = "NewCarpoolActivity.post";
    private Post mSelectedPostToJoin;
    private Post mPostJoining;

    private Post.PostType mPostTypeOfPostJoining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      // setup views:
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_new_carpool);

      // Get post object from intent:
      mSelectedPostToJoin = getIntent().getParcelableExtra(NewCarpoolActivity.EXTRA_POST_OBJECT);

      if (mSelectedPostToJoin == null) {
          throw new IllegalArgumentException(TAG + ": Must pass EXTRA_POST_OBJECT");
      }

      setPostTypeOfPostJoining();

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

      bundle.putString(PostListFragment.EXTRA_POST_TYPE, mPostTypeOfPostJoining.name());

      bundle.putInt(PostListFragment.EXTRA_TRIP_DATE, mSelectedPostToJoin.tripDate);

      // bundle.putString("driverPostKey", mSelectedPostKey);

      Fragment myPostsForDateFragment = new MyPostsForDateFragment();
        myPostsForDateFragment.setArguments(bundle);
      //display the fragment:
      getSupportFragmentManager().beginTransaction()
              .add(R.id.my_post_for_date_fragment_placeholder, myPostsForDateFragment).commit();
    }

    @Override
    public void onStart() {
      super.onStart();
    }

    @Override
    public void onStop() {
      super.onStop();

      // // Clean up comments listener
      // mAdapter.cleanupListener();
    }


    private void setPostTypeOfPostJoining() {
        if (mSelectedPostToJoin.postType == Post.PostType.RIDER) {
            mPostTypeOfPostJoining = Post.PostType.DRIVER;
        }
        else if (mSelectedPostToJoin.postType == Post.PostType.DRIVER
                || mSelectedPostToJoin.postType == Post.PostType.CARPOOL) {
            mPostTypeOfPostJoining = Post.PostType.RIDER;
        }
        else {
            throw new IllegalArgumentException(TAG + ": mSelectedPostToJoin.postType = " + mSelectedPostToJoin.postType);
        }
    }

    // Need to get reference because is only a Post object in PostListFragment (not a Post subclass):
    public void createCarpoolObject(DatabaseReference postJoiningReference) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                Carpool potentialCarpool;

                if (mSelectedPostToJoin.postType == Post.PostType.RIDER) { // mselected = rider; lurker must be driver. postType = mselected.type (true)
                    mPostJoining = dataSnapshot.getValue(DriverOfferPost.class);
                    potentialCarpool = new Carpool((DriverOfferPost) mPostJoining);
                    addRider(potentialCarpool, (RideRequestPost) mPostJoining);
                }
                else {// mselected = driver; lurker must be rider
                    mPostJoining = dataSnapshot.getValue(RideRequestPost.class);
                    potentialCarpool = new Carpool((DriverOfferPost) mPostJoining);
                    addRider(potentialCarpool, (RideRequestPost) mPostJoining);
                }

                //writeNewCarpoolObject(carpool);

                // Create the Carpool object:
                Intent intent = new Intent(NewCarpoolActivity.this, PreviewCarpoolDetailActivity.class);
                intent.putExtra("typeOfPost", Post.PostType.CARPOOL.name());

                intent.putExtra(PreviewCarpoolDetailActivity.EXTRA_CARPOOL_OBJECT, potentialCarpool);

                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        postJoiningReference.addValueEventListener(postListener);
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
}
