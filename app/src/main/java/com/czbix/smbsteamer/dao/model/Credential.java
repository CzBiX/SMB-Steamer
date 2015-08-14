package com.czbix.smbsteamer.dao.model;

public abstract class Credential {
    public abstract boolean isAonymous();
    public abstract String getDomain();
    public abstract String getUsername();
    public abstract String getPassword();
}
