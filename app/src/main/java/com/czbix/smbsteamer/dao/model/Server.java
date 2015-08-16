package com.czbix.smbsteamer.dao.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;

import org.json.JSONException;
import org.json.JSONObject;

import jcifs.smb.NtlmPasswordAuthentication;

public class Server implements Parcelable {
    private static final String KEY_HOST = "host";
    private static final String KEY_SHARE = "share";
    private static final String KEY_NAME = "name";
    private static final String KEY_CREDENTIAL = "credential";

    private final String mHost;
    private final String mShare;
    private final String mName;
    private final Credential mCredential;

    public Server(String host, String share, String name, Credential credential) {
        mHost = host;
        mShare = share;
        mName = name;
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

    public String getName() {
        return mName == null ? mShare : mName;
    }

    public String toJson() {
        final JSONObject json = new JSONObject();
        try {
            json.put(KEY_HOST, mHost)
                    .put(KEY_SHARE, mShare)
                    .put(KEY_NAME, mName)
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
            final String name = json.getString(KEY_NAME);
            final Credential credential = Credential.fromJson(json.getString(KEY_CREDENTIAL));

            return new Server(host, share, name, credential);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Server)) return false;
        Server server = (Server) o;
        return Objects.equal(mHost, server.mHost) &&
                Objects.equal(mShare, server.mShare) &&
                Objects.equal(mName, server.mName) &&
                Objects.equal(mCredential, server.mCredential);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mHost, mShare, mName, mCredential);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mHost);
        dest.writeString(this.mShare);
        dest.writeString(this.mName);
        dest.writeParcelable(this.mCredential, 0);
    }

    protected Server(Parcel in) {
        this.mHost = in.readString();
        this.mShare = in.readString();
        this.mName = in.readString();
        this.mCredential = Credential.CREATOR.createFromParcel(in);
    }

    public static final Creator<Server> CREATOR = new Creator<Server>() {
        public Server createFromParcel(Parcel source) {
            return new Server(source);
        }

        public Server[] newArray(int size) {
            return new Server[size];
        }
    };
}
