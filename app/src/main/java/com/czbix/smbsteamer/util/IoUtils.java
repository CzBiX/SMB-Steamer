package com.czbix.smbsteamer.util;

import android.util.Log;

import java.io.Closeable;

public class IoUtils {
    private static final String TAG = IoUtils.class.getSimpleName();

    public static void closeQuietly(Closeable io) {
        try {
            io.close();
        } catch (Exception e) {
            Log.w(TAG, "close io failed", e);
        }
    }
}
