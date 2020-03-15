package com.example.ghostpb;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public abstract class DisplayAvailableRoutesFunctions {

    private ArrayList<Route> routesInformation;
    private int selectedRoute;
    private View selectedView;


    /* Singleton */
/*
    private static final DisplayAvailableRoutesFunctions ourInstance = new DisplayAvailableRoutesFunctions();

    public static DisplayAvailableRoutesFunctions getInstance() {
        return ourInstance;
    }

    protected DisplayAvailableRoutesFunctions(){}

 */

    /* Singleton */


/*
    DisplayAvailableRoutesFunctions(ArrayList<Route> routesInformation){
        this.routesInformation = routesInformation;
    }


    protected void init(ArrayList<Route> routesInformation){
        this.routesInformation = routesInformation;
    }
    */
    protected void setRoutesInformation(ArrayList<Route> routesInformation){
        this.routesInformation = routesInformation;
    }

    protected ArrayList<Route> getRoutesInformation(){
        return this.routesInformation;
    }

    protected Route removeRoute(){
        return routesInformation.remove(selectedRoute);
    }

    protected void setRouteName(String name){
        routesInformation.get(selectedRoute).setName(name);
    }

    protected void setButtonsInvisible(ArrayList<Button> buttons){
        for (Button b : buttons){
            b.setVisibility(View.INVISIBLE);
        }
    }

    protected void setButtonsVisible(ArrayList<Button> buttons){
        for (Button b : buttons){
            b.setVisibility(View.VISIBLE);
        }
    }

    protected int getSelectedRoute(){
        return this.selectedRoute;
    }

    protected void setSelectedRoute(int selectedRoute){
        this.selectedRoute = selectedRoute;
    }

    protected View getSelectedView(){
        return this.selectedView;
    }

    protected void setSelectedView(View selectedView){
        this.selectedView = selectedView;
    }

    // The default view when nothing in the listview is selected
    protected void noSelection(){
        if (selectedView != null) {
            selectedView.setBackgroundResource(android.R.drawable.list_selector_background);
        }
        selectedView = null;
        selectedRoute = -1;
    }

    //updates the array holding the route names in order to fill the adapter
    protected ArrayList<String> updateNamesArray() {

        ArrayList<String> routeNames = new ArrayList<>();

        if (routesInformation != null) {
            //for each route that is stored save that name information in the routesNames array
            for (int i = 0; i < routesInformation.size(); i++) {
                routeNames.add(routesInformation.get(i).getName() + "          " + DateUtils.formatElapsedTime(routesInformation.get(i).getTime()));
            }
        }

        return routeNames;
    }

}
