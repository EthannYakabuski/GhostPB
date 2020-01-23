package com.example.ghostpb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class displayAvailableRoutesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_available_routes);

        //get a handle to the listview holding the names of the routes

        //get a handle to the intent that started this activity
        Intent intent = getIntent();

        //receive the array list of route names from the intent
        ArrayList<String> routeNames = (ArrayList<String>) intent.getSerializableExtra("routeNames");

        //for each entry in the routeNames array recevied from the intent
        

    }



}
