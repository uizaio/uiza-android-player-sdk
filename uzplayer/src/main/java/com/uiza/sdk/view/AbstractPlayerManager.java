package com.uiza.sdk.view;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
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
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist;
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
import com.uiza.sdk.interfaces.DebugCallback;
import com.uiza.sdk.interfaces.UZManagerObserver;
import com.uiza.sdk.listerner.UZBufferListener;
import com.uiza.sdk.listerner.UZProgressListener;
import com.uiza.sdk.utils.Constants;
import com.uiza.sdk.utils.ConvertUtils;
import com.uiza.sdk.utils.UZAppUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import timber.log.Timber;

abstract class AbstractPlayerManager {

    private static final String PLAYER_STATE_FORMAT = "playWhenReady:%s playbackState:%s window:%s";
    private static final String BUFFERING = "buffering";
    private static final String ENDED = "ended";
    private static final String IDLE = "idle";
    private static final String READY = "ready";
    private static final String UNKNOWN = "unknown";


    private final HttpDataSource.Factory manifestDataSourceFactory;
    private final DataSource.Factory mediaDataSourceFactory;
    protected Context context;
    UZManagerObserver managerObserver;
    String drmScheme;
    private String linkPlay;
    long contentPosition;
    protected final SimpleExoPlayer player;
    private UZPlayerEventListener uzPlayerEventListener;
    private UZVideoEventListener uzVideoEventListener;
    private boolean timeShiftSupport = false;
    private boolean extIsTimeShift = false;
    private boolean timeShiftOn = false;
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
    private boolean isCanAddViewWatchTime;
    private long timestampPlayed;
    private long bufferPosition;
    private int bufferPercentage;
    private int videoWidth;
    private int videoHeight;
    private DebugCallback debugCallback;
    private ExoPlaybackException exoPlaybackException;
    MediaSource mediaSourceVideo;
    MediaSource mediaSourceVideoExt;
    DefaultDrmSessionManager<ExoMediaCrypto> drmSessionManager;

    protected AbstractPlayerManager(@NonNull Context context, String linkPlay, String drmScheme) {
        this.timestampPlayed = System.currentTimeMillis();
        this.isCanAddViewWatchTime = true;
        this.context = context;
        this.linkPlay = linkPlay;
        this.drmScheme = drmScheme;
        this.userAgent = UZAppUtils.getUserAgent(this.context);
        this.player = createPlayer();
        // Default parameters, except allowCrossProtocolRedirects is true
        this.manifestDataSourceFactory = buildHttpDataSourceFactory();
        this.mediaDataSourceFactory =
                new DefaultDataSourceFactory(context, null /* listener */, manifestDataSourceFactory);
        this.drmSessionManager = buildDrmSessionManager();
    }

