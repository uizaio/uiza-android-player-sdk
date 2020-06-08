package com.uiza.sampleplayer;

import androidx.multidex.MultiDexApplication;

import com.uiza.api.UZApi;
import com.uiza.sdk.UZPlayer;

import timber.log.Timber;

public class LSApplication extends MultiDexApplication {

    public static final String[] urls = new String[]{
            "https://1775190502.rsc.cdn77.org/live/1ffbc4db-8dea-4087-a281-a6c3003cb121/master.m3u8?cm=eyJlbnRpdHlfaWQiOiIxZmZiYzRkYi04ZGVhLTQwODctYTI4MS1hNmMzMDAzY2IxMjEiLCJlbnRpdHlfc291cmNlIjoibGl2ZSIsImFwcF9pZCI6ImI5NjNiNDY1YzM0ZTRmZmI5YTcxOTIyNDQyZWUwZGNhIn0=",
            "https://1775190502.rsc.cdn77.org/live/1ffbc4db-8dea-4087-a281-a6c3003cb121/manifest.mpd?cm=eyJlbnRpdHlfaWQiOiIxZmZiYzRkYi04ZGVhLTQwODctYTI4MS1hNmMzMDAzY2IxMjEiLCJlbnRpdHlfc291cmNlIjoibGl2ZSIsImFwcF9pZCI6ImI5NjNiNDY1YzM0ZTRmZmI5YTcxOTIyNDQyZWUwZGNhIn0=",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            "https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd"};

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
