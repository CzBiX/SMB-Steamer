package com.czbix.smbsteamer.dao.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;

import org.json.JSONException;
import org.json.JSONObject;

import jcifs.smb.NtlmPasswordAuthentication;

public abstract class Credential implements Parcelable {
    private static final String KEY_ANONYMOUS = "anonymous";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    public abstract boolean isAnonymous();
    protected abstract String getUsername();
    protected abstract String getPassword();

    private NtlmPasswordAuthentication mAuthentication;
    public NtlmPasswordAuthentication getNtlmAuth() {
        if (mAuthentication == null) {
            if (isAnonymous()) {
                mAuthentication = NtlmPasswordAuthentication.ANONYMOUS;
            } else {
                mAuthentication = new NtlmPasswordAuthentication(null, getUsername(), getPassword());
            }
        }

        return mAuthentication;
    }

    public static Credential fromJson(String str) {
        try {
            final JSONObject json = new JSONObject(str);
            final boolean anonymous = json.getBoolean(KEY_ANONYMOUS);
            if (anonymous) {
                return ANONYMOUS;
            }

            final String username = json.getString(KEY_USERNAME);
            final String password = json.getString(KEY_PASSWORD);

            return new PasswordCredential(username, password);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String toJson() {
        final JSONObject json = new JSONObject();
        try {
            json.put(KEY_ANONYMOUS, isAnonymous());
            if (!isAnonymous()) {
                json.put(KEY_USERNAME, getUsername())
                        .put(KEY_PASSWORD, getPassword());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json.toString();
    }

    public static final Credential ANONYMOUS = new Credential() {
        @Override
        public boolean isAnonymous() {
            return true;
        }

        @Override
        public String getUsername() {
            return "";
        }

        @Override
        public String getPassword() {
            return "";
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        final boolean anonymous = isAnonymous();
        dest.writeByte((byte) (anonymous ? 1 : 0));
        if (anonymous) {
            return;
        }

        dest.writeString(getUsername());
        dest.writeString(getPassword());
    }

    public static final Creator<Credential> CREATOR = new Creator<Credential>() {
        public Credential createFromParcel(Parcel source) {
            final boolean anonymous = source.readByte() == 1;
            if (anonymous) {
                return ANONYMOUS;
            }

            final String username = source.readString();
            final String password = source.readString();

            return new PasswordCredential(username, password);
        }

        public Credential[] newArray(int size) {
            return new Credential[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Credential)) return false;
        Credential that = (Credential) o;
        if (isAnonymous() != that.isAnonymous()) {
            return false;
        }
        //noinspection SimplifiableIfStatement
        if (isAnonymous()) {
            return true;
        }
        return Objects.equal(getUsername(), that.getUsername()) &&
                Objects.equal(getPassword(), that.getPassword());
    }

    @Override
    public int hashCode() {
        if (isAnonymous()) {
            return Objects.hashCode(true);
        }
        return Objects.hashCode(getUsername(), getPassword());
    }
}
