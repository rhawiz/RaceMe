package com.example.rawand.raceme;


import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RaceActivity extends BaseActivity implements Serializable {
    //Declare layout fields
    private TabHost tabHost;
    private Button startButton;
    private Button stopButton;
    private GridLayout detailTab;
    private GridLayout mapTab;
    private TextView distanceTravelledView;
    private TextView averageSpeedView;
    private LocationManager locationManager;

    PolylineOptions routePolylineOption;
    Polyline routePolyline;


    private static Chronometer chronometer; //To count elapsed race time
    private int distanceTravelled;

    private DataUpdateReceiver dataUpdateReceiver;
    private IntentFilter intentFilter;

    public LocationClient mLocationClient;

    private ArrayList<Location> gpsCoordArray;
    GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_race, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();

        initMap();

        distanceTravelled = 0;

        dataUpdateReceiver = new DataUpdateReceiver();

        intentFilter = new IntentFilter();
        intentFilter.addAction(RaceService.UPDATE_COORDS_BROADCAST);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        distanceTravelledView = (TextView) findViewById(R.id.distance_travelled_view);
        averageSpeedView = (TextView) findViewById(R.id.average_speed_view);
        detailTab = (GridLayout) findViewById(R.id.race_details_tab);
        mapTab = (GridLayout) findViewById(R.id.race_map_tab);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        chronometer = (Chronometer) findViewById(R.id.timer);
        chronometer.setBase(SystemClock.elapsedRealtime());

        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if(!networkEnabled && !gpsEnabled){
                    Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(gpsOptionsIntent);
                }else {
                    startRaceSession();
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                }
            }
        });

        stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setEnabled(false);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                stopRaceSession();
            }
        });


        if(isServiceRunning(RaceService.class)){
            //If the service is running, it means the user has quit the application without stopping the race session
            resumeSession();

        }

    }

    private void initMap(){
        routePolylineOption = new PolylineOptions().width(5)
                .color(Color.BLUE)
                .geodesic(true);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        map.setMyLocationEnabled(true);
        map.addPolyline(routePolylineOption);
    }

    private void resumeSession(){
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        Date now = new Date();
        chronometer.setBase( SystemClock.elapsedRealtime() + (RaceService.getStartTime().getTime() - now.getTime()));
        chronometer.start();
        gpsCoordArray = RaceService.getGpsCoordArray();
        this.registerReceiver(dataUpdateReceiver,intentFilter);
        distanceTravelled = RaceUtils.calculateTotalDistance(gpsCoordArray);
        distanceTravelledView.setText(String.valueOf(distanceTravelled)+"m");
    }

    private void updateDistance(){
        if(gpsCoordArray.size() > 1){
            distanceTravelled+=RaceUtils.getDistance(gpsCoordArray.get( gpsCoordArray.size()-2),gpsCoordArray.get(gpsCoordArray.size()-1));
            distanceTravelledView.setText(String.valueOf(distanceTravelled) + "m");
            //Log.w("RESUME",gpsCoordArray.toString() + " Array Size");
            //Log.w("RESUME",String.valueOf(gpsCoordArray.get( gpsCoordArray.size()-2)) + " Parameter 1 value");
           // Log.w("RESUME",String.valueOf(gpsCoordArray.get( gpsCoordArray.size()-1)) + " Parameter 2 value");
        }

    }

    private void startRaceSession(){


        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        startService(new Intent(this, RaceService.class));
        gpsCoordArray = RaceService.getGpsCoordArray();
        this.registerReceiver(dataUpdateReceiver, intentFilter);

        //gpsLoc.setText("Last Location:" + location.getLatitude() + ":" + location.getLongitude());


    }

    private void stopRaceSession(){

        chronometer.setBase(SystemClock.elapsedRealtime());
        this.unregisterReceiver(dataUpdateReceiver);
        chronometer.stop();
        gpsCoordArray = new ArrayList<Location>();
        logSession();
        distanceTravelled = 0;
        distanceTravelledView.setText("0m");
        routePolylineOption = new PolylineOptions().width(5)
                .color(Color.BLUE)
                .geodesic(true);
        routePolyline.remove();
        map.clear();
        stopService(new Intent(this,RaceService.class));
    }

    private void logSession() {

    }

    private void initTabs() {


        tabHost = (TabHost) findViewById(R.id.race_tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("details");
        tabSpec.setContent(R.id.race_details_tab);
        tabSpec.setIndicator("Details");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("map");
        tabSpec.setContent(R.id.race_map_tab);
        tabSpec.setIndicator("Map");
        tabHost.addTab(tabSpec);


        tabHost.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            TransitionAnimations transition = new TransitionAnimations();

            public void onSwipeLeft() {
                int nextTab = tabHost.getCurrentTab() + 1;
                if (nextTab > 3) nextTab = 0;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.inFromRightAnimation());
            }

            public void onSwipeRight() {
                int nextTab = tabHost.getCurrentTab() - 1;
                if (nextTab < 0) nextTab = 3;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.outToRightAnimation());
            }
        });


    }


    public void onDestroy()
    {
        //this.unregisterReceiver(dataUpdateReceiver);
        super.onDestroy();
    }

    /**
     * To check whether a service is running or not
     *
     * @param serviceClass The service class to be checked whether it's currently running or not.
     * @return true if service is running, false if not.
     */
    public boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public class DataUpdateReceiver extends BroadcastReceiver {
        //TODO:Fix broadcast receiver
        public DataUpdateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //if(intent.getAction() == RaceService.UPDATE_COORDS_BROADCAST) {
            Bundle bundle = intent.getExtras();
            Location location = (Location) bundle.get(RaceService.GPS_COORD_LOCATION);
            gpsCoordArray = RaceService.getGpsCoordArray();

            routePolylineOption.add(new LatLng(location.getLatitude(), location.getLongitude())); // Closes the polyline.

            routePolyline = map.addPolyline(routePolylineOption);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));

            Log.w("RESUME", (Location) bundle.get(RaceService.GPS_COORD_LOCATION) + " Bundle Data");
//            if(gpsCoordArray != null) {
//                Log.w("RESUME", gpsCoordArray.toString() + " Array Size");
//                if (gpsCoordArray.size() > 1) {
//                    Log.w("RESUME", String.valueOf(gpsCoordArray.get(gpsCoordArray.size() - 2)) + " Parameter 1 value");
//                    Log.w("RESUME", String.valueOf(gpsCoordArray.get(gpsCoordArray.size() - 1)) + " Parameter 2 value");
//                }
//            }
            updateDistance();
           // }
        }
    }




}
