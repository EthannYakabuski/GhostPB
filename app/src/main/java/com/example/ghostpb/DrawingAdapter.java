package com.example.ghostpb;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/* TO-DO
Replace old method calls in MapsActivity with with drawingFacade.methodCall();
 */

/*Facade design pattern supplying a simple interface to mapsActivity.java for all drawing related functionality*/
public class DrawingAdapter {
    //the backing map object
    private GoogleMap backingMap;

    //array of the drawn polylines on the map
    private ArrayList<Polyline> lines = new ArrayList<>();

    //array of the drawn ghost circles on the map
    private ArrayList<Circle> ghosts = new ArrayList<>();

    private static final String ROUTE_TAG = "ROUTE";
    private static final String TEST_TAG = "ROUTE TEST";

    private int ghostCounter = -1;


    /* singleton */
    private static final DrawingAdapter ourInstance = new DrawingAdapter();

    public static DrawingAdapter getInstance() {
        return ourInstance;
    }

    protected DrawingAdapter() {

    }

    DrawingAdapter(GoogleMap m) {

        backingMap = m;
    }

    /* singleton */


    void resetGhostPoint() {
        ghostCounter = -1;

    }

    public void addMap(GoogleMap m) {
        backingMap = m;
    }


    //FACADE INTERFACE FUNCTIONALITY

    //clears all of the ghosts on the map
    private void clearGhosts() {

        for(int i = 0; i < ghosts.size(); i++) {

            ghosts.get(i).remove();

        }

        ghosts.clear();
    }

    //clears all of the lines on the map
    private void clearLines() {

        for(int i = 0; i < lines.size(); i++) {

            lines.get(i).remove();


        }

        lines.clear();
    }

    //clears all of the ghosts and lines on the map
    void clearMap() {
        this.clearLines();
        this.clearGhosts();

    }

    //draws the given route to the map
    void drawRoute(Route route) {

        Log.d(ROUTE_TAG, "Drawing route: " + route.getName());

        for(int i = 1; i < route.getSize(); i++) {

            Polyline line = backingMap.addPolyline(new PolylineOptions()
                    .add(route.getPoint(i-1).getLocation(), route.getPoint(i).getLocation())
                    .width(5)
                    .color(Color.RED));

            lines.add(line);
        }

    }

    //this custom function draws a line between previous and current, simulating a cohesive line being drawn behind user as they are making a route
    void drawRouteLive(LatLng previous, LatLng current) {

        //if there is actual information in the last point
        if (!(previous.longitude == 0)) {
            //draws the poly line on the map between the previous point and the current point
            Polyline line = backingMap.addPolyline(new PolylineOptions()
                    .add(previous, current)
                    .width(5)
                    .color(Color.BLUE));

            lines.add(line);
        }
    }


    //this function updates the ghost location, it requires a route, and the time that the ghost is currently at one the route
    void updateGhostLocation(Route currentRoute, long timeWhenHappenned) {

        //keep the map clean from previous runs
        if(ghostCounter == 0) {
            //clear the map
            //clearMap();

            //clear the map new refactor
            this.clearMap();


            //drawRoute(selectedRoute);
            this.drawRoute(currentRoute);
        }

        //remove the previously drawn ghost
        //clearGhosts();

        //remove the previous drawn ghost refactor
        this.clearGhosts();

        ghostCounter++;

        //if the user is taking longer than the ghost, have the ghost wait at the finish line
        if(ghostCounter >= currentRoute.getSize()) {
            ghostCounter = currentRoute.getSize() -1;
        }


        //make a new ghost at the current point where the ghost is in the race
        Circle ghostCircle = backingMap.addCircle(new CircleOptions()
                .center(currentRoute.getPoint(ghostCounter).getLocation())
                .radius(8)
                .strokeColor(Color.BLACK)
                .fillColor(Color.BLACK));

        ghosts.add(ghostCircle);




    }


}

