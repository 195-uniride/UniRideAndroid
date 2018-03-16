package com.sjsu.se195.uniride.models;

import com.google.firebase.database.IgnoreExtraProperties;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String first;
    public String last;
    public String defaultOrganizationId;

    public User() {
        //empty constructor for firebase
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;

        // TODO: fix
        this.first = null;
        this.last = null;
        defaultOrganizationId = null;
    }

    public User(String username, String first, String last, String email) {
        this.username = username;
        this.first = first;
        this.last = last;
        this.email = email;

        defaultOrganizationId = null;
    }

}
// [END blog_user_class]
