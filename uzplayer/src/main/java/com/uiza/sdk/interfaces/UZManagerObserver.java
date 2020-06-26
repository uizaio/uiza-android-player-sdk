package com.uiza.sdk.interfaces;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerView;
import com.uiza.sdk.utils.UZData;

public interface UZManagerObserver {

    default String getTitle() {
        return UZData.getInstance().getEntityName();
    }

    boolean isPIPEnable();

    PlayerView getPlayerView();

    // options
    boolean isCastingChromecast();

    boolean isAutoStart();

    UZAdPlayerCallback getAdPlayerCallback();

    // progress
    void onTimelineChanged(Timeline timeline, Object manifest, int reason);

    void onPlayerStateChanged(boolean playWhenReady, int playbackState);

    void onPlayerEnded();

    void onPlayerError(ExoPlaybackException error);
}
