package com.sjsu.se195.uniride.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ParkingSpot {

    public String state;
    public ParkingSpot() {
        // Default constructor required for calls to DataSnapshot.getValue(Organization.class)
    }


    public ParkingSpot(String state) {
        this.state = state;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("State", state);

        return result;
    }
    // [END post_to_map]

}
