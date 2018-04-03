package com.sjsu.se195.uniride;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.sjsu.se195.uniride.models.Carpool;
import com.sjsu.se195.uniride.models.RideRequestPost;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by timhdavis on 3/31/18.
 */

public class Mapper {

    // Member variables:

    private Document mDocument;

    // Constructors:

    public Mapper() {

    }

    public Mapper(Carpool carpool) {
        try {
//            LatLng driverSource = this.getLocationFromAddress(carpool.getDriverPost().source);
//            LatLng destination = this.getLocationFromAddress(carpool.getDriverPost().destination);
//
//            ArrayList<LatLng> riderSources = new ArrayList<>();
//            for (RideRequestPost riderPost : carpool.getRiderPosts()) {
//                riderSources.add(this.getLocationFromAddress(riderPost.source));
//            }

            String driverSource = carpool.getDriverPost().source.replaceAll(" ", "+");
            String destination = carpool.getDriverPost().destination.replaceAll(" ", "+");

            System.out.println("MAPPING Carpool from source @ " + carpool.getDriverPost().source + "...");

            ArrayList<String> riderSources = new ArrayList<>();
            for (RideRequestPost riderPost : carpool.getRiderPosts()) {
                System.out.println("...to pickup rider @ " + riderPost.source + "...");
                riderSources.add(riderPost.source.replaceAll(" ", "+"));
            }

            System.out.println("...to destination @ " + carpool.getDriverPost().destination + ".");

            String urlString = getCarpoolUrlString(driverSource, destination, riderSources);

            System.out.println("URL = " + urlString);

            URL url = new URL(urlString);

            DocumentFetcher documentFetcher = new DocumentFetcher();

            mDocument = documentFetcher.execute(url).get();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    // Information methods:

    // returns the
    public int getTotalTripTime() {
        return getCarpoolTotalTripDurationValue(mDocument);
    }

    // Helper methods:

    private String getCarpoolUrlString(String driverSource, String destination, List<String> riderSources) {
        if (riderSources.isEmpty()) {
            System.out.println("ERROR: Mapper:riderSources.isEmpty()");
        }

        String urlString = "http://maps.googleapis.com/maps/api/directions/xml?"
                + "origin=" + driverSource // driverSource.latitude + "," + driverSource.longitude
                + "&destination=" + destination // destination.latitude + "," + destination.longitude
                + "&waypoints=optimize:true|"; // Use waypoints for pickup points.
        // NOTE: optimize:true means will reorder the waypoints to create the fastest route.

        Iterator<String> iterator = riderSources.iterator();

        while (iterator.hasNext()) {
            String riderSource = iterator.next();
            urlString += riderSource; // riderSource.latitude + "," + riderSource.longitude;

            if (iterator.hasNext()) {
                urlString += "|"; // Add "|" between multiple waypoint stops.
            }
        }

        urlString += "&sensor=false&units=metric&mode=driving";

        // TODO: add depart time & traffic estimate parameters...

        return urlString;
    }



    private int getCarpoolTotalTripDurationValue (Document doc) {
        int duration = 0;

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();

            /*
                XML format:
                <route>
                ...
                    <leg>
                        ...
                        <duration>
                            <value>1455</value>
                            <text>24 mins</text>
                        </duration>
                    </leg>
                    <leg>
                        ...
                    </leg>
                </route>
             */

            // Go to a list of "leg" tags, wit each leg tag having a "duration" tag with a "value" tag within it:
            NodeList nodeList = (NodeList) xPath.evaluate("//leg/duration/value", doc, XPathConstants.NODESET);

            System.out.println("nodeList.getLength() = " + nodeList.getLength());

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                System.out.println("Leg/Duration/Value: Node Name[" + i + "] = " + node.getNodeName());
                // OUTPUT = Leg/Duration/Value: Node Name[0] = value
                System.out.println("Leg/Duration/Value: Node Value[" + i + "] = " + node.getTextContent());
                // OUTPUT = Leg/Duration/Value: Node Value[0] = 1455

                int legDuration = Integer.parseInt(node.getTextContent());

                duration += legDuration;
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return duration;
    }

//    //This method returns the latitude and longitude of an address
//    private LatLng getLocationFromAddress(Context context, String strAddress) throws IOException {
//        Geocoder coder = new Geocoder(context);
//
//        List<Address> addressesFound = coder.getFromLocationName(strAddress, 1);
//
//        Address address = addressesFound.get(0);
//
//        return new LatLng(address.getLatitude(), address.getLongitude());
//    }

    //This method returns the latitude and longitude of an address
//    private LatLng getLocationFromAddress(String strAddress) throws IOException {
////        Geocoder coder = new Geocoder(context);
////
////        List<Address> addressesFound = coder.getFromLocationName(strAddress, 1);
////
////        Address address = addressesFound.get(0);
////
////        return new LatLng(address.getLatitude(), address.getLongitude());
//        return null;
//    }

//    // TODO: DELETE - TESTING ONLY:
//    public void test() {
//        String driverSource = "21250 Stevens Creek Blvd, Cupertino, CA 95014, USA".replaceAll(" ", "+");
//        // String driverSource = driverSource1.replaceAll(" ", "+");// "21250+Stevens+Creek+Blvd,+Cupertino,+CA+95014,+USA";// new LatLng(37.3195396, -122.0450548); //
//        String destination = "1+Washington+Sq,+San+Jose,+CA+95192,+USA"; // new LatLng(37.3351874, -121.8810715); //
//
//        ArrayList<String> riderSources = new ArrayList<>();
//        riderSources.add("470+N+10th+St,+San+Jose,+CA+95112,+USA"); // new LatLng(37.3487380, -121.8866706)); //
//
//        String urlString = getCarpoolUrlString(driverSource, destination, riderSources);
//
//        System.out.println("TEST URL = " + urlString);
//        // http://maps.googleapis.com/maps/api/directions/xml?origin=37.3195396,-122.0450548&destination=37.3351874,-121.8810715&waypoints=optimize:true|37.348738,-121.8866706&sensor=false&units=metric&mode=driving
//
//        try {
//            URL url = new URL(urlString);
//            DocumentFetcher documentFetcher = new DocumentFetcher();
//            Document document = documentFetcher.execute(url).get();
//
//            System.out.println("Document = " + document.toString());
//
//            System.out.println("Duration (int) = " + getCarpoolTotalTripDurationValue(document));
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//    }

}

