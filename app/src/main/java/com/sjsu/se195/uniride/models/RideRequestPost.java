package com.sjsu.se195.uniride.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by akshat on 10/5/17.
 */

public class RideRequestPost extends Post {
    private String pickuppoint;

    public RideRequestPost(){}

    //Constructer
    public RideRequestPost(String uid, String author, String source, String destination, String point){
        super(uid, author, source, destination);
        this.pickuppoint = point;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("source", source);
        result.put("destination", destination);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("pickupPoint", pickuppoint);
        return result;
    }
}
