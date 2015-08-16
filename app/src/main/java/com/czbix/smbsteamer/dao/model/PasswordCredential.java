package com.czbix.smbsteamer.dao.model;

public class PasswordCredential extends Credential {
    private final String mUsername;
    private final String mPassword;

    public PasswordCredential(String username, String password) {
        mUsername = username;
        mPassword = password;
    }

    @Override
    public boolean isAnonymous() {
        return false;
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
