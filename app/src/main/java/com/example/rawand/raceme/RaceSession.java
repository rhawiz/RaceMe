package com.example.rawand.raceme;

import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by RAWAND on 03/12/2014.
 */
public class RaceSession {
    private String userId;
    private String raceType;
    private ArrayList<Location> locationArray;
    private Date startTime;
    private Date endTime;
    private String jsonCoordArray;
    private SimpleDateFormat dateFormat;
    private String formatedStartDate;
    private String formatedEndDate;

    private Map raceSessionMap;

    private String raceSessionJson;

    private String[] simpleCoordList;

    RaceSession(){

    }



    RaceSession(String json){
        this(

                (String) new Gson().fromJson(json, Map.class).get("user_id"),

                (String) new Gson().fromJson(json, Map.class).get("race_type"),

                Utilities.toLocationArray((String[]) new Gson().fromJson(((String) new Gson().fromJson(json, Map.class).get("json_gps_coords")), String[].class) ),

                Utilities.getDateFromString((String) new Gson().fromJson(json, Map.class).get("start_time")),

                Utilities.getDateFromString((String) new Gson().fromJson(json, Map.class).get("end_time"))

        );
    }

    RaceSession(String userId, String raceType, ArrayList<Location> locationArray, Date startTime, Date endTime){
        this.userId = userId;
        this.raceType = raceType;
        this.locationArray = locationArray;
        this.startTime = startTime;
        this.endTime = endTime;

        simpleCoordList = new String[locationArray.size()];

        for (int i = 0; i < locationArray.size(); i++) {
            simpleCoordList[i] = locationArray.get(i).getLatitude() +","+locationArray.get(i).getLongitude();
        }

        this.jsonCoordArray = new Gson().toJson(simpleCoordList);
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.formatedStartDate = dateFormat.format(startTime);
        this.formatedEndDate = dateFormat.format(endTime);

        raceSessionMap = new HashMap<String, Object>();
        raceSessionMap.put("user_id",userId);
        raceSessionMap.put("race_type",raceType);
        raceSessionMap.put("json_gps_coords",jsonCoordArray);
        raceSessionMap.put("start_time",formatedStartDate);
        raceSessionMap.put("end_time",formatedEndDate);


        Gson gson = new GsonBuilder().create();

        this.raceSessionJson = gson.toJson(raceSessionMap);

    }


    public String getUserId() {
        return userId;
    }

    public String getRaceType() {
        return raceType;
    }

    public ArrayList<Location> getSimpleCoordArray() {
        return locationArray;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Map<String,Object> getMap(){
        return raceSessionMap;
    }

    public String getJSON(){
        return raceSessionJson;
    }

    public String getSqlInsertStatement(){
        return "INSERT INTO `coac11`.`race_log_table` (`user_id`, `race_type`, `json_gps_coords`, `start_time`, `end_time`) VALUES ('" + userId + " ', '" + raceType+ " ', '" + jsonCoordArray + "', '" + formatedStartDate + "', '" + formatedEndDate + "');";
    }

}