    /**
     * Returns a {@link HttpDataSource.Factory}.
     */
    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent, null, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
    }

    public void register(@NonNull UZManagerObserver observer) {
        this.unregister();
        this.managerObserver = observer;
        if (managerObserver.getPlayerView() != null)
            managerObserver.getPlayerView().setPlayer(player);
        initSource();
    }

    public void unregister() {
        this.managerObserver = null;
    }

    void initWithoutReset() {
        initSource();
    }

    String getLinkPlay() {
        return linkPlay;
    }

    void setProgressListener(UZProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    void setBufferCallback(UZBufferListener bufferCallback) {
        this.bufferCallback = bufferCallback;
    }

    public void release() {
        player.stop();
        removeListeners();
        player.release();
        handler = null;
        runnable = null;
        try {
            Objects.requireNonNull(managerObserver.getPlayerView().getOverlayFrameLayout()).removeAllViews();
        } catch (NullPointerException e) {
            Timber.e(e);
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
        if (linkPlay != null) {
            setPlayWhenReady(true);
        }
        timestampPlayed = System.currentTimeMillis();
        isCanAddViewWatchTime = true;
    }

    void pause() {
        setPlayWhenReady(false);
        if (isCanAddViewWatchTime) {
            isCanAddViewWatchTime = false;
        }
    }

    void stop() {
        player.stop();
    }

    protected void reset() {
        contentPosition = player.getContentPosition();
        player.release();
        handler = null;
        runnable = null;
    }

    protected boolean isPlayingAd() {
        return false;
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
        if (player.getCurrentPosition() - backward > 0)
            player.seekTo(player.getCurrentPosition() - backward);
        else
            player.seekTo(0);
    }

    long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    private long getDuration() {
        return player.getDuration();
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
            if (bufferPosition != player.getBufferedPosition()
                    || bufferPercentage != player.getBufferedPercentage()) {
                bufferPosition = player.getBufferedPosition();
                bufferPercentage = player.getBufferedPercentage();
                progressListener.onBufferProgress(bufferPosition, bufferPercentage, duration);
            }
        }
    }

    void notifyUpdateButtonVisibility() {
        if (debugCallback != null)
            debugCallback.onUpdateButtonVisibilities();
    }

    void createMediaSourceVideo() {
        mediaSourceVideo = buildMediaSource(Uri.parse(linkPlay), drmSessionManager);
    }

    void createMediaSourceVideoExt(String linkPlayExt) {
        mediaSourceVideoExt = buildMediaSource(Uri.parse(linkPlayExt), drmSessionManager);
    }

    private void updateMediaSourceExt(HlsManifest manifest) {
        HlsMasterPlaylist playlist = manifest.masterPlaylist;
        String timeShift = ConvertUtils.getTimeShiftUrl(playlist);
        timeShiftSupport = !TextUtils.isEmpty(timeShift);
        if (timeShiftSupport && mediaSourceVideoExt == null) {
            if (timeShift.contains("extras/")) {
                String fileName = timeShift.replace("extras/", "");
                String linkPlayExt = linkPlay.replace(fileName, timeShift);
                createMediaSourceVideoExt(linkPlayExt);
                extIsTimeShift = true;
            } else {
                String linkPlayExt = linkPlay.replace(timeShift, "extras/" + timeShift);
                createMediaSourceVideoExt(linkPlayExt);
                extIsTimeShift = false;
            }
            setTimeShiftOn(!extIsTimeShift);
        }
    }

    public boolean isTimeShiftSupport() {
        return timeShiftSupport;
    }

    public boolean isExtIsTimeShift() {
        return extIsTimeShift;
    }

    public boolean isTimeShiftOn() {
        return timeShiftOn;
    }

    SimpleExoPlayer createPlayer() {
        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        DefaultRenderersFactory renderersFactory =
                new DefaultRenderersFactory(context).setExtensionRendererMode(extensionRendererMode);
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(context, videoTrackSelectionFactory);
        return new SimpleExoPlayer.Builder(context, renderersFactory)
                .setTrackSelector(trackSelector)
                .setLoadControl(UZLoadControl.createControl(bufferCallback))
                .build();
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
    }

    private DefaultDrmSessionManager<ExoMediaCrypto> buildDrmSessionManagerV18(UUID uuid, String licenseUrl) {
        return new DefaultDrmSessionManager.Builder()
                .setMultiSession(false)
                .setUuidAndExoMediaDrmProvider(uuid, FrameworkMediaDrm.DEFAULT_PROVIDER)
                .build(new HttpMediaDrmCallback(licenseUrl, manifestDataSourceFactory));
    }

    abstract void initSource();

    abstract void setRunnable();

    void setTimeShiftOn(boolean timeShiftOn) {
        this.timeShiftOn = timeShiftOn;
    }

    List<String> getSubtitleList() {
        return null; // template no support
    }

    private class UZVideoEventListener implements VideoListener {

        //This is called when the video size changes
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            videoWidth = width;
            videoHeight = height;
        }

        //This is called when first frame is rendered
        @Override
        public void onRenderedFirstFrame() {
            exoPlaybackException = null;
        }
    }

    private class UZPlayerEventListener implements Player.EventListener {
        //This is called when the current playlist changes
        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
            if (managerObserver != null)
                managerObserver.onTimelineChanged(timeline, player.getCurrentManifest(), reason);
        }

        //This is called when the available or selected tracks change
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            notifyUpdateButtonVisibility();
        }

        //This is called when either playWhenReady or playbackState changes
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_READY) {
                Object manifest = player.getCurrentManifest();
                if (manifest instanceof HlsManifest) {
                    updateMediaSourceExt((HlsManifest) manifest);
                }
            }
            notifyUpdateButtonVisibility();
            if (managerObserver != null)
                managerObserver.onPlayerStateChanged(playWhenReady, playbackState);
            if (progressListener != null)
                progressListener.onPlayerStateChanged(playWhenReady, playbackState);
        }

        //This is called then a error happens
        @Override
        public void onPlayerError(ExoPlaybackException error) {
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
            if (managerObserver != null)
                managerObserver.onPlayerError(error);
        }
    }
}
