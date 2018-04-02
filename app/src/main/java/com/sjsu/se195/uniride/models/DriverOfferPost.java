package com.sjsu.se195.uniride.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by maninder on 10/4/17.
 */

public class DriverOfferPost extends Post {

    public int passengerCount;

    public DriverOfferPost(){}

    //Constructer
    public DriverOfferPost(String uid, String author, String source, String destination, int pasCount,
                           int departureTime, int arrivalTime, int tripDay){
        super(uid, author, source, destination, departureTime, arrivalTime, tripDay);
        this.passengerCount = pasCount;
    }

    public DriverOfferPost(String source, String destination, int pasCount, int departureTime, int arrivalTime, int tripDay){
        super(source, destination, departureTime, arrivalTime, tripDay);
        this.passengerCount = pasCount;
    }

    public int getPassengerCount() { return passengerCount; }

    public  Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("source", source);
        result.put("destination", destination);
        result.put("departureTime", departure_time);
        result.put("arrivalTime", arrival_time);
        result.put("tripDate", tripDate);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("passengerCount", passengerCount);
        return result;
    }

}
