package com.sjsu.se195.uniride.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sjsu.se195.uniride.PostDetailActivity;
import com.sjsu.se195.uniride.R;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import com.sjsu.se195.uniride.viewholder.OnItemClickListener;
import com.sjsu.se195.uniride.viewholder.PostListRecyclerAdapter;
import com.sjsu.se195.uniride.viewholder.PotentialCarpoolListRecyclerAdapter;

import java.util.ArrayList;

public class SearchResultsPostListFragment extends Fragment implements OnItemClickListener {

    private static final String TAG = "SearchResultsPostListFragment";

    private RecyclerView mRecycler;
    private PotentialCarpoolListRecyclerAdapter mAdapter;
    private LinearLayoutManager mManager;

    private ArrayList<Post> mPostList;
    private ArrayList<Carpool> mPotentialCarpools;

    public SearchResultsPostListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Get list of Post objects to display:
        mPostList = getArguments().getParcelableArrayList("searchResults");

        mPotentialCarpools = getArguments().getParcelableArrayList("potentialCarpoolResults");

        System.out.println("in SearchResultsPostListFragment: just got mPostList = " + mPostList);

        View rootView;

        rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        mRecycler = rootView.findViewById(R.id.messages_list);

        mRecycler.setHasFixedSize(true);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        System.out.println("in Fragment onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        //postType = savedInstanceState.getBundle("postType");

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        loadPosts();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mAdapter != null) {
//            // mAdapter.cleanup(); // TODO?
//        }
    }


    private void loadPosts() {
        System.out.println("About to load posts.....");

        // With Posts:
        // mAdapter = new PostListRecyclerAdapter(mPostList);

        // With Potential Carpool objects:
        mAdapter = new PotentialCarpoolListRecyclerAdapter(mPotentialCarpools);

        mRecycler.setAdapter(mAdapter);

        mAdapter.setClickListener(this);

    }

    @Override
    public void onClick(View view, int position) {
        // Get post clicked from position:
        Post postClicked = mPostList.get(position);

        System.out.println("postClicked = " + postClicked + " from " + postClicked.source);

        // Launch PostDetailActivity
        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, "");
        intent.putExtra("postType", (postClicked instanceof RideRequestPost));
        intent.putExtra("post", postClicked);
        startActivity(intent);
    }
}
