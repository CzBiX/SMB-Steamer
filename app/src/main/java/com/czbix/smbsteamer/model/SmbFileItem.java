package com.czbix.smbsteamer.model;

import android.net.Uri;
import android.util.Log;

import com.czbix.smbsteamer.helper.HttpServer;

import java.net.URL;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SmbFileItem {
    private static final String TAG = SmbFileItem.class.getSimpleName();

    private final SmbFile mSmbFile;
    private boolean mDirectory;

    public SmbFileItem(SmbFile smbFile, boolean isRoot) throws SmbException {
        mSmbFile = smbFile;
        if (!isRoot) {
            init();
        }
    }

    private void init() throws SmbException {
        mDirectory = mSmbFile.isDirectory();
    }

    public SmbFile get() {
        return mSmbFile;
    }

    public boolean isDirectory() {
        return mDirectory;
    }

    public String getName() {
        return mSmbFile.getName();
    }

    public Uri getHttpUri() {
        final URL url = mSmbFile.getURL();
        final Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").encodedAuthority("127.0.0.1:" + HttpServer.PORT)
                .encodedPath(HttpServer.URI_PREFIX)
                .appendEncodedPath(url.getAuthority())
                .appendEncodedPath(Uri.encode(url.getPath().substring(1), "/"));

        final Uri uri = builder.build();
        Log.v(TAG, "get http uri: " + uri.toString());
        return uri;
    }
}
