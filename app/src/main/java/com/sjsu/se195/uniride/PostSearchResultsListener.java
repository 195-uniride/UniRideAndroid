package com.sjsu.se195.uniride;

import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.Post;

import java.util.ArrayList;

/**
 * Created by timhdavis on 4/15/18.
 */

public interface PostSearchResultsListener {
    void onSearchResultsFound(ArrayList<Post> searchResults, ArrayList<Carpool> potentialCarpools);
}
