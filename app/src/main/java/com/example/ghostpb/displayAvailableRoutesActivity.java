package com.example.ghostpb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class displayAvailableRoutesActivity extends AppCompatActivity {

    private int selectedRoute;
    private Button selectBtn;
    private Button renameBtn;
    private Button backBtn;
    private Button deleteBtn;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> routeNames = new ArrayList<>();
    private ConstraintLayout constraintLayout;
    private ListView listView;
    private View selectedView;
    private ArrayList<Route> routesInformation = new ArrayList<>();

    private static final String ROUTE_TAG = "ROUTE";
    private static final String ROUTE_ID = "routeID";
    private static final String ROUTES_INFO = "routesInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_available_routes);

        //assigning widgets
        selectBtn = (Button) findViewById(R.id.selectButton);
        renameBtn = (Button) findViewById(R.id.renameButton);
        backBtn   = (Button) findViewById(R.id.backButton);
        deleteBtn = (Button) findViewById(R.id.deleteButton);
        constraintLayout = (ConstraintLayout) findViewById(R.id.ConstraintLayout);
        listView = (ListView) findViewById(R.id.listViewRouteNames);

        //make sure widgets and values are their desired default
        noSelection();

        //get a handle to the intent that started this activity
        Intent intent = getIntent();

        //receive the array list of routes informaton from the intent
        routesInformation = intent.getParcelableArrayListExtra(ROUTES_INFO);

        //populate routeNames to fill the adapter
        updateNamesArray();

        Log.d(ROUTE_TAG, "Route Names size = " + routeNames.size());

        //make an array adapter to add items to the list view from routeNames
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, routeNames);

        //link the list view to the adapter
        listView.setAdapter(adapter);

        //create a click listener for the list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //called when an item in the list is clicked
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedRoute < 0 || selectedRoute != position) {
                    Log.d(ROUTE_TAG, "User tapped route id " + position);
                    selectedRoute = position;
                    listView.setSelection(position);

                    //reset the previous selected cell colour
                    if (selectedView != null){
                        selectedView.setBackgroundResource(android.R.drawable.list_selector_background);
                    }

                    selectedView = view;
                    selectedView.setBackgroundResource(R.color.itemSelected);
                    selectBtn.setVisibility(View.VISIBLE);
                    renameBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.VISIBLE);
                }
            }
        });


        // When the Select button is pressed
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Select button clicked");
                Log.d(ROUTE_TAG, "User selected route " + selectedRoute);

                //create an intent holding the route clicked and updated routes info to send back to the parent activity
                Intent data = new Intent(displayAvailableRoutesActivity.this, MapsActivity.class);
                data.putExtra(ROUTE_ID, selectedRoute);
                data.putExtra(ROUTES_INFO, routesInformation);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        // When the Rename button is pressed
        renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Rename button clicked");

                // This is a popup box that will take input
                AlertDialog.Builder builder = new AlertDialog.Builder(displayAvailableRoutesActivity.this);

                //create the EditText that will take the typed input
                final EditText input = new EditText(displayAvailableRoutesActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                // Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Enter the new name for " + routeNames.get(selectedRoute))
                        .setTitle(R.string.dialog_rename_title);

                // Add the buttons
                builder.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Enter button - Rename the route from the EditText if not empty, etc.
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            // Update names, routes info, and list items
                            routesInformation.get(selectedRoute).setName(newName);
                            routeNames.set(selectedRoute, newName);
                            adapter.notifyDataSetChanged();
                        }
                        else{
                            // A toast pop up for invalid input
                            Toast toast = Toast.makeText(displayAvailableRoutesActivity.this, R.string.invalid_edit_text_toast, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                        // Set back to default view
                        noSelection();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        noSelection();
                    }
                });

                AlertDialog dialog = builder.create();

                // Add the EditText to the dialog
                dialog.setView(input);

                dialog.show();
            }
        });

        // When the Back button is pressed
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Back button clicked");
                //create an intent holding the route clicked and updated routes info to send back to the parent activity
                Intent data = new Intent(displayAvailableRoutesActivity.this, MapsActivity.class);
                data.putExtra(ROUTES_INFO, routesInformation);
                setResult(RESULT_OK, data);
                Log.d(ROUTE_TAG, "Routes: " + routeNames);
                finish();
            }
        });


        // When the Delete button is pressed
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Delete button clicked");
                Log.d(ROUTE_TAG, "Number of routes: " + routeNames.size());

                // This is a popup box that will get confirmation for deletion
                AlertDialog.Builder builder = new AlertDialog.Builder(displayAvailableRoutesActivity.this);

                // Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Delete " + routeNames.get(selectedRoute) + "?")
                        .setTitle(R.string.dialog_delete_title);

                // Add the buttons
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Confirmed button - Delete the route
                        Log.d(ROUTE_TAG, "Number of routes: " + routeNames.size());

                        // Remove the route from the names, routes info, and item list
                        String route = adapter.getItem(selectedRoute);
                        routesInformation.remove(selectedRoute);
                        routeNames.remove(selectedRoute);
                        adapter.remove(route);
                        adapter.notifyDataSetChanged();

                        // Toast to show delete success to the user
                        Toast toast = Toast.makeText(displayAvailableRoutesActivity.this, R.string.delete_success_toast, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();

                        // Set back to default view
                        noSelection();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        // Set back to default view
                        noSelection();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // When somewhere other than the listView or buttons are clicked, set back to default view
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noSelection();
            }
        });
    }

    // The default view when nothing in the listview is selected
    private void noSelection(){
        if (selectedView != null) {
            selectedView.setBackgroundResource(android.R.drawable.list_selector_background);
        }
        selectedView = null;
        selectedRoute = -1;
        selectBtn.setVisibility(View.INVISIBLE);
        renameBtn.setVisibility(View.INVISIBLE);
        deleteBtn.setVisibility(View.INVISIBLE);
    }

    //updates the array holding the route names in order to fill the adapter
    public void updateNamesArray() {
        if (routesInformation != null) {
            //for each route that is stored save that name information in the routesNames array
            for (int i = 0; i < routesInformation.size(); i++) {
                routeNames.add(routesInformation.get(i).getName());
            }
        }
    }

}
