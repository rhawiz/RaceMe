package com.example.rawand.raceme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Text;

import java.util.Date;


public class ViewRaceActivity extends BaseActivity {

    private TextView dateStarted;
    private TextView dateFinished;
    private TextView timeStarted;
    private TextView timeFinished;
    private TextView activityType;
    private TextView totalDistance;
    private TextView avgSpeed;
    private TextView duration;
    private TabHost tabHost;

    private String timeStartedFormatted;
    private String timeFinishedFormatted;
    private String dateStartedFormatted;
    private String dateFinishedFormatted;

    private String totalDistanceValue;

    private Date dateStartedValue;
    private Date dateFinishedValue;
    private String avgSpeedValue;

    private GoogleMap map;
    private PolylineOptions routePolylineOption;
    private Polyline routePolyline;

    private RaceSession raceSession;

    private DateTime dateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_race);

        Intent i = getIntent();
        raceSession = (RaceSession) i.getParcelableExtra("raceSession");


        dateStarted = (TextView) findViewById(R.id.date_started_value);
        dateFinished = (TextView) findViewById(R.id.date_finished_value);
        timeStarted = (TextView) findViewById(R.id.time_started_value);
        timeFinished = (TextView) findViewById(R.id.time_finished_value);
        activityType = (TextView) findViewById(R.id.activity_type_value);
        totalDistance = (TextView) findViewById(R.id.view_race_total_distance_value);
        avgSpeed = (TextView) findViewById(R.id.avg_speed_value);
        duration = (TextView) findViewById(R.id.duration_value);


        initTabs();
        initMap();
        initDetailTabContent();

    }

    private void initDetailTabContent(){

        DateTimeFormatter timeFormat = DateTimeFormat.forPattern("kk:mm a");
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("E, d MMM yyyy");


        totalDistanceValue = String.valueOf(RaceUtils.calculateTotalDistance(raceSession.getSimpleCoordArray()));

        dateStartedValue = raceSession.getStartTime();

        dateFinishedValue = raceSession.getEndTime();

        dateTime = new DateTime(dateStartedValue);
        dateStartedFormatted = dateFormat.print(dateTime);

        dateTime = new DateTime(dateFinishedValue);
        dateFinishedFormatted = dateFormat.print(dateTime);


        dateTime = new DateTime(dateStartedValue);
        timeStartedFormatted = timeFormat.print(dateTime);

        dateTime = new DateTime(dateFinishedValue);
        timeFinishedFormatted = timeFormat.print(dateTime);

        dateStarted.setText(dateStartedFormatted);
        dateFinished.setText(dateFinishedFormatted);
        timeStarted.setText(timeStartedFormatted);
        timeFinished.setText(timeFinishedFormatted);
        activityType.setText(raceSession.getRaceType());
        totalDistance.setText(totalDistanceValue);

        if(raceSession.getTotalTimeMins() != 0)
            avgSpeed.setText(Integer.valueOf(totalDistanceValue) / raceSession.getTotalTimeMins());
        else
            avgSpeed.setText("0");

        duration.setText(String.valueOf(raceSession.getTotalTimeMins()));


        //dateStarted.setText(raceSession.getStartTime());
    }
    private void initTabs(){


        tabHost = (TabHost) findViewById(R.id.view_race_tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("details");
        tabSpec.setContent(R.id.view_race_details_tab);
        tabSpec.setIndicator("Race Details");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("map");
        tabSpec.setContent(R.id.view_race_map_tab);
        tabSpec.setIndicator("MAP");
        tabHost.addTab(tabSpec);


        tabHost.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()){
            TransitionAnimations transition = new TransitionAnimations();
            public void onSwipeLeft() {
                int nextTab = tabHost.getCurrentTab() + 1;
                if(nextTab > 2)nextTab = 0;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.inFromRightAnimation());
            }
            public void onSwipeRight() {
                int nextTab = tabHost.getCurrentTab() - 1;
                if(nextTab < 0)nextTab = 2;

                tabHost.setCurrentTab(nextTab);
                tabHost.getCurrentView().setAnimation(transition.outToRightAnimation());
            }
        });



    }

    private void initMap(){
        routePolylineOption = new PolylineOptions().width(5)
                .color(Color.BLUE)
                .geodesic(true);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.view_race_map)).getMap();

        for (android.location.Location i : raceSession.getSimpleCoordArray()) {
            routePolylineOption.add(new LatLng(i.getLatitude(), i.getLongitude())); // Closes the polyline.
        }

        routePolyline = map.addPolyline(routePolylineOption);

        if(raceSession.getSimpleCoordArray().size() > 0){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(raceSession.getSimpleCoordArray().get(0).getLatitude(), raceSession.getSimpleCoordArray().get(0).getLongitude()), 17));
        }

        map.setMyLocationEnabled(false);
        map.addPolyline(routePolylineOption);
    }


}
