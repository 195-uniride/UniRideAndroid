package com.sjsu.se195.uniride.models;

/**
 * Created by timhdavis on 5/2/18.
 */

public class WayPoint {

    private int postId;
    private int pickupTime;
    private int duration; // in seconds

    // Constructor:

    public WayPoint() {
        // Do nothing.
    }


    // Getters and setters:

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(int pickupTime) {
        this.pickupTime = pickupTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
