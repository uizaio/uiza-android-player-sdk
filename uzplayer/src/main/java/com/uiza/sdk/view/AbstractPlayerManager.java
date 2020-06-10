package com.uiza.sdk.view;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.ErrorUtils;
import com.uiza.sdk.interfaces.DebugCallback;
import com.uiza.sdk.listerner.UZBufferListener;
import com.uiza.sdk.listerner.UZProgressListener;
import com.uiza.sdk.utils.ConnectivityUtils;
import com.uiza.sdk.utils.Constants;
import com.uiza.sdk.utils.ListUtils;
import com.uiza.sdk.utils.StringUtils;
import com.uiza.sdk.utils.TmpParamData;
import com.uiza.sdk.utils.UZAppUtils;
import com.uiza.sdk.widget.UZPreviewTimeBar;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import timber.log.Timber;

abstract class AbstractPlayerManager {
    private static final String EXT_X_PROGRAM_DATE_TIME = "#EXT-X-PROGRAM-DATE-TIME:";
    private static final String EXTINF = "#EXTINF:";
    private static final long INVALID_PROGRAM_DATE_TIME = C.INDEX_UNSET;
    private static final String PLAYER_STATE_FORMAT = "playWhenReady:%s playbackState:%s window:%s";
    private static final String BUFFERING = "buffering";
    private static final String ENDED = "ended";
    private static final String IDLE = "idle";
    private static final String READY = "ready";
    private static final String UNKNOWN = "unknown";
    public static final long DEFAULT_TARGET_DURATION_MLS = 2000L; // 2s

    private final HttpDataSource.Factory manifestDataSourceFactory;
    private final DataSource.Factory mediaDataSourceFactory;
    protected Context context;
    UZVideoView uzVideoView;
    String drmScheme;
    private String linkPlay;
    private String linkPlayTimeShift;
    private boolean maybeTimeShift = false;
    long contentPosition;
    private long targetDurationMls = DEFAULT_TARGET_DURATION_MLS;
    protected SimpleExoPlayer player;
    private UZPlayerEventListener uzPlayerEventListener;
    private UZVideoEventListener uzVideoEventListener;

    protected Handler handler;
    Runnable runnable;
    UZProgressListener progressListener;

    private UZBufferListener bufferCallback;
    private long mls = 0;
    protected long duration = 0;
    int percent = 0;
    protected int s = 0;
    private DefaultTrackSelector trackSelector;
    private String userAgent;

    private boolean isFirstStateReady;
    private UZPreviewTimeBar uzTimeBar;
    private boolean isCanAddViewWatchTime;
    private long timestampPlayed;
    private long bufferPosition;
    private int bufferPercentage;
    private int videoWidth;
    private int videoHeight;
    private DebugCallback debugCallback;
    private ExoPlaybackException exoPlaybackException;

    AbstractPlayerManager(@NonNull UZVideoView uzVideo, String linkPlay, String drmScheme) {
        TmpParamData.getInstance().setPlayerInitTime(System.currentTimeMillis());
        this.timestampPlayed = System.currentTimeMillis();
        this.isCanAddViewWatchTime = true;
        this.context = uzVideo.getContext();
        this.uzVideoView = uzVideo;
        this.linkPlay = linkPlay;
        this.linkPlayTimeShift = StringUtils.timeShiftLink(linkPlay);
        this.maybeTimeShift = !this.linkPlayTimeShift.equals(linkPlay);
        this.drmScheme = drmScheme;
        this.isFirstStateReady = false;
        this.userAgent = UZAppUtils.getUserAgent(this.context);
        // Default parameters, except allowCrossProtocolRedirects is true
        this.manifestDataSourceFactory = buildHttpDataSourceFactory();
        this.mediaDataSourceFactory =
                new DefaultDataSourceFactory(context, null /* listener */, manifestDataSourceFactory);
        //SETUP OTHER
        this.uzTimeBar = uzVideo.getPreviewTimeBar();
    }

