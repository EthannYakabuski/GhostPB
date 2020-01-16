package com.example.ghostpb;



import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.sql.Array;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //the map
    private GoogleMap mMap;

    //fused location provider
    //use this to access current locaation information in the form of a LatLng object
    private FusedLocationProviderClient fusedLocationProviderClient;

    //boolean set to true when permission is granted to access location data by user
    private boolean boolLocationPermissionGranted;

    //for verbosity, used to check is location permissions have been granted. (1 is positive status code)
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //variable to hold the default zoom of the camera controlling the map
    private static final int DEFAULT_ZOOM = 15;

    //the last known location of the users device retrieved by the fused location provider
    private Location lastKnowLocation;

    //location of the user for when they are doing a route
    private Location roamingLocation;
    private LatLng locationNow;
    //holds the route points for the route currently being created
    ArrayList<RoutePoint> routePoints = new ArrayList<>();
    //holds the number associated with the route you are currently making
    private int routeNumber = -1;

    //default location for when location permissions are not granted
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    //true when user is currently making a new route
    private boolean currentlyMakingARoute;

    //array list of routes for holding the information pertaining to the users routes
    private ArrayList<Route> routesInformation = new ArrayList<>();


    //variables used for working with the timer functionality
    //int to hold the seconds since started timing
    private int time;
    //the textview that displays the time
    private TextView timer;
    //the thread pushing the updates to the textview
    Thread timerThread;

    //variable for working with periodic location updates provided by the fused location provider;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //retrieve content view that renders the map
        setContentView(R.layout.activity_maps);

        //construct the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //set the timer variable
        timer = findViewById(R.id.timerChronometer);


        //create the thread and runnable that will be used to drive the timer, and the updates to the timer TextView
        timerThread = new Thread() {

            @Override
            public void run() {


                //
                while(!isInterrupted()) {


                    try {
                        //run this threads code every second
                        Thread.sleep(1000);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                //increment the time elapsed
                                time++;

                                //update the TextView
                                timer.setText(String.valueOf(time));


                                //get the location of the user
                                try {
                                    if(boolLocationPermissionGranted) {

                                        updateDeviceLocation(currentlyMakingARoute, time, routeNumber);


                                    }



                                } catch (SecurityException e) {
                                    if(!(e.getMessage() == null)) {
                                        Log.e("Exception: %s", e.getMessage());
                                    }
                                }


                            }

                        });

                    } catch (InterruptedException e) {
                        //we got interrupted by the user, no more ticking of time

                        //reset the time variable and the textview
                        time = 0;
                        timer.setText(String.valueOf(time));
                        return;
                    }
                }

            }
        };





        //callback for when the location data of the user is available when they are making a route
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult == null) {
                    return;
                }


            }
        };


        // Obtain the SupportMapFragment and get notified when the map is ready to be used. (i.e building the map here)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //calls the custom function to ask the user for location permissions
        requestLocationPermissions();

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
       //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //turn on myLocation control button on the map (top right set my location button)
        updateLocationUI();



        //finds the phone and sets the map to go to users current location
        showDeviceLocation();


    }


    //this function will start requesting the users location every second and saving
    //LatLng objects along with time information to create ghost data at the end of the session
    public void startRoute(View view) {

        //Task<Location> currentLocation = fusedLocationProviderClient.getLastLocation();

        //get a handle to the active switch toggle
        Switch activeSwitch = findViewById(R.id.activeSwitch);

        //set the active switch to on
        activeSwitch.setChecked(true);

        //change the text associated with the active toggle
        activeSwitch.setText("Active");


        //start the thread that pushes the timer and textview updates
        timerThread.start();

    }

    //this function responds to when the Routes or New Route button has been clicked
    public void onRoutes(View view) {

        //tell logcat user is doing something with routes
        Log.d("Route", "User has clicked a button associated with routes");
        Log.d("Route", "view.getId() = " + view.getId());
        Log.d("Route", "R.id.routesButton = " + R.id.routesButton);

        //when the user hits the routes button
        //bring up the users saved routes
        if(view.getId() == R.id.routesButton) {


            //START: testing only here want to see if the route is being saved properly
            Log.d("ROUTE TEST", " Size of route array: " + routesInformation.size());
            Log.d("ROUTE TEST", " Size of route in the first spot in the routes array: " + routesInformation.get(0).getSize());


            int size;
            //for each route stored
            for(int i = 0; i < routesInformation.size(); i++) {

                size = routesInformation.get(i).getSize();

                //for each point in the route stored
                for(int x = 0; x < size; x++) {

                    Log.d("ROUTE TEST", "Point: " + x + " Latitude: " + routesInformation.get(i).getPoint(x).getLocation().latitude + " Longitude: " + routesInformation.get(i).getPoint(x).getLocation().longitude);



                }

            }




            //END: testing only here want to see if the route is being saved properly

        }

        //when the user hits the new route button
        //start the route making process
        if (view.getId() == R.id.newRouteButton) {

            //increment how route number the user is working on
            routeNumber++;

            routesInformation.add(new Route(routeNumber));
            //tell logcat user is creating a route
            Log.d("Route", "User has clicked new route");

            //set the boolean keeping track of route making status to true
            currentlyMakingARoute = true;

            //get a handle to the new route button
            Button newRoute = findViewById(R.id.newRouteButton);

            //handle to the regular routes button
            Button routesButton = findViewById(R.id.routesButton);

            //make it so the new route button is no longer clickable, because they are starting the routing process now
            newRoute.setClickable(false);
            routesButton.setClickable(false);

            //calls custom function to start routing the route and tracking the users location and time
            startRoute(view);


        }



    }

    //this function responds to when the user hits the active toggle
    public void onToggle(View view) {

        Switch activeSwitch = findViewById(R.id.activeSwitch);

        //get a handle to the new route button
        Button newRoute = findViewById(R.id.newRouteButton);

        //handle to the regular routes button
        Button routesButton = findViewById(R.id.routesButton);

        //if the user hit the toggle from offline to go online
        if(activeSwitch.isChecked()) {

            Log.d("Route", "The user is active");

            //update the text associated with the switch
            activeSwitch.setText("Active");

        //the user hit the toggle to go from online to offline
        } else {

            //set the buttons to be clickable again
            newRoute.setClickable(true);
            routesButton.setClickable(true);

            //update the text associated with the switch
            activeSwitch.setText("Offline");

            //stop the thread that is keeping track of the elapsed time
            timerThread.interrupt();


            //user just finished making a new route, handle this here
            if(currentlyMakingARoute) {
                currentlyMakingARoute = false;


            }



            Log.d("Route", "The user is not active");

        }

    }


    //prompt user with box for device location permission
    private void requestLocationPermissions() {

        //if location permissions are already granted
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            boolLocationPermissionGranted = true;

        } else { //else ask the user for permission
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        }

    }

    //callback for requestLocationpermissions()
    @Override
    public void onRequestPermissionsResult(int code,@NonNull String[] permissions,@NonNull int[] allowResults) {
        boolLocationPermissionGranted = false;

        switch(code) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                //if permission has not been granted the array would be empty
                if(allowResults.length > 0 && allowResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolLocationPermissionGranted = true;
                }
            }
        }

        //update the UI based on permissions
        updateLocationUI();


    }


    //after location permissions are granted, this function will update the map UI
    private void updateLocationUI() {

        //check to see if the map exists first
        if (mMap == null) {
            return;
        }

        try {
            //if location permission is already granted
            if(boolLocationPermissionGranted) {
                //tell the map that permission has been granted
                mMap.setMyLocationEnabled(true);

                //adds the button that jumps to your current location
                mMap.getUiSettings().setMyLocationButtonEnabled(true);





            //location permission is not on yet, so ask the user for these permissions
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                lastKnowLocation = null;

            }

            //catch errors
        } catch (SecurityException e) {
            if(!(e.getMessage() == null)) {
                Log.e("Exception: %s", e.getMessage());
            }
        }

    }


    //this function simply updates the device location and does nothing else
    //updates the roamingLocation global variable

    private void updateDeviceLocation(final boolean makingARoute, final int timeWhenHappenned, final int routeNum) {

        try {
            if(boolLocationPermissionGranted) {
                Task<Location> currentLocation = fusedLocationProviderClient.getLastLocation();

                currentLocation.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    //called when the fusedLocationProviderClient is done finding the last known location
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        //give coordinates of current location to the camera controlling the view of the map
                        if(task.isSuccessful()) {

                            //save the result of the task (finding the last known location) to the lastKnowLocation private variable
                            roamingLocation = task.getResult();

                            //if there was a result
                            if (roamingLocation != null) {
                                Log.d("Route", "Roaming location updated");

                                locationNow = new LatLng(roamingLocation.getLatitude(), roamingLocation.getLongitude());
                                //if the user is currently making a route, add this information to the temporary store of the route points
                                if(makingARoute) {

                                    //make a new route point and add it to the temporary structure
                                    routesInformation.get(routeNum).addPoint(new RoutePoint(locationNow, timeWhenHappenned));

                                }


                            } else {
                                Log.d("Route", "Problem updating roaming location");
                            }

                        }


                    }

                });


            }

            //catch errors
        } catch (SecurityException e) {
            if(!(e.getMessage() == null)) {
                Log.e("Exception: %s", e.getMessage());
            }
        }



    }

    private void showDeviceLocation() {


        try {
            if(boolLocationPermissionGranted) {
                Task<Location> currentLocation = fusedLocationProviderClient.getLastLocation();

                currentLocation.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    //called when the fusedLocationProviderClient is done finding the last known location
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        //give coordinates of current location to the camera controlling the view of the map
                        if(task.isSuccessful()) {

                            //save the result of the task (finding the last known location) to the lastKnowLocation private variable
                            lastKnowLocation = task.getResult();

                            //if there was a result
                            if (lastKnowLocation != null) {

                                //move the camera to the last known location
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude()), DEFAULT_ZOOM));

                            }

                        } else {

                                //the location was null, use the default location
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));

                                //turn the find my location button off the GUI
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                            }

                        }

                });


            }

            //catch errors
        } catch (SecurityException e) {
            if(!(e.getMessage() == null)) {
                Log.e("Exception: %s", e.getMessage());
            }
        }


    }

}
