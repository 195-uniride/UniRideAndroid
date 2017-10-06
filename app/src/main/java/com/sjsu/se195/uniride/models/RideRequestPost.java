package com.sjsu.se195.uniride.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by akshat on 10/5/17.
 */

public class RideRequestPost extends Post {
    private String pickuppoint;

    public RideRequestPost(){
        //default constructor
    }

    //Constructer
    public RideRequestPost(String uid, String author, String title, String body, String point){
        super(uid, author, title, body);
        this.pickuppoint = point;
    }

    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("pickupPoint", pickuppoint);
        return result;
    }
}
