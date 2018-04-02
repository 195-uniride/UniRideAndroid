package com.sjsu.se195.uniride.models;

import com.sjsu.se195.uniride.Mapper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by akshat on 2/4/18.
 */

public class Carpool extends DriverOfferPost {
    //TODO: generate list of carpool waypoints from driverpost and riderposts (sources and destinations)

    private DriverOfferPost driverPost;
    private String carpoolId;
    private ArrayList<RideRequestPost> riderPosts;
    private Date actualStartTime; //TODO: change to time.zonedDateTime
    private Date actualCompletionTime;
    private enum CarpoolState{
        INCOMPLETE, CANCELLED, PLANNED, ONGOING, ABORTED, COMPLETED
    }
    private CarpoolState carpoolState;
    private Location currentLocation; //TODO: add setter. only if carpool ONGOING
    private int totalTripTime = -1; // in seconds

    // Constructors:
    //TODO: used by parties with passengers only.
    public Carpool(){
        carpoolState = CarpoolState.PLANNED;
        riderPosts = new ArrayList<RideRequestPost>();
    }

    public Carpool(DriverOfferPost i_driverPost) {
        super(i_driverPost.source, i_driverPost.destination, i_driverPost.passengerCount, i_driverPost.departure_time,
                i_driverPost.arrival_time, i_driverPost.tripDate);
        this.driverPost = i_driverPost;
        carpoolState = CarpoolState.PLANNED;
        riderPosts = new ArrayList<RideRequestPost>();
        //TODO: get plannedStartTime from driver post (when driver has time)
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

    public List<RideRequestPost> getRiderPosts() {
        return riderPosts;
    }

    public DriverOfferPost getDriverPost() {
        return driverPost;
    }

    // Setup methods:

    public void addRider(RideRequestPost rider) throws OverPassengerLimitException {
        if (riderPosts.size() >= driverPost.getPassengerCount()){
            throw new OverPassengerLimitException("Over passenger limit. The carpool already has " + riderPosts.size() + " passengers.");
        }
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

        System.out.println("getTotalTripTime() = " + getTotalTripTime()); // TODO remove....

        System.out.println(); // TODO remove....
        System.out.println("------ DONE -------"); // TODO remove....
        System.out.println(); // TODO remove....

        return false; // TODO.
    }

    public int getTotalTripTime() {
        if (totalTripTime == -1) { // if totalTripTime is unset or in an invalid state:
            totalTripTime = calculateTotalTripTime();
        }

        return totalTripTime;
    }

    private int calculateTotalTripTime() {
        System.out.println("Calculating Total Trip Time...");

        System.out.println("...for Carpool: driver = " + getDriverPost() + "....");

        for (RideRequestPost riderPost : getRiderPosts()) {
            System.out.println("...and rider = " + riderPost + "; with riderPost.source = " + riderPost.source + "....");
        }

        System.out.println("...now starting Mapper....");

        Mapper mapper = new Mapper(this);

        return mapper.getTotalTripTime();
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

    // Needed for saving to Firebase:
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("carpoolId", carpoolId);
        result.put("driverPost", driverPost);
        result.put("riderPosts", riderPosts);
        result.put("actualStartTime", actualStartTime);
        result.put("actualCompletionTime", actualCompletionTime);
        result.put("carpoolState", carpoolState);
        result.put("currentLocation", currentLocation); //TODO: firebase nested object saving ??
        return result;
    }
}
