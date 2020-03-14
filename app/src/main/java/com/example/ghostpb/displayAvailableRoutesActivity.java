package com.example.ghostpb;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class displayAvailableRoutesActivity extends AppCompatActivity {

    private ArrayList<Route> routesInformation = new ArrayList<>();

    private static final String ROUTE_TAG = "ROUTE";
    private static final String ROUTES_INFO = "routesInfo";

    private DisplayAvailableRoutesEvents dare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_available_routes);

        //get a handle to the intent that started this activity
        Intent intent = getIntent();

        //receive the array list of routes informaton from the intent
        routesInformation = intent.getParcelableArrayListExtra(ROUTES_INFO);

        dare = new DisplayAvailableRoutesEvents(displayAvailableRoutesActivity.this, routesInformation);
    }

    @Override
    public void onBackPressed(){
        Log.d(ROUTE_TAG, "Android Back button clicked");
        //create an intent holding the route clicked and updated routes info to send back to the parent activity
        Intent data = new Intent(displayAvailableRoutesActivity.this, MapsActivity.class);
        data.putExtra(ROUTES_INFO, dare.getRoutesInformation());
        setResult(RESULT_OK, data);
        finish();
    }

}

