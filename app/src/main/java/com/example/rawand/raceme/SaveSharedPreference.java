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
 */
public class SaveSharedPreference
{
    private static Boolean isTestMode = false;
    private static String units = "meter";
    static final String USER_DETAILS = "userDetails";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserDetails(Context context, User userDetails)
    {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        Gson gson = new Gson();
        String json = gson.toJson(userDetails);
        editor.clear();
        editor.putString(USER_DETAILS, json);
        editor.commit();
    }

    public static User getUserDetails(Context context)
    {
        Gson gson = new Gson();
        String json = getSharedPreferences(context).getString(USER_DETAILS,"");
        return gson.fromJson(json,User.class);

    }

    public static Boolean isTestMode(){
        return isTestMode;
    }

    public static void toggleTestMode(){
        if( isTestMode == false){
            isTestMode = true;
        }else{
            isTestMode = false;
        }
    }

    public static String getUnits(){
        return units;
    }

    public static void setUnits( String unitType){
         units = unitType.toLowerCase();
    }

}