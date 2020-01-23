package com.example.ghostpb;

import java.util.ArrayList;

public class Route {

    private ArrayList<RoutePoint> routePoints = new ArrayList<>();
    private int routeID;
    private String routeName;

    //adds the given RoutePoint object to the backing arraylist
    void addPoint(RoutePoint p) {
        routePoints.add(p);
    }

    RoutePoint getPoint(int i) {
        return routePoints.get(i);
    }

    int getSize() { return routePoints.size(); }
    String getName() { return routeName; }

    void setName(String rN) { routeName = rN; }

    //sets the backing arraylist to the given arraylist of RoutePoints
    public void setAllRoutePoints(ArrayList<RoutePoint> rp) {
        routePoints = rp;
    }

    Route(int id) {
        routeID = id;
        routeName = "Route " + Integer.toString(id);
    }
    public Route(ArrayList<RoutePoint> rp, int id) {
        routePoints = rp;
        routeID = id;
        routeName = "Route " + Integer.toString(id);
    }
}
