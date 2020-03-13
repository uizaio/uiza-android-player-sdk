package com.uiza.sdk.view;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.MediaSource;

public final class UZPlayerNoAdsManager extends IUizaPlayerManager {

    public UZPlayerNoAdsManager(@NonNull UZVideoView uzVideo, String linkPlay, String thumbnailsUrl) {
        super(uzVideo, linkPlay, thumbnailsUrl);
        setRunnable();
    }

    @Override
    public void setRunnable() {
        handler = new Handler();
        runnable = () -> {
            if (uzVideo == null || uzVideo.getUzPlayerView() == null)
                return;
            handleVideoProgress();
            if (handler != null && runnable != null) {
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.postDelayed(runnable, 0);
    }

    @Override
    void initSource() {
        DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = buildDrmSessionManager(drmScheme);
        if (drmScheme != null && drmSessionManager == null) return;

        player = buildPlayer(drmSessionManager);
        playerHelper = new UZPlayerHelper(player);
        uzVideo.getUzPlayerView().setPlayer(player);

        MediaSource mediaSourceVideo = createMediaSourceVideo();
        // Prepare the player with the source.
        addPlayerListener();

        player.prepare(mediaSourceVideo);
        setPlayWhenReady(uzVideo.isAutoStart());
        if (uzVideo.isLivestream()) {
            playerHelper.seekToDefaultPosition();
        } else {
            seekTo(contentPosition);
        }
        notifyUpdateButtonVisibility();
    }
}
