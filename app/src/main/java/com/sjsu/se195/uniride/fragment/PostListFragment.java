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
import android.widget.Toast;
import android.widget.ImageView;

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
import com.sjsu.se195.uniride.NewCarpoolActivity;
import com.sjsu.se195.uniride.PostDetailActivity;
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.UserInformation;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.User;
import com.sjsu.se195.uniride.viewholder.PostViewHolder;
import com.yalantis.phoenix.PullToRefreshView;

public abstract class PostListFragment extends Fragment {

    private static final String TAG = "PostListFragment";
    public static final String EXTRA_POST_TYPE = "postType";
    public static final String EXTRA_ORGANIZATION_ID = "organizationId";

    public static final String EXTRA_TRIP_DATE = "tripDate";

    private User currentUser;
    private User postUser;
    private PullToRefreshView mPullToRefreshView;

    protected DatabaseReference mDatabase;
    protected DatabaseReference mUserReference;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    protected RecyclerView mRecycler;
    protected LinearLayoutManager mManager;
    // protected boolean postType; //true = driverpost ; false = riderequest
    protected Post.PostType mPostType;
    private String username;

    private String mSelectedOrganizationId;

    private int mTripDate = -1;

    public PostListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // postType = getArguments().getBoolean(PostListFragment.EXTRA_POST_TYPE);
        // System.out.println("PostListFragment line67 PostType = " + postType);

        mPostType = Post.PostType.valueOf(getArguments().getString(PostListFragment.EXTRA_POST_TYPE));

        mSelectedOrganizationId = getArguments().getString(PostListFragment.EXTRA_ORGANIZATION_ID);

        // May pass trip date as a filter:
        mTripDate = getArguments().getInt(PostListFragment.EXTRA_TRIP_DATE);

        View rootView;
        rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        mPullToRefreshView = (PullToRefreshView) rootView.findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setOrientation(LinearLayoutManager.VERTICAL);
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
//        System.out.println("Getting selected org ID from MainSubcategoryActivity...");
        if (getActivity() instanceof MainSubcategoryActivity) {
            return ((MainSubcategoryActivity)getActivity()).getSelectedOrganizationId();
        }
        else {
            /*
                DO NOT USE: mSelectedOrganizationId will not be updated by OrgSpinner.
                 Make sure to add additional if-statement if add another Activity that
                 uses this fragment, and have it return the currently
                 selected organization ID.
             */
            return mSelectedOrganizationId;
        }



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

                System.out.println("model.postType = " + model.postType);
                if (model.postType != null && model.postType != Post.PostType.UNKNOWN) {
                    mPostType = model.postType;
                }

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);

                        intent.putExtra("typeOfPost", model.postType.name());

                        startActivity(intent);
                    }
                });

//                String uid = model.uid;

//                mUserReference = mDatabase.child("users").child(uid);
//                String username = getPostUser();
//                if(username == null) {
//                    username = "#" + uid.substring(uid.length()-5);
//                }


                // Determine if the current user has liked this post and set UI accordingly
                /*if (model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }*/

                // Need to get user:
                DatabaseReference postUserReference =
                        FirebaseDatabase.getInstance().getReference().child("users").child(model.uid);

                postUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get Organization object and use the values to update the UI
                        User postUser = dataSnapshot.getValue(User.class);


                        //username = "";//UserInformation.getShortName(postUser);
                        username = UserInformation.getShortName(postUser);
//                        if (model.author != null) {
//                            username = model.author;
//                        }

                        // Bind Post to ViewHolder, setting OnClickListener for the star button
                        viewHolder.bindToPost(username, model.postType, model);

                        /*
                        , new View.OnClickListener() {
                            @Override
                            public void onClick(View starView) {
                                // Need to write to both places the post is stored
                                DatabaseReference globalPostRef;
                                DatabaseReference userPostRef;
                                if (mPostType == Post.PostType.DRIVER) { //if(!postType){
                                    globalPostRef = mDatabase.child("posts").child("driveOffers").child(postRef.getKey());
                                    userPostRef = mDatabase.child("user-posts").child(model.uid).child("driveOffers").child(postRef.getKey());
                                }
                                else {
                                    globalPostRef = mDatabase.child("posts").child("rideRequests").child(postRef.getKey());
                                    userPostRef = mDatabase.child("user-posts").child(model.uid).child("rideRequests").child(postRef.getKey());
                                }

                                // Run two transactions
                                onStarClicked(globalPostRef);
                                onStarClicked(userPostRef);
                            }
                        });
                         */
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
                    }
                });



            }

        };
        mRecycler.setAdapter(mAdapter);
    }

//    public User getCurrentUser() {
//        return currentUser;
//    }


    protected int getSelectedTripDate() {
        return mTripDate;
    }
}
