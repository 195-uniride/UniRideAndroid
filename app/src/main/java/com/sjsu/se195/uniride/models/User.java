package com.sjsu.se195.uniride.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String email;
    public String username;
    public String firstName;
    public String lastName;
    public String phoneNumber;
    public String defaultOrganizationId;

    public User() {
        //empty constructor for firebase
    }

    // Sets email and sets all other user attributes to empty string ("").
    public User(String email) {
        this.email = email;

        // set the other fields as empty strings:
        this.username = "";
        this.firstName = "";
        this.lastName = "";
        this.phoneNumber = "";
        this.defaultOrganizationId = "";
    }

    public User(String username, String firstName, String lastName, String email, String phoneNumber, String defaultOrganizationId) {
        this.email = email;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.defaultOrganizationId = defaultOrganizationId;
    }


    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("username", username);
        result.put("firstName", firstName);
        result.put("lastName", lastName);
        result.put("phoneNumber", phoneNumber);
        result.put("defaultOrganizationId", defaultOrganizationId);

        return result;
    }
    // [END post_to_map]
}
// [END blog_user_class]
