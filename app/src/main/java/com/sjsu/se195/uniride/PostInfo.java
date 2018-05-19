package com.sjsu.se195.uniride;

import android.util.Log;

import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.DriverOfferPost;
import com.sjsu.se195.uniride.models.Post;
import com.sjsu.se195.uniride.models.RideRequestPost;
import com.sjsu.se195.uniride.models.WayPoint;

import java.text.DecimalFormat;
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

    /**
     * Returns time (as int: 1335 or 955) in format: "1:35 PM" or "9:55 AM".
     */
    public static String getDateTimeText(int time) {
        Date date = PostInfo.getDateTime(time);

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


    public static String getRouteDescription(Post post) {
        /*
            Thursday, May 5:
            9:00 AM - depart from
               LocA (source)
            9:30 AM - arrive at
               LocC (destination)

         */


        String routeDescription = "Route Details: \n";

        if (post instanceof Carpool) {
            Carpool carpoolPost = (Carpool) post;

            // Show seats taken - 1 because 1 will always be taken by the potential new rider:
            String passengerCount = (carpoolPost.getNumberSeatsTaken() - 1) + " / "
                    + carpoolPost.getPassengerCount() + " Passengers";

            String estimatedTravelTime = (carpoolPost.getEstimatedTotalTripTimeInMinutes() + " minutes");

            DecimalFormat decimalFormat = new DecimalFormat("0.##");
            String estimatedTravelDistance = decimalFormat.format(carpoolPost.getEstimatedTotalTripDistanceInMiles()) + " mi";

            routeDescription += passengerCount + "\n" + "Estimated trip time: " + estimatedTravelTime
                    + " (" + estimatedTravelDistance + ")\n";
        }



        routeDescription += "\n";

        routeDescription += PostInfo.getTripDateText(post) + ": \n";

        if (post instanceof DriverOfferPost) {

            String driverName = "";

            if (post.author != null) {
                driverName = " " + post.author;
            }

            routeDescription += PostInfo.getDepartureDateTimeText(post) + " - Driver" + driverName + " will depart from: \n";
        }
        else if (post instanceof RideRequestPost) {
            routeDescription += PostInfo.getDepartureDateTimeText(post) + " - pickup at: \n";
        }

        routeDescription += "   " + post.source + "\n";
        routeDescription += "\n";

        if (post instanceof Carpool) {
            Carpool carpoolPost = (Carpool) post;

            if (carpoolPost.getNumberSeatsTaken() == 0) {
                routeDescription += "No passengers yet. \n";
            }
            else {

                routeDescription += "Picking up " + carpoolPost.getNumberSeatsTaken() + " passenger";
                if (carpoolPost.getNumberSeatsTaken() > 1) {
                    routeDescription += "s"; // add plural for passenger(s).
                }
                routeDescription += ": \n";

                // TODO: go by waypoint order!

                for (WayPoint riderWayPoint : carpoolPost.riderWaypoints) {

                    RideRequestPost rider = carpoolPost.riderPosts.get(riderWayPoint.getRiderIndex());

                    String passengerName = "";
                    if (post.author != null) {
                        passengerName = " " + rider.author;
                    }

                    //routeDescription += PostInfo.getDepartureDateTimeText(rider);

                    long pickupTimeInMilliseconds = riderWayPoint.getPickupTime();

                    Date dateTime = new Date(pickupTimeInMilliseconds);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);

                    String pickupTimeString = dateFormat.format(dateTime);

                    routeDescription += pickupTimeString;

                    routeDescription += " - pickup passenger"
                            + passengerName + " at: \n";

                    routeDescription += "   " + rider.source + "\n";
                }
            }

            routeDescription += "\n";
        }

        routeDescription += PostInfo.getArrivalDateTimeText(post) + " - arrive at: \n";
        routeDescription += "   " + post.destination;

        return routeDescription;
    }

}
