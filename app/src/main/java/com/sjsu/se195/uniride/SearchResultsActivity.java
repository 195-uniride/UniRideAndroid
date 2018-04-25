package com.sjsu.se195.uniride;

/**
 * Created by timhdavis on 4/15/18.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.FirebaseDatabase;
import com.sjsu.se195.uniride.fragment.SearchResultsPostListFragment;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.Post;

import java.util.ArrayList;


public class SearchResultsActivity extends BaseActivity implements PostSearchResultsListener {

    private static final String TAG = "SearchResultsActivity";

    private Post mPost;

    private ProgressBar loadingIndicator;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_results);

        loadingIndicator = (ProgressBar)findViewById(R.id.loadingIndicator);
        loadingIndicator.setIndeterminate(false);
        startLoadingSpinAnimation();

        // Get Post object from Intent extras:
        mPost = getIntent().getExtras().getParcelable("post");

        if (mPost == null) {
            System.out.println("ERROR: ==== CANNOT START SEARCH ====; mPost = " + mPost);
        }
        else {


            System.out.println("==== STARTING SEARCH ====");
            System.out.println("=== Searching with mPost = " + mPost + "; with mPost.source = " + mPost.source);



            PostSearcher searcher = new PostSearcher(FirebaseDatabase.getInstance().getReference());

            searcher.findSearchResults(mPost); // Note: asynchronous function. Use onSearchResultsFound to get results.

            searcher.addListener(SearchResultsActivity.this);
        }
    }


    @Override
    public void onSearchResultsFound(ArrayList<Post> searchResults, ArrayList<Carpool> potentialCarpools) {

        stopLoadingSpinAnimation();

        // Load Posts:
        System.out.println("....About to show SearchResultsPostListFragment ...");
        Bundle bundle = new Bundle();

        System.out.println("....Sending bundle with searchResults = " + searchResults);
        // Add bundle arguments:
        bundle.putParcelableArrayList("searchResults", searchResults);
        bundle.putParcelableArrayList("potentialCarpoolResults", potentialCarpools);

        Fragment searchResultsFragment = new SearchResultsPostListFragment();
        searchResultsFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().add(R.id.post_fragment_placeholder, searchResultsFragment, "PostsList").commit();
    }

    public void startLoadingSpinAnimation() {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    public void stopLoadingSpinAnimation() {
        loadingIndicator.setVisibility(View.GONE);
    }

}


