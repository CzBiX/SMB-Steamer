package com.czbix.smbsteamer.service;

import android.app.IntentService;
import android.content.Intent;

import com.czbix.smbsteamer.helper.HttpServer;

import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class StreamService extends IntentService {
    private static final int SLEEP_SECONDS = 60 * 1000;
    private static boolean mRunning;
    private static volatile boolean mStop;

    public StreamService() {
        super("StreamService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mRunning = true;
        final HttpServer httpServer = new HttpServer();
        try {
            httpServer.start();
            while (true) {
                if (mStop) {
                    mStop = false;
                    break;
                }

                Thread.sleep(SLEEP_SECONDS);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // ignore it
        }

        httpServer.stop();
        mRunning = false;
    }

    public static void stop() {
        mStop = true;
    }

    public static boolean isRunning() {
        return mRunning;
    }
}
