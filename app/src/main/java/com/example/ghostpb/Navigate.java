package com.example.ghostpb;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

class Navigate {
    private static final Navigate ourInstance = new Navigate();

    public static Navigate getInstance() {
        return ourInstance;
    }

    private Navigate() {
    }

    // Returns the routes total distance in meters
    public double calculateRouteDistance(Route route) {
        double totalDistance = 0;
        for(int i=0; i<route.getSize()-1; i++) {
            LatLng p1 = route.getPoint(i).getLocation();
            LatLng p2 = route.getPoint(i+1).getLocation();
            totalDistance += SphericalUtil.computeDistanceBetween(p1, p2);
        }
        return totalDistance;
    }

    // TO DO - Implement
    // Returns the distance from the user to the ghost.
    // Requires Ghost's route and location as input, and user location as input
    /*public double calculateDistanceFromGhost(Route route, LatLng ghostLocation, LatLng userLocation) {
        return 0;
    }*/
    // This one is just temporary for testing
    public double calculateDistanceFromGhost(LatLng ghostLocation, LatLng userLocation) {
        //return SphericalUtil.computeDistanceBetween(ghostLocation, userLocation);
        return 0;
    }

    // TO DO - Design and Implement
    // Checks if the user is still sticking to the route. Design is still in progress
    // Idea is: Check at each user location update if the user is within X (possibly 10?) meters
    // of the route. If the user if off track for X seconds/updates, give them a warning (this
    // is not to be done in this function, either it is done in the logic calling this function, or
    // it can be done in another function in this class), if the user is off track for 2X or
    // 3X seconds/updates, the race terminates. Ex: 15 seconds receive warnings, 30 or 45 seconds terminate
    // The values can be adjusted as necessary.
    // Requires Ghost's route as input and user location as input
    public boolean checkIfUserOnTrack(Route route) {
        return false;
    }

    // TO DO - Design and Implement
    // Directs the user to the start of the route.
    // Google Maps API provides something for this, just need to mess around with it
    // and see how it works
    // Requires at least the first point of the Ghost's route as input and user location as input
    public void navigateToRouteStart(Route route) {

    }






}
