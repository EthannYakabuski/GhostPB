package com.example.ghostpb;

import java.util.ArrayList;

public class Route {

    private ArrayList<RoutePoint> routePoints = new ArrayList<>();
    private int routeID;

    //adds the given RoutePoint object to the backing arraylist
    void addPoint(RoutePoint p) {
        routePoints.add(p);
    }

    RoutePoint getPoint(int i) {
        return routePoints.get(i);
    }

    int getSize() { return routePoints.size(); }


    //sets the backing arraylist to the given arraylist of RoutePoints
    public void setAllRoutePoints(ArrayList<RoutePoint> rp) {
        routePoints = rp;
    }



    Route(int id) {
        routeID = id;
    }
    public Route(ArrayList<RoutePoint> rp, int id) {
        routePoints = rp;
        routeID = id;
    }
}
