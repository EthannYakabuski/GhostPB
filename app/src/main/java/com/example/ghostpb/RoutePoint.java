package com.example.ghostpb;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;


//class for holding a LatLng object and a Time object,
//over the course of making a route the user will generate many of these,
//the information will be later used to draw the route using polylines functionality
public class RoutePoint {

    //holds the latitude and longitude of the user when this route point was created
    private LatLng location;
    //holds a time object that was created when the user was at the above location
    private long timeAtLocation;

    //returns the LatLng object private variable location
    LatLng getLocation() {

        return location;
    }

    //returns the Time object associated with the above LatLng object
    public long getTime() {

        return timeAtLocation;

    }

    //constructor
    RoutePoint(LatLng latLngObj, long time) {

        location = latLngObj;
        timeAtLocation = time;

    }

}
