package com.uiza.sampleplayer;

import android.support.multidex.BuildConfig;
import android.support.multidex.MultiDexApplication;

import com.uiza.sdk.UZPlayer;

import timber.log.Timber;

public class LSApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        UZPlayer.init(this);
    }

}
