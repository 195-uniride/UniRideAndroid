package com.sjsu.se195.uniride.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by akshat on 10/5/17.
 */

public class RideRequestPost extends Post {
    private LatLng pickuppoint;

    public RideRequestPost() {
        postType = PostType.RIDER;
    }

    // Constructors:

    public RideRequestPost(String uid, String author, String source, String destination
            , int departureTime, int arrivalTime, int tripDate){
        super(uid, author, source, destination, departureTime, arrivalTime, tripDate);

        postType = PostType.RIDER;
    }

    public RideRequestPost(LatLng point){
        this.pickuppoint = point;

        postType = PostType.RIDER;
    }

    // Firebase Mapping methods:

    public Map<String, Object> toMap_pickupPoint(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude", pickuppoint.latitude);
        result.put("longitude", pickuppoint.longitude);
        return result;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("postType", postType);
        result.put("organizationId", organizationId);
        result.put("postId", postId);
        result.put("author", author);
        result.put("source", source);
        result.put("destination", destination);
        result.put("departureTime", departureTime);
        result.put("arrivalTime", arrivalTime);
        result.put("tripDate", tripDate);
        result.put("starCount", starCount);
        result.put("stars", stars);
        return result;
    }


    // Parcelable methods:

    // Constructor for loading from a Parcel:
    public RideRequestPost(Parcel in) {

        super(in);

        this.pickuppoint = in.readParcelable(LatLng.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {

        super.writeToParcel(out, flags);

        out.writeParcelable(pickuppoint, flags);
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<RideRequestPost> CREATOR
            = new Parcelable.Creator<RideRequestPost>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public RideRequestPost createFromParcel(Parcel in) {
            return new RideRequestPost(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public RideRequestPost[] newArray(int size) {
            return new RideRequestPost[size];
        }
    };


    // To String:

    @Override
    public String toString() {
        String className = this.getClass().getSimpleName();

        String postString =  super.toString();
        if (this.pickuppoint == null) {
            postString += "   " + className + ".pickuppoint = null \n";
        }
        else {
            postString += "   " + className + ".pickuppoint = " + this.pickuppoint.toString() + " \n";
        }


        return postString;
    }

}
