package com.czbix.smbsteamer.dao.model;

public class PasswordCredential extends Credential {
    private final String mDomain;
    private final String mUsername;
    private final String mPassword;

    public PasswordCredential(String domain, String username, String password) {
        mDomain = domain;
        mUsername = username;
        mPassword = password;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public String getDomain() {
        return mDomain;
    }

    @Override
    public String getUsername() {
        return mUsername;
    }

    @Override
    public String getPassword() {
        return mPassword;
    }
}
