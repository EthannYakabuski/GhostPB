package com.example.ghostpb;

import java.util.ArrayList;

public class MapsActivityFunctions {

    private ArrayList<Route> routesInformation = new ArrayList<>();
    private int routeNumber;

    protected void setRoutesInformation(ArrayList<Route> routesInformation){
        this.routesInformation = routesInformation;
    }

    protected void setRouteNumber(int routeNumber){
        this.routeNumber = routeNumber;
    }
}
