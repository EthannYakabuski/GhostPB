package com.example.ghostpb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class displayAvailableRoutesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_available_routes);

        //user can store up to ten names
        String[] routeNamesFormatted = new String[10];

        //get a handle to the listview holding the names of the routes
        final ListView listView = findViewById(R.id.listViewRouteNames);

        //get a handle to the intent that started this activity
        Intent intent = getIntent();

        //receive the array list of route names from the intent
        ArrayList<String> routeNames = (ArrayList<String>) intent.getSerializableExtra("arrayNames");

        Log.d("ROUTE", "Route Names size = " + routeNames.size());

        for(int i = 0; i < routeNames.size(); i++) {

            routeNamesFormatted[i] = routeNames.get(i);

            Log.d("ROUTE", "Adding route name: " + routeNames.get(i));


        }

        //make an array adapter to add items to the list view from the routeNames array
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routeNamesFormatted);

        //link the list view to the adapter
        listView.setAdapter(adapter);

        //create a click listener for the list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            //called when an item in the list is clicked
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //create an intent holding the route name clicked to send back to the parent activity

            }
        });

    }



}
