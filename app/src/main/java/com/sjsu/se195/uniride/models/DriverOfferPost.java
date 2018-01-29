package com.sjsu.se195.uniride.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maninder on 10/4/17.
 */

public class DriverOfferPost extends Post {

    private int passengerCount;

    public DriverOfferPost(){}

    //Constructer
    public DriverOfferPost(String uid, String author, String source, String destination, int pasCount){
        super(uid, author, source, destination);
        this.passengerCount = pasCount;
    }

    public  Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("source", source);
        result.put("destination", destination);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("passengerCount", passengerCount);
        return result;
    }
}
