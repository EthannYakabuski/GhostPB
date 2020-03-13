package com.example.ghostpb;

import com.google.android.gms.maps.model.LatLng;

/* Private class data design pattern
 */

/* TO-DO:
integrate into RoutePoint.java
 */
public class RoutePointDataClass {

    //holds the latitude and longitude of the user when this route point was created
    private LatLng location;
    //holds a time object that was created when the user was at the above location
    private long timeAtLocation;

    public RoutePointDataClass(LatLng loc, long time) {
        location = loc;
        timeAtLocation = time;
    }

    public LatLng getLocation() {
        return location;
    }

    public long getTimeAtLocation() {
        return timeAtLocation;
    }

}
