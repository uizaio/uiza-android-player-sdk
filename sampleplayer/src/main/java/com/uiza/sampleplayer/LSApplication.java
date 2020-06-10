package com.uiza.sampleplayer;

import androidx.multidex.MultiDexApplication;

import com.uiza.api.UZApi;
import com.uiza.sdk.UZPlayer;

import timber.log.Timber;

public class LSApplication extends MultiDexApplication {

    public static final String[] urls = new String[]{
            "https://1775190502.rsc.cdn77.org/live/6ee23995-f4aa-45d3-9395-17ce60eebc40/extras/master.m3u8?cm=eyJlbnRpdHlfaWQiOiI2ZWUyMzk5NS1mNGFhLTQ1ZDMtOTM5NS0xN2NlNjBlZWJjNDAiLCJlbnRpdHlfc291cmNlIjoibGl2ZSIsImFwcF9pZCI6IjkyYThkMjAzMmZlODQ5MmFhNzc4MDRiNGMyYzUxOWM1In0=",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            "https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"};

    public static final String thumbnailUrl = "https://1955897154.rsc.cdn77.org/6fd7eafc8e6c441ea3f14c528f7266e6-static/2020/05/27/94a04fa4-07e2-43e5-9b86-d65f01bca611/thumbnail-10-8-720.jpeg";

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
