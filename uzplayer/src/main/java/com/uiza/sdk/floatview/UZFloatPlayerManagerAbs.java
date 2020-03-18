package com.uiza.sdk.floatview;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.uiza.sdk.exceptions.ErrorUtils;
import com.uiza.sdk.util.TmpParamData;
import com.uiza.sdk.view.VideoViewBase;

abstract class UZFloatPlayerManagerAbs {
    protected final String TAG = getClass().getSimpleName();
    protected Context context;
    protected UZFloatVideoView fuzVideo;
    protected DebugTextViewHelper debugTextViewHelper;
    protected DataSource.Factory manifestDataSourceFactory;
    protected DataSource.Factory mediaDataSourceFactory;
    protected SimpleExoPlayer player;
    protected String linkPlay;
    protected Handler handler;
    protected Runnable runnable;
    protected boolean isCanAddViewWatchTime;
    protected long timestampPlayed;
    protected VideoViewBase.ProgressListener progressListener;
    protected int videoWidth;
    protected int videoHeight;
    protected DefaultTrackSelector trackSelector;

    public void setProgressListener(VideoViewBase.ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    public void seekTo(long position) {
        if (player != null)
            player.seekTo(position);
    }

    MediaSource createMediaSourceVideo() {
        //Video Source
        return buildMediaSource(Uri.parse(linkPlay));
    }

    public boolean togglePauseResume() {
        if (player == null)
            return false;
        if (player.getPlayWhenReady()) {
            pauseVideo();
            return false;
        } else {
            resumeVideo();
            return true;
        }
    }

    public void resumeVideo() {
        if (player != null)
            player.setPlayWhenReady(true);
        timestampPlayed = System.currentTimeMillis();
        isCanAddViewWatchTime = true;
    }

    public void pauseVideo() {
        if (player != null) {
            player.setPlayWhenReady(false);
            if (isCanAddViewWatchTime) {
                long durationWatched = System.currentTimeMillis() - timestampPlayed;
                TmpParamData.getInstance().addViewWatchTime(durationWatched);
                isCanAddViewWatchTime = false;
            }
        }
    }

    public void reset() {
        if (player != null) {
            player.release();
            player = null;
            handler = null;
            runnable = null;
            if (debugTextViewHelper != null) {
                debugTextViewHelper.stop();
                debugTextViewHelper = null;
            }
        }
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;
            handler = null;
            runnable = null;
            if (debugTextViewHelper != null) {
                debugTextViewHelper.stop();
                debugTextViewHelper = null;
            }
        }
    }

    // Internal methods.
    MediaSource buildMediaSource(Uri uri) {
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        manifestDataSourceFactory).createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        manifestDataSourceFactory).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    protected int getVideoWidth() {
        return videoWidth;
    }

    protected int getVideoHeight() {
        return videoHeight;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }

    protected void setVolume(float volume) {
        if (player != null)
            player.setVolume(volume);
    }

    protected void setVolumeOn() {
        if (player != null)
            player.setVolume(1f);
    }

    protected void setVolumeOff() {
        if (player != null)
            player.setVolume(0f);
    }

    abstract void init(boolean isLiveStream, long contentPosition);

    class FUZPlayerEventListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (fuzVideo != null)
                fuzVideo.onPlayerStateChanged(playWhenReady, playbackState);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (fuzVideo != null)
                fuzVideo.onPlayerError(ErrorUtils.exceptionPlayback());
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        }

        @Override
        public void onSeekProcessed() {
        }
    }

    class FUZVideoListener implements VideoListener {
        @Override
        public void onVideoSizeChanged(int width, int height, int unAppliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            videoWidth = width;
            videoHeight = height;
            if (fuzVideo != null)
                fuzVideo.onVideoSizeChanged(width, height);
        }

        @Override
        public void onSurfaceSizeChanged(int width, int height) {
        }

        @Override
        public void onRenderedFirstFrame() {
        }
    }
}
