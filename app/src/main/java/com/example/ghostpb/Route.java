package com.example.ghostpb;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;

public class Route implements Parcelable {

    private ArrayList<RoutePoint> routePoints = new ArrayList<>();
    private ArrayList<Polyline> routeLines = new ArrayList<>();
    private int routeID;
    private String routeName;

    //adds the given RoutePoint object to the backing arraylist
    void addPoint(RoutePoint p) {
        routePoints.add(p);
    }

    RoutePoint getPoint(int i) {
        return routePoints.get(i);
    }

    int getSize() { return routePoints.size(); }
    String getName() { return routeName; }

    void setName(String rN) { routeName = rN; }
    void setRoutePoints(ArrayList<RoutePoint> rp) { routePoints = rp; }

    //sets the backing arraylist to the given arraylist of RoutePoints
    public void setAllRoutePoints(ArrayList<RoutePoint> rp) {
        routePoints = rp;
    }

    public void addLine(Polyline line) {
        routeLines.add(line);
    }

    Route(int id) {
        routeID = id;
        routeName = "Route " + Integer.toString(id);
    }
    public Route(ArrayList<RoutePoint> rp, int id) {
        routePoints = rp;
        routeID = id;
        routeName = "Route " + Integer.toString(id);
    }
    public Route(String name) {
        routeID = 9001;
        routeName = name;
    }

    // Parcels are for being able to pass the Object through the intents
    public Route(Parcel in){
        routeID = in.readInt();
        routeName = in.readString();
        routePoints = (ArrayList<RoutePoint>) in.readArrayList(RoutePoint.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return this.hashCode();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(routeID);
        dest.writeString(routeName);
        dest.writeList(routePoints);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

}
