package com.czbix.smbsteamer.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {
    private static final PreferenceHelper INSTANCE;
    private static final String KEY_ONLY_VIDEO = "only_video";

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

    public static boolean isOnlyVideo() {
        return getPreferences().getBoolean(KEY_ONLY_VIDEO, true);
    }
}
