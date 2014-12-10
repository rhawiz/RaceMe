package com.example.rawand.raceme;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by RAWAND on 20/11/2014.
 *
 * Methods related to shared preferences throughout the application.
 *
 */
public class SaveSharedPreference
{
    private static Boolean isTestMode = false;
    private static String units = "meter";
    static final String USER_DETAILS = "userDetails";


    /**
     * Get the shared preferences
     *
     * @param ctx Activity context
     * @return SharedPreferences object
     */
    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }


    /**
     * Sets user details in shared preferences
     *
     * @param context Activity context.
     * @param userDetails User object new user details
     */
    public static void setUserDetails(Context context, User userDetails)
    {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(userDetails);
        editor.clear();
        editor.putString(USER_DETAILS, json);
        editor.commit();
    }


    /**
     * Retrieve user details which was set in SharedPreferences
     *
     * @param context Activity context.
     * @return User user object containing current user details.
     */
    public static User getUserDetails(Context context)
    {
        Gson gson = new Gson();
        String json = getSharedPreferences(context).getString(USER_DETAILS,"");
        return gson.fromJson(json,User.class);

    }


    /**
     * Check if race is in testmode
     * @return true if in testmode, false if not.
     */
    public static Boolean isTestMode(){
        return isTestMode;
    }


    /**
     * Turn test mode on / off
     */
    public static void toggleTestMode(){
        if( isTestMode == false){
            isTestMode = true;
        }else{
            isTestMode = false;
        }
    }


    /**
     * retrieve units user has set.
     *
     * @return String units
     */
    public static String getUnits(){
        return units;
    }


    /**
     * Set the units
     * @param unitType String units.
     */
    public static void setUnits( String unitType){
        units = unitType.toLowerCase();
    }

}