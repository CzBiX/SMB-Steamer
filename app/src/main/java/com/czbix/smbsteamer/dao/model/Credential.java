package com.czbix.smbsteamer.dao.model;

import org.json.JSONException;
import org.json.JSONObject;

import jcifs.smb.NtlmPasswordAuthentication;

public abstract class Credential {
    private static final String KEY_ANONYMOUS = "anonymous";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    public abstract boolean isAnonymous();
    protected abstract String getDomain();
    protected abstract String getUsername();
    protected abstract String getPassword();

    private NtlmPasswordAuthentication mAuthentication;
    public NtlmPasswordAuthentication getNtlmAuth() {
        if (mAuthentication == null) {
            if (isAnonymous()) {
                mAuthentication = NtlmPasswordAuthentication.ANONYMOUS;
            } else {
                mAuthentication = new NtlmPasswordAuthentication(getDomain(), getUsername(), getPassword());
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

            final String domain = json.getString(KEY_DOMAIN);
            final String username = json.getString(KEY_USERNAME);
            final String password = json.getString(KEY_PASSWORD);

            return new PasswordCredential(domain, username, password);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String toJson() {
        final JSONObject json = new JSONObject();
        try {
            json.put(KEY_ANONYMOUS, isAnonymous());
            if (!isAnonymous()) {
                json.put(KEY_DOMAIN, getDomain())
                        .put(KEY_USERNAME, getUsername())
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
        public String getDomain() {
            return "";
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
}
