package com.sjsu.se195.uniride.fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Post;
import com.yalantis.phoenix.PullToRefreshView;

public abstract class MyPostsFragment extends PostListFragment {

    private PullToRefreshView mPullToRefreshView;

    protected DatabaseReference mDatabase;

    protected String uID;

    private RecyclerView mRecycler;

    private String DRIVER_TAB_TITLE;
    private String RIDER_TAB_TITLE;
    private TextView mTabTitle;
    private LinearLayout linearLayout;

    public MyPostsFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        linearLayout = rootView.findViewById(R.id.fragment_all_posts_linearlayout);
        this.uID = getArguments().getString("uID");
        String firstname = getArguments().getString("userName");
        DRIVER_TAB_TITLE = firstname +  "'s Driver Posts";
        RIDER_TAB_TITLE = firstname  + "'s Rider Posts";
        System.out.println("uID: " + uID);

        if(this instanceof MyDriverPostsFragment){
            this.createTitle(this.DRIVER_TAB_TITLE);
            mPostType = Post.PostType.DRIVER; // super.postType = false;
        }
        else{
            this.createTitle(this.RIDER_TAB_TITLE);
            mPostType = Post.PostType.RIDER; // super.postType = true;
        }
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

    private void createTitle(String title){
        mTabTitle = new TextView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        mTabTitle.setLayoutParams(layoutParams);
        mTabTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mTabTitle.setPadding(10, 10, 10, 10);
        mTabTitle.setText(title);
        linearLayout.addView(mTabTitle, 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.mDatabase = this.mDatabase;
        super.mRecycler = this.mRecycler;
        super.onActivityCreated(savedInstanceState);
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}
