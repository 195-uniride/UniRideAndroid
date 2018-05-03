package com.sjsu.se195.uniride.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Created by timhdavis on 5/2/18.
 */

public class WayPoint implements Parcelable {

    private String postId;
    private long pickupTime; // in Date milliseconds
    private int duration; // in seconds
    private int riderIndex;

    // Constructor:

    public WayPoint() {
        // Do nothing.
    }


    // Getters and setters:

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public long getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(long pickupTime) {
        this.pickupTime = pickupTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        String wayPointInfo = "Waypoint: \n";

        wayPointInfo += "postId = " + postId + "\n";
        wayPointInfo += "pickupTime = " + pickupTime + "\n";
        wayPointInfo += "duration = " + duration + "\n";
        wayPointInfo += "riderIndex = " + riderIndex + "\n";

        return wayPointInfo;
    }

    public int getRiderIndex() {
        return riderIndex;
    }

    public void setRiderIndex(int riderIndex) {
        this.riderIndex = riderIndex;
    }

    // Parcelable:

    /*
    private String postId;
    private long pickupTime; // in Date milliseconds
    private int duration; // in seconds
    private int riderIndex;
     */

    // Constructor for loading from a Parcel:
    public WayPoint(Parcel in) {

        // NOTE: order MUST be exactly the same as writeToParcel:

        this.postId = in.readString();

        this.pickupTime = in.readLong();

        this.duration = in.readInt();

        this.riderIndex = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {

        // NOTE: order MUST be exactly the same as Post(Parcel in):

        out.writeString(this.postId);

        out.writeLong(this.pickupTime);

        out.writeInt(this.duration);

        out.writeInt(this.riderIndex);

        // TODO: write areAllTripTimeLimitsSatisfied...
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<WayPoint> CREATOR
            = new Parcelable.Creator<WayPoint>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public WayPoint createFromParcel(Parcel in) {
            return new WayPoint(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public WayPoint[] newArray(int size) {
            return new WayPoint[size];
        }
    };
}
