package com.sjsu.se195.uniride.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.MainSubcategoryActivity;
import com.sjsu.se195.uniride.PostDetailActivity;
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import com.sjsu.se195.uniride.models.User;
import com.sjsu.se195.uniride.viewholder.PostViewHolder;

import java.util.ArrayList;
import java.util.Date;

public abstract class PostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";

    private User currentUser;

    // [START define_database_reference]
    protected DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    protected boolean postType; //true = driverpost ; false = riderequest

    public PostListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        postType = getArguments().getBoolean("postType");
        View rootView;

        rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);
        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        System.out.println("in Fragment onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        //postType = savedInstanceState.getBundle("postType");

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Get User Object and Set up FirebaseRecyclerAdapter with the Query:
        setCurrentUserAndLoadPosts();

    }


    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
    // [END post_stars_transaction]

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // TODO:
    public String getSelectedOrganizationId() {
        System.out.println("Getting selected org ID from MainSubcategoryActivity...");
        return ((MainSubcategoryActivity)getActivity()).getSelectedOrganizationId();
    }

    // TODO: change to throw error if user doesn't have a default organization:
    public String getUserDefaultOrganizationId() {

        String defaultUserOrganizationId = "";

        if (getCurrentUser() != null) {
            defaultUserOrganizationId = getCurrentUser().defaultOrganizationId;
        }

        if (defaultUserOrganizationId == null || defaultUserOrganizationId.equals("")) {
            System.out.println("ERROR: No default Org Id found for user: " + getCurrentUser());

            // TODO: show load page with no results and prompt user to choose a default organization.

            // WIP ONLY: For testing purposes: set an arbitrary Org Id: // TODO: REMOVE: FOR WIP STATE ONLY.
            defaultUserOrganizationId = "-L47q6ayVu4wPq23hnmm"; // "Marta's Organization"
            System.out.println("WIP ONLY: Setting default Org Id to arbitrary Id: " + defaultUserOrganizationId);
        }

        System.out.println("User's default Org Id = " + defaultUserOrganizationId);

        return defaultUserOrganizationId;
    }

    public abstract Query getQuery(DatabaseReference databaseReference);


    public void setCurrentUserAndLoadPosts() {

        // System.out.println("Starting to set user....");

        mDatabase.child("users").child(getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Need to get the user object before loading posts because the query to find posts requires user.

                // Get User object and use the values to update the UI
                currentUser = dataSnapshot.getValue(User.class);

                loadPosts();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //getUid()
    }

    private void loadPosts() {
        System.out.println("About to load posts....."); // TODO: investigate why fragment reload not calling again...
        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);

        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class, R.layout.item_post,
                PostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Post model, final int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        intent.putExtra("postType", postType);
                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                if (model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef;
                        DatabaseReference userPostRef;
                        if(!postType){
                            globalPostRef = mDatabase.child("posts").child("driveOffers").child(postRef.getKey());
                            userPostRef = mDatabase.child("user-posts").child(model.uid).child("driveOffers").child(postRef.getKey());
                        }
                        else{
                            globalPostRef = mDatabase.child("posts").child("rideRequests").child(postRef.getKey());
                            userPostRef = mDatabase.child("user-posts").child(model.uid).child("rideRequests").child(postRef.getKey());
                        }

                        // Run two transactions
                        onStarClicked(globalPostRef);
                        onStarClicked(userPostRef);
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
    }


    public User getCurrentUser() {
        return currentUser;
    }

    // ~~~~~~~~~~~~~~~~~~~~ SEARCHING: ~~~~~~~~~~~~~~~~~~~~
    // TODO: move to own fragment? SearchFragment (?)

    public ArrayList<Post> mSearchResultsPosts;

    // ==== SEARCH ALGORITHM:
    // parameters:
    // - userPost: the post that we are checking every other post against.
    // - user: the owner of the user post
    public void findSearchResults(Post userPost, User user) {

        // step 1: filter posts by type (drive offer or rider request): (in getAllPostsBySearchType)
        boolean isLookingForDriver = true;

        if (userPost instanceof DriverOfferPost) {
            isLookingForDriver = false;
        }
        else { // if (userPost instanceof RideRequestPost) {
            isLookingForDriver = true;
        }


        // step 2: filter posts by date: (orderByChild("tripDate").equalTo(userPost.tripDate))

        Query searchQuery = getAllPostsBySearchType(isLookingForDriver).orderByChild("tripDate").equalTo(userPost.tripDate); // TODO: add date field.

        if (isLookingForDriver) {
            findDriveOfferSearchResults(userPost, searchQuery);
        }
        else {
            findRideRequestSearchResults(userPost, searchQuery);
        }



        // step 3: filter posts by general area:

//        matchedPosts = filterPostsByGeneralArea(matchedPosts);

        // step 4: filter posts based on
        //  whether each participant can reach the destination on time or not:

//        matchedPosts = filterPostsByTimePossibility(matchedPosts);

        // return the matches:

        // list is set asynchronously.
    }

    // Search helper methods:

    private Query getAllPostsBySearchType(boolean isLookingForDriver) {
        if (isLookingForDriver) {
            return getAllDriveOfferPosts();
        }
        else {
            return getAllRideRequestPosts();
        }
    }


    private void findRideRequestSearchResults(final Post userPost, Query searchQuery) {
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // Handle each post:
                    RideRequestPost postToCheck = dataSnapshot.getValue(RideRequestPost.class);

                    if (isTripTimeWithinTimeLimit(userPost, postToCheck)) {
                        mSearchResultsPosts.add(postToCheck);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    private void findDriveOfferSearchResults(final Post userPost, Query searchQuery) {
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    // Handle each post:
                    DriverOfferPost postToCheck = dataSnapshot.getValue(DriverOfferPost.class);

                    if (isTripTimeWithinTimeLimit(userPost, postToCheck)) {
                        mSearchResultsPosts.add(postToCheck);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }

    /*
        Returns true if potential carpool trip time is within the time limits,
         where the potential carpool is a carpool including existingPost and newPostToCheck,
         and where being within the time limits means that the carpool trip time is
         less than the time between arrival time and destination time.
     */
    private boolean isTripTimeWithinTimeLimit(Post existingPost, Post newPostToCheck) {
        Carpool potentialCarpool = null;

        // Case 1: newPostToCheck is a Ride Request Post:
        if (newPostToCheck instanceof RideRequestPost) {
            // Check if existingPost is already a Carpool or not:
            if (existingPost instanceof Carpool) {
                // Duplicate carpool object because don't want to edit existing carpool object:
                potentialCarpool = new Carpool((Carpool) existingPost); // Use Copy Constructor to duplicate.
            }
            else if (existingPost instanceof DriverOfferPost) {
                potentialCarpool = new Carpool((DriverOfferPost) existingPost);
            }
            else {
                System.out.println("ERROR: Search:isTripTimeWithinTimeLimit with newPostToCheck=R.R. && existingPost NOT Carpool nor D.O.");
                return false;
            }

            return isAddingNewRiderPossible(potentialCarpool, (RideRequestPost) newPostToCheck);
        }
        // Case 2: newPostToCheck is a Drive Offer Post:
        else if (newPostToCheck instanceof DriverOfferPost) {
            if (!(existingPost instanceof RideRequestPost)) { // if existingPost NOT a Ride Request:
                System.out.println("ERROR: Search:isTripTimeWithinTimeLimit with newPostToCheck=D.O. && existingPost NOT R.R.");
                return false;
            }

            potentialCarpool = new Carpool((DriverOfferPost) newPostToCheck);

            return isAddingNewRiderPossible(potentialCarpool, (RideRequestPost) existingPost);
        }
        // Error Case:
        else {
            System.out.println("Search:isTripTimeWithinTimeLimit fell through to false.");
            return false;
        }
    }

    private boolean isAddingNewRiderPossible(Carpool potentialCarpool, RideRequestPost potentialNewRider) {
        try {
            potentialCarpool.addRider((RideRequestPost) potentialNewRider);

            // this method will calculate the distance of the trip and
            //  make sure for each participant that arrival and destination time are met:
            return potentialCarpool.areAllTripTimeLimitsSatisfied();
        } catch (Carpool.OverPassengerLimitException e) {
            return false; // If over passenger limit, then consider this NOT a match.
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~ SEARCHING end. ~~~~~~~~~~~~~~~~

    protected Query getAllDriveOfferPosts() {
        return mDatabase.child("organization-posts").child(getUserDefaultOrganizationId())
                .child("driveOffers");
    }

    protected Query getAllRideRequestPosts() {
        return mDatabase.child("organization-posts").child(getUserDefaultOrganizationId())
                .child("rideRequests");
    }
}
