package com.example.ghostpb;

import java.util.ArrayList;

public class Route {

    ArrayList<RoutePoint> routePoints = new ArrayList<>();

    //adds the given RoutePoint object to the backing arraylist
    public void addPoint(RoutePoint p) {
        routePoints.add(p);
    }


    //sets the backing arraylist to the given arraylist of RoutePoints
    public void setAllRoutePoints(ArrayList<RoutePoint> rp) {
        routePoints = rp;
    }



    public Route() {
    }
    public Route(ArrayList<RoutePoint> rp) {
        routePoints = rp;
    }
}
