package com.sjsu.se195.uniride.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by akshat on 2/4/18.
 */

public class Carpool extends DriverOfferPost{
    //TODO: generate list of carpool waypoints from driverpost and riderposts (sources and destinations)

    private DriverOfferPost driverPost;
    private String carpoolId;
    private List<RideRequestPost> riderPosts;
    private Date actualStartTime; //TODO: change to time.zonedDateTime
    private Date actualCompletionTime;
    private enum CarpoolState{
        INCOMPLETE, CANCELLED, PLANNED, ONGOING, ABORTED, COMPLETED
    }
    private CarpoolState carpoolState;
    private Location currentLocation; //TODO: add setter. only if carpool ONGOING

    //TODO: used by parties with passengers only.
    public Carpool(){
        carpoolState = CarpoolState.PLANNED;
        riderPosts = new ArrayList<RideRequestPost>();
    }

    public Carpool(DriverOfferPost i_driverPost){
        super(i_driverPost.source, i_driverPost.destination, i_driverPost.passengerCount, i_driverPost.departure_time,
                i_driverPost.arrival_time, i_driverPost.tripDate);
        this.driverPost = i_driverPost;
        carpoolState = CarpoolState.PLANNED;
        riderPosts = new ArrayList<RideRequestPost>();
        //TODO: get plannedStartTime from driver post (when driver has time)
    }

    public List<RideRequestPost> getRiderPosts() {
      return riderPosts;
    }

    public DriverOfferPost getDriverPost() {
      return driverPost;
    }

    public void addRider(RideRequestPost rider) throws OverPassengerLimitException {
        System.out.println("in addRider: " + rider.uid);
        if (riderPosts.size() >= driverPost.getPassengerCount()) {
            throw new OverPassengerLimitException("Over passenger limit. The carpool already has " + riderPosts.size() +
            " passengers and there are only " + driverPost.getPassengerCount() + " seats.");
        }
        riderPosts.add(rider);
    }

    //TODO: add driver method for parties with passengers only.

    public class OverPassengerLimitException extends Exception {
        OverPassengerLimitException(String message) {
            super(message);
            //TODO: any Android message stuff.
        }
    }

    public void startTrip(){
        //TODO: confirm carpool ready to start. driver presence, at least one rider, any additional steps.
        actualStartTime = Calendar.getInstance().getTime();
        carpoolState = CarpoolState.ONGOING;
        // TODO: any additional steps.
    }

    //TODO: add completeTrip()

    //TODO: add abortTrip()

    //TODO: add Mapper for Firebase.
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("carpoolId", carpoolId);
        result.put("driverPost", driverPost);
        result.put("actualStartTime", actualStartTime);
        result.put("actualCompletionTime", actualCompletionTime);
        result.put("carpoolState", carpoolState);
        result.put("currentLocation", currentLocation); //TODO: firebase nested object saving ??
        return result;
    }

    public Map<String, RideRequestPost> riderToMap(){
        HashMap<String, RideRequestPost> result = new HashMap<>();
        for(RideRequestPost r : this.riderPosts){
            System.out.println("in Carpool:riderToMap, rider ID = " + r.uid);
            result.put(r.uid, r);
        }
        return result;
    }
}
