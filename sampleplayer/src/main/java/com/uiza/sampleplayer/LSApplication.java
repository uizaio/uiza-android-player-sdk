package com.uiza.sampleplayer;

import androidx.multidex.MultiDexApplication;

import com.uiza.api.UZApi;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.utils.UZData;

import timber.log.Timber;

public class LSApplication extends MultiDexApplication {

    public static final String[] urls = new String[]{
            "https://1775190502.rsc.cdn77.org/live/4e6dfc96-c1a1-4659-ad28-956f7b8340ff/master.m3u8?cm=eyJlbnRpdHlfaWQiOiI0ZTZkZmM5Ni1jMWExLTQ2NTktYWQyOC05NTZmN2I4MzQwZmYiLCJlbnRpdHlfc291cmNlIjoibGl2ZSIsImFwcF9pZCI6ImYxOWE0ZmQzYmFhMjQ0YmZhMThhODZjMDE0NDEzYWU4In0=",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            "https://bitmovin-a.akamaihd.net/content/playhouse-vr/mpds/105560.mpd",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"};

    public static final String thumbnailUrl = "https://1955897154.rsc.cdn77.org/6fd7eafc8e6c441ea3f14c528f7266e6-static/2020/05/27/94a04fa4-07e2-43e5-9b86-d65f01bca611/thumbnail-10-8-720.jpeg";
    public static final String VAST_SAMPLE_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&" +
            "iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&" +
            "env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26" +
            "sample_ct%3Dskippablelinear&correlator=";
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        UZPlayer.init(this);
        UZApi.init(this,"SamplePlayer", BuildConfig.VERSION_NAME);
//        UZData.getInstance().setUrlIMAAd(VAST_SAMPLE_URL);
    }
}
