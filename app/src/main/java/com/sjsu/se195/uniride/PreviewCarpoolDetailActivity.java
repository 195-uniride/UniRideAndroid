package com.sjsu.se195.uniride;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import com.sjsu.se195.uniride.models.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PreviewCarpoolDetailActivity extends MainActivity {

    private static final String TAG = "PreviewCarpool";
    public static final String EXTRA_CARPOOL_OBJECT = "PreviewCarpoolDetailActivity.carpool";

    private Carpool mPotentialCarpool;

    private TextView mAuthorView;
    private TextView mSourceView;
    private TextView mDestinationView;
    private TextView routeDescriptionText;
    private Button mConfirmCarpoolButton;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preview_carpool_detail);

        // Get Carpool object from intent:
        mPotentialCarpool = getIntent().getParcelableExtra(PreviewCarpoolDetailActivity.EXTRA_CARPOOL_OBJECT);

        if (mPotentialCarpool == null) {
            throw new IllegalArgumentException(TAG + ": Must pass EXTRA_CARPOOL_OBJECT");
        }

        // Get Firebase reference:
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Views:
        mAuthorView = (TextView) findViewById(R.id.post_cardview_author_name);
        mSourceView = (TextView) findViewById(R.id.post_source);
        mDestinationView = (TextView) findViewById(R.id.post_destination);
        routeDescriptionText = findViewById(R.id.text_route_details);

        mConfirmCarpoolButton = findViewById(R.id.button_confirm_carpool);

        // Start with the confirmation button disabled:
        mConfirmCarpoolButton.setEnabled(false); // Wait until determined that the carpool is possible.

        mConfirmCarpoolButton.setOnClickListener(new View.OnClickListener() {

            // TODO: save Carpool to database!

            @Override
            public void onClick(View view) {
                try {
                    writeNewCarpoolObject(mPotentialCarpool);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        setupViewsForPost(mPotentialCarpool);

        if (mPotentialCarpool.areAllTripTimeLimitsSatisfied()) {
            // If carpool is possible, let user confirm carpool:

            routeDescriptionText.setText("Preview Potential Carpool: \n"
                    + PostInfo.getRouteDescription(mPotentialCarpool));

            mConfirmCarpoolButton.setEnabled(true);
        }
        else {
            // Tell user that carpool is impossible:
            routeDescriptionText.setText("Preview Potential Carpool: \n\n"
                    + "[!] Due to incompatible time constraints, the carpool is not possible:"
                + "\n\n" + PostInfo.getRouteDescription(mPotentialCarpool));

            mConfirmCarpoolButton.setEnabled(false);
        }
    }


    @Override
    public void onStop() {
        super.onStop();

    }

    private void setupViewsForPost(Post post) {
        if (post.author != null) {
            mAuthorView.setText(post.author);
        }
        mSourceView.setText(post.source);
        mDestinationView.setText(post.destination);

        TextView toText = findViewById(R.id.post_card_address_to);
        toText.setText("to");

        TextView postTripDateText = findViewById(R.id.post_date);
        postTripDateText.setText(PostInfo.getTripDateText(post));

        TextView postDateTimeText = findViewById(R.id.post_time);
        postDateTimeText.setText("Arrive at " + PostInfo.getArrivalDateTimeText(post));

        setPostAuthor();

        // TODO: Fix:
//        source_latlng = md.getLocationFromAddress(PostDetailActivity.this, post.source);
//        dest_latlng = md.getLocationFromAddress(PostDetailActivity.this, post.destination);
//        source_marker = new MarkerOptions()
//                .position(new LatLng(source_latlng.latitude, source_latlng.longitude))
//                .title("Source")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_48dp));
//        destination_marker = new MarkerOptions()
//                .position(new LatLng(dest_latlng.latitude, dest_latlng.longitude))
//                .title("Destination")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_48dp));
    }

//    @Override
//    public void onClick(View v) {
//        int i = v.getId();
//        if (i == R.id.button_post_comment) {
//            postComment();
//        }
//    }


    private void setPostAuthor() {

        // Need to get user:
        DatabaseReference postUserReference =
                FirebaseDatabase.getInstance().getReference().child("users").child(mPotentialCarpool.uid);

        postUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Organization object and use the values to update the UI
                User postUser = dataSnapshot.getValue(User.class);

                mAuthorView.setText(UserInformation.getShortName(postUser));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Organization failed, log a message
                Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(PreviewCarpoolDetailActivity.this, "Failed to load user.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        });
    }

    // Saves the new carpool object to the databse:
    private void writeNewCarpoolObject(Carpool carpool) throws ParseException {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("posts").child("carpools").push().getKey();

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

        // Add under user-carpool with user role: (necessary?):

        Map<String, Object> childUpdatesUserCarpool = new HashMap<>();
        Map<String, String> carpoolValuesUser = carpool.userToMap("driver");
        childUpdatesUserCarpool.put("/user-carpools/" + carpool.getDriverPost().uid + "/" + key, carpoolValuesUser);

        carpoolValuesUser = carpool.userToMap("rider");
        for (RideRequestPost post : carpool.getRiderPosts()) {
            childUpdatesUserCarpool.put("/user-carpools/" + post.uid + "/" + key, carpoolValuesUser);
        }

        mDatabase.updateChildren(childUpdatesUserCarpool);

        //make an alarm
        make_alarm(carpool);

        // Now do Intent to newly-created PostDetailActivity (of carpool post).

        // Launch PostDetailActivity
        Intent intent = new Intent(PreviewCarpoolDetailActivity.this, PostDetailActivity.class);

        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, key);
        intent.putExtra(PostDetailActivity.EXTRA_POST_TYPE, Post.PostType.CARPOOL.name());

        startActivity(intent);


        // Also save the list of Riders: (DON'T NEED TO: will automatically save with new carpool method.

//        Map<String, Map<String, Object>> carpoolRiders = carpool.riderToMap();

//        Map<String, Object> childUpdates_Posts_Riders = new HashMap<>();
//        System.out.println("Saving CarpoolRiders to: " + "/posts/carpools/" + key + "/riderposts" + "...with carpoolRiders = " + carpoolRiders);
//        childUpdates_Posts_Riders.put("/posts/carpools/" + key + "/riderposts", carpoolRiders);
//        mDatabase.updateChildren(childUpdates_Posts_Riders);

//        Map<String, Object> childUpdates_OrganizationPosts_Riders = new HashMap<>();
//        System.out.println("Saving Carpool with organizationId = " + carpool.organizationId);
//        if (carpool.organizationId != null) {
//            System.out.println("Saving CarpoolRiders to : " + "/organization-posts/" + carpool.organizationId + "/driveOffers/" + key + "/riderposts");
//            childUpdates_OrganizationPosts_Riders.put("/organization-posts/" + carpool.organizationId + "/driveOffers/" + key + "/riderposts", carpoolRiders);
//        }
//        mDatabase.updateChildren(childUpdates_OrganizationPosts_Riders);

    }

    //This method is making the alarm on the device on the date of the trip
    public void make_alarm(Carpool carpool) throws ParseException {
        AlarmManager alarm = (AlarmManager) PreviewCarpoolDetailActivity.this.getSystemService(Context.ALARM_SERVICE);

        //Getting the date and time for the alarm
        DateFormat date_and_time_format = new SimpleDateFormat("yyyyMMdd hhmm", Locale.ENGLISH);
        //adding 100 because the months seems to be behind a value
        // (even with the fact that their index starts from 0)

        //Currently the driver's info is being used to set the alarm
        int departure_time = carpool.getDriverPost().departureTime;
        int tripDate = carpool.getDriverPost().tripDate;

        String date_and_time = Integer.toString(tripDate + 100)+ " " + Integer.toString(departure_time);
        Date date_and_time_of_carpool = date_and_time_format.parse(date_and_time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date_and_time_of_carpool);

        Log.w(TAG,"***************************file - NewCarpoolActivity************************************");
        System.out.println("Month of carpool is: " + calendar.get(Calendar.MONTH));
        System.out.println("The alarm will be set for: " + date_and_time_of_carpool);

        //Setting up the intent to start later for the alarm
        Intent intent = new Intent(PreviewCarpoolDetailActivity.this, AlarmListener.class);

        //setting the up the intent to be called through the alarm
        PendingIntent pendingIntent = PendingIntent.getBroadcast(PreviewCarpoolDetailActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }
}
