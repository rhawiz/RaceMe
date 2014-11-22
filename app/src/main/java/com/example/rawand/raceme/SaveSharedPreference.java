package com.example.rawand.raceme;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by RAWAND on 20/11/2014.
 */
public class SaveSharedPreference
{
    static final String USER_NAME= "username";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context context, String userName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(USER_NAME, userName);
        editor.commit();
    }

    public static String getUserName(Context context)
    {
        return getSharedPreferences(context).getString(USER_NAME, "");
    }
}