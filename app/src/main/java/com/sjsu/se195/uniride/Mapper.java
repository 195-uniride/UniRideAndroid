package com.sjsu.se195.uniride;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
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


    // TODO: DELETE - TESTING ONLY:
    public void test() {
        LatLng driverSource = new LatLng(37.3195396, -122.0450548); // 21250 Stevens Creek Blvd, Cupertino, CA 95014, USA
        LatLng destination = new LatLng(37.3351874, -121.8810715); // 1 Washington Sq, San Jose, CA 95192, USA

        ArrayList<LatLng> riderSources = new ArrayList<>();
        riderSources.add(new LatLng(37.3487380, -121.8866706)); // 470 N 10th St, San Jose, CA 95112, USA

        String urlString = getUrlString(driverSource, destination, riderSources);

        System.out.println("Test URL = " + urlString);
        // http://maps.googleapis.com/maps/api/directions/xml?origin=37.3195396,-122.0450548&destination=37.3351874,-121.8810715&waypoints=optimize:true|37.348738,-121.8866706&sensor=false&units=metric&mode=driving

        // TODO: System.out.println("Duration = " + getDurationValue(getDocument(urlString)));

        try {
            URL url = new URL(urlString);
            DocumentFetcher documentFetcher = new DocumentFetcher();
            Document document = documentFetcher.execute(url).get();

            System.out.println("Document = " + document.toString());

            System.out.println("Duration (int) = " + getTotalTripDurationValue(document));

            System.out.println("Duration (text) = " + getDurationText(document));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        // Try 2:
//        try {
//            Document doc = (Document) new GMapV2Direction().execute(driverSource, destination).get();
//
//            System.out.println("Document = " + doc);
//
//            System.out.println("Duration = " + getDurationValue(doc));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }

    }

    private class DocumentFetcher extends AsyncTask<URL, Void, Document> {

        @Override
        protected Document doInBackground(URL... urls) {
            return getDocument(urls[0]);
        }

        private Document getDocument(URL url) {
            Document document = null;

            try {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                document = builder.parse(in);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return document;
        }
    }

    public String getUrlString(LatLng driverSource, LatLng destination, List<LatLng> riderSources) {
        if (riderSources.isEmpty()) {
            System.out.println("ERROR: Mapper:riderSources.isEmpty()");
        }

        String urlString = "http://maps.googleapis.com/maps/api/directions/xml?"
                + "origin=" + driverSource.latitude + "," + driverSource.longitude
                + "&destination=" + destination.latitude + "," + destination.longitude
                + "&waypoints=optimize:true|"; // Use waypoints for pickup points.
        // NOTE: optimize:true means will reorder the waypoints to create the fastest route.

        Iterator<LatLng> iterator = riderSources.iterator();

        while (iterator.hasNext()) {
            LatLng riderSource = iterator.next();
            urlString += riderSource.latitude + "," + riderSource.longitude;

            if (iterator.hasNext()) {
                urlString += "|"; // Add "|" between multiple waypoint stops.
            }
        }

        urlString += "&sensor=false&units=metric&mode=driving";

        System.out.println("Mapper: Created URL String = " + urlString);

        return urlString;
    }



    public int getTotalTripDurationValue (Document doc) {
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

    public String getDurationText (Document doc) {
        NodeList nl1 = doc.getElementsByTagName("duration");
        Node node1 = nl1.item(0);
        NodeList nl2 = node1.getChildNodes();
        Node node2 = nl2.item(getNodeIndex(nl2, "text"));
        Log.i("DurationText", node2.getTextContent());
        return node2.getTextContent();
    }

    private int getNodeIndex(NodeList nl, String nodename) {
        for(int i = 0 ; i < nl.getLength() ; i++) {
            if(nl.item(i).getNodeName().equals(nodename))
                return i;
        }
        return -1;
    }


}
