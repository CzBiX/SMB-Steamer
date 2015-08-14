package com.czbix.smbsteamer;

import android.app.Application;

import com.czbix.smbsteamer.util.SmbUtils;

public class AppCtx extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initSmb();
    }

    private void initSmb() {
        SmbUtils.init();
    }
}
