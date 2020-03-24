package com.example.ghostpb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.regex.Pattern;

import androidx.constraintlayout.widget.ConstraintLayout;

import static android.app.Activity.RESULT_OK;

public class DisplayAvailableRoutesEvents extends DisplayAvailableRoutesFunctions {

    private Button selectBtn;
    private Button renameBtn;
    private Button backBtn;
    private Button deleteBtn;
    private ConstraintLayout constraintLayout;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<Button> visibilityButtons = new ArrayList<>();
    private ArrayList<String> routeNames;
    private String newName;
    private String newNameAndTime;
    private String time;
    private Route selectedRoute;

    private static final String ROUTE_TAG = "ROUTE";
    private static final String ROUTE_ID = "routeID";
    private static final String ROUTES_INFO = "routesInfo";
    private static final String CHAR_FILTER = "^[!@#$&()`.+,/\\\"]*$";
    private static final Pattern CHAR_PATTERN = Pattern.compile(CHAR_FILTER);
    private static final int MAX_NAME_LENGTH = 15;

    /* Singleton */
    private static final DisplayAvailableRoutesEvents ourInstance = new DisplayAvailableRoutesEvents();

    public static DisplayAvailableRoutesEvents getInstance() {
        return ourInstance;
    }

    private DisplayAvailableRoutesEvents() {
        super();
    }
    /* Singleton */

    protected void init(final Context displayAvailableRoutes, ArrayList<Route> routesInformation){

        setRoutesInformation(routesInformation);

        final Context dar = displayAvailableRoutes;

        //assigning widgets
        selectBtn = (Button) ((displayAvailableRoutesActivity) dar).findViewById(R.id.selectButton);
        renameBtn = (Button) ((displayAvailableRoutesActivity) dar).findViewById(R.id.renameButton);
        backBtn   = (Button) ((displayAvailableRoutesActivity) dar).findViewById(R.id.backButton);
        deleteBtn = (Button) ((displayAvailableRoutesActivity) dar).findViewById(R.id.deleteButton);
        constraintLayout = (ConstraintLayout) ((displayAvailableRoutesActivity) dar).findViewById(R.id.ConstraintLayout);
        listView = (ListView) ((displayAvailableRoutesActivity) dar).findViewById(R.id.listViewRouteNames);

        visibilityButtons.add(selectBtn);
        visibilityButtons.add(renameBtn);
        visibilityButtons.add(deleteBtn);

        noSelection();
        setButtonsInvisible(visibilityButtons);

        routeNames = updateNamesArray();

        adapter = new ArrayAdapter<>((displayAvailableRoutesActivity) dar, android.R.layout.simple_list_item_1, routeNames);

        //link the list view to the adapter
        listView.setAdapter(adapter);

        //create a click listener for the list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //called when an item in the list is clicked
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getSelectedRoute() < 0 || getSelectedRoute() != position) {
                    Log.d(ROUTE_TAG, "User tapped route id " + position);
                    setSelectedRoute(position);
                    listView.setSelection(position);

                    //reset the previous selected cell colour
                    if (getSelectedView() != null){
                        getSelectedView().setBackgroundResource(android.R.drawable.list_selector_background);
                    }

                    setSelectedView(view);
                    getSelectedView().setBackgroundResource(R.color.itemSelected);
                    setButtonsVisible(visibilityButtons);
                }
            }
        });

        // When the Select button is pressed
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Select button clicked");
                Log.d(ROUTE_TAG, "User selected route " + getSelectedRoute());

                //create an intent holding the route clicked and updated routes info to send back to the parent activity
                Intent data = new Intent(dar, MapsActivity.class);
                data.putExtra(ROUTE_ID, getSelectedRoute());
                data.putExtra(ROUTES_INFO, getRoutesInformation());
                ((displayAvailableRoutesActivity) dar).setResult(RESULT_OK, data);
                ((displayAvailableRoutesActivity) dar).finish();

            }
        });

        // When the Back button is pressed
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Back button clicked");
                //create an intent holding the route clicked and updated routes info to send back to the parent activity
                Intent data = new Intent(dar, MapsActivity.class);
                data.putExtra(ROUTES_INFO, getRoutesInformation());
                ((displayAvailableRoutesActivity) dar).setResult(RESULT_OK, data);
                ((displayAvailableRoutesActivity) dar).finish();
            }
        });

        // When the Rename button is pressed
        renameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(ROUTE_TAG, "Rename button clicked");

                // This is a popup box that will take input
                AlertDialog.Builder builder = new AlertDialog.Builder(dar);

                //create the EditText that will take the typed input
                final EditText input = new EditText(dar);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                // Sets the max name length
                //input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_NAME_LENGTH)});

                selectedRoute = getRoutesInformation().get(getSelectedRoute());

                // Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Enter the new name for " + selectedRoute.getName().substring(0, Math.min(selectedRoute.getName().length(), MAX_NAME_LENGTH)) + " (Max Char: 15)")
                        .setTitle(R.string.dialog_rename_title);

                // Add the buttons
                builder.setPositiveButton(R.string.enter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Enter button - Rename the route from the EditText if not empty, etc.
                        newName = input.getText().toString().trim();
                        if (!newName.isEmpty() && !CHAR_PATTERN.matcher(newName).matches()) {
                            // Update names, routes info, and list items
                            newName = newName.substring(0, Math.min(newName.length(), MAX_NAME_LENGTH));
                            setRouteName(newName);
                            time = formatTime(getRoutesInformation().get(getSelectedRoute()).getTime());
                            newNameAndTime = newName + String.format("%" + 1000 + "s", time);
                            routeNames.set(getSelectedRoute(), newNameAndTime);
                            adapter.notifyDataSetChanged();
                        }
                        else{
                            // A toast pop up for invalid input
                            Toast toast = Toast.makeText(dar, R.string.invalid_edit_text_toast, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                        // Set back to default view
                        noSelection();
                        setButtonsInvisible(visibilityButtons);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        noSelection();
                        setButtonsInvisible(visibilityButtons);
                    }
                });

                AlertDialog dialog = builder.create();

                // Add the EditText to the dialog
                dialog.setView(input);

                dialog.show();
            }
        });

        // When the Delete button is pressed
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(ROUTE_TAG, "Delete button clicked");
                Log.d(ROUTE_TAG, "Number of routes: " + getRoutesInformation().size());

                // This is a popup box that will get confirmation for deletion
                AlertDialog.Builder builder = new AlertDialog.Builder(dar);

                // Chain together various setter methods to set the dialog characteristics
                selectedRoute = getRoutesInformation().get(getSelectedRoute());
                builder.setMessage("Delete " + selectedRoute.getName().substring(0, Math.min(selectedRoute.getName().length(), MAX_NAME_LENGTH)) + "?")
                        .setTitle(R.string.dialog_delete_title);

                // Add the buttons
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Confirmed button - Delete the route

                        // Remove the route from the names, routes info, and item list
                        String route = adapter.getItem(getSelectedRoute());
                        removeRoute();
                        adapter.remove(route);
                        adapter.notifyDataSetChanged();

                        Log.d(ROUTE_TAG, "Number of routes: " + getRoutesInformation().size());

                        // Toast to show delete success to the user
                        Toast toast = Toast.makeText(dar, R.string.delete_success_toast, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                        toast.show();

                        // Set back to default view
                        noSelection();
                        setButtonsInvisible(visibilityButtons);

                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        // Set back to default view
                        noSelection();
                        setButtonsInvisible(visibilityButtons);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noSelection();
                setButtonsInvisible(visibilityButtons);
            }
        });

    }

}
