package com.example.ghostpb;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class Route {

    private ArrayList<RoutePoint> routePoints = new ArrayList<>();
    private ArrayList<Polyline> routeLines = new ArrayList<>();
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
    void setRoutePoints(ArrayList<RoutePoint> rp) { routePoints = rp; }

    //sets the backing arraylist to the given arraylist of RoutePoints
    public void setAllRoutePoints(ArrayList<RoutePoint> rp) {
        routePoints = rp;
    }

    public void addLine(Polyline line) {
        routeLines.add(line);
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
    public Route(String name) {
        routeID = 9001;
        routeName = name;
    }
}
