package com.uiza.sdk;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.uiza.sdk.chromecast.Casty;
import com.uiza.sdk.models.UZPlaybackInfo;
import com.uiza.sdk.util.LocalData;
import com.uiza.sdk.util.UZAppUtils;
import com.uiza.sdk.util.UZData;

public class UZPlayer {
    private UZPlayer() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * @param context see {@link Context}
     */
    public static void init(@NonNull Context context) {
        init(context, R.layout.uz_player_skin_1);
    }

    /**
     * InitSDK
     *
     * @param context         see {@link Context}
     * @param currentPlayerId Skin of player
     */
    public static void init(@NonNull Context context,
                            @LayoutRes int currentPlayerId) {
        if (!UZAppUtils.isDependencyAvailable("com.google.android.exoplayer2.SimpleExoPlayer")) {
            throw new NoClassDefFoundError("Exo Player library is missing");
        }
        LocalData.init(context);
        setCurrentPlayerId(currentPlayerId);
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
     * set Player Id
     *
     * @param resLayoutMain: id of layout xml
     */
    public static void setCurrentPlayerId(@LayoutRes int resLayoutMain) {
        UZData.getInstance().setCurrentPlayerId(resLayoutMain);
    }

    /**
     * user with VDHView
     *
     * @param isUseWithVDHView: boolean
     */
    public static void setUseWithVDHView(boolean isUseWithVDHView) {
        UZData.getInstance().setUseWithVDHView(isUseWithVDHView);
    }

    /**
     * set current PlayBackInfo for Custom Link Play
     *
     * @param playback: {@link UZPlaybackInfo}
     */
    public static void setCurrentPlaybackInfo(UZPlaybackInfo playback) {
        UZData.getInstance().setPlaybackInfo(playback);
    }
}
