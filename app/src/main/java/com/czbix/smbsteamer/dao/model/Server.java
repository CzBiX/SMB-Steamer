package com.czbix.smbsteamer.dao.model;

import org.json.JSONException;
import org.json.JSONObject;

import jcifs.smb.NtlmPasswordAuthentication;

public class Server {
    private static final String KEY_HOST = "host";
    private static final String KEY_SHARE = "share";
    private static final String KEY_CREDENTIAL = "credential";

    private final String mHost;
    private final String mShare;
    private final Credential mCredential;

    public Server(String host, String share, Credential credential) {
        mHost = host;
        mShare = share;
        mCredential = credential;
    }

    public NtlmPasswordAuthentication getCredential() {
        return mCredential.getNtlmAuth();
    }

    public String getHost() {
        return mHost;
    }

    public String getShare() {
        return mShare;
    }

    public String toJson() {
        final JSONObject json = new JSONObject();
        try {
            json.put(KEY_HOST, mHost)
                    .put(KEY_SHARE, mShare)
                    .put(KEY_CREDENTIAL, mCredential.toJson());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json.toString();
    }

    public static Server fromJson(String str) {
        try {
            final JSONObject json = new JSONObject(str);
            final String host = json.getString(KEY_HOST);
            final String share = json.getString(KEY_SHARE);
            final Credential credential = Credential.fromJson(json.getString(KEY_CREDENTIAL));

            return new Server(host, share, credential);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
