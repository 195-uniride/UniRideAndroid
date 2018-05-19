package com.sjsu.se195.uniride.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.sjsu.se195.uniride.Mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by akshat on 2/4/18.
 */

public class Carpool extends DriverOfferPost {
    //TODO: generate list of carpool waypoints from driverpost and riderposts (sources and destinations)

    private DriverOfferPost driverPost;
    private String carpoolId;
    public List<RideRequestPost> riderPosts;
    private Date actualStartTime;
    private Date actualCompletionTime;
    private enum CarpoolState {
        INCOMPLETE, CANCELLED, PLANNED, ONGOING, ABORTED, COMPLETED
    }
    private CarpoolState carpoolState;
    private Location currentLocation; //TODO: add setter. only if carpool ONGOING
    private int totalTripTime = -1; // in seconds; -1 represents an unset or invalid state.
    private int totalTripDistance = -1; // in meters; -1 represents an unset or invalid state.

    private boolean areAllTripTimeLimitsSatisfied = false;

    private Mapper carpoolMapper = null;

    public List<WayPoint> riderWaypoints = null;

    // Constructors:
    //TODO: used by parties with passengers only.
    public Carpool() {
        carpoolState = CarpoolState.PLANNED;
        riderPosts = new ArrayList<RideRequestPost>();

        postType = PostType.CARPOOL;
    }

    public Carpool(DriverOfferPost i_driverPost) {
        super(i_driverPost.source, i_driverPost.destination, i_driverPost.passengerCount, i_driverPost.departureTime,
                i_driverPost.arrivalTime, i_driverPost.tripDate);
        this.driverPost = i_driverPost;
        carpoolState = CarpoolState.PLANNED;
        riderPosts = new ArrayList<RideRequestPost>();

        this.uid = driverPost.uid;
        this.author = driverPost.author;
        this.source = driverPost.source;
        this.destination = driverPost.destination;
        // this.departureTime = departureTime;
        // this.arrivalTime = arrivalTime;
        this.tripDate = driverPost.tripDate;

        this.organizationId = driverPost.organizationId;

        postType = PostType.CARPOOL;
    }

    
    // Copy Constructor for duplicating a carpool object (clone):
    public Carpool(Carpool carpoolToCopy) {
        this(carpoolToCopy.getDriverPost()); // Call the Constructor with the Drive Offer Post.

        for (RideRequestPost rideRequestPost : carpoolToCopy.getRiderPosts()) {
            try {
                this.addRider(rideRequestPost);
            } catch (OverPassengerLimitException e) {
                // NOTE: This error should never happen because
                //  was already an acceptable carpool object with all of these rideRequestPosts.
                e.printStackTrace();
            }
        }
    }

    // Getters and Setters:

    public String getCarpoolId() {
        return carpoolId;
    }

    public void setCarpoolId(String carpoolId) {
        this.carpoolId = carpoolId;
    }

    public int getNumberSeatsTaken() {
        return (riderPosts == null)? 0 : riderPosts.size(); // if no riders set, return passenger count (all seats available).
    }

    public int getNumberSeatsAvailable() {
        return (passengerCount - getNumberSeatsTaken()); // if no riders set, return passenger count (all seats available).
    }

    public List<RideRequestPost> getRiderPosts() {
        return riderPosts;
    }

    public DriverOfferPost getDriverPost() {
        return driverPost;
    }

    // Setup methods:

    public void addRider(RideRequestPost rider) throws OverPassengerLimitException {
        if (riderPosts.size() >= driverPost.getPassengerCount()){
            throw new OverPassengerLimitException("Over passenger limit of " + driverPost.getPassengerCount() +
                    " passengers. The carpool already has " + riderPosts.size() + " passengers.");
        }

        totalTripTime = -1; // Need to reset total trip time if was set because will need to be recalculated.

        riderPosts.add(rider);
    }

    //TODO: add driver method for parties with passengers only.

    public class OverPassengerLimitException extends Exception {
        OverPassengerLimitException(String message) {
            super(message);
        }
    }


    // Information methods:

