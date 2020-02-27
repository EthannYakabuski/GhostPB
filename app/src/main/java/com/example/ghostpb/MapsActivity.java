package com.example.ghostpb;



import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputFilter;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.sql.Array;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
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

    private Route selectedRoute = null;

    private int selectedID = -1;

    //access to the chronometer
    private Chronometer timerFunctionality;
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

    //buttons for the click event listeners
    private Button clearBtn;
    private Button stopBtn;
    private Button routesBtn;
    private Button newRouteBtn;
    private Switch activeSwitch;

    private static final String EXTRA_MESSAGE = "com.example.ghostpb.MESSAGE";
    private static final String ROUTE_TAG = "ROUTE";
    private static final String TEST_TAG = "ROUTE TEST";
    private static final String DISPLAY_ROUTES = "display_routes";
    private static final int DISPLAY_ROUTES_CODE = 0;
    private static final String ROUTE_ID = "routeID";
    private static final String ROUTES_INFO = "routesInfo";

    public boolean racingAGhost;
    public int ghostPointLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //retrieve content view that renders the map
        setContentView(R.layout.activity_maps);

        // Widgets for the click event listeners
        clearBtn     = (Button) findViewById(R.id.clearMapButton);
        stopBtn      = (Button) findViewById(R.id.stopButton);
        routesBtn    = (Button) findViewById(R.id.routesButton);
        newRouteBtn  = (Button) findViewById(R.id.newRouteButton);
        activeSwitch = (Switch) findViewById(R.id.activeSwitch);

        //construct the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //set the chronometer variable used for stopwatch functionality
        timerFunctionality = findViewById(R.id.timerChronometer);
        timerFunctionality.setFormat("Time: %s");
        timerFunctionality.setBase(SystemClock.elapsedRealtime());


        //add the routes for DEMO D2
        populateDemoRoutes();

        //the function that is called every second when the chronometer ticks
        //in our case we cant to update the users location and save the route point to the proper route in the backing arraylist
        timerFunctionality.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {

                long elapsedMillis = SystemClock.elapsedRealtime() - timerFunctionality.getBase();

                //call the custom function to update users location and store route information
                updateDeviceLocation(currentlyMakingARoute, elapsedMillis, routeNumber);

            }
        });


        activeSwitch.setChecked(false);
        activeSwitch.setClickable(false);


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

                //set the buttons to be clickable again
                routesBtn.setClickable(true);
                newRouteBtn.setClickable(true);

                //update the text associated with the switch
                activeSwitch.setText(R.string.switch_offline);

                //change the state of the toggle
                activeSwitch.setChecked(false);

                //stop the thread that is keeping track of the elapsed time
                //timerThread.interrupt();

                //stop the timer
                timerFunctionality.stop();

                //reset the variable keeping track of user location for live drawing of their route
                lastPoint = new LatLng(0, 0);

                //reset the ghost ticker index
                ghostPointLocation = 0;

                //reset the timer
                timerFunctionality.setBase(SystemClock.elapsedRealtime());

                //user just finished making a new route, handle this here
                if(currentlyMakingARoute) {
                    currentlyMakingARoute = false;

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
                            if (!newName.isEmpty()) {
                                // Set route name
                                routesInformation.get(routeNumber).setName(newName);

                                // A toast will pop up showing success
                                Toast toast = Toast.makeText(MapsActivity.this, newName + " saved", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            }
                            else{
                                routesInformation.get(routeNumber).setName("Unnamed Route");

                                // A toast pop up for invalid input. Will put route name to default
                                Toast toast = Toast.makeText(MapsActivity.this, "Unnamed Route saved", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();
                            }

                        }
                    });
                    builder.setNegativeButton(R.string.dialog_delete_title, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog - do not save the route
                            Toast toast = Toast.makeText(MapsActivity.this, "Route not saved", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    });
                    AlertDialog dialog = builder.create();

                    // Add the EditText to the dialog
                    dialog.setView(input);

                    dialog.show();
                }

                Log.d(ROUTE_TAG, "The user is not active");
            }
        });

        // When the Clear button is pressed
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Clear button clicked");

                clearMap();
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

            int selectedID = intent.getIntExtra(ROUTE_ID, -1);
            routesInformation = (ArrayList<Route>) intent.getSerializableExtra(ROUTES_INFO);

            if (routesInformation == null){
                routesInformation = new ArrayList<>();
            }
            routeNumber = routesInformation.size();

            Log.d(ROUTE_TAG, "Got selected id: " + selectedID);
            Log.d(ROUTE_TAG, "Routes: " + routesNames);
            Log.d(ROUTE_TAG, "Routes Info: " + routesInformation);

            if (selectedID < 0) return;

            selectedRoute = routesInformation.get(selectedID);
            drawRoute(selectedRoute);
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


    public void showDemoRoutes() {

        for(int i = 0; i < demoRoutes.size(); i++) {
            drawRoute(demoRoutes.get(i));
        }

    }


    //this function populates the 'simulated' routes for use with DEMO D2
    //simulated routes can be created by using this resource: https://getlatlong.net

    //TO PLAY SCENARIO 1 for DEMO D2
    //see campusAveUserRoute.gpx in github 'folder gpx files'
    //https://mapstogpx.com/ to create a gpx file from a google maps direction link

    //TO PLAY GPX FILE:
    //hit '...' on emulated phone options bar at the bottom
    //hit location tab top right
    //hit Load GPS/KML button on bottom left
    //choose speed and hit play (comments below reflect that of a 1x speed playback)
    public void populateDemoRoutes() {


        Route campusAveLoop = new Route("Campus Ave Loop");

        //draws a line down Campus ave. Carleton University -- continue later

        //user is ahead of the ghost
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.385408, -75.696361), 1));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.385378, -75.696359), 2));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.385310,-75.696372), 3));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.385250, -75.696364), 4));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.385197, -75.696367), 5));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.385125, -75.696364), 6));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.385065,-75.696361), 7));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384990, -75.696364), 8));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384922, -75.696361), 9));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384877, -75.696364), 10));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384820, -75.696372), 11));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384752, -75.696370), 12));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384647, -75.696367), 13));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384587, -75.696364), 14));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384492, -75.696356), 15));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384440, -75.696353), 16));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384370, -75.696364), 17));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384291, -75.696351),18));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384238, -75.696348), 19));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384174, -75.696351), 20));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384129,-75.696353 ), 21));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.384080,-75.696345 ), 21));
        //user has built a substantial lead on the ghost, but is not travelling slower
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.383997,-75.696337 ), 22));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.383941, -75.696340), 23));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.383903, -75.696332), 24));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.383835,-75.696329 ), 25));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.383782, -75.696311), 26));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.383465,-75.696246), 27));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.383391 ,-75.696226 ), 28));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.383142 ,-75.696116 ), 29));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382970 ,-75.696037 ), 30));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382758 ,-75.695934 ), 31));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382713 ,-75.695900), 32));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382540 ,-75.695809 ), 33));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382395 ,-75.695694 ), 34));
        //the ghost has quickly closed the distance
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382295 ,-75.695568 ), 35));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382244 ,-75.695436), 36));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382274 ,-75.695286 ), 37));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382317 ,-75.695138 ), 38));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382367 ,-75.694985 ), 39));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382433 ,-75.694854 ), 40));
        campusAveLoop.addPoint(new RoutePoint(new LatLng(45.382483 ,-75.694694 ), 41));
        //finish behind the ghost


        demoRoutes.add(campusAveLoop);

        //activeGhostRoute = demoRoutes.get(0);

        //racingAGhost = true;

        //showDemoRoutes();



    }


    public void clearGhosts() {

        for(int i = 0; i < ghostCircles.size(); i++) {

            ghostCircles.get(i).remove();

        }

        ghostCircles.clear();
    }


    //this function clears everything that has been drawn on the map so far
    public void clearMap() {

        //for each polyline that has been drawn to the map
        for(int i = 0; i < polyLines.size(); i++) {

            //remove the polyline stored at this index
            polyLines.get(i).remove();
        }

        //empty the array holding the drawn polylines
        polyLines.clear();
        //mMap.clear();

        //if there is still a gost lingering on the map
        if(ghostCircles.size() >= 1) {
            clearGhosts();
        }
    }




    //this custom function draws the route live while the user is making it
    public void drawRouteLive(LatLng previous, LatLng current) {


        //if there is actual information in the last point
        if (!(previous.longitude == 0)) {
            //draws the poly line on the map between the previous point and the current point
            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(previous, current)
                    .width(5)
                    .color(Color.BLUE));

            polyLines.add(line);
        }
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


    private void drawRoute(Route route) {

        Log.d(ROUTE_TAG, "Drawing route: " + selectedRoute.getName());

        for(int i = 1; i < route.getSize(); i++) {

            /**
            Polyline testLine = mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(45.3826692, -75.6979663), new LatLng(46.3826692, -75.6979663))
                    .width(5)
                    .color(Color.RED));
             */


            Polyline line = mMap.addPolyline(new PolylineOptions()
                    .add(route.getPoint(i-1).getLocation(), route.getPoint(i).getLocation())
                    .width(5)
                    .color(Color.RED));

            polyLines.add(line);
        }

    }


    //this function updates the ghost location, it requires a route, and the time that the ghost is currently at one the route
    private void updateGhostLocation(Route currentRoute, long timeWhenHappenned) {

        clearGhosts();

        Log.d("GHOST TEST", "timeWhenHappenned: "+ timeWhenHappenned);

        for(int i = 0; i < currentRoute.getSize(); i++) {
            Log.d("GHOST TEST", "currentRoute.getPoint(i).getTime(): "+ currentRoute.getPoint(i).getTime());

            if(currentRoute.getPoint(i).getTime() == timeWhenHappenned) {

                Log.d("GHOST TEST","Current Ghost location found");

                //draw the ghost to the map
                Circle ghostCircle = mMap.addCircle(new CircleOptions()
                        .center(currentRoute.getPoint(i).getLocation())
                        .radius(5)
                        .strokeColor(Color.BLACK)
                        .fillColor(Color.BLACK));

                ghostCircles.add(ghostCircle);

            }
        }

    }


    //this function updates the device location and saves the point to the associated route
    //updates the roamingLocation global variable
    //

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

                                drawRouteLive(lastPoint, locationNow);

                                lastPoint = locationNow;

                                //if the user is currently racing a ghost, update the ghost location on the map
                                if(racingAGhost) {
                                    ghostPointLocation++;
                                    Log.d("GHOST TEST", "Racing against a ghost");

                                    updateGhostLocation(activeGhostRoute, ghostPointLocation);

                                }

                                if(makingARoute) {

                                    //make a new route point and add it to the temporary structure
                                    routesInformation.get(routeNumber).addPoint(new RoutePoint(locationNow, timeWhenHappenned));

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

}


