package com.uiza.sdk.view;

import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.MediaSource;
import com.uiza.sdk.utils.UZAppUtils;

import timber.log.Timber;

public final class UZPlayerNoAdsManager extends AbstractPlayerManager {

    public UZPlayerNoAdsManager(@NonNull UZVideoView uzVideo, String linkPlay, String thumbnailsUrl) {
        super(uzVideo, linkPlay, thumbnailsUrl);
        setRunnable();
    }

    @Override
    public void setRunnable() {
        handler = new Handler();
        runnable = () -> {
            if (uzVideoView == null || uzVideoView.getUZPlayerView() == null)
                return;
            handleVideoProgress();
            if (handler != null && runnable != null)
                handler.postDelayed(runnable, 1000);
        };
        handler.postDelayed(runnable, 0);
    }

    @Override
    void initSource() {
        DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = buildDrmSessionManager(drmScheme);
        if (drmScheme != null && drmSessionManager == null) return;

        player = buildPlayer(drmSessionManager);
        playerHelper = new UZPlayerHelper(player);
        uzVideoView.getUZPlayerView().setPlayer(player);

        MediaSource mediaSourceVideo = createMediaSourceVideo();
        // Prepare the player with the source.
        addPlayerListener();

        player.prepare(mediaSourceVideo);
        setPlayWhenReady(uzVideoView.isAutoStart());
        notifyUpdateButtonVisibility();
        if (UZAppUtils.hasSupportPIP(context)) {
            //Use Media Session Connector from the EXT library to enable MediaSession Controls in PIP.
            MediaSessionCompat mediaSession = new MediaSessionCompat(context, context.getPackageName());
            MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
            mediaSessionConnector.setPlayer(player);
            mediaSession.setActive(true);
        }
    }
}
