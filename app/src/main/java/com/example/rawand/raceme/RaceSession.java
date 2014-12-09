package com.example.rawand.raceme;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by RAWAND on 03/12/2014.
 */
public class RaceSession implements Parcelable{
    private String userId;
    private String raceType;
    private ArrayList<Location> locationArray;
    private Date startTime;
    private Date endTime;
    private String jsonCoordArray;
    private SimpleDateFormat dateFormat;
    private String formatedStartDate;
    private String formatedEndDate;
    private int totalTimeMins;

    private Map raceSessionMap;

    private String raceSessionJson;

    private String[] simpleCoordList;


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

        this.totalTimeMins = (int) ((long)TimeUnit.MILLISECONDS.toMinutes(endTime.getTime() - startTime.getTime()));

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

    public int getTotalTimeMins(){
        return totalTimeMins;
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

    @Override
    public String toString(){
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("E, d MMM yyyy");
        DateTime dateTime = new DateTime(startTime);

        String startDateFormatted = dateFormat.print(dateTime);


        return startDateFormatted + "\nTotal Duration:"+totalTimeMins+" mins";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(userId);
        out.writeString(raceType);
        out.writeTypedList(locationArray);
        out.writeValue(startTime);
        out.writeValue(endTime);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<RaceSession> CREATOR = new Parcelable.Creator<RaceSession>() {
        public RaceSession createFromParcel(Parcel in) {
            return new RaceSession(in);
        }

        public RaceSession[] newArray(int size) {
            return new RaceSession[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private RaceSession(Parcel in) {
        this.userId = in.readString();
        this.raceType = in.readString();
        locationArray = new ArrayList<Location>();
        in.readTypedList(locationArray,Location.CREATOR);
        this.startTime = (Date) in.readValue(null);
        this.endTime = (Date) in.readValue(null);
    }

}
