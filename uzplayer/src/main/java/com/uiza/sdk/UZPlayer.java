package com.uiza.sdk;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.uiza.sdk.chromecast.Casty;
import com.uiza.sdk.models.UZPlayback;
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
        init(context, R.layout.uzplayer_skin_1);
    }

    /**
     *
     * @param context      see {@link Context}
     * @param skinLayoutId Skin of player
     */
    public static void init(@NonNull Context context,
                            @LayoutRes int skinLayoutId) {
        if (!UZAppUtils.isDependencyAvailable("com.google.android.exoplayer2.SimpleExoPlayer")) {
            throw new NoClassDefFoundError("Exo Player library is missing");
        }
        LocalData.init(context);
        setUZPlayerSkinLayoutId(skinLayoutId);
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
}
