package com.example.rawand.raceme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by RAWAND on 03/12/2014.
 */
public class Utilities {
    public static AlertDialog getAlertDialog(Activity activity, String title, String msg, int icon){
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setIcon(icon);
        return alertDialog;
    }



    public static ArrayList<Location> toLocationArray(String[] locationList){

        Log.w("LOCATIONARRAY", locationList.toString());

        ArrayList<Location> locationArray = new ArrayList<Location>();

        for (int i = 0; i < locationList.length; i++) {
            String[] coords = locationList[i].split(",");
            double latitude = Double.parseDouble(coords[0]);
            double longitude = Double.parseDouble(coords[1]);
            Location location = new Location("location");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            locationArray.add(location);
        }

        return locationArray;
    }

    public static Date getDateFromString(String strDate){
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.parseDateTime(strDate).toDate();
    }

    public static boolean uploadLocalSessions(Activity activity){
        //TODO:COMPLETE THIS AND CALL IN BaseActivity
        ArrayList<RaceSession> localRaceSessions = RaceUtils.getLocalRaceSessions(activity);
        ArrayList<RaceSession> raceSessionsFailedUploads = new ArrayList<RaceSession>();
        for (int i = 0; i < localRaceSessions.size(); i++) {
            if(!RaceUtils.logRaceSession(localRaceSessions.get(i))){
                raceSessionsFailedUploads.add(localRaceSessions.get(i));
            }
        }

        RaceUtils.clearLocalRaceSessions(activity);
        if(raceSessionsFailedUploads.size() > 0){
            for (int i = 0; i < raceSessionsFailedUploads.size(); i++) {
                RaceUtils.storeRaceSessionLocally(raceSessionsFailedUploads.get(i),activity);
            }
        }

        if(localRaceSessions.size() - raceSessionsFailedUploads.size() == 0){
            return false;
        }

        return true;
    }

    public static void showToast(final String msg, final int len,final Activity activity){
        activity.runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(activity.getApplicationContext(), msg,
                        len).show();
            }
        });


    }

}
