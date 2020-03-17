package com.example.ghostpb;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Locale;

class Navigate {
    // Instance of this class, only one instance is needed.
    private static final Navigate ourInstance = new Navigate();

    // Returns instance
    public static Navigate getInstance() {
        return ourInstance;
    }

    private Navigate() {
    }

    // Returns the distance between two LatLng points
    public double calculateDistance(LatLng p1, LatLng p2) {
        Log.d("CalculateDistance", "RUNNING");
        if (p1 == null) {
            Log.d("CalculateDistance", "p1 is null");
            return 0; }
        if (p2 == null) {
            Log.d("CalculateDistance", "p2 is null");
            return 0; }
        Log.d("CalculateDistance", "Returning distance");
        return SphericalUtil.computeDistanceBetween(p1, p2);
    }

    // Returns the routes total distance in meters
    public double calculateRouteDistance(Route route) {
        if (route == null) { return -1; }
        double totalDistance = 0;
        for(int i=0; i<route.getSize()-1; i++) {
            LatLng p1 = route.getPoint(i).getLocation();
            LatLng p2 = route.getPoint(i+1).getLocation();
            totalDistance += calculateDistance(p1, p2);
        }
        return totalDistance;
    }

    // Returns the closest point in the ghost's route to the users location
    // Takes in the ghost's route as input, and the users current location as input
    // Returns the closest point in ghost's route
    public RoutePoint findClosestRoutePoint(Route route, LatLng userPoint) {
        if (route == null) { return null; }
        if (route.getPoint(0) == null) { return null; }
        if (userPoint == null) { return null; }
        RoutePoint closestPoint = route.getPoint(0);
        for(int i=0; i<route.getSize()-1; i++) {
            if (calculateDistance(route.getPoint(i).getLocation(), userPoint)
                    < calculateDistance(closestPoint.getLocation(), userPoint))
                closestPoint = route.getPoint(i);
        }
        return closestPoint;
    }

    public double distanceFromRoute(Route route, LatLng userPoint) {
        if (route == null) { return 0; }
        if (userPoint == null) { return 0; }
        RoutePoint closestPoint = findClosestRoutePoint(route, userPoint);
        return calculateDistance(closestPoint.getLocation(), userPoint);
    }

    // Checks if the user is still sticking to the route. Design is still in progress
    // Idea is: Check at each user location update if the user is within X (possibly 10?) meters
    // of the route. If the user if off track for X seconds/updates, give them a warning (this
    // is not to be done in this function, either it is done in the logic calling this function, or
    // it can be done in another function in this class), if the user is off track for 2X or
    // 3X seconds/updates, the race terminates. Ex: 15 seconds receive warnings, 30 or 45 seconds terminate
    // The values can be adjusted as necessary.
    // Requires Ghost's route as input and user location as input
    // Returns true if on track, false if off track
    public boolean checkIfUserOnTrack(Route route, LatLng userPoint) {
        if (route == null) { return false; }
        if (userPoint == null) { return false; }
        // For now, this just checks if were within the distance of the closest point of the route
        // Ideally we'd want to know the previous point before we went off track and make sure we get
        // back within distance of THAT point. That is still to be done.
        double distanceFromTrack = distanceFromRoute(route, userPoint);
        if(distanceFromTrack > 100) {
            return false;
        }
        else {
            return true;
        }
    }

    // This is meant to be used to help determine if the ghost is ahead or behind
    public int findIndexOfRoute(Route route, LatLng p1) {
        if (route == null) { return -1; }
        if (p1 == null) { return -1; }
        for (int i=0; i<route.getSize()-1; i++) {
            if(p1 == route.getPoint(i).getLocation()) {
                return i;
            }
        }
        return -1;
    }



    // TO DO - Implement
    // Returns the distance from the user to the ghost.
    // Requires Ghost's route and location as input, and user location as input
    /*
    public double calculateDistanceFromGhost(Route route, LatLng ghostLocation, LatLng userLocation) {
        if (route == null) { return -1; }
        if (ghostLocation == null) { return -1; }
        if (userLocation == null) { return -1; }
        RoutePoint closestPoint = findClosestRoutePoint(route, userLocation);
        int userIndex = findIndexOfRoute(route, closestPoint.getLocation());
        int ghostIndex = findIndexOfRoute(route, ghostLocation);
        // user > ghost -> user ahead, ghost > user -> ghost ahead, user == ghost -> close together
        if(userIndex == ghostIndex) {
            return calculateDistance(ghostLocation, userLocation);
        }
        else if(userIndex > ghostIndex) {
            // Find the subarray route[ghostIndex:userIndex] and then
            //calculateRouteDistance(route[ghostIndex:userIndex]);
        }
        else {
            // Find the subarray route[userIndex:ghostIndex] and then
            //calculateRouteDistance(route[userIndex:ghostIndex]);
        }
        return 0;
    }*/
    // This one is just temporary for testing
    public double calculateDistanceFromGhost(LatLng ghostLocation, LatLng userLocation) {
        if (ghostLocation == null) { return -1; }
        if (userLocation == null) { return -1; }
        return calculateDistance(ghostLocation, userLocation);
    }



    // TO DO - Design and Implement
    // Directs the user to the start of the route.
    // Google Maps API provides something for this, just need to mess around with it
    // and see how it works
    // Requires at least the first point of the Ghost's route as input and user location as input
    public void navigateToRouteStart(Route route) {

    }
}
