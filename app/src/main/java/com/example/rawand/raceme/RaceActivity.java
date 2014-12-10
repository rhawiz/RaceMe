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
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ListView;
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
import java.util.HashMap;
import java.util.List;

/**
 * Race screen where user is able to start a race session, view current route on a map and view past routes.
 */
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

    private User currentUser;


    private RadioGroup raceTypeRadioGroup;

    private RadioButton walkRadio;
    private RadioButton runRadio;
    private RadioButton cycleRadio;

    PolylineOptions routePolylineOption;
    Polyline routePolyline;


    private static Chronometer chronometer; //To count elapsed race time
    private int distanceTravelled;

    private DataUpdateReceiver dataUpdateReceiver;
    private IntentFilter intentFilter;

    private String raceType;

    public LocationClient mLocationClient;

    private ArrayList<Location> gpsCoordArray;
    GoogleMap map;

    private ArrayList<RaceSession> raceSessionArray;
    private RaceArrayAdapter raceSessionListAdapter;
    private ListView raceSessionListView;
    private TextView noRaceSessionTextView;


    Handler alertHandler;
    private int averageSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_race, frameLayout);
        drawerList.setItemChecked(position, true);
        setTitle(listArray[position]);

        initTabs();

        initMap();

        currentUser =  SaveSharedPreference.getUserDetails(RaceActivity.this);

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

        raceSessionListView = (ListView) findViewById(R.id.race_session_listview);
        raceSessionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos, long id) {
                RaceSession selectedSession = raceSessionListAdapter.getItem(pos);
                Intent intent = new Intent(RaceActivity.this, ViewRaceActivity.class);
                intent.putExtra("raceSession",(Parcelable) selectedSession);
                startActivity(intent);
            }
        });

        noRaceSessionTextView = (TextView) findViewById(R.id.race_session_label);

        stopButton = (ImageButton) findViewById(R.id.stop_img_button);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
                stopRaceSession();
            }
        });

        GetRaceSessionsTask getRaceSessionsTask = new GetRaceSessionsTask();
        getRaceSessionsTask.execute();


        if(isServiceRunning(RaceService.class)){
            //If the service is running, it means the user has quit the application without stopping the race session
            resumeSession();

        }

    }


    /**
     * Initialise the Google Maps
     */
    private void initMap(){
        routePolylineOption = new PolylineOptions().width(5)
                .color(Color.BLUE)
                .geodesic(true);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.race_map)).getMap();

        map.setMyLocationEnabled(true);
        map.addPolyline(routePolylineOption);
    }


    /**
     * Called when application was closed but race session was not stopped.
     * Will communicate with the running race service and retrieve data to contiune the race session.
     */
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

    /**
     * Called when receiving a location change broadcast.
     * It will calculate the distance from the last received coordinates and update total distance, average distance and the map route drawing.
     */

    private void updateDistance(){
        if(gpsCoordArray.size() > 1){

            distanceTravelled+=RaceUtils.getDistance(gpsCoordArray.get( gpsCoordArray.size()-2),gpsCoordArray.get(gpsCoordArray.size()-1));
            distanceTravelledView.setText(String.valueOf(distanceTravelled));
        }

        Chronometer clock = (Chronometer)findViewById(R.id.timer);
        String timeStr = (String) clock.getText();
        String[] tokens = timeStr.split(":");
        int mins = Integer.parseInt(tokens[0]);
        int secs = Integer.parseInt(tokens[1]);
        int duration = (60 * mins) + secs;
        if (duration > 0) {
            averageSpeed = distanceTravelled / (int) duration;
            TextView averageSpeedText = (TextView) findViewById(R.id.average_speed_view);
            averageSpeedText.setText(String.valueOf(averageSpeed));
        }

    }


    /**
     * Called when user touches start race button
     * Will set all elements to their start states and start the race background service
     */
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


    /**
     * Called when user touches stop race.
     * Will stop the race service and call logSession to log the current race route.
     */
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


    /**
     * Creates new RaceSession object to store session details.
     * Then calls an Async Task to handle logging of the session to the database
     */
    private void logSession() {
        String userId = SaveSharedPreference.getUserDetails(this).getUserId();
        RaceSession session = new RaceSession(userId,raceType,gpsCoordArray,startTime,endTime);

        LogSessionTask logSessionTask = new LogSessionTask(session);
        logSessionTask.execute();


    }


    /**
     * Initialise all tabs
     */
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

    /**
     * Custom Array adapter to handle and store user RaceSessions when viewing past races
     */
    private class RaceArrayAdapter extends ArrayAdapter<RaceSession> {

        HashMap<RaceSession, Integer> mIdMap = new HashMap<RaceSession, Integer>();

        public RaceArrayAdapter(Context context, int textViewResourceId,
                                List<RaceSession> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            RaceSession item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
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


    /**
     * Broadcast receiver to receive broadcasts from RaceService whenever there is a change in location.
     * Will handle what needs to be called whenever the user location changes.
     *
     */
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


    /**
     * Created and called when the user stops the current session.
     * Takes in a RaceSession object and will insert into database table.
     */
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


    /**
     * Async task to retrieve and populate past race sessions.
     */
    public class GetRaceSessionsTask extends AsyncTask<Void, Void, Boolean> {



        public GetRaceSessionsTask() {

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String userId = currentUser.getUserId();

            raceSessionArray = DatabaseHelper.getUserRaces(userId);

            raceSessionListAdapter = new RaceArrayAdapter(RaceActivity.this,android.R.layout.simple_list_item_1, raceSessionArray);


            return true;


        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {

                raceSessionListView.setAdapter(raceSessionListAdapter);


                if(raceSessionListView.getCount() == 0){
                    noRaceSessionTextView.setVisibility(View.VISIBLE);
                    raceSessionListView.setVisibility(View.GONE);
                }

            }
        }

        @Override
        protected void onCancelled() {

        }


    }




}
