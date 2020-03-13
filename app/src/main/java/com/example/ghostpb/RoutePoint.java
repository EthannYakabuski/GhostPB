package com.example.ghostpb;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Time;
/* TO-DO: integrate RoutePointDataClass */

//class for holding a LatLng object and a Time object,
//over the course of making a route the user will generate many of these,
//the information will be later used to draw the route using polylines functionality
public class RoutePoint implements Parcelable {

    //holds the latitude and longitude of the user when this route point was created
    private LatLng location;
    //holds a time object that was created when the user was at the above location
    private long timeAtLocation;

    //holds the data class for this routepoint
    private RoutePointDataClass pointData;

    //returns the LatLng object private variable location
    LatLng getLocation() {
        return location;
    }

    //returns the Time object associated with the above LatLng object
    public long getTime() {
        return timeAtLocation;
    }


    //returns this route point in a string
    @Override
    public String toString() {

        String returnString = "";

        returnString = returnString + Double.toString(location.latitude);
        returnString = returnString + "//";
        returnString = returnString + Double.toString(location.longitude);

        //Log.d("FILE-TEST", "to string route point: " + returnString);

        return returnString;

    }

    //constructor
    RoutePoint(LatLng latLngObj, long time) {

        location = latLngObj;
        timeAtLocation = time;

        //new code
        //set the data class
        //pointData = new RoutePointDataClass(latLngObj, time);
        //new code end
    }

    // Parcels are for being able to pass the Object through the intents
    RoutePoint(Parcel in){
        location = (LatLng) in.readValue(LatLng.class.getClassLoader());
        timeAtLocation = in.readLong();
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(location);
        dest.writeLong(timeAtLocation);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public RoutePoint createFromParcel(Parcel in) {
            return new RoutePoint(in);
        }

        public RoutePoint[] newArray(int size) {
            return new RoutePoint[size];
        }
    };

}
