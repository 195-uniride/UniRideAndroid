package com.sjsu.se195.uniride;

import com.sjsu.se195.uniride.models.User;

/**
 * Created by Marta on 1/26/18.
 */

public class UserInformation {


    public static String getShortName(User user) {
        String shortName = "";

        if (user == null) {
            return "";
        }

        if (user.firstName != null) {
            shortName += user.firstName;
        }

        if (user.lastName != null && user.lastName.length() >= 1) {
            shortName += " " + user.lastName.charAt(0) + ".";
        }

        return shortName;
    }

//    public String first;
//    public String last;
    public String email;

//    public UserInformation(String first, String last, String email){
    public UserInformation(String email){
//        this.first = first;
//        this.last = last;
        this.email = email;
    }
}
