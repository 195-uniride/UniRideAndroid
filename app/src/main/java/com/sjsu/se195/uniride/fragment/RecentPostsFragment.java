package com.sjsu.se195.uniride.fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.Post;

public class RecentPostsFragment extends PostListFragment {

    public RecentPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery;

        // Load Ride Request Posts:
        if(mPostType == Post.PostType.RIDER) {
            recentPostsQuery = mDatabase.child("organization-posts").child(getSelectedOrganizationId())
                    .child("rideRequests").limitToFirst(100);
        }
        // Load Drive Offer Posts:
        else {

            recentPostsQuery = mDatabase.child("organization-posts").child(getSelectedOrganizationId())
                    .child("driveOffers").limitToFirst(100);
        }
        // [END recent_posts_query]

        return recentPostsQuery;
    }
}
