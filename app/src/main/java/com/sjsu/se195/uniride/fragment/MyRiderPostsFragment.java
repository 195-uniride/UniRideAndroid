package com.sjsu.se195.uniride.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.sjsu.se195.uniride.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyRiderPostsFragment extends MyPostsFragment {


    public MyRiderPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return  databaseReference.child("user-posts").child(super.uID)
                .child("rideRequests");
    }

}
