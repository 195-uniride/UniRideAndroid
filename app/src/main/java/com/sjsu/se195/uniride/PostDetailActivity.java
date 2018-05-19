package com.sjsu.se195.uniride;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.fragment.RecentPostsFragment;
import com.sjsu.se195.uniride.fragment.RouteWayPointListFragment;
import com.sjsu.se195.uniride.fragment.SearchResultsPostListFragment;
import com.sjsu.se195.uniride.fragment.TripMapFragment;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import com.sjsu.se195.uniride.models.RouteWayPoint;
import com.sjsu.se195.uniride.models.User;
import com.sjsu.se195.uniride.models.Comment;
import com.sjsu.se195.uniride.models.WayPoint;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PostDetailActivity extends MainActivity
        implements View.OnClickListener, OnMapReadyCallback {

    private static final String TAG = "PostDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_POST_TYPE = "typeOfPost";
    public static final String EXTRA_POST_OBJECT = "post";

    // private boolean postType; // True = RideRequestPost, False = DirverOfferPost

    private DatabaseReference mPostReference;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private CommentAdapter mAdapter;

    private Post mPost;
    private Post.PostType mPostType = Post.PostType.UNKNOWN;

    private TextView mAuthorView;
    private TextView mSourceView;
    private TextView mDestinationView;
    private EditText mCommentField;
    private Button mCommentButton;
    private ImageButton delete_button;
    private FloatingActionButton mShowMapButton;
    private Button mCreateCarpoolButton;
    private Button mFindMatchingPostsButton;
    private RecyclerView mCommentsRecycler;
    View my_view;

    private GoogleMap m_map;
    private boolean mapReady;

    private Animation alpha_animation;

    //markers
    private MarkerOptions sjsu;
    private MarkerOptions source_marker;
    private MarkerOptions destination_marker;

    //latlng
    private LatLng source_latlng;
    private LatLng dest_latlng;

    //Outside class GMapV2
    private GMapV2Direction md = new GMapV2Direction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3_post_detail);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        //postType = getIntent().getExraBoo("postType");

        // Get type of post (RIDER, DRIVER, CARPOOL) from intent:
        try {
            mPostType = Post.PostType.valueOf(getIntent().getStringExtra(PostDetailActivity.EXTRA_POST_TYPE));
        }
        catch (NullPointerException ex) { // If this post does not have this postType attribute, set to UNKNOWN:
            mPostType = Post.PostType.UNKNOWN;
        }

        System.out.println("Setting Post to Type: " + mPostType);

        // Get post key from intent: If not sent Firebase key, check if sent Post object itself:
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null || mPostKey.equals("")) {
            mPost = getIntent().getParcelableExtra(PostDetailActivity.EXTRA_POST_OBJECT);

            if (mPost == null) {
                throw new IllegalArgumentException("PostDetailActivity: Must pass EXTRA_POST_KEY or Post Object");
            }
        }


        // Initialize Views
        mAuthorView = (TextView) findViewById(R.id.post_cardview_author_name);
        mSourceView = (TextView) findViewById(R.id.post_source);
        mDestinationView = (TextView) findViewById(R.id.post_destination);

        setupCommentSection();

        setupJoinButton();

        setupFindMatchingPostsButton();

        setupMapButton();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mPost == null) { // If post was not loaded directly from Intent.
            loadPostFromFirebase();
        }
        else { // If post was loaded directly from Intent:
            setupViewsForPost(mPost);

            setupPostRouteDescription(mPost);
        }

        // TODO: comment section if sent Post object...
    }

    private void loadWayPointList() {
        Bundle bundle = new Bundle();

        bundle.putParcelableArrayList(RouteWayPointListFragment.EXTRA_ROUTE_WAYPOINT_LIST, getRouteWayPoints());

        Fragment wayPointListFragment = new RouteWayPointListFragment();
        wayPointListFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.waypoint_list_fragment_placeholder, wayPointListFragment, "WayPointList").commit();
    }

    private ArrayList<RouteWayPoint> getRouteWayPoints() {
        ArrayList<RouteWayPoint> wayPoints = new ArrayList<RouteWayPoint>();

        // NOTE: Must add in reverse order:

        // Destination:

        RouteWayPoint wayPointDestination = new RouteWayPoint();
        wayPointDestination.type = "destination";
        wayPointDestination.text = "Reach destination";
        wayPointDestination.address = mPost.destination; // "N 10th St";
        wayPointDestination.time = PostInfo.getArrivalDateTimeText(mPost); // "9:00 AM";
        wayPoints.add(wayPointDestination);

        // Carpool passengers:
        if (mPostType == Post.PostType.CARPOOL) { // TODO: will need to reverse order...

            Carpool carpoolPost = (Carpool) mPost;



            for (WayPoint riderWayPoint : carpoolPost.getRiderWaypoints()) {

                RideRequestPost riderPost = carpoolPost.riderPosts.get(riderWayPoint.getRiderIndex());

                //=====
                RouteWayPoint passengerWayPoint = new RouteWayPoint();
                passengerWayPoint.type = "passenger";
                passengerWayPoint.text = "Pickup passenger";
                passengerWayPoint.participantName = riderPost.author; // "Sam B.";
                passengerWayPoint.address = riderPost.source; // "N 10th St";
                passengerWayPoint.time = PostInfo.getDepartureDateTimeText(riderPost); // "9:00 AM";
                wayPoints.add(passengerWayPoint);

            }
        }
        // TODO...

        // DRIVER:
        if (mPostType == Post.PostType.CARPOOL || mPostType == Post.PostType.DRIVER) {
            RouteWayPoint wayPoint1 = new RouteWayPoint();
            wayPoint1.type = "driver";
            wayPoint1.text = "Driver departs";
            wayPoint1.participantName = mPost.author; // "Sam B.";
            wayPoint1.address = mPost.source; // "N 10th St";
            wayPoint1.time = PostInfo.getDepartureDateTimeText(mPost); // "9:00 AM";
            wayPoints.add(wayPoint1);
        }
        else if (mPostType == Post.PostType.RIDER) {
            RouteWayPoint wayPoint1 = new RouteWayPoint();
            wayPoint1.type = "passenger";
            wayPoint1.text = "Pickup passenger";
            wayPoint1.participantName = mPost.author; // "Sam B.";
            wayPoint1.address = mPost.source; // "N 10th St";
            wayPoint1.time = PostInfo.getDepartureDateTimeText(mPost); // "9:00 AM";
            wayPoints.add(wayPoint1);
        }







        return wayPoints;
    }

    private void setupCommentSection() {
        mCommentField = (EditText) findViewById(R.id.field_comment_text);
        mCommentButton = (Button) findViewById(R.id.button_post_comment);
        mCommentsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);

        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupJoinButton() {
        mCreateCarpoolButton = findViewById(R.id.button_create_carpool);

        if(mPostType == Post.PostType.RIDER) {
            mCreateCarpoolButton.setText("Offer Ride");
        }
        else if(mPostType == Post.PostType.DRIVER) {
            mCreateCarpoolButton.setText("Request Ride");
        }
        else if(mPostType == Post.PostType.CARPOOL) {
            mCreateCarpoolButton.setText("Join Carpool");
        }

        /*
            Make button gone at first.
            After Post is loaded, make visible if post is NOT the user's post.
            (Can only join posts by other users):
         */
        mCreateCarpoolButton.setEnabled(false);
        mCreateCarpoolButton.setVisibility(View.INVISIBLE);

        // Set OnClick action:
        mCreateCarpoolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Starting NewCarpoolActivity...");

                Intent intent = new Intent(PostDetailActivity.this, NewCarpoolActivity.class);

                intent.putExtra(NewCarpoolActivity.EXTRA_POST_OBJECT, mPost);

                startActivity(intent);
            }
        });
    }



    private void setupFindMatchingPostsButton() {
        mFindMatchingPostsButton = findViewById(R.id.button_find_matching_posts);

        /*
            Make button gone at first.
            After Post is loaded, make visible if post IS the user's post.
            (Can only find matches for your own posts):
         */
        mFindMatchingPostsButton.setEnabled(false);
        mFindMatchingPostsButton.setVisibility(View.INVISIBLE);

        mFindMatchingPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println("..... SENDING POST FOR SEARCHING .....");
                System.out.println("..... Sending mPost = " + mPost + "; with mPost.source = " + mPost.source);

                Intent intent = new Intent(PostDetailActivity.this, SearchResultsActivity.class);
                intent.putExtra(SearchResultsActivity.EXTRA_POST_OBJECT, mPost);

                startActivity(intent);
            }
        });
    }


    private void setupPostRouteDescription(Post post) {

        // TODO:
        loadWayPointList();

        LinearLayout topLayout = findViewById(R.id.layout_top);

        if (mPostType == Post.PostType.DRIVER) {
            topLayout.setBackgroundColor(getResources().getColor(R.color.driveOfferColor));
        }
        else if (mPostType == Post.PostType.RIDER) {
            topLayout.setBackgroundColor(getResources().getColor(R.color.rideRequestColor));
        }
        else if (mPostType == Post.PostType.CARPOOL) {
            topLayout.setBackgroundColor(getResources().getColor(R.color.carpoolColor));
        }


//        TextView routeDescriptionText = findViewById(R.id.text_route_details);
//
//        routeDescriptionText.setText(PostInfo.getRouteDescription(post));
    }

    private void setupMapButton() {
        //Button MapLinkButton = findViewById(R.id.button_link_to_map);


//        Fragment tripMapFragment = new TripMapFragment();
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.map_fragment_placeholder, tripMapFragment, "TripMap").commit();


        setupMapButton_OLD();

        /*

        Launch Google Maps Navigation:

        mShowMapButton = (FloatingActionButton) findViewById(R.id.fab_show_map);

        mShowMapButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map_white_48dp));

        mShowMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println("..... LINKING TO MAP .....");

                if (mPost != null) {
                    String destinationAddressURL = mPost.destination.replaceAll(" ", "+");

                    //String destinationAddressURL = "Taronga+Zoo,+Sydney+Australia";
                    openGoogleMapsApp(destinationAddressURL); // DEBUG ONLY...// TODO: change...
                }

            }
        });
        */
    }

    /**
        Opens Google Maps application on the user's device
         and starts directions navigation to destinationAddressURL.
         @param destinationAddressURL the destination address. All spaces must be replaced with '+'.
     */
    private void openGoogleMapsApp(String destinationAddressURL) {
        // Create a Uri from an intent string. Use the result to create an Intent.

        Uri gmmIntentUri = Uri.parse("google.navigation:q="+destinationAddressURL); // TODO: change...

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);

        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        // Attempt to start an activity that can handle the Intent
        // Checks if user has an application that can handle the request:
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Log.e(PostDetailActivity.TAG, "Cannot open Google Maps application. "
                    + "There is no application available on this device that can process this request.");
        }
    }

    private void setupMapButton_OLD() {
        alpha_animation = AnimationUtils.loadAnimation(this, R.anim.alpha_anim);;

        final View mMapOverlay = (View) findViewById(R.id.map_overlay);

        //marker for sjsu
        sjsu = new MarkerOptions()
                .position(new LatLng(37.335188, -121.881066))
                .title("SJSU")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.extra_icon));

        //MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        my_view = findViewById(R.id.for_map_layout);

        mShowMapButton = (FloatingActionButton) findViewById(R.id.fab_show_map);
        mShowMapButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map_white_48dp));

        mShowMapButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                // get the center for the clipping circle
                int cx = Math.round(mShowMapButton.getX() + (mShowMapButton.getWidth())/2);
                int cy = Math.round(mShowMapButton.getY() + (mShowMapButton.getHeight())/2);

                // get the final radius for the clipping circle
                float finalRadius1 = (float) Math.hypot(cx, cy);
                // create the animator for this view (the start radius is zero)

                if(my_view.getVisibility() == View.INVISIBLE){
                    mShowMapButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white_48dp));
                    Animator anim1 = ViewAnimationUtils.createCircularReveal(my_view, cx, cy, 0, finalRadius1);
                    anim1.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                        /*mMapOverlay.animate().alpha(0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mMapOverlay.setVisibility(View.GONE);
                            }
                        });*/
                            mMapOverlay.setVisibility(View.GONE);
                            mMapOverlay.startAnimation(alpha_animation);
                            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                            mapFragment.getMapAsync(PostDetailActivity.this);
                        }
                    });
                    // make the view visible and start the animation
                    my_view.setVisibility(View.VISIBLE);
                    anim1.start();
                }
                else{
                    mShowMapButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map_white_48dp));
                    Animator anim1 = ViewAnimationUtils.createCircularReveal(my_view, cx, cy, finalRadius1, 0);
                    anim1.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                            mMapOverlay.animate().alphaBy(1f).setDuration(300);
                            mMapOverlay.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            my_view.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    // make the view visible and start the animation
                    anim1.start();
                }
            }
        });
    }

    private void loadPostFromFirebase() {

        // Initialize Database
        if(mPostType == Post.PostType.RIDER) {
            mPostReference = FirebaseDatabase.getInstance().getReference()
                    .child("posts").child("rideRequests").child(mPostKey);
            mCommentsReference = FirebaseDatabase.getInstance().getReference()
                    .child("post-comments").child(mPostKey);
        }
        else if(mPostType == Post.PostType.DRIVER) {
            mPostReference = FirebaseDatabase.getInstance().getReference()
                    .child("posts").child("driveOffers").child(mPostKey);
            mCommentsReference = FirebaseDatabase.getInstance().getReference()
                    .child("post-comments").child(mPostKey);
        }
        else if(mPostType == Post.PostType.CARPOOL) {
            mPostReference = FirebaseDatabase.getInstance().getReference()
                    .child("posts").child("carpools").child(mPostKey);
            mCommentsReference = FirebaseDatabase.getInstance().getReference()
                    .child("post-comments").child(mPostKey);
        }
        else {
            Log.e(PostDetailActivity.TAG, "ERROR: mPostType = " + mPostType);
        }




        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                System.out.println(dataSnapshot.toString());

                if(mPostType == Post.PostType.RIDER) {
                    final RideRequestPost post = dataSnapshot.getValue(RideRequestPost.class);
                    // [START_EXCLUDE]
                    if (post != null && (post.postType == null || post.postType == Post.PostType.UNKNOWN)) {
                        post.postType = Post.PostType.RIDER; // Set post type if wasn't present in databse.
                    }

                    if(post != null) {
                        mPost = post;
                        setupViewsForPost(post);
                        setupPostRouteDescription(post);
                        Log.w(TAG, "---> The current logged user id: " + getUid() + ", .....the id of post is: " + post.postId);
                    }

                    //check if the post is by the current user
                    if(post != null && post.uid.equals(getUid().toString())){
                        Log.w(TAG, "---> The current logged in user made this");
                        //initialize the delete button
                        delete_button = findViewById(R.id.delete_post);
                        delete_button.setVisibility(View.VISIBLE);
                        delete_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                initializeDelete(post);
                            }
                        });
                    }
                }
                else if(mPostType == Post.PostType.DRIVER) {
                    final DriverOfferPost post = dataSnapshot.getValue(DriverOfferPost.class);
                    // [START_EXCLUDE]
                    if (post != null && (post.postType == null || post.postType == Post.PostType.UNKNOWN)) {
                        post.postType = Post.PostType.DRIVER; // Set post type if wasn't present in databse.
                    }

                    if(post != null) {
                        mPost = post;
                        setupViewsForPost(post);
                        setupPostRouteDescription(post);
                        Log.w(TAG, "---> The current logged user id: " + getUid() + ", .....the id of post is: " + post.uid);
                    }

                    //check if the post is by the current user
                    if(post != null && post.uid.equals(getUid().toString())){
                        Log.w(TAG, "---> The current logged in user made this");
                        //initialize the delete button
                        delete_button = findViewById(R.id.delete_post);
                        delete_button.setVisibility(View.VISIBLE);
                        delete_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                initializeDelete(post);
                            }
                        });
                    }
                }
                else if(mPostType == Post.PostType.CARPOOL) {
                    Carpool post = dataSnapshot.getValue(Carpool.class);
                    // [START_EXCLUDE]
                    if (post != null && (post.postType == null || post.postType == Post.PostType.UNKNOWN)) {
                        post.postType = Post.PostType.CARPOOL; // Set post type if wasn't present in databse.
                    }

                    System.out.println("LOADING A CARPOOL OBJECT: post = " + post);

                    mPost = post;
                    if(post!=null) {
                        setupViewsForPost(post);
                        setupPostRouteDescription(post);
                        Log.w(TAG, "---> The current logged user id: " + getUid() + ", .....the id of post is: " + post.uid);
                    }//setupCarpoolRouteDescription(post);
                }

                if (mPost.postId == null || mPost.postId.isEmpty()) {
                    mPost.postId = mPostKey; // Set the post's key as ID if wasn't stored in database.
                }

                System.out.println("PostDetailActivity: Loaded mPost: \n" + mPost.toString());

                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(PostDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener
        if (mAdapter != null) {
            mAdapter.cleanupListener();
        }

    }

    private void setupViewsForPost(Post post) {

        mSourceView.setText(post.source);
        mDestinationView.setText(post.destination);

        TextView postTripDateText = findViewById(R.id.post_date);
        postTripDateText.setText(PostInfo.getTripDateText(post));

        TextView postDateTimeText = findViewById(R.id.post_time);
        postDateTimeText.setText("Arrive at " + PostInfo.getArrivalDateTimeText(post));

        TextView toText = findViewById(R.id.post_card_address_to);
        toText.setText("to");

        // Set FindMatches or Join button:
        setFindMatchesOrJoinButton(getUid(), post);

        setPostAuthor();

        // TODO: Fix:
        source_latlng = md.getLocationFromAddress(PostDetailActivity.this, post.source);
        dest_latlng = md.getLocationFromAddress(PostDetailActivity.this, post.destination);
        source_marker = new MarkerOptions()
                .position(new LatLng(source_latlng.latitude, source_latlng.longitude))
                .title("Source")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_48dp));
        destination_marker = new MarkerOptions()
                .position(new LatLng(dest_latlng.latitude, dest_latlng.longitude))
                .title("Destination")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_black_48dp));
        if(post instanceof Carpool){

        }
    }


    private void setPostAuthor() {

        // Need to get user:
        DatabaseReference postUserReference =
                FirebaseDatabase.getInstance().getReference().child("users").child(mPost.uid);

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
                Toast.makeText(PostDetailActivity.this, "Failed to load user.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        });
    }

    private void setFindMatchesOrJoinButton(String userId, Post post) {
        if (post.uid != null) {
            if (post.uid.equals(userId) && mPostType != Post.PostType.CARPOOL) {
                // Make FindMatches button active:
                mFindMatchingPostsButton.setEnabled(true);
                mFindMatchingPostsButton.setVisibility(View.VISIBLE);
            }
            else if (!post.uid.equals(userId)) {
                // Make Join button active:
                mCreateCarpoolButton.setEnabled(true);
                mCreateCarpoolButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_post_comment) {
            postComment();
        }
    }

    private void postComment() {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;

                        // Create new comment object
                        String commentText = mCommentField.getText().toString();
                        Comment comment = new Comment(uid, authorName, commentText);

                        // Push the comment, it will appear in the list
                        mCommentsReference.push().setValue(comment);

                        // Clear the field
                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    //Initializing the delete button
    private void initializeDelete(Post post){
        //If a rider, only need to delete the instance of the post from all the places
        if(post.postType == Post.PostType.RIDER){
            System.out.println("Delete ride request: " + post.postId);
            delete_post(post, "rideRequests");
        }
        //If driver, need to delete the instance, and the carpool objects the driver is part of
        else if(post.postType == Post.PostType.DRIVER){
            System.out.println("Delete drive offer request: " + post.postId);
            //delete post instance
            delete_post(post, "driveOffers");
            //delete carpool instance
            //delete_carpool(post);
        }
    }

    //Deleting methods
    private void delete_post(Post post, String drive_or_ride){
        String post_org = post.organizationId;
        Log.w(TAG, "About to delete all the posts instances for the post, " + post.postId + ", by user: " + post.uid);
        mDatabaseReference.child("organization-posts").child(post_org).child(drive_or_ride).child(post.postId).removeValue();
        mDatabaseReference.child("posts").child(drive_or_ride).child(post.postId).removeValue();
        mDatabaseReference.child("user-posts").child(getUid().toString()).child(drive_or_ride).child(post.postId).removeValue();
        mDatabaseReference.child("post-comments").child(post.postId).removeValue();
//        Intent intent = new Intent(PostDetailActivity.this, ProfilePageActivity.class);
//        startActivity(intent);
//        //finishActivity(0);
        //Finish this activity
        finish();
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView authorView;
        public TextView bodyView;

        public CommentViewHolder(View itemView) {
            super(itemView);

            authorView = (TextView) itemView.findViewById(R.id.comment_author);
            bodyView = (TextView) itemView.findViewById(R.id.comment_body);
        }
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    Comment comment = dataSnapshot.getValue(Comment.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Replace with the new data
                        mComments.set(commentIndex, newComment);

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    String commentKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        // Remove data from the list
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.authorView.setText(comment.author);
            holder.bodyView.setText(comment.text);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }

    //Mandatory method when creating a map is involved
    @Override
    public void onMapReady(GoogleMap map){
        mapReady = true;
        m_map = map;
        if(source_marker != null)
            m_map.addMarker(source_marker);
        if(destination_marker != null)
            m_map.addMarker(destination_marker);
        //LatLng city = new LatLng(37.3394, -121.8938);
        //CameraPosition target = CameraPosition.builder().target(city).zoom(14).build();
        //m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

        //Here the method that draws polylines gets called
        try {
            if(source_latlng != null && dest_latlng != null)
                md.drawDirections(source_latlng, dest_latlng, m_map);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(source_marker != null && destination_marker != null)
            setCamera();
    }

    //to animate when moving to a new location
    public void flyTo(CameraPosition target){
        m_map.animateCamera(CameraUpdateFactory.newCameraPosition(target));
    }

    //Sets the camera for the view of the map
    private void setCamera(){
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(source_marker.getPosition());
        builder.include(destination_marker.getPosition());
        System.out.println(source_marker.getPosition());
        System.out.println(destination_marker.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels, newLatLngBounds(LatLngBounds, int, int, int)
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        int zoomLevel = 0;
        m_map.moveCamera(cu);
    }

}
