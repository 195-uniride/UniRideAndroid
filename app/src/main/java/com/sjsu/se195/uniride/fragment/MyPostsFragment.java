package com.sjsu.se195.uniride.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sjsu.se195.uniride.R;
import com.yalantis.phoenix.PullToRefreshView;

public class MyPostsFragment extends PostListFragment {

    private PullToRefreshView mPullToRefreshView;

    protected DatabaseReference mDatabase;

    private RecyclerView mRecycler;

    public MyPostsFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);
        super.postType = false;
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
        super.mDatabase = mDatabase;
        super.mRecycler = mRecycler;
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        return  databaseReference.child("user-posts").child(getUid())
                .child("driveOffers");
    }
}
