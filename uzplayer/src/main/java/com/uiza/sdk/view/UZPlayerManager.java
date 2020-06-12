package com.uiza.sdk.view;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo;
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.uiza.sdk.interfaces.UZAdPlayerCallback;
import com.uiza.sdk.utils.UZAppUtils;

import java.util.Objects;

import timber.log.Timber;

/**
 * Manages the {@link ExoPlayer}, the IMA plugin and all video playback.
 */
//https://medium.com/@takusemba/understands-callbacks-of-exoplayer-c05ac3c322c2
public final class UZPlayerManager extends AbstractPlayerManager {

    private String urlIMAAd;
    private ImaAdsLoader adsLoader = null;
    private boolean isOnAdEnded;
    private UZAdPlayerCallback uzAdPlayerCallback;
    private UZVideoAdPlayerListener uzVideoAdPlayerListener = new UZVideoAdPlayerListener();
    MediaSource mediaSourceVideo;
    MediaSource mediaSourceTimeShift;
    MediaSessionCompat mediaSession;

    public static class Builder {
        private Context context;
        private String playUrl;
        private String imaAdUrl;
        private String drmScheme;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder withPlayUrl(String playUrl) {
            this.playUrl = playUrl;
            return this;
        }

        public Builder withIMAAdUrl(String imaAdUrl) {
            this.imaAdUrl = imaAdUrl;
            return this;
        }

        public Builder withDrmScheme(String drmScheme) {
            this.drmScheme = drmScheme;
            return this;
        }

        public UZPlayerManager build() {
            return new UZPlayerManager(context, playUrl, imaAdUrl, drmScheme);
        }
    }


    private UZPlayerManager(@NonNull Context context, String linkPlay, String urlIMAAd, String drmSchema) {
        super(context, linkPlay, drmSchema);
        this.urlIMAAd = urlIMAAd;
        setRunnable();
    }

    private void onAdEnded() {
        if (!isOnAdEnded) {
            isOnAdEnded = true;
            if (progressListener != null)
                progressListener.onAdEnded();
        }
    }

    @Override
    protected boolean isPlayingAd() {
        return player!= null && player.isPlayingAd();
    }

    @Override
    public void setRunnable() {
        handler = new Handler();
        runnable = () -> {
            if (managerCallback == null || managerCallback.getPlayerView() == null)
                return;
            if (!isPlayingAd()) {
                handleVideoProgress();
            }
            if (handler != null && runnable != null) {
                handler.postDelayed(runnable, 1000);
            }
        };
        new Handler().postDelayed(runnable, 0);
    }

    @Override
    void initSource() {
        isOnAdEnded = false;
        DefaultDrmSessionManager<ExoMediaCrypto> drmSessionManager = buildDrmSessionManager();
        if (this.drmScheme != null && drmSessionManager == null) return;
        mediaSourceVideo = createMediaSourceVideo(drmSessionManager);
        mediaSourceTimeShift = createMediaSourceTimeShift(drmSessionManager);
        // Compose the content media source into a new AdsMediaSource with both ads and content.
        initPlayerListeners();
        if (!TextUtils.isEmpty(urlIMAAd) && UZAppUtils.isAdsDependencyAvailable()) {
            mediaSourceVideo = createAdsMediaSource(mediaSourceVideo, Uri.parse(urlIMAAd));
            if (mediaSourceTimeShift != null) {
                mediaSourceTimeShift = createAdsMediaSource(mediaSourceTimeShift, Uri.parse(urlIMAAd));
            }
            if (adsLoader != null) {
                adsLoader.setPlayer(player);
                adsLoader.addCallback(uzVideoAdPlayerListener);
            }
        }
        player.prepare(mediaSourceVideo);
        setPlayWhenReady(managerCallback.isAutoStart());
        notifyUpdateButtonVisibility();
        if (UZAppUtils.hasSupportPIP(context)) {
            //Use Media Session Connector from the EXT library to enable MediaSession Controls in PIP.
            mediaSession = new MediaSessionCompat(context, context.getPackageName());
            MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(mediaSession);
            mediaSessionConnector.setPlayer(player);
            mediaSession.setCallback(mediasSessionCallback);
            mediaSession.setActive(true);
        }
    }

