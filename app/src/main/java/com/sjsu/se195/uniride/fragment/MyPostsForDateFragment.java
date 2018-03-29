package com.sjsu.se195.uniride.fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MyPostsForDateFragment extends PostListFragment {

    public MyPostsForDateFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentUserPostsQuery;

        String userKey = getUid();
        if(!postType){
            recentUserPostsQuery = databaseReference.child("posts").child("driveOffers").orderByChild("uid").equalTo(userKey).limitToFirst(10);
        }
        else{
            recentUserPostsQuery = databaseReference.child("posts").child("rideRequests").orderByChild("uid").equalTo(userKey).limitToFirst(10);
        }
        // [END recent_posts_query]

        return recentUserPostsQuery;
    }
}
