package com.uiza.sampleplayer;

import androidx.multidex.MultiDexApplication;

import com.uiza.api.UZApi;
import com.uiza.sdk.UZPlayer;

import timber.log.Timber;

public class LSApplication extends MultiDexApplication {

    public static final String[] urls = new String[]{
            "https://1443866306.rsc.cdn77.org/live/177957b6-e8f6-4bd4-b774-fa954d6315d1/master.m3u8?cm=eyJlbnRpdHlfaWQiOiIxNzc5NTdiNi1lOGY2LTRiZDQtYjc3NC1mYTk1NGQ2MzE1ZDEiLCJlbnRpdHlfc291cmNlIjoibGl2ZSIsImFwcF9pZCI6ImEwYzJiMTM1YzU4ZTQ4ZmJhNDZkMDlmZjA4MGYyYTJkIn0=",
            "https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            "https://s3-ap-southeast-1.amazonaws.com/cdnetwork-test/drm_sample_byterange/manifest.mpd"};

    public static final String thumbnailUrl = "https://i.insider.com/5ae1e2b3bd96711e008b4704?width=1100&format=jpeg&auto=webp";

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        UZPlayer.init(this);
        UZApi.init(this,"SamplePlayer", BuildConfig.VERSION_NAME);
    }

}
