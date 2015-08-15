package com.czbix.smbsteamer.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {
    private static final PreferenceHelper INSTANCE;

    static {
        INSTANCE = new PreferenceHelper();
    }

    public static PreferenceHelper getInstance() {
        return INSTANCE;
    }

    public static SharedPreferences getPreferences() {
        return INSTANCE.mPreferences;
    }

    private SharedPreferences mPreferences;

    public void init(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }
}
