package com.uiza.sampleplayer;

import androidx.multidex.MultiDexApplication;

import com.uiza.api.UZApi;
import com.uiza.sdk.UZPlayer;

import timber.log.Timber;

public class LSApplication extends MultiDexApplication {

    public static final String[] urls = new String[]{
            "https://1775190502.rsc.cdn77.org/live/b938c0a6-e9bc-4b25-9e66-dbf81d755c25/master.m3u8?cm=eyJlbnRpdHlfaWQiOiJiOTM4YzBhNi1lOWJjLTRiMjUtOWU2Ni1kYmY4MWQ3NTVjMjUiLCJlbnRpdHlfc291cmNlIjoibGl2ZSIsImFwcF9pZCI6ImI5NjNiNDY1YzM0ZTRmZmI5YTcxOTIyNDQyZWUwZGNhIn0=",
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
