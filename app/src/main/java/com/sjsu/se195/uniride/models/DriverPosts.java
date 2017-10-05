package com.sjsu.se195.uniride.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maninder on 10/4/17.
 */

public class DriverPosts extends Post {

    private int passengerCount;

    //Constructer
    public DriverPosts(String uid, String author, String title, String body){
        super(uid, author, title, body, true);
        this.passengerCount = 0;
    }

    public  Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("title", title);
        result.put("body", body);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("passengerCount", passengerCount);
        return result;
    }
}