    private MediaSessionCompat.Callback mediasSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onPause() {
            super.onPause();
            pause();
        }
        @Override
        public void onStop() {
            super.onStop();
            stop();
        }
        @Override
        public void onPlay() {
            super.onPlay();
            resume();
        }
    };

    boolean switchTimeShift(boolean useTimeShift) {
        if (mediaSourceTimeShift != null) {
            player.prepare(useTimeShift ? mediaSourceTimeShift : mediaSourceVideo);
            player.setPlayWhenReady(true);
            return true;
        }
        return false;
    }

    private MediaSource createAdsMediaSource(MediaSource mediaSource, Uri adTagUri) {
        if (adsLoader == null) {
            adsLoader = new ImaAdsLoader(context, adTagUri);
        }
        MediaSourceFactory adMediaSourceFactory = new MediaSourceFactory() {
            DrmSessionManager<?> drmSessionManager;

            @Override
            public MediaSourceFactory setDrmSessionManager(DrmSessionManager<?> drmSessionManager) {
                this.drmSessionManager = drmSessionManager;
                return this;
            }

            @Override
            public MediaSource createMediaSource(Uri uri) {
                return buildMediaSource(uri, drmSessionManager);
            }

            @Override
            public int[] getSupportedTypes() {
                return new int[]{C.TYPE_DASH, C.TYPE_SS, C.TYPE_HLS, C.TYPE_OTHER};
            }
        };
        return new AdsMediaSource(mediaSource, adMediaSourceFactory, adsLoader,
                managerCallback.getPlayerView());

    }

    @Override
    public void release() {
        if(mediaSession != null)
            mediaSession.release();
        if (adsLoader != null) {
            adsLoader.removeCallback(uzVideoAdPlayerListener);
            adsLoader.setPlayer(null);
            adsLoader.release();
            adsLoader = null;
            urlIMAAd = null;
        }
        super.release();
    }

    void setAdPlayerCallback(UZAdPlayerCallback uzAdPlayerCallback) {
        this.uzAdPlayerCallback = uzAdPlayerCallback;
    }

    private class UZVideoAdPlayerListener implements VideoAdPlayer.VideoAdPlayerCallback {

        @Override
        public void onPlay(AdMediaInfo mediaInfo) {
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onPlay();
        }

        @Override
        public void onVolumeChanged(AdMediaInfo mediaInfo, int i) {
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onVolumeChanged(i);
        }

        @Override
        public void onPause(AdMediaInfo mediaInfo) {
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onPause();
        }

        @Override
        public void onAdProgress(AdMediaInfo adMediaInfo, VideoProgressUpdate videoProgressUpdate) {
            isOnAdEnded = false;
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onAdProgress(videoProgressUpdate);
            if (progressListener != null) {
                duration = (int) videoProgressUpdate.getDuration();
                s = (int) (videoProgressUpdate.getCurrentTime()) + 1;//add 1 second
                if (duration != 0)
                    percent = (int) (s * 100 / duration);
                progressListener.onAdProgress(s, (int) duration, percent);
            }
        }

        @Override
        public void onLoaded(AdMediaInfo mediaInfo) {
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onLoaded();
        }

        @Override
        public void onResume(AdMediaInfo mediaInfo) {
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onResume();
        }

        @Override
        public void onEnded(AdMediaInfo mediaInfo) {
            onAdEnded();
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onEnded();
        }

        @Override
        public void onContentComplete() {
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onContentComplete();
        }

        @Override
        public void onError(AdMediaInfo mediaInfo) {
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onError();
        }

        @Override
        public void onBuffering(AdMediaInfo mediaInfo) {
            if (uzAdPlayerCallback != null) uzAdPlayerCallback.onBuffering();
        }
    }
}
