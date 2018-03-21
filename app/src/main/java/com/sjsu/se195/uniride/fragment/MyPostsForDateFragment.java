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
        Query recentPostsQuery;

        // [START my_top_posts_query]
        // My top posts by number of stars
        String myUserId = getUid();
        recentPostsQuery = databaseReference.child("user-posts").child(myUserId)
                .orderByChild("starCount");
        // [END my_top_posts_query]

        return recentPostsQuery;
    }
}