    public boolean areAllTripTimeLimitsSatisfied() {

        // Skip calculation if have done before:
        if (areAllTripTimeLimitsSatisfied) {
            return true;
        }

        System.out.println("getTotalTripTime() = " + getEstimatedTotalTripTimeInSeconds()); // TODO remove....

        System.out.println("getEarliestArrivalTimeOfParticipants() = " + getEarliestArrivalTimeOfParticipants()); // TODO remove....

        System.out.println("getDesiredArrivalDateTime() = " + getDesiredArrivalDateTime()); // TODO remove....

        System.out.println("getDesiredArrivalDateTime().toString() = " + getDesiredArrivalDateTime().toString()); // TODO remove....

        // Calculate: getEarliestArrivalTimeOfParticipants - getTotalTripTime => time need to leave by.
        // can then check if departure times are within this.

        // Need to leave before (earliest arrival time - total trip time):

        Date timeDateNeedToLeaveBefore = getTimeNeedToLeaveBefore();

        System.out.println("timeDateNeedToLeaveBefore = " + timeDateNeedToLeaveBefore); // TODO remove....

        // Trip is impossible if driver.departureTime > (is after) timeNeedToLeaveBefore:
        //if (getDriverPost().departureTime > timeNeedToLeaveBefore) {
        Date driverDepartDateTime = getDateTime(getDriverPost().departureTime);

        if (driverDepartDateTime.after(timeDateNeedToLeaveBefore)) {

            System.out.println("Trip is IMPOSSIBLE because driverDepartDateTime [" + driverDepartDateTime +
                    "] is after timeDateNeedToLeaveBefore [" + timeDateNeedToLeaveBefore + "]."); // TODO remove....

            return false;
        }
        else {
            // TODO: Update departure and arrival times for post:

            this.departureTime = getIntTime(timeDateNeedToLeaveBefore);
            this.arrivalTime = getEarliestArrivalTimeOfParticipants();

            System.out.println("Trip is POSSIBLE because driverDepartDateTime [" + driverDepartDateTime +
                    "] <= (is before or at) timeDateNeedToLeaveBefore [" + timeDateNeedToLeaveBefore + "]."); // TODO remove....

            return true;
        }
    }



    private Date getTimeNeedToLeaveBefore() {
        Date earliestArrivalDateTime = getDateTime(getEarliestArrivalTimeOfParticipants());

        return getTimeBefore(earliestArrivalDateTime, getEstimatedTotalTripTimeInMinutes());
    }

    private Date getTimeBefore(Date initialTime, int minutes) {
        final long ONE_MINUTE_IN_MILLISECONDS = 60000;//millisecs

        long initialTimeInMilliseconds = initialTime.getTime();
        return new Date(initialTimeInMilliseconds - (minutes * ONE_MINUTE_IN_MILLISECONDS));
    }

    private Date getTimeAfter(Date initialTime, int minutes) {
        final long ONE_MINUTE_IN_MILLISECONDS = 60000;//millisecs

        long initialTimeInMilliseconds = initialTime.getTime();
        return new Date(initialTimeInMilliseconds + (minutes * ONE_MINUTE_IN_MILLISECONDS));
    }

    /*
        Returns the estimated total trip time (in seconds) of the carpool.
     */
    public int getEstimatedTotalTripTimeInSeconds() {
        // Only calculate the total trip time if necessary:
        if (totalTripTime == -1) { // if totalTripTime is unset or in an invalid state:
            totalTripTime = calculateTotalTripTime();
        }

        return totalTripTime;
    }

    /*
        Returns the estimated total trip time (in minutes) of the carpool.
     */
    public int getEstimatedTotalTripTimeInMinutes() {
        // Convert totalTripTime to minutes: (NOTE: OK to floor by int division)
        return (getEstimatedTotalTripTimeInSeconds() / 60); // time in seconds * (1 min / 60 sec) = time in minutes.
    }


    /*
        Returns the estimated total trip distance (in meters) of the carpool.
     */
    public int getEstimatedTotalTripDistanceInMeters() {
        // Only calculate the total distance time if necessary:
        if (totalTripDistance == -1) { // if totalTripDistance is unset or in an invalid state:
            totalTripDistance = calculateTotalTripDistance();
        }

        Log.d("Carpool", "totalTripDistance (in meters) = " + totalTripDistance);

        return totalTripDistance;
    }

    /*
        Returns the estimated total trip distance (in km) of the carpool.
     */
    public double getEstimatedTotalTripDistanceInMiles() {
        final double METERS_PER_MILE = 1609.344;
        return ((double)getEstimatedTotalTripDistanceInMeters() / METERS_PER_MILE);
    }

