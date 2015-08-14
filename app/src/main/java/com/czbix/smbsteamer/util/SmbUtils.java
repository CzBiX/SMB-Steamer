package com.czbix.smbsteamer.util;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import jcifs.Config;

public class SmbUtils {
    private static final String TAG = SmbUtils.class.getSimpleName();

    public static void init() {
        Config.setProperty("jcifs.resolveOrder", "BCAST,DNS");
    }

    public static String getMineType(String url) {
        final String ext = getFileExtensionFromUrl(url);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    }

    private static String getFileExtensionFromUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }

            int filenamePos = url.lastIndexOf('/');
            String filename =
                    0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            if (!filename.isEmpty()) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }

        return "";
    }
}
