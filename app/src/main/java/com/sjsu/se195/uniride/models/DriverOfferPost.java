package com.sjsu.se195.uniride.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maninder on 10/4/17.
 */

public class DriverOfferPost extends Post implements Parcelable {

    public int passengerCount;

    public DriverOfferPost() {
        postType = PostType.DRIVER;
    }

    // Constructors:

    public DriverOfferPost(String uid, String author, String source, String destination, int pasCount,
                           int departureTime, int arrivalTime, int tripDay){
        super(uid, author, source, destination, departureTime, arrivalTime, tripDay);
        this.passengerCount = pasCount;

        this.postType = PostType.DRIVER;
    }

    public DriverOfferPost(String source, String destination, int pasCount, int departureTime, int arrivalTime, int tripDay){
        super(source, destination, departureTime, arrivalTime, tripDay);
        this.passengerCount = pasCount;

        this.postType = PostType.DRIVER;
    }

    // Getters and Setters:

    public int getPassengerCount() { return passengerCount; }

    // Firebase Mapping methods:

    public  Map<String, Object> toMap(){
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
        result.put("passengerCount", passengerCount);
        return result;
    }

    // Parcelable methods:

    // Constructor for loading from a Parcel:
    public DriverOfferPost(Parcel in) {

        super(in);

        this.passengerCount = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {

        super.writeToParcel(out, flags);

        out.writeInt(this.passengerCount);
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<DriverOfferPost> CREATOR
            = new Parcelable.Creator<DriverOfferPost>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public DriverOfferPost createFromParcel(Parcel in) {
            return new DriverOfferPost(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public DriverOfferPost[] newArray(int size) {
            return new DriverOfferPost[size];
        }
    };

    // To String:

    @Override
    public String toString() {
        String className = this.getClass().getSimpleName();
        String postString =  super.toString();
        postString += "   " + className + ".passengerCount = " + this.passengerCount + " \n";

        return postString;
    }

}
