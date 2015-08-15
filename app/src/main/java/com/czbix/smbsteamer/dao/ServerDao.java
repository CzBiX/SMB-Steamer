package com.czbix.smbsteamer.dao;

import android.support.annotation.Nullable;

import com.czbix.smbsteamer.dao.model.Server;
import com.czbix.smbsteamer.helper.PreferenceHelper;

public class ServerDao {
    private static final String KEY_SERVER = "server";

    @Nullable
    public Server getServer() {
        final String server = PreferenceHelper.getPreferences().getString(KEY_SERVER, null);
        if (server == null) {
            return null;
        }

        return Server.fromJson(server);
    }
}
