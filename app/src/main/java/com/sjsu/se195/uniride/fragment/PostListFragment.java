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



    protected Query getAllDriveOfferPosts() {
        return mDatabase.child("organization-posts").child(getUserDefaultOrganizationId())
                .child("driveOffers");
    }

    protected Query getAllRideRequestPosts() {
        return mDatabase.child("organization-posts").child(getUserDefaultOrganizationId())
                .child("rideRequests");
    }
}