    /*
        Returns the arrival time (in format HHmm, i.e. 1325 for 1:25pm) of the
         post with the earliest arrival time (by finding the minimum
         of the arrival time fields of all the posts).
     */
    public int getEarliestArrivalTimeOfParticipants() {
        int min = driverPost.arrivalTime;

        for (RideRequestPost riderPosts : getRiderPosts()) {
            min = Math.min(min, riderPosts.arrivalTime);
        }

        return min;
    }

    /*
        Returns the date and time of the desired arrival time of the carpool
         by considering the earliest arrival time of all participants
         and the trip date.
     */
    public Date getDesiredArrivalDateTime() {
        SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd-HHmm"); // Note: capital 'H' means military time.
        // example: "20180230-2219" Parses as Fri Mar 02 22:19:00 UTC 2018

        String dateTimeString =  Integer.toString(getDriverPost().tripDate); // Note: in format: yyyMMdd

        dateTimeString += "-" + String.format("%04d", this.getEarliestArrivalTimeOfParticipants()); // Use the earliest arrival time to satisfy all participants.

        System.out.print(dateTimeString + " Parses as ");

        Date dateTime = null;
        try {
            dateTime = ft.parse(dateTimeString);
            System.out.println(dateTime);
        } catch (ParseException e) {
            System.out.println("ERROR: dateTimeString=" + dateTimeString + " is Unparseable using " + ft);
        }

        return dateTime;
    }

    private int getIntTime(Date dateTime) {
        SimpleDateFormat ft = new SimpleDateFormat ("HHmm", Locale.US); // Note: capital 'H' means military time.
        // example: "20180230-2219" Parses as Fri Mar 02 22:19:00 UTC 2018

        String timeString = "0";
        try {
            timeString = ft.format(dateTime);
        } catch (Exception ex ) {
            System.out.println(ex);
        }

        Log.d("getIntTime", "from Date [" + dateTime + "] -> int[" + timeString + "]");

        return Integer.parseInt(timeString);
    }


    private Date getDateTime(int intTmeFormat) {
        SimpleDateFormat ft = new SimpleDateFormat ("yyyyMMdd-HHmm"); // Note: capital 'H' means military time.
        // example: "20180230-2219" Parses as Fri Mar 02 22:19:00 UTC 2018

        String dateTimeString =  Integer.toString(getDriverPost().tripDate); // Note: in format: yyyMMdd

        dateTimeString += "-" + String.format("%04d", intTmeFormat);

        System.out.print(dateTimeString + " Parses as ");

        Date dateTime = null;
        try {
            dateTime = ft.parse(dateTimeString);
            System.out.println(dateTime);
        } catch (ParseException e) {
            System.out.println("ERROR: dateTimeString=" + dateTimeString + " is Unparseable using " + ft);
        }

        return dateTime;
    }

    /*
        Returns the calculated expected total trip time of the carpool.
         Time is calculated by using a Mapper to perform a
         Google Maps API request with the carpool's trip details.
     */
    private int calculateTotalTripTime() {
        System.out.println("Calculating Total Trip Time...");

        System.out.println("...for Carpool: driver = " + getDriverPost().author + "....");

        for (RideRequestPost riderPost : getRiderPosts()) {
            System.out.println("...and rider = " + riderPost.author + "; with riderPost.source = " + riderPost.source + "....");
        }

        return getCarpoolMapper().getTotalTripTime();
    }

    /*
        Returns the calculated expected total trip distance of the carpool.
         Distance is calculated by using a Mapper to perform a
         Google Maps API request with the carpool's trip details.
     */
    private int calculateTotalTripDistance() {
        System.out.println("Calculating Total Trip Distance...");

        System.out.println("...for Carpool: driver = " + getDriverPost().author + "....");

        for (RideRequestPost riderPost : getRiderPosts()) {
            System.out.println("...and rider = " + riderPost.author + "; with riderPost.source = " + riderPost.source + "....");
        }

        return getCarpoolMapper().getTotalTripDistance();
    }


    private Mapper getCarpoolMapper() {
        if (carpoolMapper == null) {
            System.out.println("...now starting Mapper....");
            carpoolMapper = new Mapper(this);

            setWayPoints();
        }

        return carpoolMapper;
    }

    /*
        Returns the Google Maps API call URL for the trip
         used by the Mapper.
     */
    public String getTripURL() {
        return getCarpoolMapper().getTripURL();
    }


