package com.example.rawand.raceme;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TableLayout;
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
import com.google.gson.Gson;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RaceActivity extends BaseActivity implements Serializable {
    //Declare layout fields
    private TabHost tabHost;
    private ImageButton startButton;
    private ImageButton stopButton;
    private TableLayout detailTab;
    private GridLayout mapTab;
    private TextView distanceTravelledView;
    private TextView averageSpeedView;
    private TextView raceStatus;
    private LocationManager locationManager;
    private Date startTime;
    private Date endTime;

    private RadioGroup raceTypeRadioGroup;

    private RadioButton walkRadio;
    private RadioButton runRadio;
    private RadioButton cycleRadio;

    PolylineOptions routePolylineOption;
    Polyline routePolyline;


    private static Chronometer chronometer; //To count elapsed race time
    private int distanceTravelled;
    private int averageSpeed;

    private DataUpdateReceiver dataUpdateReceiver;
    private IntentFilter intentFilter;

    private String raceType;

    public LocationClient mLocationClient;

    private ArrayList<Location> gpsCoordArray;
    GoogleMap map;

    Handler alertHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_race, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();

        initMap();

        distanceTravelled = 0;
        alertHandler = new Handler();

        dataUpdateReceiver = new DataUpdateReceiver();

        intentFilter = new IntentFilter();
        intentFilter.addAction(RaceService.UPDATE_COORDS_BROADCAST);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        raceStatus = (TextView) findViewById(R.id.race_status);

        distanceTravelledView = (TextView) findViewById(R.id.distance_travelled_view);
        averageSpeedView = (TextView) findViewById(R.id.average_speed_view);
        detailTab = (TableLayout) findViewById(R.id.race_details_tab);
        mapTab = (GridLayout) findViewById(R.id.race_map_tab);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        chronometer = (Chronometer) findViewById(R.id.timer);
        chronometer.setBase(SystemClock.elapsedRealtime());

        raceTypeRadioGroup = (RadioGroup) findViewById(R.id.race_type_radio_group);
        walkRadio = (RadioButton) findViewById(R.id.walk_radio);
        runRadio = (RadioButton) findViewById(R.id.run_radio);
        cycleRadio = (RadioButton) findViewById(R.id.cycle_radio);

        raceTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton selectedButton = (RadioButton) findViewById(raceTypeRadioGroup.getCheckedRadioButtonId());
                raceType = (String) selectedButton.getText();
             }
        });

        startButton = (ImageButton) findViewById(R.id.start_img_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                String gpsProvider = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.LOCATION_PROVIDERS_ALLOWED);


                if(!networkEnabled && (!gpsEnabled || gpsProvider.equals(""))){

                    AlertDialog.Builder builder = new AlertDialog.Builder(RaceActivity.this);
                    builder.setTitle("Location disabled")
                            .setMessage("Please enable Locations.")
                            .setCancelable(true)
                            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                             })
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent gpsOptionsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(gpsOptionsIntent);
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();


                }else {
                    startRaceSession();
                    startButton.setVisibility(View.GONE);
                    stopButton.setVisibility(View.VISIBLE);
                }
            }
        });

        stopButton = (ImageButton) findViewById(R.id.stop_img_button);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
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
        startButton.setVisibility(View.GONE);
        stopButton.setVisibility(View.VISIBLE);


        startTime = RaceService.getStartTime();

        Date now = new Date();

        chronometer.setBase( SystemClock.elapsedRealtime() + (RaceService.getStartTime().getTime() - now.getTime()));
        chronometer.start();
        gpsCoordArray = RaceService.getGpsCoordArray();

        for (Location i : gpsCoordArray) {
            routePolylineOption.add(new LatLng(i.getLatitude(), i.getLongitude())); // Closes the polyline.
        }

        routePolyline = map.addPolyline(routePolylineOption);

        this.registerReceiver(dataUpdateReceiver,intentFilter);
        distanceTravelled = RaceUtils.calculateTotalDistance(gpsCoordArray);
        distanceTravelledView.setText(String.valueOf(distanceTravelled));
    }

    private void updateDistance(){
        if(gpsCoordArray.size() > 1){

            distanceTravelled+=RaceUtils.getDistance(gpsCoordArray.get( gpsCoordArray.size()-2),gpsCoordArray.get(gpsCoordArray.size()-1));
            distanceTravelledView.setText(String.valueOf(distanceTravelled));
        }
        //averageSpeed = distanceTravelled / (int) chronometer.getBase();
        /*averageSpeedView.setText(averageSpeed);*/
        //Log.w("raceme", "speed " + averageSpeed);
    }

    private void startRaceSession(){
        distanceTravelled = 0;
        distanceTravelledView.setText("0");
        gpsCoordArray = new ArrayList<Location>();
        map.clear();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.stop();
        routePolylineOption = new PolylineOptions().width(5)
                .color(Color.BLUE)
                .geodesic(true);
        if(routePolyline != null)routePolyline.remove();

        startTime = new Date();
        raceStatus.setText("Started");
        walkRadio.setEnabled(false);
        runRadio.setEnabled(false);
        cycleRadio.setEnabled(false);



        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        startService(new Intent(this, RaceService.class));
        gpsCoordArray = new ArrayList<Location>();
        this.registerReceiver(dataUpdateReceiver, intentFilter);

        //gpsLoc.setText("Last Location:" + location.getLatitude() + ":" + location.getLongitude());


    }

    private void stopRaceSession(){
        //Utilities.getAlertDialog(RaceActivity.this,"Route logged.","Well done!\nYour session has been saved.",R.drawable.ic_launcher).show();

        endTime = new Date();
        logSession();
        raceStatus.setText("Stopped");
        walkRadio.setEnabled(true);
        runRadio.setEnabled(true);
        cycleRadio.setEnabled(true);
        chronometer.stop();
        this.unregisterReceiver(dataUpdateReceiver);
        stopService(new Intent(this,RaceService.class));
    }

    private void logSession() {
        String userId = SaveSharedPreference.getUserDetails(this).getUserId();
        RaceSession session = new RaceSession(userId,raceType,gpsCoordArray,startTime,endTime);

        LogSessionTask logSessionTask = new LogSessionTask(session);
        logSessionTask.execute();


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

        tabSpec = tabHost.newTabSpec("past_race");
        tabSpec.setContent(R.id.race_history_tab);
        tabSpec.setIndicator("Past races");
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

            updateDistance();

        }
    }


    public class LogSessionTask extends AsyncTask<Void, Void, Boolean> {


        private DatabaseConnection dbConnection = null;
        private RaceSession session;

        public LogSessionTask(RaceSession session) {
            this.session = session;
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            return RaceUtils.logRaceSession(session);
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (!success) {
                Utilities.showToast("Could not find network connection.",Toast.LENGTH_LONG,RaceActivity.this);
                RaceUtils.storeRaceSessionLocally(session, RaceActivity.this);
            }else{
                Utilities.showToast("Race session has been logged.",Toast.LENGTH_LONG,RaceActivity.this);

            }
        }

        @Override
        protected void onCancelled() {

        }


    }




}
