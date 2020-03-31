package com.example.ghostpb;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.maps.android.SphericalUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //the map
    private GoogleMap mMap;

    public DrawingAdapter drawingAdapter;

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

    private Route selectedRoute = null;

    private int selectedID = -1;

    //access to the chronometer
    protected ChronometerExtended timerFunctionality;
    private boolean chronometerRunning;

    //variables used to help draw the route live
    private LatLng lastPoint = new LatLng(0, 0);

    //array list of routes for holding the information pertaining to the users routes
    private ArrayList<Route> routesInformation = new ArrayList<>();

    private ArrayList<String> routesNames = new ArrayList<>();

    //array of the drawn polylines on the map
    private ArrayList<Polyline> polyLines = new ArrayList<>();

    //array of the drawn ghost circles on the map
    private ArrayList<Circle> ghostCircles = new ArrayList<>();

    //array of 'simulated' routes, used for DEMO D2
    //simulated routes can be created by using this resource: https://getlatlong.net
    private ArrayList<Route> demoRoutes = new ArrayList<>();

    //variable for holding the route the ghost is currently navigating
    private Route activeGhostRoute;

    //variable for holding the route that the user is currently navigating
    private Route activeUserRoute;

    //variable for working with periodic location updates provided by the fused location provider;
    private LocationCallback locationCallback;

    // The end time for a route
    private long endTime = 0;

    //buttons for the click event listeners
    private Button clearBtn;
    private Button stopBtn;
    private Button routesBtn;
    private Button newRouteBtn;
    private Button startRaceBtn;
    private Button stopRaceBtn;
    private Switch activeSwitch;

    private static final String ROUTE_TAG = "ROUTE";
    private static final String TEST_TAG = "ROUTE TEST";
    private static final int DISPLAY_ROUTES_CODE = 0;
    private static final String ROUTE_ID = "routeID";
    private static final String ROUTES_INFO = "routesInfo";
    private static final String CHRONOMETER_TAG = "TIME-TICK";
    private static final String CHAR_FILTER = "^[!@#$&()`.+,/\\\"]*$";
    private static final Pattern CHAR_PATTERN = Pattern.compile(CHAR_FILTER);

    // textview for distance tracker
    private TextView distanceCounter;

    public boolean racingAGhost;
    public int ghostPointLocation;
    public int ghostCounter = -1;

    public double totalDistance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //retrieve content view that renders the map
        setContentView(R.layout.activity_maps);

        //load the saved contents from the file
        readFile();

        // Widgets for the click event listeners
        clearBtn     = findViewById(R.id.clearMapButton);
        stopBtn      = findViewById(R.id.stopButton);
        routesBtn    = findViewById(R.id.routesButton);
        newRouteBtn  = findViewById(R.id.newRouteButton);
        startRaceBtn = findViewById(R.id.startRaceButton);
        stopRaceBtn  = findViewById(R.id.stopRaceButton);
        activeSwitch = findViewById(R.id.activeSwitch);

        // set distanceCounter to invisible by default
        distanceCounter = findViewById(R.id.distanceCounter);
        distanceCounter.setVisibility(View.INVISIBLE);

        // buttons associated with racing the ghost are invisible until the user has selected a route
        startRaceBtn.setVisibility(View.INVISIBLE);
        stopRaceBtn.setVisibility(View.INVISIBLE);

        //construct the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //set the chronometer variable used for stopwatch functionality
        timerFunctionality = findViewById(R.id.timerChronometer);
        //timerFunctionality.setFormat("Time: %s");
        timerFunctionality.setBase(SystemClock.elapsedRealtime());

        //the function that is called every second when the chronometer 60 times. This makes updateDeviceLocation get called once per second
        //in our case we cant to update the users location and save the route point to the proper route in the backing arraylist
        timerFunctionality.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            long mTicks = 0;
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                long elapsedMillis = SystemClock.elapsedRealtime() - timerFunctionality.getBase();

                if ((mTicks / 60) == 1 ) {
                    Log.d(CHRONOMETER_TAG,"ticking");
                    //call the custom function to update users location and store route information
                    updateDeviceLocation(currentlyMakingARoute, elapsedMillis, routeNumber);
                    mTicks = 0;
                }
                mTicks++;
                //Log.d(ROUTE_TAG, "Current elapsed millis" + elapsedMillis);
                //Log.d(ROUTE_TAG, "The current tick is : " + mTicks);
            }
        });


        activeSwitch.setChecked(false);
        activeSwitch.setClickable(false);


        //callback for when the location data of the user is available when they are making a route
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

            }
        };


        // Obtain the SupportMapFragment and get notified when the map is ready to be used. (i.e building the map here)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // This will auto-size the map to always be half the height and full width of the phone's screen
        /* Code Sources:
        https://stackoverflow.com/questions/21469345/how-do-you-programmatically-change-the-width-height-of-google-maps-v2-support
        http://www.androidtutorialshub.com/how-to-get-width-and-height-android-screen-in-pixels/
         */
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.e(ROUTE_TAG, "Device width of " + width);
        Log.e(ROUTE_TAG, "Device height of " + height);
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = height / 2;
        params.width = width;
        mapFragment.getView().setLayoutParams(params);


        // When the Stop button is pressed
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Stop button clicked");

                //update the text associated with the switch
                activeSwitch.setText(R.string.switch_offline);

                //change the state of the toggle
                activeSwitch.setChecked(false);

                //stop the thread that is keeping track of the elapsed time
                //timerThread.interrupt();

                //stop the timer
                timerFunctionality.stop();

                // Get the time it took
                //endTime = (SystemClock.elapsedRealtime() - timerFunctionality.getBase()) / 1000;
                //Log.d(ROUTE_TAG, "Time: " + DateUtils.formatElapsedTime(endTime));
                endTime = timerFunctionality.getTimePassed();

                //reset the variable keeping track of user location for live drawing of their route
                lastPoint = new LatLng(0, 0);

                //reset the ghost ticker index
                ghostPointLocation = 0;

                //reset the timer
                timerFunctionality.setBase(SystemClock.elapsedRealtime());

                //user just finished making a new route, handle this here
                if(currentlyMakingARoute) {
                    currentlyMakingARoute = false;

                    // Make the buttons unclickable so not pressed before finished with Dialog
                    clearBtn.setClickable(false);
                    stopBtn.setClickable(false);
                    routesBtn.setClickable(false);
                    newRouteBtn.setClickable(false);

                    // get input for the name of the route

                    // This is a popup box that will take input
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

                    //create the EditText that will take the typed input
                    final EditText input = new EditText(MapsActivity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);

                    // Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("Enter the name for this route:")
                            .setTitle(R.string.dialog_name_title);

                    // Add the buttons
                    builder.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked Enter button - Rename the route from the EditText if not empty, etc.
                            String newName = input.getText().toString().trim();
                            if (!newName.isEmpty() && !CHAR_PATTERN.matcher(newName).matches()) {
                                // Set route name
                                routesInformation.get(routeNumber).setName(newName);

                                // Set the time
                                routesInformation.get(routeNumber).setTime(endTime);

                                // A toast will pop up showing success
                                Toast toast = Toast.makeText(MapsActivity.this, newName + " saved", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                                
                                clearBtn.setClickable(true);
                                stopBtn.setClickable(true);
                                routesBtn.setClickable(true);
                                newRouteBtn.setClickable(true);

                                //when a new route is created remake the saved route file
                                writeFile();
                            }
                            else{
                                routesInformation.get(routeNumber).setName("Unnamed Route");

                                // Set the time
                                routesInformation.get(routeNumber).setTime(endTime);

                                // A toast pop up for invalid input. Will put route name to default
                                Toast toast = Toast.makeText(MapsActivity.this, "Invalid Input. Unnamed Route saved", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();

                                clearBtn.setClickable(true);
                                stopBtn.setClickable(true);
                                routesBtn.setClickable(true);
                                newRouteBtn.setClickable(true);
                            }

                        }
                    });
                    builder.setNegativeButton(R.string.dialog_delete_title, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog - do not save the route
                            routesInformation.remove(routeNumber);
                            routeNumber = routesInformation.size();
                            Toast toast = Toast.makeText(MapsActivity.this, "Route not saved", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();

                            clearBtn.setClickable(true);
                            stopBtn.setClickable(true);
                            routesBtn.setClickable(true);
                            newRouteBtn.setClickable(true);
                        }
                    });
                    AlertDialog dialog = builder.create();

                    // Add the EditText to the dialog
                    dialog.setView(input);

                    dialog.show();
                }

                distanceCounter.setVisibility(View.INVISIBLE);

                Log.d(ROUTE_TAG, "The user is not active");
            }
        });

        // When the Clear button is pressed
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Clear button clicked");

                //clearMap();
                drawingAdapter.clearMap();
            }
        });
 
        // When the Routes button is pressed, brings up the saved routes
        routesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start a new activity (a new screen on the app) that shows all of the available routes to select

                Log.d(ROUTE_TAG, "Routes button clicked");
                //make a new intent
                Intent intent = new Intent(MapsActivity.this, displayAvailableRoutesActivity.class);
                TextView activeRouteLabel = findViewById(R.id.activeRouteLabel);

                //add the array of names of the routes to the new intent
                intent.putParcelableArrayListExtra(ROUTES_INFO, routesInformation);

                //start the new window activity passing along the intent we just created
                startActivityForResult(intent, DISPLAY_ROUTES_CODE);

                /*
                //START: testing only here want to see if the route is being saved properly
                Log.d("TEST_TAG", " Size of route array: " + routesInformation.size());

                int size;
                //for each route stored
                for(int i = 0; i < routesInformation.size(); i++) {

                    size = routesInformation.get(i).getSize();

                    //for each point in the route stored
                    for(int x = 0; x < size; x++) {

                        Log.d("TEST_TAG", "Time: " + routesInformation.get(i).getPoint(x).getTime() + " Point: " + x + " Latitude: " + routesInformation.get(i).getPoint(x).getLocation().latitude + " Longitude: " + routesInformation.get(i).getPoint(x).getLocation().longitude);

                    }
                }

                //call draw routes on all the routes created
                for(int z = 0; z < routesInformation.size(); z++) {

                    Log.d("TEST_TAG", "Drawing a route");

                    drawRoute(routesInformation.get(z));
                }

                //END: testing only here want to see if the route is being saved properly
                 */
            }
        });


        // When the start race button is clicked (button is only available when the user has selected a route
        startRaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("GHOST-TEST", "Starting race against the ghost");

                //make all of the other UI buttons not-clickable
                routesBtn.setClickable(false);
                newRouteBtn.setClickable(false);
                stopBtn.setClickable(false);
                clearBtn.setClickable(false);

                //change the text of the toggle
                activeSwitch.setText(R.string.switch_active);
                //change the state of the toggle
                activeSwitch.setChecked(true);

                //start race button dissappears
                startRaceBtn.setVisibility(View.INVISIBLE);
                //stop race button appears
                stopRaceBtn.setVisibility(View.VISIBLE);

                racingAGhost = true;

                timerFunctionality.setBase(SystemClock.elapsedRealtime());
                timerFunctionality.start();


            }


        });

        // When the stop race button is clicked (button is only available when the user is currently racing a ghost
        stopRaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("GHOST-TEST", "Stopping the race");

                //make all of the other UI buttons not-clickable
                routesBtn.setClickable(true);
                newRouteBtn.setClickable(true);
                stopBtn.setClickable(true);
                clearBtn.setClickable(true);

                //change the text of the toggle
                activeSwitch.setText(R.string.switch_offline);
                //change the state of the toggle
                activeSwitch.setChecked(false);

                racingAGhost = false;

                stopRaceBtn.setVisibility(View.INVISIBLE);
                startRaceBtn.setVisibility(View.VISIBLE);

                timerFunctionality.stop();
                timerFunctionality.setBase(SystemClock.elapsedRealtime());

                ghostCounter = -1;


            }

        });

        // When the New Route button is pressed, starts the route making process
        newRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "New Routes button clicked");

                //increment how route number the user is working on
                routeNumber = routesInformation.size();

                routesInformation.add(new Route (routeNumber));

                //set the boolean keeping track of route making status to true
                currentlyMakingARoute = true;

                //change the text of the toggle
                activeSwitch.setText(R.string.switch_active);

                //change the state of the toggle
                activeSwitch.setChecked(true);

                //when the user is creating a new route
                stopBtn.setVisibility(View.VISIBLE);


                //make it so the new route button is no longer clickable, because they are starting the routing process now
                newRouteBtn.setClickable(false);
                routesBtn.setClickable(false);

                // set distanceCounter to visible while making new route AFTER 1 second has passed
                //distanceCounter.setVisibility(View.VISIBLE);
                distanceCounter.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        distanceCounter.setVisibility(View.VISIBLE);
                    }
                }, 1000);

                //calls custom function to start routing the route and tracking the users location and time
                startRoute(v);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (resultCode != Activity.RESULT_OK)return;

        if (requestCode == DISPLAY_ROUTES_CODE){
            if (intent == null) return;

            selectedID = intent.getIntExtra(ROUTE_ID, -1);

            Log.d("GHOST-TEST", "selectedID: " + selectedID);

            routesInformation = (ArrayList<Route>) intent.getSerializableExtra(ROUTES_INFO);

            if (routesInformation == null){
                routesInformation = new ArrayList<>();
            }
            routeNumber = routesInformation.size();

            //write the new array after user changes to the saved file
            writeFile();

            Log.d(ROUTE_TAG, "Got selected id: " + selectedID);
            Log.d(ROUTE_TAG, "Routes: " + routesNames);
            Log.d(ROUTE_TAG, "Routes Info: " + routesInformation);

            if (selectedID < 0) return;

            selectedRoute = routesInformation.get(selectedID);

            //draw the selected route to the screen
            //drawRoute(selectedRoute);

            //draw the selected route to the screen new refactor
            drawingAdapter.drawRoute(selectedRoute);

            //move camera to start of the route
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedRoute.getPoint(0).getLocation(),DEFAULT_ZOOM));
            //show the button to start the race against the ghost on the selected route
            showGhostRaceButton();


        }
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

        drawingAdapter = new DrawingAdapter(mMap);

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

        //show demo route
        //showDemoRoutes();

    }

    public void showGhostRaceButton() {
        startRaceBtn.setVisibility(View.VISIBLE);
    }


    public void startRoute(View view) {

        //Task<Location> currentLocation = fusedLocationProviderClient.getLastLocation();

        //get a handle to the active switch toggle
        Switch activeSwitch = findViewById(R.id.activeSwitch);

        //set the active switch to on
        activeSwitch.setChecked(true);

        //change the text associated with the active toggle
        activeSwitch.setText(R.string.switch_active);

        //start the thread that pushes the timer and textview updates
        //timerThread.start();

        //start the timer
        timerFunctionality.setBase(SystemClock.elapsedRealtime());
        timerFunctionality.start();

        currentlyMakingARoute = true;

    }

    //this function responds to when the user hits the active toggle
    public void onToggle(View view) {

    }


    //prompt user with box for device location permission
    private void requestLocationPermissions() {

        //if location permissions are already granted
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            boolLocationPermissionGranted = true;

        } else { //else ask the user for permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

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


    //this function updates the device location and saves the point to the associated route
    //updates the roamingLocation global variable
    private void updateDeviceLocation(final boolean makingARoute, final long timeWhenHappenned, final int routeNum) {

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
                                Log.d(ROUTE_TAG, "Roaming location updated");

                                locationNow = new LatLng(roamingLocation.getLatitude(), roamingLocation.getLongitude());
                                //if the user is currently making a route, add this information to the temporary store of the route points

                                //draw the route live
                                //drawRouteLive(lastPoint, locationNow);


                                //draw the route live new refactor
                                drawingAdapter.drawRouteLive(lastPoint, locationNow);

                                lastPoint = locationNow;

                                //if the user is currently racing a ghost, update the ghost location on the map
                                if(racingAGhost) {
                                    ghostPointLocation++;
                                    Log.d("GHOST TEST", "Racing against a ghost");

                                    //updateGhostLocation(activeGhostRoute, ghostPointLocation);

                                    //new refactor updateGhostLocation
                                    drawingAdapter.updateGhostLocation(selectedRoute, ghostPointLocation);

                                }

                                if(makingARoute) {

                                    //make a new route point and add it to the temporary structure
                                    routesInformation.get(routeNumber).addPoint(new RoutePoint(locationNow, timeWhenHappenned));
                                    // calculate the total distance of the route to this point
                                    totalDistance = calculateDistance(routesInformation.get(routeNumber));
                                    String distanceText = String.format(Locale.CANADA, "Total Distance: %f Meters", totalDistance);
                                    distanceCounter.setText(distanceText);
                                }


                            } else {
                                Log.d(ROUTE_TAG, "Problem updating roaming location");
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


    // Returns the routes total distance in meters
    private double calculateDistance(Route route) {
        double totalDistance = 0;
        for(int i=0; i<route.getSize()-1; i++) {
            LatLng p1 = route.getPoint(i).getLocation();
            LatLng p2 = route.getPoint(i+1).getLocation();
            totalDistance += SphericalUtil.computeDistanceBetween(p1, p2);
        }
        return totalDistance;
    }


    //this function will read from the saved file containing the routes information and re-populate
    //the global routes array based on the information stored in the text file when the app reboots
    //this simulates persistent storage for user
    private void readFile() {

        try {
            //setting up things needed to read from the file
            FileInputStream fis = openFileInput("routes.txt");
            InputStreamReader isr = new InputStreamReader(fis);

            BufferedReader br = new BufferedReader(isr);
            StringBuffer sb = new StringBuffer();

            String lines;

            //while there is still content in the file to read
            while((lines = br.readLine()) != null) {
                sb.append(lines);
            }

            //tested to show the correct file when the user exits the apps and then reloads
            //Log.d("FILE-TEST", "String buffer contains: " + sb.toString());


            //add code here to parse sb.toString() and re-populate the global routes array
            String delimiter = "//";

            String[] parsed = sb.toString().split(delimiter);

            //for(int i = 0; i < parsed.length; i++) {

                //Log.d("FILE-TEST", parsed[i]);
            //}

            //if there is data in the file that has been read
            if(parsed.length > 0) {

                //the array that we will be populating with routes created using the data in the saved file
                ArrayList<Route> returnRoutesList = new ArrayList<>();

                //the number of routes stored in the text file will always be on the first line
                //Log.d("FILE-TEST", "Saving number of routes with data: " + parsed[0]);
                int numberOfRoutes = Integer.parseInt(parsed[0]);

                //Log.d("FILE-TEST", "Saved number of routes");

                int currentLine = 1;

                //list to hold the sizes of the routes, needed to navigate through the saved file data in a specific fashion
                ArrayList<Integer> routeSizes = new ArrayList<>();
                boolean thereAreRoutes;

                if (numberOfRoutes == 0) {
                    thereAreRoutes = false;

                } else {

                    thereAreRoutes = true;

                    //starting at one because we have already added in information from the first route
                    for (int r = 0; r < numberOfRoutes; r++) {

                        //this is the last line of saved data
                        if (parsed[currentLine].equals("?????")) {


                        } else {

                            //Log.d("FILE-TEST", "Working with 1: " + parsed[currentLine]);

                            if (parsed[currentLine].equals("!!!!!")) {
                                //good
                                currentLine++;
                            }

                            //Log.d("FILE-TEST", "Inside the else");

                            //the name of this route will always be at currentLine
                            String localRouteName = parsed[currentLine];
                            currentLine++;


                            //Log.d("FILE-TEST", "Working with 2: " + parsed[currentLine]);

                            //the id of this route will always be at currentLine
                            int localRouteID = Integer.parseInt(parsed[currentLine]);
                            currentLine++;

                            //Log.d("FILE-TEST", "Working with 3: " + parsed[currentLine]);
                            //the size of the route will always be at currentLine
                            int localRouteSize = Integer.parseInt(parsed[currentLine]);

                            currentLine++;

                            //Log.d("FILE-TEST", "Working with 4: " + parsed[currentLine]);

                            //going to populate this array with route points stored in the file
                            ArrayList<RoutePoint> localRoutePoints = new ArrayList<>();

                            //Log.d("FILE-TEST", "just about to start looping for each route point in this route");
                            //for each route point saved in this route
                            for (int q = 0; q < localRouteSize; q++) {

                                //parse on the comma separating the two values
                                //Log.d("FILE-TEST", "currentLine: " + currentLine);
                                //Log.d("FILE-TEST", "parsed[currentLine] = " + parsed[currentLine]);

                               // String latitudeString = parsed[currentLine +1 ];
                               // String longitudeString = parsed[currentLine + 2];

                                 String latitudeString = parsed[currentLine];
                                 String longitudeString = parsed[currentLine+1];

                                //Log.d("FILE-TEST", "Latitude: " + latitudeString);
                                //Log.d("FILE-TEST", "Longitude: " + longitudeString);

                                //one line for each lat,long entry and one line for an empty line
                                currentLine = currentLine + 2;

                                //Log.d("FILE-TEST", "just about to make the location object using data" + latitudeString + "," + longitudeString);
                                //make a new LatLng object for the routepoint we are about to make
                                LatLng location = giveMeLatLng(latitudeString, longitudeString);

                                //make a new route point using the location above, and time is the current index in the loop
                                RoutePoint localRoutePoint = new RoutePoint(location, q);

                                //add this routepoint to the routepoints array we are building for this route, and then keep iterating
                                localRoutePoints.add(localRoutePoint);


                            }


                            //only do this is there are routes
                            if(thereAreRoutes) {
                                //we are just finished the loop that populates the route points
                                //our task now is to create a new route with the above information collected and then keep looping for each additional route
                                Route localRoute = new Route(localRoutePoints, localRouteID, localRouteName);

                                //add it to the array we are populating with the saved route information
                                returnRoutesList.add(localRoute);
                            } else {

                                //there are no routes, so we need an empty data file
                                String saveData = "0//";

                                //attempt to write the saveData to the saved file
                                try {
                                    FileOutputStream fos = openFileOutput("routes.txt", MODE_PRIVATE);
                                    fos.write(saveData.getBytes());
                                    fos.close();

                                    Log.d("FILE-TEST", "route data save");

                                } catch (FileNotFoundException e) {
                                    //catches openFileOutput
                                    Log.d("FILE-TEST", "route data save issue");
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    //catches fos.write
                                    Log.d("FILE-TEST", "route data save issue");
                                    e.printStackTrace();
                                }

                            }


                        }

                    }

                    //we have just finished the loop that runs for each route
                    //our task now is to populate the global array that holds the routes with this new route array we just created

                    //clear the information that is already stored there in the case of updating the saved file, not on app-reboot
                    routesInformation.clear();

                    //add all the routes we just created to the global routes list
                    routesInformation.addAll(returnRoutesList);


                }

            }





        } catch (FileNotFoundException e) {
            //catches openFileInput
            e.printStackTrace();
            Log.d("FILE-TEST", "No file written yet");
        } catch (IOException e) {
            //catches br.readLine()
            e.printStackTrace();
            Log.d("FILE-TEST", "Error with reader input");
        }



    }

    private LatLng giveMeLatLng(String lat, String lon) {

        double latitude = Double.parseDouble(lat);
        double longitude = Double.parseDouble(lon);

        return new LatLng(latitude, longitude);
    }


    //this function will request all routes to toString themselves
    //it will then write each route to a new file which replace the old one stored
    protected void writeFile() {

        String saveData = "";

        //add the amount of routes to the start
        saveData = saveData + routesInformation.size();
        saveData = saveData + "//";

        //for each route
        for(int i = 0; i < routesInformation.size(); i++) {
            //toString the route and add to saved data
            saveData = saveData + routesInformation.get(i).toString();
        }

        //add end of file characters
        saveData = saveData + "?????//";

        //Log.d("FILE-TEST", "Saved data: " + saveData);


        //attempt to write the saveData to the saved file
        try {
            FileOutputStream fos = openFileOutput("routes.txt", MODE_PRIVATE);
            fos.write(saveData.getBytes());
            fos.close();

            //Log.d("FILE-TEST", "route data save");

        } catch (FileNotFoundException e) {
            //catches openFileOutput
            //Log.d("FILE-TEST", "route data save issue");
            e.printStackTrace();
        } catch (IOException e) {
            //catches fos.write
            //Log.d("FILE-TEST", "route data save issue");
            e.printStackTrace();
        }

    }

    protected void setRoutesInformation(ArrayList<Route> routesInformation){
        this.routesInformation = routesInformation;
    }

    protected void setRouteNumber(int routeNumber){
        this.routeNumber = routeNumber;
    }

}


