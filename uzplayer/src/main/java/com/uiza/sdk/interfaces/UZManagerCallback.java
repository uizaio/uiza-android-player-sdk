package com.uiza.sdk.interfaces;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ui.PlayerView;

public interface UZManagerCallback {

    PlayerView getPlayerView();

    // options
    boolean isCastingChromecast();

    boolean isAutoStart();

    void setUseController(boolean useController);

    // progress
    void onTimelineChanged(Timeline timeline, Object manifest, int reason);

    void onPlayerStateChanged(boolean playWhenReady, int playbackState);

    void onPlayerEnded();

    void onPlayerError(ExoPlaybackException error);
}
