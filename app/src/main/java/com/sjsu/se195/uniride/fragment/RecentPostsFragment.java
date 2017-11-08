package com.sjsu.se195.uniride.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RecentPostsFragment extends PostListFragment {

    public RecentPostsFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery;

        if(!postType){
            recentPostsQuery = databaseReference.child("posts").child("driveOffers").limitToFirst(100);
        }
        else{
            recentPostsQuery = databaseReference.child("posts").child("rideRequests").limitToFirst(100);
        }
        // [END recent_posts_query]

        return recentPostsQuery;
    }
}
