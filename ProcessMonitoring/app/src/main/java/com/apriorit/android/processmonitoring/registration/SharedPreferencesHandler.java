package com.apriorit.android.processmonitoring.registration;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHandler {
    private SharedPreferences mSharedPref;
    private static final String APP_PREFERENCES = "authentication_data";
    private static final String APP_PREFERENCES_LOGIN = "Login";
    private static final String APP_PREFERENCES_MASTER_KEY = "Key";

    public SharedPreferencesHandler(Context context) {
        mSharedPref = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    void saveLogin(String login) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(APP_PREFERENCES_LOGIN, login);
        editor.apply();
    }

    void saveKey(String key) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(APP_PREFERENCES_MASTER_KEY, key);
        editor.apply();
    }

    public String getLogin() {
        if (mSharedPref.contains(APP_PREFERENCES_LOGIN)) {
            return mSharedPref.getString(APP_PREFERENCES_LOGIN, "");
        }
        return null;
    }

    public void clearData() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.clear();
        editor.apply();
    }

    public String getMasterKey() {
        if (mSharedPref.contains(APP_PREFERENCES_MASTER_KEY)) {
            return mSharedPref.getString(APP_PREFERENCES_MASTER_KEY, "");
        }
        return null;
    }
}
