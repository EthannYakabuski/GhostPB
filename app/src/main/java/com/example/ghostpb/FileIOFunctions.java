
//COMMENTED ATM SINCE NOT FULLY WORKING

/*
package com.example.ghostpb;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.content.Context;
import static android.content.Context.MODE_PRIVATE;

public class FileIOFunctions {
    private void readFile(Context context) {

        try {
            //setting up things needed to read from the file
            FileInputStream fis = context.openFileInput("routes.txt");
            InputStreamReader isr = new InputStreamReader(fis);

            BufferedReader br = new BufferedReader(isr);
            StringBuffer sb = new StringBuffer();

            String lines;

            //while there is still content in the file to read
            while ((lines = br.readLine()) != null) {
                sb.append(lines);
            }

            //tested to show the correct file when the user exits the apps and then reloads
            //Log.d("FILE-TEST", "String buffer contains: " + sb.toString());


            //add code here to parse sb.toString() and re-populate the global routes array
            String delimiter = "//";

            String[] parsed = sb.toString().split(delimiter);

            for (int i = 0; i < parsed.length; i++) {

                //Log.d("FILE-TEST", parsed[i]);
            }

            //if there is data in the file that has been read
            if (parsed.length > 0) {

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
                                String longitudeString = parsed[currentLine + 1];

                                //Log.d("FILE-TEST", "Latitude: " + latitudeString);
                                //Log.d("FILE-TEST", "Longitude: " + longitudeString);

                                //one line for each lat,long entry and one line for an empty line
                                currentLine = currentLine + 2;

                                //Log.d("FILE-TEST", "just about to make the location object using data" + latitudeString + "," + longitudeString);
                                //make a new LatLng object for the routepoint we are about to make
                                //giveMeLatLng is private in MapsActivity
                                LatLng location = giveMeLatLng(latitudeString, longitudeString);

                                //make a new route point using the location above, and time is the current index in the loop
                                RoutePoint localRoutePoint = new RoutePoint(location, q);

                                //add this routepoint to the routepoints array we are building for this route, and then keep iterating
                                localRoutePoints.add(localRoutePoint);


                            }


                            //only do this is there are routes
                            if (thereAreRoutes) {
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
                                    FileOutputStream fos = context.openFileOutput("routes.txt", MODE_PRIVATE);
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

    protected void writeFile(Context context) {

        String saveData = "";

        //add the amount of routes to the start
        saveData = saveData + routesInformation.size();
        saveData = saveData + "//";

        //for each route
        for (int i = 0; i < routesInformation.size(); i++) {
            //toString the route and add to saved data
            saveData = saveData + routesInformation.get(i).toString();
        }

        //add end of file characters
        saveData = saveData + "?????//";

        //Log.d("FILE-TEST", "Saved data: " + saveData);


        //attempt to write the saveData to the saved file
        try {
            FileOutputStream fos = context.openFileOutput("routes.txt", MODE_PRIVATE);
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
}

*/