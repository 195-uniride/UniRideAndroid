package com.sjsu.se195.uniride.models;

import android.os.Parcel;
import android.os.Parcelable;

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
    public int departureTime = 0;
    public int arrivalTime = 0;
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
                int departure_time, int arrivalTime, int date) {
        this.uid = uid;
        this.author = author;
        this.source = source;
        this.destination = destination;
        this.departureTime = departure_time;
        this.arrivalTime = arrivalTime;
        this.tripDate = date;

        this.postType = PostType.UNKNOWN; // Default type. Set correctly in constructor of subclasses.
    }

    public Post(String source, String destination, int departure_time, int arrivalTime, int date){
        this.source = source;
        this.destination = destination;
        this.departureTime = departure_time;
        this.arrivalTime = arrivalTime;
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

    // Getters and Setters: NOTE: must match attributes exactly

    // Parcelable methods:
    // see: https://guides.codepath.com/android/using-parcelable

    // Constructor for loading from a Parcel:
    public Post(Parcel in) {
        // NOTE: order MUST be exactly the same as writeToParcel:
        this.uid = in.readString();
        this.author = in.readString();
        this.source = in.readString();
        this.destination = in.readString();
        this.departureTime = in.readInt();
        this.arrivalTime = in.readInt();
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
        out.writeInt(this.departureTime);
        out.writeInt(this.arrivalTime);
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

    // To String:

    @Override
    public String toString() {
        String className = this.getClass().getSimpleName();

        String postString =  className + ".toString: \n";
        postString += "   " + className + ".postId = " + this.postId + " \n";
        postString += "   " + className + ".uid = " + this.uid + " \n";
        postString += "   " + className + ".organizationId = " + this.organizationId + " \n";
        postString += "   " + className + ".postType = " + this.postType.name() + " \n";
        postString += "   " + className + ".author = " + this.author + " \n";
        postString += "   " + className + ".source = " + this.source + " \n";
        postString += "   " + className + ".destination = " + this.destination + " \n";
        postString += "   " + className + ".departureTime = " + this.departureTime + " \n";
        postString += "   " + className + ".arrivalTime = " + this.arrivalTime + " \n";
        postString += "   " + className + ".tripDate = " + this.tripDate + " \n";
        postString += "   " + className + ".starCount = " + this.starCount + " \n";
        postString += "   " + className + ".stars = " + this.stars.toString() + " \n";

        return postString;
    }

}
// [END post_class]
