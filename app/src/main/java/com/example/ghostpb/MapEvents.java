package com.example.ghostpb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class MapEvents {

    //variables used to help draw the route live
    private LatLng lastPoint = new LatLng(0, 0);

    //holds the number associated with the route you are currently making
    protected int routeNumber = -1;

    //true when user is currently making a new route
    private boolean currentlyMakingARoute;

    // The time for when a route is finished
    private long endTime = 0;

    //array list of routes for holding the information pertaining to the users routes
    protected ArrayList<Route> routesInformation = new ArrayList<>();

    private String newName;

    //buttons for the click event listeners
    private Button clearBtn;
    private Button stopBtn;
    private Button routesBtn;
    private Button newRouteBtn;
    private Button startRaceBtn;
    private Button stopRaceBtn;
    private Switch activeSwitch;

    // private Context mapsActivity;

    private static final String ROUTE_TAG = "ROUTE";
    private static final String TEST_TAG = "ROUTE TEST";
    private static final int DISPLAY_ROUTES_CODE = 0;
    private static final String ROUTE_ID = "routeID";
    private static final String ROUTES_INFO = "routesInfo";
    private static final String CHAR_FILTER = "^[!@#$&()`.+,/\\\"]*$";
    private static final Pattern CHAR_PATTERN = Pattern.compile(CHAR_FILTER);
    private static final int MAX_NAME_LENGTH = 15;

    // textview for distance tracker
    private TextView distanceCounter;

    private boolean racingAGhost;
    private int ghostPointLocation;
    private int ghostCounter = -1;

    /* Singleton */
    private static final MapEvents ourInstance = new MapEvents();

    public static MapEvents getInstance() {
        return ourInstance;
    }

    private MapEvents() {}
    /* Singleton */

    protected void init(final Context mapsActivity){

        // Widgets for the click event listeners
        clearBtn     = (Button) ((MapsActivity) mapsActivity).findViewById(R.id.clearMapButton);
        stopBtn      = (Button) ((MapsActivity) mapsActivity).findViewById(R.id.stopButton);
        routesBtn    = (Button) ((MapsActivity) mapsActivity).findViewById(R.id.routesButton);
        newRouteBtn  = (Button) ((MapsActivity) mapsActivity).findViewById(R.id.newRouteButton);
        startRaceBtn = (Button) ((MapsActivity) mapsActivity).findViewById(R.id.startRaceButton);
        stopRaceBtn  = (Button) ((MapsActivity) mapsActivity).findViewById(R.id.stopRaceButton);
        activeSwitch = (Switch) ((MapsActivity) mapsActivity).findViewById(R.id.activeSwitch);

        // set distanceCounter to invisible by default
        distanceCounter = (TextView) ((MapsActivity) mapsActivity).findViewById(R.id.distanceCounter);
        distanceCounter.setVisibility(View.INVISIBLE);

        // buttons associated with racing the ghost are invisible until the user has selected a route
        startRaceBtn.setVisibility(View.INVISIBLE);
        stopRaceBtn.setVisibility(View.INVISIBLE);

        activeSwitch.setChecked(false);
        activeSwitch.setClickable(false);

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
                ((MapsActivity) mapsActivity).timerFunctionality.stop();

                // Get the time it took
                endTime = (SystemClock.elapsedRealtime() - ((MapsActivity) mapsActivity).timerFunctionality.getBase()) / 1000;
                Log.d(ROUTE_TAG, "Time: " + DateUtils.formatElapsedTime(endTime));

                //reset the variable keeping track of user location for live drawing of their route
                lastPoint = new LatLng(0, 0);

                //reset the ghost ticker index
                ghostPointLocation = 0;

                //reset the timer
                ((MapsActivity) mapsActivity).timerFunctionality.setBase(SystemClock.elapsedRealtime());

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(mapsActivity);

                    //create the EditText that will take the typed input
                    final EditText input = new EditText(mapsActivity);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);

                    // Sets the max name length
                    // InputFilter[] editTextFilter = {new InputFilter.LengthFilter(MAX_NAME_LENGTH)};
                    // input.setFilters(editTextFilter);

                    // Chain together various setter methods to set the dialog characteristics
                    builder.setMessage("Enter the name for this route (Max Char: 15):")
                            .setTitle(R.string.dialog_name_title);

                    // Add the buttons
                    builder.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked Enter button - Rename the route from the EditText if not empty, etc.
                            newName = input.getText().toString().trim();
                            if (!newName.isEmpty() && !CHAR_PATTERN.matcher(newName).matches()) {

                                // Set route name
                                newName = newName.substring(0, Math.min(newName.length(), MAX_NAME_LENGTH));
                                routesInformation.get(routeNumber).setName(newName);

                                // Set the time
                                routesInformation.get(routeNumber).setTime(endTime);

                                ((MapsActivity) mapsActivity).setRoutesInformation(routesInformation);

                                // A toast will pop up showing success
                                Toast toast = Toast.makeText(mapsActivity, newName + " saved", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                                toast.show();

                                clearBtn.setClickable(true);
                                stopBtn.setClickable(true);
                                routesBtn.setClickable(true);
                                newRouteBtn.setClickable(true);

                                //when a new route is created remake the saved route file
                                ((MapsActivity) mapsActivity).writeFile();
                            }
                            else{
                                routesInformation.get(routeNumber).setName("Unnamed Route");

                                // Set the time
                                routesInformation.get(routeNumber).setTime(endTime);

                                ((MapsActivity) mapsActivity).setRoutesInformation(routesInformation);


                                // A toast pop up for invalid input. Will put route name to default
                                Toast toast = Toast.makeText(mapsActivity, "Invalid Input. Unnamed Route saved", Toast.LENGTH_SHORT);
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
                            ((MapsActivity) mapsActivity).setRoutesInformation(routesInformation);
                            routeNumber = routesInformation.size();
                            ((MapsActivity) mapsActivity).setRouteNumber(routeNumber);
                            Toast toast = Toast.makeText(mapsActivity, "Route not saved", Toast.LENGTH_SHORT);
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

                ((MapsActivity) mapsActivity).drawingFacade.clearMap();
            }
        });

        // When the Routes button is pressed, brings up the saved routes
        routesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start a new activity (a new screen on the app) that shows all of the available routes to select

                Log.d(ROUTE_TAG, "Routes button clicked");
                //make a new intent
                Intent intent = new Intent(mapsActivity, displayAvailableRoutesActivity.class);
                TextView activeRouteLabel = ((MapsActivity) mapsActivity).findViewById(R.id.activeRouteLabel);

                //add the array of names of the routes to the new intent
                intent.putParcelableArrayListExtra(ROUTES_INFO, routesInformation);

                //start the new window activity passing along the intent we just created
                ((MapsActivity) mapsActivity).startActivityForResult(intent, DISPLAY_ROUTES_CODE);

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

                ((MapsActivity) mapsActivity).timerFunctionality.setBase(SystemClock.elapsedRealtime());
                ((MapsActivity) mapsActivity).timerFunctionality.start();


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

                ((MapsActivity) mapsActivity).timerFunctionality.stop();
                ((MapsActivity) mapsActivity).timerFunctionality.setBase(SystemClock.elapsedRealtime());

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
                ((MapsActivity) mapsActivity).setRouteNumber(routeNumber);


                routesInformation.add(new Route (routeNumber));
                ((MapsActivity) mapsActivity).setRoutesInformation(routesInformation);

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

                // set distanceCounter to visible while making new route
                distanceCounter.setVisibility(View.VISIBLE);

                //calls custom function to start routing the route and tracking the users location and time
                ((MapsActivity) mapsActivity).startRoute(v);
            }
        });
    }
}
