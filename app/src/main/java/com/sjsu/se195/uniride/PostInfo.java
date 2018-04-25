package com.sjsu.se195.uniride;

import android.util.Log;

import com.sjsu.se195.uniride.models.Post;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by timhdavis on 4/25/18.
 */

/**
 * This class serves as a way for converting Post attributes to more useful formats.
 *  This must be a separate class from Post because Post can only contain get methods
 *   that will be stored in Firebase. Because these methods should not be stored in Firebase,
 *   this class can be used with a Post object to do common conversions of Post attributes.
 */
public class PostInfo {

    // NOTE: All get methods will be automatically turned into Firebase attributes!!!
    public static Date getDateOfTrip(Post post) {
        Date date = null;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);

        try {
            date = dateFormat.parse(Integer.toString(post.tripDate));
        } catch (ParseException e) {
            return null;
        }

        return date;
    }

    /**
     * Returns the trip date in the format: "Friday, October 2".
     */
    public static String getTripDateText(Post post) {
        Date date = PostInfo.getDateOfTrip(post);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd", Locale.US);

        return dateFormat.format(date);
    }

    /**
     * Returns departure time in format: "1:35 PM".
     */
    public static String getDepartureDateTimeText(Post post) {
        System.out.println("post = " + post);
        System.out.println("post.departureTime = " + post.departureTime);

        Date date = PostInfo.getDateTime(post.departureTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);

        return dateFormat.format(date);
    }

    /**
     * Returns arrival time in format: "1:35 PM".
     */
    public static String getArrivalDateTimeText(Post post) {
        Date date = PostInfo.getDateTime(post.arrivalTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);

        return dateFormat.format(date);
    }

    private static Date getDateTime(int timeInt) {
        Date dateTime = null;

        String timeString = String.format("%04d", timeInt); // pad timeInt with zeros on left: (1 -> 0001)
        System.out.println("Converted Int to String: timeInt=" + timeInt + " -> timeString=" + timeString);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm", Locale.US);

        try {
            dateTime = dateFormat.parse(timeString);
        } catch (ParseException e) {
            Log.e("PostInfo", "ERROR: getDateTime: could not parse timeInt=" + timeInt + " into format HHmm");
            return null;
        }

        return dateTime;
    }

}
