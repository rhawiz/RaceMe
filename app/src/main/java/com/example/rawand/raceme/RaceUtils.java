package com.example.rawand.raceme;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.location.Location;

import java.util.ArrayList;

/**
 * Created by RAWAND on 26/11/2014.
 */
public final class RaceUtils{
    private static final float MILES_TO_METER_CONVERSION = (float) 1609.344;


    /**
     * Returns the distance between two given locations in meters.
     *
     * @param loc1 First location object
     * @param loc2 Second location object
     * @return distance between Loc1 & Loc2 in meters.
     */
    public static float getDistance(Location loc1, Location loc2) {
        double lat1 = loc1.getLatitude();
        double lng1 = loc1.getLongitude();
        double lat2 = loc2.getLatitude();
        double lng2 = loc2.getLongitude();

        double earthRad = 6371; //kilometers
        double dLatitude = Math.toRadians(lat2-lat1);
        double dLongitude = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLatitude/2) * Math.sin(dLatitude/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLongitude/2) * Math.sin(dLongitude/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRad * c);

        return dist * MILES_TO_METER_CONVERSION;
    }

    public static int calculateTotalDistance(ArrayList<Location> gpsCoordArray){
        int totalDistance = 0;

        for (int i = 1; i < gpsCoordArray.size(); i++) {
            totalDistance += getDistance(gpsCoordArray.get(i-1),gpsCoordArray.get(i));
        }
        
        return totalDistance;
    }


}
