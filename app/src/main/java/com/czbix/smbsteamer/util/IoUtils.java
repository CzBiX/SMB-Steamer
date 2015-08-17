package com.czbix.smbsteamer.util;

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.common.io.Files;

import java.io.Closeable;

public class IoUtils {
    private static final String TAG = IoUtils.class.getSimpleName();

    public static boolean isVideoFile(String fileName) {
        final String mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(Files.getFileExtension(fileName));

        return mimeType != null && mimeType.startsWith("video/");
    }

    public static void closeQuietly(Closeable io) {
        try {
            io.close();
        } catch (Exception e) {
            Log.w(TAG, "close io failed", e);
        }
    }
}
