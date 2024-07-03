package com.samsung.mcc.webapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.prefs.Preferences;

public class MyPreferences {

    private static MyPreferences myPreferences;
    private SharedPreferences preferences;

    public MyPreferences(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static MyPreferences getInstance(Context context) {
        if (myPreferences == null)
            myPreferences = new MyPreferences(context);
        return myPreferences;
    }

    public final static String PREFS_NAME = "mcc_scanner_prefs";

    public boolean sharedPreferenceExist(String key) {
        if (!preferences.contains(key)) {
            return true;
        } else {
            return false;
        }
    }

    public void setInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key,int defaulValue) {
        return preferences.getInt(key, defaulValue);
    }

    public void setStr(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getStr(String key,String defaulValue) {
        return preferences.getString(key, defaulValue);
    }

    public void setBool(String key, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBool(String key,boolean defaulValue) {
        return preferences.getBoolean(key, defaulValue);
    }
}