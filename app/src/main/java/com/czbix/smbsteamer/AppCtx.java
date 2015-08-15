package com.czbix.smbsteamer;

import android.app.Application;

import com.czbix.smbsteamer.helper.PreferenceHelper;
import com.czbix.smbsteamer.util.SmbUtils;

public class AppCtx extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initPrefs();
        initSmb();
    }

    private void initPrefs() {
        PreferenceHelper.getInstance().init(this);
    }

    private void initSmb() {
        SmbUtils.init();
    }
}
