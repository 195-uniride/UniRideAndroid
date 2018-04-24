package com.sjsu.se195.uniride.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Post implements Parcelable {

    public String uid;
    public String author;
    public String source;
    public String destination;
    public int starCount = 0;
    public int departure_time = 0;
    public int arrival_time = 0;
    public int tripDate = 0;
    public Map<String, Boolean> stars = new HashMap<>();

    public String organizationId;
    public String postId;

    public enum PostType {
        UNKNOWN, RIDER, DRIVER, CARPOOL
    }
    public PostType postType;

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
        this.postType = PostType.UNKNOWN; // Default type. Set correctly in constructor of subclasses.
    }

    public Post(String uid, String author, String source, String destination,
                int departure_time, int arrival_time, int date) {
        this.uid = uid;
        this.author = author;
        this.source = source;
        this.destination = destination;
        this.departure_time = departure_time;
        this.arrival_time = arrival_time;
        this.tripDate = date;

        this.postType = PostType.UNKNOWN; // Default type. Set correctly in constructor of subclasses.
    }

    public Post(String source, String destination, int departure_time, int arrival_time, int date){
        this.source = source;
        this.destination = destination;
        this.departure_time = departure_time;
        this.arrival_time = arrival_time;
        this.tripDate = date;

        this.postType = PostType.UNKNOWN; // Default type. Set correctly in constructor of subclasses.
    }

    // [START post_to_map]
   /* @Exclude
    protected Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);
        return result;
    }*/
    // [END post_to_map]

    // Getters and Setters:

    public int getDepartureTime() {
        return departure_time;
    }

    public void setDepartureTime(int departureTime) {
        this.departure_time = departureTime;
    }

    public int getArrivalTime() {
        return arrival_time;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrival_time = arrivalTime;
    }

    // Parcelable methods:
    // see: https://guides.codepath.com/android/using-parcelable

    // Constructor for loading from a Parcel:
    public Post(Parcel in) {
        // NOTE: order MUST be exactly the same as writeToParcel:
        this.uid = in.readString();
        this.author = in.readString();
        this.source = in.readString();
        this.destination = in.readString();
        this.departure_time = in.readInt();
        this.arrival_time = in.readInt();
        this.tripDate = in.readInt();

        this.organizationId = in.readString();
        this.postId = in.readString();

        this.postType = PostType.valueOf(in.readString()); // .valueOf(...) converts String to Enum type (must be exact match).
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // NOTE: order MUST be exactly the same as Post(Parcel in):
        out.writeString(this.uid);
        out.writeString(this.author);
        out.writeString(this.source);
        out.writeString(this.destination);
        out.writeInt(this.departure_time);
        out.writeInt(this.arrival_time);
        out.writeInt(this.tripDate);

        out.writeString(this.organizationId);
        out.writeString(this.postId);

        out.writeString(this.postType.name()); // .name() converts Enum to String.
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<Post> CREATOR
            = new Parcelable.Creator<Post>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

}
// [END post_class]