    public List<WayPoint> getRiderWaypoints() {

        if (riderWaypoints == null) {
            getCarpoolMapper(); // calls setWayPoints.
        }

        return riderWaypoints;
    }

    private void setWayPoints() {

        ArrayList<Integer> legDurationsInSeconds = carpoolMapper.getTripWaypointTimes();
        ArrayList<String> legAddresses = carpoolMapper.getTripWaypointAddresses();

        if (legDurationsInSeconds.size() != legAddresses.size()) {
            Log.e("setWayPoints", "legDurationsInSeconds.size() ["
                    + legDurationsInSeconds.size() + "] != legAddresses.size() ["
                    + legAddresses.size() + "]");
        }

        riderWaypoints = new ArrayList<WayPoint>();

        Date previousPointTime = getTimeNeedToLeaveBefore(); // Estimated Start Time

        /*
            Use (legDurationsInSeconds.size() - 1) because don't add a waypoint for the last leg.
            Last leg of trip is just from last pickup point to final destination (which isn't considered
            a pickup point).
         */
        for (int i = 0; i < (legDurationsInSeconds.size() - 1); i++) {
            WayPoint wayPoint = new WayPoint();

            for (int riderIndex = 0; riderIndex < riderPosts.size(); riderIndex++) {
                if (riderPosts.get(riderIndex).source.equals(legAddresses.get(i))) {

                    wayPoint.setPostId(riderPosts.get(riderIndex).postId);
                    wayPoint.setRiderIndex(riderIndex);
                }
            }

            wayPoint.setDuration(legDurationsInSeconds.get(i));

            // NOTE: Ok to floor for this estimate purpose:
            int legDurationInMinutes = legDurationsInSeconds.get(i) / 60;  // time in seconds * (1 min / 60 sec) = time in minutes.

            Date waypointArrivalTime = getTimeAfter(previousPointTime, legDurationInMinutes);

            wayPoint.setPickupTime(waypointArrivalTime.getTime());

            // add waypoint:

            riderWaypoints.add(wayPoint);

            // increment previous waypoint time:
            previousPointTime = waypointArrivalTime;
        }

        // DEBUG:

        for (int i = 0; i < riderWaypoints.size(); i++) {
            Log.d("Carpool", "riderWaypoints[" + i + "]: " + riderWaypoints.get(i).toString());
        }


    }

    // State-changing methods:

    public void startTrip() {
        //TODO: confirm carpool ready to start. driver presence, at least one rider, any additional steps.
        actualStartTime = Calendar.getInstance().getTime();
        carpoolState = CarpoolState.ONGOING;
        // TODO: any additional steps.
    }

    //TODO: add completeTrip()

    //TODO: add abortTrip()

    // Other methods:

    // Firebase Mapping methods:

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("carpoolId", carpoolId);
        result.put("postType", postType);
        result.put("organizationId", organizationId);
        result.put("postId", postId);
        result.put("driverPost", driverPost);
        result.put("actualStartTime", actualStartTime);
        result.put("actualCompletionTime", actualCompletionTime);
        result.put("carpoolState", carpoolState);
        // result.put("currentLocation", currentLocation); //TODO: firebase nested object saving ??

        // also need to save driver fields:
        result.put("uid", uid);
        result.put("author", author);
        result.put("source", source);
        result.put("destination", destination);
        result.put("departureTime", departureTime);
        result.put("arrivalTime", arrivalTime);
        result.put("tripDate", tripDate);
        result.put("starCount", starCount);
        result.put("stars", stars);
        result.put("passengerCount", passengerCount);

        result.put("riderPosts", riderPosts);

        result.put("waypoints", riderWaypoints);

        return result;
    }

