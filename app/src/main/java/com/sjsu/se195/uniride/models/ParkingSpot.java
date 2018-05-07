package com.sjsu.se195.uniride.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ParkingSpot {

    public String state;
    public String number;
    public String section;
    public ParkingSpot() {
        // Default constructor required for calls to DataSnapshot.getValue(Organization.class)
    }


    public ParkingSpot(String state, String number, String section) {
        this.state = state;
        this.section = section;
        this.number = number;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("State", state);
        result.put("Section", section);
        result.put("Number", number);
        return result;
    }
    // [END post_to_map]

}
