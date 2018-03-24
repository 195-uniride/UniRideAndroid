package com.sjsu.se195.uniride;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import com.sjsu.se195.uniride.models.User;
import com.sjsu.se195.uniride.models.Comment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PostDetailActivity extends MainActivity implements View.OnClickListener, OnMapReadyCallback{

    private static final String TAG = "PostDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";
    private boolean postType;

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private CommentAdapter mAdapter;

    private Post mPost;

    private TextView mAuthorView;
    private TextView mSourceView;
    private TextView mDestinationView;
    private EditText mCommentField;
    private Button mCommentButton;
    private FloatingActionButton mShowMapButton;
    private FloatingActionButton mCreateCarpoolButton;
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
        postType = getIntent().getExtras().getBoolean("postType");
        // Get post key from intent
        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (mPostKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        // Initialize Database
        if(postType){
            mPostReference = FirebaseDatabase.getInstance().getReference()
                    .child("posts").child("rideRequests").child(mPostKey);
            mCommentsReference = FirebaseDatabase.getInstance().getReference()
                    .child("post-comments").child(mPostKey);
        }else{
            mPostReference = FirebaseDatabase.getInstance().getReference()
                    .child("posts").child("driveOffers").child(mPostKey);
            mCommentsReference = FirebaseDatabase.getInstance().getReference()
                    .child("post-comments").child(mPostKey);
        }

        System.out.println(mPostReference.toString());
        /*mCommentsReference = FirebaseDatabase.getInstance().getReference()
                .child("post-comments").child(mPostKey);*/
        alpha_animation = AnimationUtils.loadAnimation(this, R.anim.alpha_anim);;
        // Initialize Views
        mAuthorView = (TextView) findViewById(R.id.post_author);
        mSourceView = (TextView) findViewById(R.id.post_source);
        mDestinationView = (TextView) findViewById(R.id.post_destination);
        mCommentField = (EditText) findViewById(R.id.field_comment_text);
        mCommentButton = (Button) findViewById(R.id.button_post_comment);
        mCommentsRecycler = (RecyclerView) findViewById(R.id.recycler_comments);
        final View mMapOverlay = (View) findViewById(R.id.map_overlay);

        mCommentButton.setOnClickListener(this);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));

            //marker for sjsu
        sjsu = new MarkerOptions()
                .position(new LatLng(37.335188, -121.881066))
                .title("SJSU")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.extra_icon));

        //MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        my_view = findViewById(R.id.for_map_layout);
        mCreateCarpoolButton = (FloatingActionButton) findViewById(R.id.fab_create_carpool);
        mShowMapButton = (FloatingActionButton) findViewById(R.id.fab_show_map);
        if(my_view.getVisibility()==View.VISIBLE){
            mShowMapButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white_48dp));
        }
        else{
            mShowMapButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map_white_48dp));
        }
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
                    mShowMapButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_close_white_48dp));
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

        mCreateCarpoolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PostDetailActivity.this, NewCarpoolActivity.class);
                intent.putExtra("isRiderPost", postType);
                intent.putExtra("postId", mPostKey); // for: FirebaseDatabase.getInstance().getReference().child("posts").child("rideRequests").child(mPostKey);

                System.out.println("Starting NewCarpoolActivity...");
                startActivity(intent);
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
                if(postType){
                    RideRequestPost post = dataSnapshot.getValue(RideRequestPost.class);
                    // [START_EXCLUDE]
                    mAuthorView.setText(post.author);
                    mSourceView.setText(post.source);
                    mDestinationView.setText(post.destination);
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

                    mPost = post;
                }
                else{
                    DriverOfferPost post = dataSnapshot.getValue(DriverOfferPost.class);
                    // [START_EXCLUDE]
                    mAuthorView.setText(post.author);
                    mSourceView.setText(post.source);
                    mDestinationView.setText(post.destination);
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

                    mPost = post;
                }
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
        mAdapter.cleanupListener();
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
