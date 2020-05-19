package com.uiza.sdk;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.uiza.sdk.analytics.UZAnalytic;
import com.uiza.sdk.chromecast.Casty;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZAppUtils;
import com.uiza.sdk.utils.UZData;

public class UZPlayer {
    private UZPlayer() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    private static long elapsedTime = SystemClock.elapsedRealtime();

    /**
     * init SDK
     */
    public static void init(@NonNull Context context) {
        init(context, R.layout.uzplayer_skin_1);
    }

    /**
     * initSDK
     *
     * @param skinLayoutId Skin of player
     */
    public static void init(@NonNull Context context, @LayoutRes int skinLayoutId) {
        if (!UZAppUtils.isDependencyAvailable("com.google.android.exoplayer2.SimpleExoPlayer")) {
            throw new NoClassDefFoundError("Exo Player library is missing");
        }
        UZAnalytic.init(context.getApplicationContext());
        setUZPlayerSkinLayoutId(skinLayoutId);
        elapsedTime = SystemClock.elapsedRealtime();
    }

    /**
     * set Casty
     *
     * @param activity: Activity
     */
    public static void setCasty(@NonNull Activity activity) {
        if (UZAppUtils.isTV(activity))
            return;
        if (!UZAppUtils.checkChromeCastAvailable()) {
            throw new NoClassDefFoundError("Chromecast library is missing");
        }
        UZData.getInstance().setCasty(Casty.create(activity));
    }

    /**
     * @return Casty
     */
    public static Casty getCasty() {
        return UZData.getInstance().getCasty();
    }

    /**
     * set Player Skin layout_id
     *
     * @param resLayoutMain: id of layout xml
     */
    public static void setUZPlayerSkinLayoutId(@LayoutRes int resLayoutMain) {
        UZData.getInstance().setUZPlayerSkinLayoutId(resLayoutMain);
    }

    /**
     * user with UZDragView
     *
     * @param useUZDragView: boolean
     */
    public static void setUseWithUZDragView(boolean useUZDragView) {
        UZData.getInstance().setUseWithUZDragView(useUZDragView);
    }

    /**
     * set current UZPlayBack for Custom Link Play
     *
     * @param playback: {@link UZPlayback}
     */
    public static void setCurrentPlayback(UZPlayback playback) {
        UZData.getInstance().setPlayback(playback);
    }

    public static long getElapsedTime() {
        return elapsedTime;
    }
}
