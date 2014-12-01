package com.example.rawand.raceme;

import android.annotation.TargetApi;
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


public class RaceService extends Service  {
    public static Date startTime;
    private static ArrayList<Location> gpsCoordArray  = new ArrayList<Location>();
    private Timer timer;
    private Location lastReceivedLocation;
    private Location lastKnownLocation;
    private TimerTask timerTask;
    private int distanceTravelled;

    private Notification notification;

    final static int serviceID = 1;

    static final public String UPDATE_COORDS_BROADCAST = "com.example.rawand.raceme.RACE_UPDATE_COORDS";
    static final public String GPS_COORD_LOCATION = "com.example.rawand.raceme.RACE_GPS_COORDS";

    int notifyID = 1;
    private LocationListener locationListener;
    private LocationManager locationManager;

    public RaceService() {

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

        //TODO:Delete timer function once broadcast receiver has been fixed.
//        timer = new Timer();
//
//        class testFunc extends TimerTask {
//            double latitude = 52.765538;
//            double longitude =  -1.219219;
//            int counter = 0;
//
//            public void run() {
//                Location loc = new Location("location");
//                loc.setLongitude(longitude);
//                loc.setLatitude(latitude);
//
//                gpsCoordArray.add(loc);
//                sendLocationBroadcast(loc);
//                longitude += 0.0001;
//                counter++;
//                if(counter % 3 == 0){
//                    latitude += 0.0001;
//                }
//            }
//        }
//        TimerTask testFunc = new testFunc();
//        timer.scheduleAtFixedRate(testFunc,0,1000);
        lastKnownLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

        startForeground(serviceID, notification);

        return START_STICKY;

    }

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

    private void startRaceSession(){

        startTime = new Date();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if(lastReceivedLocation != null && !(lastReceivedLocation.getAccuracy() <= 2) && !(lastReceivedLocation.distanceTo(location) >= 10)){
                    lastReceivedLocation = location;
                    gpsCoordArray.add(location);
                    sendLocationBroadcast(location);

                }
                else{
                    lastReceivedLocation = location;
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
            Log.w("RACESERVICE","USING NETWORK");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }else if(gpsEnabled) {
            Log.w("RACESERVICE","USING GPS");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

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

    public static Date getStartTime(){
        return startTime;
    }

    private void stopRaceSession(){
        super.onDestroy();
        stopForeground(true);
        //timer.cancel();
        gpsCoordArray = new ArrayList<Location>();
        locationManager.removeUpdates(locationListener);
        locationManager = null;
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        Date now = new Date();
        String totalTime = sdf.format(new Date(now.getTime() - startTime.getTime()));
    }

}
