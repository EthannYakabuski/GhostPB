package com.example.ghostpb;



import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //the map
    private GoogleMap mMap;

    //fused location provider
    private FusedLocationProviderClient fusedLocationProviderClient;

    //boolean set to true when permission is granted to access location data
    private boolean boolLocationPermissionGranted;

    //for verbosity, used to check is location permissions have been granted. (1 is positive status code)
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //variable to hold the default zoom of the camera controlling the map
    private static final int DEFAULT_ZOOM = 15;

    //the last known location retrieved by the fused location provider
    private Location lastKnowLocation;

    //default location for when location permissions are not granted
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //retrieve content view that renders the map

        setContentView(R.layout.activity_maps);

        //construct the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


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

        //turn on myLocation control button on the map
        updateLocationUI();



        //finds the phone and sets the map to go to your current location
        showDeviceLocation();


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
    public void onRequestPermissionsResult(int code, String[] permissions, int[] allowResults) {
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

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }

    }

    private void showDeviceLocation() {


        try {
            if(boolLocationPermissionGranted) {
                Task<Location> currentLocation = fusedLocationProviderClient.getLastLocation();

                currentLocation.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    //called when the fusedLocationProviderClient is done finding the last known location
                    @Override
                    public void onComplete(Task<Location> task) {

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
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }


    }

}
