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
//            DatabaseReference userRef = mDatabase.child("users").child(getUid()).getRef();//("users")
//
//            System.out.println("find user email?? = " + (userRef.orderByValue().endAt("email")));
//            userRef.orderByValue().on("value", function(data) {
//
//                data.forEach(function(data) {
//                    console.log("The " + data.key + " rating is " + data.val());
//                });
//
//            });

            recentPostsQuery = databaseReference.child("organization-posts").child(getUserOrganizationId())
                    .child("rideRequests").limitToFirst(100);
        }
        // [END recent_posts_query]

        return recentPostsQuery;
    }
}