    /**
     * Returns a {@link HttpDataSource.Factory}.
     */
    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent, null, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
    }

    public void init(@NonNull UZVideoView uzVideo) {
        reset();
        if (this.uzVideoView == null) {
            this.uzVideoView = uzVideo;
            this.context = uzVideo.getContext();
        }
        initSource();
    }

    void initWithoutReset() {
        initSource();
    }

    String getLinkPlay() {
        return linkPlay;
    }

    String getLinkPlayTimeShift() { return linkPlayTimeShift; }

    void setProgressListener(UZProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    void setBufferCallback(UZBufferListener bufferCallback) {
        this.bufferCallback = bufferCallback;
    }

    public void release() {
        if (player != null) {
            removeListeners();
            player.release();
            player = null;
            handler = null;
            runnable = null;
        }
    }

    DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    void setDebugCallback(DebugCallback debugCallback) {
        this.debugCallback = debugCallback;
    }

    //if player is playing then turn off connection -> player is error -> store current position
    //then if connection is connected again, resume position
    void setResumeIfConnectionError() {
        contentPosition = mls;
    }

    void resume() {
        setPlayWhenReady(true);
        timestampPlayed = System.currentTimeMillis();
        isCanAddViewWatchTime = true;
    }

    void pause() {
        if (player == null) return;
        setPlayWhenReady(false);
        if (isCanAddViewWatchTime) {
            long durationWatched = System.currentTimeMillis() - timestampPlayed;
            TmpParamData.getInstance().addViewWatchTime(durationWatched);
            isCanAddViewWatchTime = false;
        }
    }

    protected void reset() {
        if (player == null) return;
        contentPosition = player.getContentPosition();
        player.release();
        player = null;
        handler = null;
        runnable = null;
    }

    protected boolean isPlayingAd() {
        return false;
    }

    void hideProgress() {
        if (uzVideoView.isCastingChromecast())
            return;
        uzVideoView.getProgressBar().setVisibility(View.GONE);
    }

    void showProgress() {
        uzVideoView.getProgressBar().setVisibility(View.VISIBLE);
    }

    SimpleExoPlayer getPlayer() {
        return player;
    }

    ExoPlaybackException getExoPlaybackException() {
        return exoPlaybackException;
    }

    int getVideoWidth() {
        return videoWidth;
    }

    int getVideoHeight() {
        return videoHeight;
    }

    float getVolume() {
        return player != null ? player.getVolume() : -1;
    }

    void setVolume(float volume) {
        if (player != null)
            player.setVolume(volume);
    }

    void setPlayWhenReady(boolean ready) {
        if (player != null)
            player.setPlayWhenReady(ready);
    }

    boolean seekTo(long positionMs) {
        if (player != null) {
            player.seekTo(positionMs);
            return true;
        }
        return false;
    }

    //forward  10000mls
    void seekToForward(long forward) {
        if (player != null)
            player.seekTo(Math.min(player.getCurrentPosition() + forward, player.getDuration()));
    }

    //next 10000mls
    void seekToBackward(long backward) {
        if (player != null) {
            if (player.getCurrentPosition() - backward > 0)
                player.seekTo(player.getCurrentPosition() - backward);
            else
                player.seekTo(0);
        }
    }

    long getCurrentPosition() {
        return player != null ? player.getCurrentPosition() : 0;
    }

    private long getDuration() {
        return player != null ? player.getDuration() : 0;
    }

    public boolean isMaybeTimeShift() {
        return maybeTimeShift;
    }

    protected boolean isVOD() {
        return player != null && !player.isCurrentWindowDynamic();
    }

    protected boolean isLIVE() {
        return player != null && player.isCurrentWindowDynamic();
    }

    protected String getDebugString() {
        return getPlayerStateString() + getVideoString() + getAudioString();
    }

    /**
     * Returns a string containing player state debugging information.
     */
    private String getPlayerStateString() {
        if (player == null) return null;
        String playbackStateString;
        switch (player.getPlaybackState()) {
            case Player.STATE_BUFFERING:
                playbackStateString = BUFFERING;
                break;
            case Player.STATE_ENDED:
                playbackStateString = ENDED;
                break;
            case Player.STATE_IDLE:
                playbackStateString = IDLE;
                break;
            case Player.STATE_READY:
                playbackStateString = READY;
                break;
            default:
                playbackStateString = UNKNOWN;
                break;
        }
        return String.format(PLAYER_STATE_FORMAT, player.getPlayWhenReady(), playbackStateString, player.getCurrentWindowIndex());
    }

    /**
     * Returns a string containing video debugging information.
     */
    private String getVideoString() {
        if (player == null) return null;
        Format format = player.getVideoFormat();
        if (format == null) return null;
        return "\n" + format.sampleMimeType + "(id:" + format.id + " r:" + format.width + "x"
                + format.height + getPixelAspectRatioString(format.pixelWidthHeightRatio)
                + getDecoderCountersBufferCountString(player.getVideoDecoderCounters()) + ")";
    }

    int getVideoProfileW() {
        if (player == null) return 0;
        Format format = player.getVideoFormat();
        if (format == null) return 0;
        return format.width;
    }

    int getVideoProfileH() {
        if (player == null) return 0;
        Format format = player.getVideoFormat();
        if (format == null) return 0;
        return format.height;
    }

    /**
     * Returns a string containing audio debugging information.
     */
    private String getAudioString() {
        if (player == null) return null;
        Format format = player.getAudioFormat();
        if (format == null) return null;
        return "\n" + format.sampleMimeType
                + "(id:" + format.id
                + " hz:" + format.sampleRate
                + " ch:" + format.channelCount
                + getDecoderCountersBufferCountString(player.getAudioDecoderCounters()) + ")";
    }

    protected String getDecoderCountersBufferCountString(DecoderCounters counters) {
        if (counters == null) return null;
        counters.ensureUpdated();
        return " sib:" + counters.skippedInputBufferCount
                + " sb:" + counters.skippedOutputBufferCount
                + " rb:" + counters.renderedOutputBufferCount
                + " db:" + counters.droppedBufferCount
                + " mcdb:" + counters.maxConsecutiveDroppedBufferCount
                + " dk:" + counters.droppedToKeyframeCount;
    }

    protected String getPixelAspectRatioString(float pixelAspectRatio) {
        return pixelAspectRatio == Format.NO_VALUE || pixelAspectRatio == 1f ? "" : (" par:" + String.format(Locale.US, "%.02f", pixelAspectRatio));
    }

    MediaSource buildMediaSource(Uri uri, DrmSessionManager<?> drmSessionManager) {
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        manifestDataSourceFactory).setDrmSessionManager(drmSessionManager).createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        manifestDataSourceFactory).setDrmSessionManager(drmSessionManager).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory).setDrmSessionManager(drmSessionManager).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory).setDrmSessionManager(drmSessionManager).createMediaSource(uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    void handleVideoProgress() {
        if (progressListener != null && player != null) {
            mls = getCurrentPosition();
            duration = getDuration();
            mls = Math.min(mls, duration);
            if (duration != 0)
                percent = (int) (mls * 100 / duration);
            s = Math.round(mls / 1000.0f);
            progressListener.onVideoProgress(mls, s, duration, percent);
            //buffer changing
            if (bufferPosition != uzVideoView.getBufferedPosition()
                    || bufferPercentage != uzVideoView.getBufferedPercentage()) {
                bufferPosition = uzVideoView.getBufferedPosition();
                bufferPercentage = uzVideoView.getBufferedPercentage();
                progressListener.onBufferProgress(bufferPosition, bufferPercentage, duration);
            }
        }
    }

    void notifyUpdateButtonVisibility() {
        if (debugCallback != null)
            debugCallback.onUpdateButtonVisibilities();
    }

    MediaSource createMediaSourceVideo(DefaultDrmSessionManager<ExoMediaCrypto> drmSessionManager) {
        return buildMediaSource(Uri.parse(linkPlay), drmSessionManager);
    }

    MediaSource createMediaSourceTimeShift(DefaultDrmSessionManager<ExoMediaCrypto> drmSessionManager){
        return maybeTimeShift ? buildMediaSource(Uri.parse(linkPlayTimeShift), drmSessionManager) : null;
    }

    SimpleExoPlayer buildPlayer() {
        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        DefaultRenderersFactory renderersFactory =
                new DefaultRenderersFactory(context).setExtensionRendererMode(extensionRendererMode);
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(context, videoTrackSelectionFactory);
        SimpleExoPlayer.Builder builder = new SimpleExoPlayer.Builder(context, renderersFactory);
        builder.setTrackSelector(trackSelector).setLoadControl(new UZLoadControl() {
            @Override
            public boolean shouldContinueLoading(long bufferedDurationUs, float playbackSpeed) {
                if (bufferCallback != null)
                    bufferCallback.onBufferChanged(bufferedDurationUs, playbackSpeed);
                return super.shouldContinueLoading(bufferedDurationUs, playbackSpeed);
            }
        });
        return builder.build();
    }

    DefaultDrmSessionManager<ExoMediaCrypto> buildDrmSessionManager() {
        DefaultDrmSessionManager<ExoMediaCrypto> drmSessionManager = null;
        if (!TextUtils.isEmpty(drmScheme)) {
            String drmLicenseUrl = Constants.DRM_LICENSE_URL;
            UUID drmSchemeUuid = Util.getDrmUuid(drmScheme);
            drmSessionManager = buildDrmSessionManagerV18(drmSchemeUuid, drmLicenseUrl);
        }
        return drmSessionManager;
    }

    void initPlayerListeners() {
        if (uzPlayerEventListener == null) {
            uzPlayerEventListener = new UZPlayerEventListener();
            player.addListener(uzPlayerEventListener);
        }
        if (uzVideoEventListener == null) {
            uzVideoEventListener = new UZVideoEventListener();
            player.addVideoListener(uzVideoEventListener);
        }
        if (uzVideoView != null && uzVideoView.getVideoListener() != null) {
            player.addVideoListener(uzVideoView.getVideoListener());
        }
        if (uzVideoView != null && uzVideoView.getMetadataOutput() != null) {
            player.addMetadataOutput(uzVideoView.getMetadataOutput());
        }
        if (uzVideoView != null && uzVideoView.getTextOutput() != null) {
            player.addTextOutput(uzVideoView.getTextOutput());
        }
        if (uzVideoView != null && uzVideoView.getAudioListener() != null) {
            player.addAudioListener(uzVideoView.getAudioListener());
        }
    }

    private void removeListeners() {
        if (uzPlayerEventListener != null) {
            player.removeListener(uzPlayerEventListener);
            uzPlayerEventListener = null;
        }
        if (uzVideoEventListener != null) {
            player.removeVideoListener(uzVideoEventListener);
            uzVideoEventListener = null;
        }
        if (uzVideoView != null && uzVideoView.getVideoListener() != null) {
            player.removeVideoListener(uzVideoView.getVideoListener());
        }
        if (uzVideoView != null && uzVideoView.getMetadataOutput() != null) {
            player.removeMetadataOutput(uzVideoView.getMetadataOutput());
        }
        if (uzVideoView != null && uzVideoView.getTextOutput() != null) {
            player.removeTextOutput(uzVideoView.getTextOutput());
        }
        if (uzVideoView != null && uzVideoView.getAudioListener() != null) {
            player.removeAudioListener(uzVideoView.getAudioListener());
        }
    }

    private void onFirstStateReady() {
        if (uzVideoView == null) return;
        long durationInSec = uzVideoView.getDuration() / 1000;
        TmpParamData.getInstance().setEntityDuration(String.valueOf(durationInSec));
        TmpParamData.getInstance().setEntitySourceDuration(String.valueOf(durationInSec));
        uzVideoView.removeVideoCover(false);
    }

    private DefaultDrmSessionManager<ExoMediaCrypto> buildDrmSessionManagerV18(UUID uuid, String licenseUrl) {
        return new DefaultDrmSessionManager.Builder()
                .setMultiSession(false)
                .setUuidAndExoMediaDrmProvider(uuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
                .build(new HttpMediaDrmCallback(licenseUrl, manifestDataSourceFactory));
    }

    abstract void initSource();

    abstract void setRunnable();

    List<String> getSubtitleList() {
        return null; // template no support
    }

    public long getTargetDurationMls() {
        return targetDurationMls;
    }

    private class UZVideoEventListener implements VideoListener {

        //This is called when the video size changes
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            videoWidth = width;
            videoHeight = height;
            TmpParamData.getInstance().setEntitySourceWidth(width);
            TmpParamData.getInstance().setEntitySourceHeight(height);
        }

        //This is called when first frame is rendered
        @Override
        public void onRenderedFirstFrame() {
            exoPlaybackException = null;
        }
    }

    private class UZPlayerEventListener implements Player.EventListener {
        private long timestampReBufferStart;
        private final Player.EventListener eventListener;

        UZPlayerEventListener() {
            if (uzVideoView != null)
                eventListener = uzVideoView.getEventListener();
            else
                eventListener = null;
        }

        //This is called when the current playlist changes
        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
            if (uzVideoView == null) return;
            if (eventListener != null)
                eventListener.onTimelineChanged(timeline, reason);
            Object manifest = player.getCurrentManifest();
            if (manifest instanceof HlsManifest) {
                HlsMediaPlaylist playlist = ((HlsManifest) manifest).mediaPlaylist;
                targetDurationMls = C.usToMs(playlist.targetDurationUs);
                // From the current playing frame to end time of chunk
                long timeToEndChunk = player.getDuration() - player.getCurrentPosition();
                long extProgramDateTime = getProgramDateTimeValue(playlist, timeToEndChunk);
                if (extProgramDateTime == INVALID_PROGRAM_DATE_TIME) {
                    uzVideoView.hideTextLiveStreamLatency();
                    return;
                }
                long elapsedTime = SystemClock.elapsedRealtime() - UZPlayer.getElapsedTime();
                long currentTime = System.currentTimeMillis() + elapsedTime;
                long latency = currentTime - extProgramDateTime;
                uzVideoView.updateLiveStreamLatency(latency);
            } else
                uzVideoView.hideTextLiveStreamLatency();
        }

        private long getProgramDateTimeValue(HlsMediaPlaylist playlist, long timeToEndChunk) {
            if (playlist == null || ListUtils.isEmpty(playlist.tags))
                return INVALID_PROGRAM_DATE_TIME;
            final String emptyStr = "";
            final int tagSize = playlist.tags.size();
            long totalTime = 0;
            int playingIndex = tagSize;
            // Find the playing frame index
            while (playingIndex > 0) {
                String tag = playlist.tags.get(playingIndex - 1);
                if (tag.contains(EXTINF)) {
                    totalTime +=
                            Double.parseDouble(tag.replace(",", emptyStr).replace(EXTINF, emptyStr)) * 1000;
                    if (totalTime >= timeToEndChunk) {
                        break;
                    }
                }
                playingIndex--;
            }
            if (playingIndex >= tagSize) {
                // That means the livestream latency is larger than 1 segment (duration).
                // we should skip to calc latency in this case
                return INVALID_PROGRAM_DATE_TIME;
            }
            // Find the playing frame EXT_X_PROGRAM_DATE_TIME
            String playingDateTime = emptyStr;
            for (int i = playingIndex; i < tagSize; i++) {
                String tag = playlist.tags.get(i);
                if (tag.contains(EXT_X_PROGRAM_DATE_TIME)) {
                    playingDateTime = tag.replace(EXT_X_PROGRAM_DATE_TIME, emptyStr);
                    break;
                }
            }

            if (TextUtils.isEmpty(playingDateTime)) {
                // That means something wrong with the format, check with server
                // we should skip to calc latency in this case
                return INVALID_PROGRAM_DATE_TIME;
            }
            // int list of frame, we get the EXT_X_PROGRAM_DATE_TIME of current playing frame
            return StringUtils.convertUTCMs(playingDateTime);
        }

        //This is called when the available or selected tracks change
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            notifyUpdateButtonVisibility();
            if (eventListener != null)
                eventListener.onTracksChanged(trackGroups, trackSelections);
        }

        //This is called when ExoPlayer starts or stops loading sources(TS files, fMP4 files…)
        @Override
        public void onLoadingChanged(boolean isLoading) {
            if (eventListener != null)
                eventListener.onLoadingChanged(isLoading);
        }

        //This is called when either playWhenReady or playbackState changes
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    showProgress();
                    if (uzVideoView != null) {
                        if (playWhenReady) {
                            TmpParamData.getInstance()
                                    .setViewRebufferDuration(
                                            System.currentTimeMillis() - timestampReBufferStart);
                            timestampReBufferStart = 0;
                        } else {
                            timestampReBufferStart = System.currentTimeMillis();
                            TmpParamData.getInstance().addViewRebufferCount();
                        }
                    }
                    break;
                case Player.STATE_ENDED:
                    if (uzVideoView != null) {
                        uzVideoView.onPlayerEnded();
                    }
                    hideProgress();
                    break;
                case Player.STATE_IDLE:
                    showProgress();
                    break;
                case Player.STATE_READY:
                    hideProgress();
                    if (playWhenReady) {
                        // media actually playing
                        if (uzVideoView != null) {
                            uzVideoView.hideLayoutMsg();
                            uzVideoView.resetCountTryLinkPlayError();
                        }
                        if (uzTimeBar != null)
                            uzTimeBar.hidePreview();
                    }
                    if (!isFirstStateReady) {
                        onFirstStateReady();
                        isFirstStateReady = true;
                    }
                    if (uzVideoView != null) {
                        ((Activity) uzVideoView.getContext()).setResult(Activity.RESULT_OK);
                    }
                    break;
            }
            notifyUpdateButtonVisibility();
            if (progressListener != null)
                progressListener.onPlayerStateChanged(playWhenReady, playbackState);
            if (eventListener != null)
                eventListener.onPlayerStateChanged(playWhenReady, playbackState);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            if (eventListener != null)
                eventListener.onRepeatModeChanged(repeatMode);
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            if (eventListener != null)
                eventListener.onShuffleModeEnabledChanged(shuffleModeEnabled);
        }

        //This is called then a error happens
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            hideProgress();
            if (error == null)
                return;
            Timber.e(error, "onPlayerError ");
            if (error.type == ExoPlaybackException.TYPE_SOURCE)
                Timber.e("onPlayerError TYPE_SOURCE");
            else if (error.type == ExoPlaybackException.TYPE_RENDERER)
                Timber.e("onPlayerError TYPE_RENDERER");
            else if (error.type == ExoPlaybackException.TYPE_UNEXPECTED)
                Timber.e("onPlayerError TYPE_UNEXPECTED");
            exoPlaybackException = error;
            notifyUpdateButtonVisibility();
            if (uzVideoView == null)
                return;
            uzVideoView.handleError(ErrorUtils.exceptionPlayback());
            if (ConnectivityUtils.isConnected(context))
                uzVideoView.tryNextLinkPlay();
            else
                uzVideoView.pause();
            if (eventListener != null)
                eventListener.onPlayerError(error);
        }

        //This is called when a position discontinuity occurs without a change to the timeline
        @Override
        public void onPositionDiscontinuity(int reason) {
            if (eventListener != null)
                eventListener.onPositionDiscontinuity(reason);
        }

        //This is called when the current playback parameters change
        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            if (eventListener != null)
                eventListener.onPlaybackParametersChanged(playbackParameters);
        }

        //This is called when seek finishes
        @Override
        public void onSeekProcessed() {
            if (eventListener != null)
                eventListener.onSeekProcessed();
        }
    }
}
