package com.uiza.samplebroadcast;

import androidx.multidex.MultiDexApplication;

import timber.log.Timber;

public class SampleLiveApplication extends MultiDexApplication {

    public static final String EXTRA_STREAM_ENDPOINT = "uiza_live_extra_stream_endpoint";

    public static final String LIVE_URL = "rtmp://679b139b89-in.streamwiz.dev/transcode";
    public static final String STREAM_KEY = "live_ljNx4GLp3F";

    public static String getLiveEndpoint() {
        return LIVE_URL + "/" + STREAM_KEY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
