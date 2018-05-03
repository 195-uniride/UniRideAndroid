package com.sjsu.se195.uniride.fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.Post;

public class MyPostsForDateFragment extends PostListFragment {

    public MyPostsForDateFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentUserPostsQuery;

        // TODO: limit to only show user's posts....(Switch to using PotentialCarpoolListFragment...)
        if (mPostType == Post.PostType.RIDER) { // if(!postType){
            recentUserPostsQuery = mDatabase.child("organization-posts").child(getSelectedOrganizationId())
                    .child("rideRequests").orderByChild("tripDate").equalTo(getSelectedTripDate());
            //databaseReference.child("posts").child("rideRequests").orderByChild("uid").equalTo(userKey).limitToFirst(10);
            //databaseReference.child("posts").child("rideRequests").orderByChild("uid").equalTo(userKey).limitToFirst(10);
        }
        else{
            recentUserPostsQuery = mDatabase.child("organization-posts").child(getSelectedOrganizationId())
                    .child("driveOffers").orderByChild("tripDate").equalTo(getSelectedTripDate());
            // databaseReference.child("posts").child("driveOffers").orderByChild("uid").equalTo(userKey).limitToFirst(10);
        }
        // [END recent_posts_query]

        return recentUserPostsQuery;
    }
}
