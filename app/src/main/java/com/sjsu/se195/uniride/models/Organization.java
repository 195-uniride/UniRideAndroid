package com.sjsu.se195.uniride.models;

/**
 * Created by timhdavis on 10/8/17.
 */

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START organization_class]
@IgnoreExtraProperties
public class Organization {

    public String name;
    public String classification;
    public String description;
    public String emailPattern;
    public String website;

    public Organization() {
        // Default constructor required for calls to DataSnapshot.getValue(Organization.class)
    }


    public Organization(String name, String classification, String description, String emailPattern, String website) {
        this.name = name;
        this.classification = classification;
        this.description = description;
        this.emailPattern = emailPattern;
        this.website = website;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("classification", classification);
        result.put("description", description);
        result.put("emailPattern", emailPattern);
        result.put("website", website);

        return result;
    }
    // [END post_to_map]

}
// [END organization_class]