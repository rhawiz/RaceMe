package com.example.rawand.raceme;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.drive.internal.ac;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    public static boolean logRaceSession(RaceSession session){

        String sqlQuery = session.getSqlInsertStatement();

        DatabaseConnection dbConnection;

        try {
            //TODO: put these into databaseHelper class
            dbConnection =  new DatabaseConnection("co-project.lboro.ac.uk:3306", "coac11", "wme38aie");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (InstantiationException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        DatabaseQuery dbQuery = new DatabaseQuery(dbConnection,sqlQuery);

        if(dbQuery.run()){
            return true;
        }
        return false;
    }

    public static boolean storeRaceSessionLocally(RaceSession session, Activity activity){
        String filename = "data";



        Gson gson = new GsonBuilder().create();

        String json = session.getJSON();
        FileOutputStream outputStream;

        File outFile = new File(activity.getApplicationContext().getFilesDir(), filename);

        if(!outFile.exists()){
            try {
                outFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            outputStream = activity.openFileOutput(filename ,Context.MODE_APPEND);
            outputStream.write(json.getBytes());
            outputStream.write("\n".getBytes());
            Log.w("RaceUtils",outputStream.toString());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    public static ArrayList<RaceSession> getLocalRaceSessions(Activity activity){

        if(activity == null) {
            return new ArrayList<RaceSession>();
        }

        File file = new File(activity.getApplicationContext().getFilesDir(), "data");

        ArrayList raceSessionArray = new ArrayList();
        Gson gson = new Gson();

        if(file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    try {
                        RaceSession raceSession = new RaceSession(line);
                        raceSessionArray.add(raceSession);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            ;
        }

        return raceSessionArray;

    }

    public static void clearLocalRaceSessions(Activity activity){
        File file = new File(activity.getApplicationContext().getFilesDir(), "data");
        activity.getApplicationContext().deleteFile("data");
    }

}
