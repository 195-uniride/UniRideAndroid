package com.sjsu.se195.uniride;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.fragment.SearchResultsPostListFragment;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import com.sjsu.se195.uniride.models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timhdavis on 4/1/18.
 *
 * Classes that implement PostSearchResultsListener
 *  and add themselves as a listener to an object of this class type
 *  can use onSearchResultsFound to get search results.
 */


public class PostSearcher {

    private List<PostSearchResultsListener> listeners = new ArrayList<>();

    private static final String TAG = "PostSearcher";
    private DatabaseReference mDatabase;

    public ArrayList<Post> mSearchResultsPosts;

    public ArrayList<Carpool> mPotentialCarpools;

    // Constructor:

    public PostSearcher(DatabaseReference databaseReference) {
        mDatabase = databaseReference;
        mSearchResultsPosts = new ArrayList<>();
        mPotentialCarpools = new ArrayList<>();
    }

    // Listener Pattern:
    public void addListener(PostSearchResultsListener listenerToAdd) {
        listeners.add(listenerToAdd);
    }

    // ==== SEARCH ALGORITHM:
    // parameters:
    // - userPost: the post that we are checking every other post against.
    // - user: the owner of the user post
    public void findSearchResults(Post userPost) {

        // step 1: filter posts by type (drive offer or rider request): (in getAllPostsBySearchType)
        boolean isLookingForDriver = true;

        if (userPost instanceof DriverOfferPost) {
            isLookingForDriver = false;
        }
        else if (userPost instanceof RideRequestPost) {
            isLookingForDriver = true;
        }
        else {
            System.out.println("ERROR: userPost instanceof != R.R. or D.O...userPost.getClass() = " + userPost.getClass());
        }

        System.out.println("Search: userPost.getClass() = " + userPost.getClass());
        System.out.println("Search:isLookingForDriver = " + isLookingForDriver);

        // step 2: filter posts by date: (orderByChild("tripDate").equalTo(userPost.tripDate))
        Query searchQuery = getAllPostsBySearchType(userPost, isLookingForDriver).orderByChild("tripDate").equalTo(userPost.tripDate);

        findPostSearchResults(userPost, searchQuery, isLookingForDriver);

        // list is set asynchronously.
    }

    // Search helper methods:

    private Query getAllPostsBySearchType(Post userPost, boolean isLookingForDriver) {
        if (isLookingForDriver) {
            //return getAllDriveOfferPosts();
            // TODO: return mDatabase.child("organization-posts").child(userPost.organizationId).child("driveOffers");
            return mDatabase.child("posts").child("driveOffers"); // TEMP
        }
        else {
            // TODO: return mDatabase.child("organization-posts").child(userPost.organizationId).child("driveOffers");
            return mDatabase.child("posts").child("rideRequests"); // TEMP
        }
    }


    private void findPostSearchResults(final Post userPost, Query searchQuery, final boolean isLookingForDriver) {
        System.out.println("findRideRequestSearchResults with searchQuery = " + searchQuery);
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot postListSnapshot) {

                // System.out.println("Line102:postLISTSnapshot = " + postListSnapshot);

                int postCount = 0;

                for (DataSnapshot postSnapshot: postListSnapshot.getChildren()) {

                    // System.out.println("Line108:postSnapshot = " + postSnapshot);

                    // Handle each post:
                    Post postToCheck;

                    if (isLookingForDriver) {
                        postToCheck = (DriverOfferPost) postSnapshot.getValue(DriverOfferPost.class);
                    }
                    else {
                        postToCheck = (RideRequestPost) postSnapshot.getValue(RideRequestPost.class);

                        // System.out.println("Rider-postToCheck: uid = " + postToCheck.uid);
                        // System.out.println("Rider-postToCheck: source = " + postToCheck.source);
                        // System.out.println("Rider-postToCheck: author = " + postToCheck.author);
                    }

                    System.out.println("Search: ---- Looking at postToCheck: " + postToCheck +
                            " with key = " + postSnapshot.getKey() + " ----");

                    if (isTripTimeWithinTimeLimit(userPost, postToCheck)) {
                        System.out.println("Search: post IS a match: " + postToCheck +
                                " with key = " + postSnapshot.getKey() + " ----");
                        // Add to list of matching posts:
                        mSearchResultsPosts.add(postToCheck);
                    }
                    else {
                        System.out.println("Search: post NOT a match: " + postToCheck +
                                " with key = " + postSnapshot.getKey() + " ----");
                    }
                    postCount++;

                    System.out.println("---- ... ----");
                }

                System.out.println("--- DONE SEARCHING ---");

                System.out.println("Search: Looked through " + postCount + " post(s).");

                System.out.println("Search: Found " + mSearchResultsPosts.size() + " matching post(s).");

                for (Post post : mSearchResultsPosts) {
                    System.out.println("Search: found match: post = " + post);
                }


                for (Carpool carpool : mPotentialCarpools) {
                    System.out.println("Search: created a potential carpool = " + carpool + "...");
                    System.out.println("...from source @ " + carpool.getDriverPost().source + "...");

                    for (RideRequestPost riderPost : carpool.getRiderPosts()) {
                        System.out.println("...to pickup rider @ " + riderPost.source + "...");
                    }

                    System.out.println("...to destination @ " + carpool.getDriverPost().destination + "...");
                }

                // Sort Search Results by trip time:

                // TODO: mPotentialCarpools.sort();

                // Show Results:

                notifyDoneSearching();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }



    /*
        Returns true if potential carpool trip time is within the time limits,
         where the potential carpool is a carpool including existingPost and newPostToCheck,
         and where being within the time limits means that the carpool trip time is
         less than the time between arrival time and destination time.
     */
    private boolean isTripTimeWithinTimeLimit(Post existingPost, Post newPostToCheck) {
        Carpool potentialCarpool = null;

        System.out.println("existingPost = " + existingPost + "; newPostToCheck = " + newPostToCheck);

        // Case 1: newPostToCheck is a Ride Request Post:
        if (newPostToCheck instanceof RideRequestPost) {
            // Check if existingPost is already a Carpool or not:
            if (existingPost instanceof Carpool) {
                // Duplicate carpool object because don't want to edit existing carpool object:
                potentialCarpool = new Carpool((Carpool) existingPost); // Use Copy Constructor to duplicate.
                System.out.println("existingPost instanceof Carpool");
            }
            else if (existingPost instanceof DriverOfferPost) {
                potentialCarpool = new Carpool((DriverOfferPost) existingPost);
                System.out.println("existingPost instanceof DriverOfferPost");
            }
            else {
                System.out.println("ERROR: Search:isTripTimeWithinTimeLimit with newPostToCheck=R.R. && existingPost NOT Carpool nor D.O.");
                return false;
            }

            return isAddingNewRiderPossible(potentialCarpool, (RideRequestPost) newPostToCheck);
        }
        // Case 2: newPostToCheck is already a Carpool:
        else if (newPostToCheck instanceof Carpool) {
            System.out.println("newPostToCheck instanceof Carpool");
            // Duplicate carpool object because don't want to edit existing carpool object:
            potentialCarpool = new Carpool((Carpool) newPostToCheck); // Use Copy Constructor to duplicate.

            if (!(existingPost instanceof RideRequestPost)) { // if existingPost NOT a Ride Request:
                System.out.println("ERROR: Search:isTripTimeWithinTimeLimit with newPostToCheck=Carpool && existingPost NOT R.R.");
                return false;
            }

            return isAddingNewRiderPossible(potentialCarpool, (RideRequestPost) existingPost);
        }
        // Case 3: newPostToCheck is a Drive Offer Post:
        else if (newPostToCheck instanceof DriverOfferPost) {
            System.out.println("newPostToCheck instanceof DriverOfferPost");

            if (!(existingPost instanceof RideRequestPost)) { // if existingPost NOT a Ride Request:
                System.out.println("ERROR: Search:isTripTimeWithinTimeLimit with newPostToCheck=D.O. && existingPost NOT R.R.");
                return false;
            }

            potentialCarpool = new Carpool((DriverOfferPost) newPostToCheck);

            return isAddingNewRiderPossible(potentialCarpool, (RideRequestPost) existingPost);
        }
        // Error Case:
        else {
            System.out.println("ERROR: Search:isTripTimeWithinTimeLimit: newPostToCheck was not a Rider, Driver, or Carpool Post.");
            return false;
        }
    }

    private boolean isAddingNewRiderPossible(Carpool potentialCarpool, RideRequestPost potentialNewRider) {
        System.out.println("checking isAddingNewRiderPossible...");
        try {
            potentialCarpool.addRider(potentialNewRider);

            // this method will calculate the distance of the trip and
            //  make sure for each participant that arrival and destination time are met:
            if (potentialCarpool.areAllTripTimeLimitsSatisfied()) {
                System.out.println("...CAN add rider because potentialCarpool.areAllTripTimeLimitsSatisfied() = TRUE.");

                mPotentialCarpools.add(potentialCarpool); // Add to a list to use later.

                return true;
            }
            else {
                System.out.println("...CAN'T add rider because potentialCarpool.areAllTripTimeLimitsSatisfied() = FALSE.");
                return false;
            }
        } catch (Carpool.OverPassengerLimitException e) {
            System.out.println("...CAN'T add rider because over passenger limit: MESSAGE = " + e.getMessage());
            return false; // If over passenger limit, then consider this NOT a match.
        }
    }


    private void notifyDoneSearching() {
        for (PostSearchResultsListener listener : listeners) {
            listener.onSearchResultsFound(mSearchResultsPosts, mPotentialCarpools);
        }
    }

//    // TODO:
//    private void loadPosts() { // TODO: Do this in a Listener??? And make the Activity do this OnSearchFinished...
//
//    }

}