//    public Map<String, RideRequestPost> riderToMap() {
//        HashMap<String, RideRequestPost> result = new HashMap<>();
//        for(RideRequestPost r : this.riderPosts){
//            System.out.println("in Carpool:riderToMap, rider ID = " + r.uid);
//            result.put(r.uid, r);
//        }
//        return result;
//    }

    public Map<String, Map<String, Object>> riderToMap() {
        HashMap<String, Map<String, Object>> result = new HashMap<>();
        for(RideRequestPost r : this.riderPosts){
            System.out.println("in Carpool:riderToMap ver2, rider ID = " + r.uid);
            result.put(r.uid, r.toMap());
        }
        return result;
    }

    public Map<String, String> userToMap(String role){
        HashMap<String, String> result = new HashMap<>();
        result.put("userRole", role);
        return result;
    }
    // TODO: Add Parcelable implementation:
    // ...

    // Parcelable methods:

    /*
    private DriverOfferPost driverPost;
    private String carpoolId;
    public List<RideRequestPost> riderPosts;
    private Date actualStartTime;
    private Date actualCompletionTime;
    private enum CarpoolState {
        INCOMPLETE, CANCELLED, PLANNED, ONGOING, ABORTED, COMPLETED
    }
    private CarpoolState carpoolState;
    private Location currentLocation; //TODO: add setter. only if carpool ONGOING
    private int totalTripTime = -1; // in seconds; -1 represents an unset or invalid state.
    private int totalTripDistance = -1; // in meters; -1 represents an unset or invalid state.

    private boolean areAllTripTimeLimitsSatisfied = false;

    private Mapper carpoolMapper = null;
     */

    // Constructor for loading from a Parcel:
    public Carpool(Parcel in) {

        super(in);

        // NOTE: order MUST be exactly the same as writeToParcel:

        this.driverPost = in.readParcelable(DriverOfferPost.class.getClassLoader());

        RideRequestPost[] ridersArray = in.createTypedArray(RideRequestPost.CREATOR);
        this.riderPosts = Arrays.asList(ridersArray);

        this.carpoolId = in.readString();

        this.carpoolState = CarpoolState.valueOf(in.readString());

        this.totalTripTime = in.readInt();

        this.totalTripDistance = in.readInt();

        WayPoint[] wayPointArray = in.createTypedArray(WayPoint.CREATOR);
        this.riderWaypoints = Arrays.asList(wayPointArray);

        // TODO: read areAllTripTimeLimitsSatisfied...

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {

        super.writeToParcel(out, flags);

        // NOTE: order MUST be exactly the same as Post(Parcel in):

        out.writeParcelable(this.driverPost, flags);

        out.writeTypedArray(getRiderPostsInArray(), flags);

        out.writeString(this.carpoolId);

        out.writeString(this.carpoolState.name());

        out.writeInt(this.totalTripTime);

        out.writeInt(this.totalTripDistance);

        out.writeTypedArray(getWayPointsInArray(), flags);

        // TODO: write areAllTripTimeLimitsSatisfied...
    }

    // Parcelable-Helper method:
    private RideRequestPost[] getRiderPostsInArray() {
        RideRequestPost[] ridersArray = new RideRequestPost[riderPosts.size()];

        ridersArray = riderPosts.toArray(ridersArray);

        return ridersArray;
    }

    // Parcelable-Helper method:
    private WayPoint[] getWayPointsInArray() {
        WayPoint[] wayPointArray = new WayPoint[riderWaypoints.size()];

        wayPointArray = riderWaypoints.toArray(wayPointArray);

        return wayPointArray;
    }

    // After implementing the `Parcelable` interface, we need to create the
    // `Parcelable.Creator<MyParcelable> CREATOR` constant for our class;
    // Notice how it has our class specified as its type.
    public static final Parcelable.Creator<Carpool> CREATOR
            = new Parcelable.Creator<Carpool>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public Carpool createFromParcel(Parcel in) {
            return new Carpool(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public Carpool[] newArray(int size) {
            return new Carpool[size];
        }
    };


    // To String:

    @Override
    public String toString() {
        String className = this.getClass().getSimpleName();
        String postString =  super.toString();
        postString += "   " + className + ".carpoolId = " + this.carpoolId + " \n";
        postString += "   " + className + ".carpoolState = " + this.carpoolState.name() + " \n";
        postString += "   " + className + ".getNumberSeatsTaken() = " + this.getNumberSeatsTaken() + " \n";
        postString += "   " + className + ".totalTripTime = " + this.totalTripTime + " \n";
        postString += "   " + className + ".totalTripDistance = " + this.totalTripDistance + " \n";
        postString += "   " + className + ".driverPost = " + this.driverPost.toString() + " \n";
        for (int i = 0; i < getNumberSeatsTaken(); i++) {
            postString += "   " + className + ".riderPost[" + i + "] = "
                    + riderPosts.get(i).toString() + " \n";
        }


        return postString;
    }

}
