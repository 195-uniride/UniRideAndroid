package com.sjsu.se195.uniride;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import com.sjsu.se195.uniride.models.User;

import java.util.ArrayList;

/**
 * Created by timhdavis on 4/1/18.
 */

public class PostSearcher {

    private static final String TAG = "PostSearcher";
    private DatabaseReference mDatabase;

    public ArrayList<Post> mSearchResultsPosts;

    // Constructor:

    public PostSearcher(DatabaseReference databaseReference) {
        mDatabase = databaseReference;
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
        // TODO: fix trip date:
        Query searchQuery = getAllPostsBySearchType(userPost, isLookingForDriver);//TODO: .orderByChild("tripDate").equalTo(userPost.tripDate); // TODO: add date field.

        findPostSearchResults(userPost, searchQuery, isLookingForDriver);

        // step 3: filter posts by general area:

//        matchedPosts = filterPostsByGeneralArea(matchedPosts);

        // step 4: filter posts based on
        //  whether each participant can reach the destination on time or not:

//        matchedPosts = filterPostsByTimePossibility(matchedPosts);

        // return the matches:

        // list is set asynchronously.
    }

    // Search helper methods:

    private Query getAllPostsBySearchType(Post userPost, boolean isLookingForDriver) {
        if (isLookingForDriver) {
            //return getAllDriveOfferPosts();
            // TODO: return mDatabase.child("organization-posts").child(post.organizationId).child("driveOffers");
            return mDatabase.child("posts").child("driveOffers"); // TEMP: TODO: NEED TO SPECIFY POST ID, or will just return a list of id's and can't get a RR.class in the lookup.
        }
        else {
            // TODO: return mDatabase.child("organization-posts").child(post.organizationId).child("driveOffers");
            return mDatabase.child("posts").child("rideRequests"); // TEMP
        }
    }


    private void findPostSearchResults(final Post userPost, Query searchQuery, final boolean isLookingForDriver) {
        System.out.println("findRideRequestSearchResults with searchQuery = " + searchQuery);
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot postListSnapshot) {

                System.out.println("Line102:postLISTSnapshot = " + postListSnapshot);

                int postCount = 0;

                for (DataSnapshot postSnapshot: postListSnapshot.getChildren()) {

                    System.out.println("Line108:postSnapshot = " + postSnapshot);

                    // Handle each post:
                    Post postToCheck;
                    if (isLookingForDriver) {
                        postToCheck = (DriverOfferPost) postSnapshot.getValue(DriverOfferPost.class);
                    }
                    else {
                        postToCheck = (RideRequestPost) postSnapshot.getValue(RideRequestPost.class);

                        System.out.println("Rider-postToCheck: uid = " + postToCheck.uid);
                        System.out.println("Rider-postToCheck: source = " + postToCheck.source);
                        System.out.println("Rider-postToCheck: author = " + postToCheck.author);
                    }

                    System.out.println("postToCheck = " + postToCheck);

                    if (isTripTimeWithinTimeLimit(userPost, postToCheck)) {
                        mSearchResultsPosts.add(postToCheck);
                    }
                    postCount++;
                }
                System.out.println("Search: postCount = " + postCount);


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
        // Case 2: newPostToCheck is a Drive Offer Post:
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
            System.out.println("Search:isTripTimeWithinTimeLimit fell through to false.");
            return false;
        }
    }

    private boolean isAddingNewRiderPossible(Carpool potentialCarpool, RideRequestPost potentialNewRider) {
        System.out.println("isAddingNewRiderPossible");
        try {
            potentialCarpool.addRider(potentialNewRider);

            // this method will calculate the distance of the trip and
            //  make sure for each participant that arrival and destination time are met:
            return potentialCarpool.areAllTripTimeLimitsSatisfied();
        } catch (Carpool.OverPassengerLimitException e) {
            return false; // If over passenger limit, then consider this NOT a match.
        }
    }

}
