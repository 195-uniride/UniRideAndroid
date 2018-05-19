package com.sjsu.se195.uniride.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by timhdavis on 5/18/18.
 */

public class RouteWayPoint implements Parcelable {

    public String text;
    public String time;
    public String participantName;
    public String address;

    public String type;


    public RouteWayPoint() {

    }


    // Constructor for loading from a Parcel:
    public RouteWayPoint(Parcel in) {
        // NOTE: order MUST be exactly the same as writeToParcel:
        this.text = in.readString();
        this.time = in.readString();
        this.participantName = in.readString();
        this.address = in.readString();
        this.type = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // NOTE: order MUST be exactly the same as Post(Parcel in):
        out.writeString(this.text);
        out.writeString(this.time);
        out.writeString(this.participantName);
        out.writeString(this.address);
        out.writeString(this.type);
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<RouteWayPoint> CREATOR
            = new Parcelable.Creator<RouteWayPoint>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public RouteWayPoint createFromParcel(Parcel in) {
            return new RouteWayPoint(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public RouteWayPoint[] newArray(int size) {
            return new RouteWayPoint[size];
        }
    };



}
