package com.example.rawand.raceme;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Background service that gets called whenever a race session has started.
 * Will store and update session status until race session has been stopped.
 */

public class RaceService extends Service  {
    public static Date startTime;
    private static ArrayList<Location> gpsCoordArray  = new ArrayList<Location>();
    private Timer timer;
    private Location lastReceivedLocation;
    private TimerTask timerTask;
    private int distanceTravelled;

    private boolean TESTMODE = false; //True to use mock locations. False to use real location.


    private Notification notification;

    final static int serviceID = 1;

    static final public String UPDATE_COORDS_BROADCAST = "com.example.rawand.raceme.RACE_UPDATE_COORDS";
    static final public String GPS_COORD_LOCATION = "com.example.rawand.raceme.RACE_GPS_COORDS";

    int notifyID = 1;
    private LocationListener locationListener;
    private LocationManager locationManager;

    public RaceService() {

        if( SaveSharedPreference.isTestMode() == true ){
            setTestMode(true);
        }else{
            setTestMode(false);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        startRaceSession();
        notification = getRaceSessionNotification();

        startForeground(serviceID, notification);

        return START_STICKY;

    }


    /**
     * Create and handle notification icon
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification getRaceSessionNotification(){
        // The PendingIntent to launch our activity if the user selects
        // this notification
        Intent raceIntent = new Intent(this, RaceActivity.class);
        raceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, raceIntent, 0);


        Notification.Builder builder = new Notification.Builder(this);
        Resources res = getApplicationContext().getResources();
        return builder.setContentIntent(pendIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
                .setTicker(res.getString(R.string.race_session_notification_ticker))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(res.getString(R.string.race_session_notification_title))
                .setContentText(res.getString(R.string.race_session_notification_text)).build();
    }


    @Override
    public void onDestroy() {
        stopRaceSession();
    }


    /**
     * Start the race session
     */
    private void startRaceSession(){

        startTime = new Date(); //Capture the exact start time

        if(TESTMODE) {
            TimerTask testLocationRoute = new testLocationRoute();
            timer = new Timer();
            timer.scheduleAtFixedRate(testLocationRoute,0,1000);
        }
        else{
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (lastReceivedLocation == null) {
                        lastReceivedLocation = location;
                    }
                    if (location.getAccuracy() >= 2 && !(lastReceivedLocation.distanceTo(location) >= 100)) {
                        lastReceivedLocation = location;
                        gpsCoordArray.add(location);
                        sendLocationBroadcast(location);

                    }

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
            boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if(networkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }else if(gpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }


    }


    /**
     * Returns list of coordinates defining the users route.
     *
     * @return ArrayList of location objects
     */
    public static ArrayList<Location> getGpsCoordArray(){
        return gpsCoordArray;
    }

    private void sendLocationBroadcast(Location location){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(UPDATE_COORDS_BROADCAST);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(GPS_COORD_LOCATION,location);

        sendBroadcast(broadcastIntent);
    }


    /**
     * Get the exact date and time user started the session
     *
     * @return Date start date object
     */
    public static Date getStartTime(){
        return startTime;
    }

    /**
     * Stop the race session. Store the exact finish date and time.
     */

    private void stopRaceSession(){
        super.onDestroy();
        stopForeground(true);
        if(TESTMODE) {
            timer.cancel();
        }
        gpsCoordArray = new ArrayList<Location>();

        if(!TESTMODE) {
            locationManager.removeUpdates(locationListener);
            locationManager = null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        Date now = new Date();
        String totalTime = sdf.format(new Date(now.getTime() - startTime.getTime()));
    }


    /**
     * This class will dynamically create a new location object and call a broadcast event.
     * To be used for testing purposes.
     */

    class testLocationRoute extends TimerTask {

        double latitude = 52.765538;
        double longitude =  -1.219219;
        int counter = 0;

        public void run() {
            Location loc = new Location("location");
            loc.setLongitude(longitude);
            loc.setLatitude(latitude);
            Log.w("raceme", gpsCoordArray.toString());

            gpsCoordArray.add(loc);
            sendLocationBroadcast(loc);
            longitude += 0.0001;
            counter++;
            if(counter % 3 == 0){
                latitude += 0.0001;
            }
        }
    }


    /**
     * Toggle test mode
     *
     * @param action True if test mode on, false if test mode off.
     */
    public void setTestMode(boolean action){
        TESTMODE = action;
    }

}
