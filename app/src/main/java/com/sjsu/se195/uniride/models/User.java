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
    public String imageURL;

    public User() {
        //empty constructor for firebase
    }

    // Sets email and sets all other user attributes to empty string ("").
    public User(String email, String first, String last, String phone, String imageURL) {
        this.email = email;

        // set the other fields as empty strings:
        this.username = "";
        this.firstName = first;
        this.lastName = last;
        this.phoneNumber = phone;
        this.defaultOrganizationId = "";
        this.imageURL = imageURL;

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
