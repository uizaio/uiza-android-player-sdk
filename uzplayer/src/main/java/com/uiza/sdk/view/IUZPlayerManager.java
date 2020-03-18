package com.uiza.sdk.view;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
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
import com.uiza.sdk.exceptions.ErrorUtils;
import com.uiza.sdk.interfaces.DebugCallback;
import com.uiza.sdk.listerner.UZBufferListener;
import com.uiza.sdk.util.ConnectivityUtils;
import com.uiza.sdk.util.Constants;
import com.uiza.sdk.util.ImageUtils;
import com.uiza.sdk.util.ListUtils;
import com.uiza.sdk.util.LocalData;
import com.uiza.sdk.util.StringUtils;
import com.uiza.sdk.util.TmpParamData;
import com.uiza.sdk.widget.UZPreviewTimeBar;
import com.uiza.sdk.widget.autosize.UZImageButton;
import com.uiza.sdk.widget.previewseekbar.PreviewLoader;

import java.util.List;
import java.util.UUID;

import timber.log.Timber;

abstract class IUZPlayerManager implements PreviewLoader {
    private static final String EXT_X_PROGRAM_DATE_TIME = "#EXT-X-PROGRAM-DATE-TIME:";
    private static final String EXTINF = "#EXTINF:";
    private static final long INVALID_PROGRAM_DATE_TIME = 0;
    protected final String TAG = "TAG" + getClass().getSimpleName();
    private final DataSource.Factory manifestDataSourceFactory;
    private final DataSource.Factory mediaDataSourceFactory;
    protected Context context;
    protected UZVideoView uzVideoView;
    protected long contentPosition;
    protected SimpleExoPlayer player;
    protected UZPlayerHelper playerHelper;
    protected Handler handler;
    protected Runnable runnable;
    protected VideoViewBase.ProgressListener progressListener;
    protected UZBufferListener bufferCallback;
    protected long mls = 0;
    protected long duration = 0;
    protected int percent = 0;
    protected int s = 0;
    protected DefaultTrackSelector trackSelector;
    protected String drmScheme;
    private String linkPlay;
    private boolean isFirstStateReady;
    private UZPreviewTimeBar uzTimeBar;
    private String thumbnailsUrl;
    private ImageView imageView;
    private boolean isCanAddViewWatchTime;
    private long timestampPlayed;
    private long bufferPosition = 0;
    private int bufferPercentage = 0;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private float volumeToggle;
    private DebugCallback debugCallback;
    private ExoPlaybackException exoPlaybackException;

    IUZPlayerManager(@NonNull UZVideoView uzVideo, String linkPlay, String thumbnailsUrl) {
        TmpParamData.getInstance().setPlayerInitTime(System.currentTimeMillis());
        this.timestampPlayed = System.currentTimeMillis();
        this.isCanAddViewWatchTime = true;
        this.context = uzVideo.getContext();
        this.videoWidth = 0;
        this.videoHeight = 0;
        this.mls = 0;
        this.bufferPosition = 0;
        this.bufferPercentage = 0;
        this.uzVideoView = uzVideo;
        this.linkPlay = linkPlay;
        this.isFirstStateReady = false;

        // OPTION 1 OK
        /* manifestDataSourceFactory = new DefaultDataSourceFactory(context, userAgent);
        mediaDataSourceFactory = new DefaultDataSourceFactory(
                context,
                userAgent,
                new DefaultBandwidthMeter());*/

        // OPTION 2 PLAY LINK REDIRECT
        // Default parameters, except allowCrossProtocolRedirects is true
        this.manifestDataSourceFactory =
                new DefaultHttpDataSourceFactory(Constants.USER_AGENT, null /* listener */,
                        DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                        DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                        true /* allowCrossProtocolRedirects */);
        this.mediaDataSourceFactory =
                new DefaultDataSourceFactory(context, null /* listener */, manifestDataSourceFactory);

        //SETUP OTHER
        this.imageView = uzVideo.getIvThumbnail();
        this.uzTimeBar = uzVideo.getUZTimeBar();
        this.thumbnailsUrl = thumbnailsUrl;
    }

    public void init() {
        reset();
        initSource();
    }

    public void initWithoutReset() {
        initSource();
    }

    public String getLinkPlay() {
        return linkPlay;
    }

    public void setProgressListener(VideoViewBase.ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void setBufferCallback(UZBufferListener bufferCallback) {
        this.bufferCallback = bufferCallback;
    }

    public void release() {
        if (isPlayerValid()) {
            playerHelper.release();
            handler = null;
            runnable = null;
        }
    }

    public void loadPreview(long currentPosition, long max) {
        if (!isPlayerValid()) return;
        setPlayWhenReady(false);
        if (thumbnailsUrl != null)
            ImageUtils.loadThumbnail(imageView, thumbnailsUrl, currentPosition);
    }

    public DefaultTrackSelector getTrackSelector() {
        return trackSelector;
    }

    public void setDebugCallback(DebugCallback debugCallback) {
        this.debugCallback = debugCallback;
    }

    //if player is playing then turn off connection -> player is error -> store current position
    //then if connection is connected again, resume position
    public void setResumeIfConnectionError() {
        contentPosition = mls;
    }

    protected void resume() {
        setPlayWhenReady(true);
        timestampPlayed = System.currentTimeMillis();
        isCanAddViewWatchTime = true;
    }

    protected void pause() {
        if (!isPlayerValid()) return;
        setPlayWhenReady(false);
        if (isCanAddViewWatchTime) {
            long durationWatched = System.currentTimeMillis() - timestampPlayed;
            TmpParamData.getInstance().addViewWatchTime(durationWatched);
            isCanAddViewWatchTime = false;
        }
    }

    protected void reset() {
        if (!isPlayerValid()) return;
        contentPosition = playerHelper.getContentPosition();
        playerHelper.release();
        handler = null;
        runnable = null;
    }

    protected boolean isPlayingAd() {
        return false;
    }

    protected void hideProgress() {
        if (uzVideoView.isCastingChromecast())
            return;
        uzVideoView.getProgressBar().setVisibility(View.GONE);
    }

    protected void showProgress() {
        uzVideoView.getProgressBar().setVisibility(View.VISIBLE);
    }

    protected SimpleExoPlayer getPlayer() {
        return playerHelper.getPlayer();
    }

    protected ExoPlaybackException getExoPlaybackException() {
        return exoPlaybackException;
    }

    protected boolean isPlayerValid() {
        return playerHelper != null && playerHelper.isPlayerValid();
    }

    protected int getVideoWidth() {
        return videoWidth;
    }

    protected int getVideoHeight() {
        return videoHeight;
    }

    protected void toggleVolumeMute(UZImageButton exoVolume) {
        if (!isPlayerValid() || exoVolume == null) return;
        if (getVolume() == 0f) {
            setVolume(volumeToggle);
            exoVolume.setSrcDrawableEnabled();
        } else {
            volumeToggle = getVolume();
            setVolume(0f);
            exoVolume.setSrcDrawableDisabledCanTouch();
        }
    }

    protected float getVolume() {
        return playerHelper.getVolume();
    }

    protected void setVolume(float volume) {
        if (!isPlayerValid()) return;
        playerHelper.setVolume(volume);
        if (uzVideoView == null) return;
        if (uzVideoView.getIbVolumeIcon() != null) {
            if (getVolume() != 0f) {
                uzVideoView.getIbVolumeIcon().setSrcDrawableEnabled();
            } else {
                uzVideoView.getIbVolumeIcon().setSrcDrawableDisabledCanTouch();
            }
        }
    }

    protected void setPlayWhenReady(boolean ready) {
        playerHelper.setPlayWhenReady(ready);
    }

    protected boolean seekTo(long positionMs) {
        return playerHelper.seekTo(positionMs);
    }

    //forward  10000mls
    protected void seekToForward(long forward) {
        playerHelper.seekToForward(forward);
    }

    //next 10000mls
    protected void seekToBackward(long backward) {
        playerHelper.seekToBackward(backward);
    }

    protected long getCurrentPosition() {
        return playerHelper.getCurrentPosition();
    }

    protected long getDuration() {
        return playerHelper.getDuration();
    }

    protected boolean isVOD() {
        return playerHelper.isVOD();
    }

    protected boolean isLIVE() {
        return playerHelper.isLIVE();
    }

    protected String getDebugString() {
        return getPlayerStateString() + getVideoString() + getAudioString();
    }

    /**
     * Returns a string containing player state debugging information.
     */
    protected String getPlayerStateString() {
        return playerHelper.getPlayerStateString();
    }

    /**
     * Returns a string containing video debugging information.
     */
    protected String getVideoString() {
        return playerHelper.getVideoString();
    }

    protected int getVideoProfileW() {
        return playerHelper.getVideoProfileW();
    }

    protected int getVideoProfileH() {
        return playerHelper.getVideoProfileH();
    }

    /**
     * Returns a string containing audio debugging information.
     */
    protected String getAudioString() {
        return playerHelper.getAudioString();
    }

    protected String getDecoderCountersBufferCountString(DecoderCounters counters) {
        return playerHelper.getDecoderCountersBufferCountString(counters);
    }

    protected String getPixelAspectRatioString(float pixelAspectRatio) {
        return playerHelper.getPixelAspectRatioString(pixelAspectRatio);
    }

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

    void handleVideoProgress() {
        if (progressListener != null && isPlayerValid()) {
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

    MediaSource createMediaSourceVideo() {
        return buildMediaSource(Uri.parse(linkPlay));
    }

    SimpleExoPlayer buildPlayer(DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        DefaultRenderersFactory renderersFactory =
                new DefaultRenderersFactory(context).setExtensionRendererMode(extensionRendererMode);
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        return ExoPlayerFactory.newSimpleInstance(context, renderersFactory, trackSelector,
                new DefaultLoadControl() {
                    @Override
                    public boolean shouldContinueLoading(long bufferedDurationUs, float playbackSpeed) {
                        if (bufferCallback != null)
                            bufferCallback.onBufferChanged(bufferedDurationUs, playbackSpeed);
                        return super.shouldContinueLoading(bufferedDurationUs, playbackSpeed);
                    }
                }, drmSessionManager);
    }

    DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManager(String drmScheme) {
        DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
        if (!TextUtils.isEmpty(drmScheme)) {
            String drmLicenseUrl = Constants.DRM_LICENSE_URL;
            String errorStringId = "An unknown DRM error occurred";
            try {
                UUID drmSchemeUuid = Util.getDrmUuid(drmScheme);
                drmSessionManager =
                        buildDrmSessionManagerV18(drmSchemeUuid, drmLicenseUrl, null,
                                false);
            } catch (UnsupportedDrmException e) {
                Timber.e(e, "UnsupportedDrmException");
            }
            if (drmSessionManager == null)
                Timber.e("Error drmSessionManager: %s", errorStringId);
        }
        return drmSessionManager;
    }

    void addPlayerListener() {
        player.addListener(new UZPlayerEventListener());
        player.addAudioListener(new UZAudioEventListener());
        player.addVideoListener(new UZVideoEventListener());
        player.addMetadataOutput(new UZMetadataOutputListener());
        player.addTextOutput(new UZTextOutputListener());
    }

    private void onFirstStateReady() {
        if (uzVideoView == null) return;
        long durationInSec = uzVideoView.getDuration() / 1000;
        TmpParamData.getInstance().setEntityDuration(String.valueOf(durationInSec));
        TmpParamData.getInstance().setEntitySourceDuration(String.valueOf(durationInSec));
        uzVideoView.removeVideoCover(false);
    }

    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(UUID uuid,
                                                                                     String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession)
            throws UnsupportedDrmException {

        HttpDataSource.Factory licenseDataSourceFactory = buildHttpDataSourceFactory();
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory);
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                        keyRequestPropertiesArray[i + 1]);
            }
        }
        return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback, null,
                multiSession);
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(Constants.USER_AGENT);
    }

    abstract void initSource();

    abstract void setRunnable();

    public List<String> getSubtitleList() {
        return null; // template no support
    }

    class UZAudioEventListener implements AudioListener {
        @Override
        public void onAudioSessionId(int audioSessionId) {
            if (uzVideoView != null && uzVideoView.audioListener != null)
                uzVideoView.audioListener.onAudioSessionId(audioSessionId);
        }

        @Override
        public void onAudioAttributesChanged(AudioAttributes audioAttributes) {
            if (uzVideoView != null && uzVideoView.audioListener != null)
                uzVideoView.audioListener.onAudioAttributesChanged(audioAttributes);
        }

        @Override
        public void onVolumeChanged(float volume) {
            if (uzVideoView != null && uzVideoView.audioListener != null)
                uzVideoView.audioListener.onVolumeChanged(volume);
        }
    }

    class UZVideoEventListener implements VideoListener {
        //This is called when the video size changes
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            videoWidth = width;
            videoHeight = height;
            TmpParamData.getInstance().setEntitySourceWidth(width);
            TmpParamData.getInstance().setEntitySourceHeight(height);
            if (uzVideoView != null && uzVideoView.videoListener != null)
                uzVideoView.videoListener.onVideoSizeChanged(width, height, unappliedRotationDegrees,
                        pixelWidthHeightRatio);
        }

        @Override
        public void onSurfaceSizeChanged(int width, int height) {
            if (uzVideoView != null && uzVideoView.videoListener != null)
                uzVideoView.videoListener.onSurfaceSizeChanged(width, height);
        }

        //This is called when first frame is rendered
        @Override
        public void onRenderedFirstFrame() {
            exoPlaybackException = null;
            if (uzVideoView != null && uzVideoView.videoListener != null)
                uzVideoView.videoListener.onRenderedFirstFrame();
        }
    }

    class UZMetadataOutputListener implements MetadataOutput {

        //This is called when there is metadata associated with current playback time
        @Override
        public void onMetadata(Metadata metadata) {
            if (uzVideoView != null && uzVideoView.metadataOutput != null)
                uzVideoView.metadataOutput.onMetadata(metadata);
        }
    }

    class UZTextOutputListener implements TextOutput {

        @Override
        public void onCues(List<Cue> cues) {
            if (uzVideoView != null && uzVideoView.textOutput != null)
                uzVideoView.textOutput.onCues(cues);
        }
    }

    class UZPlayerEventListener implements Player.EventListener {
        private long timestampRebufferStart;

        //This is called when the current playlist changes
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            if (uzVideoView == null) return;
            if (uzVideoView.eventListener != null)
                uzVideoView.eventListener.onTimelineChanged(timeline, manifest, reason);
            if (manifest instanceof HlsManifest) {
                HlsMediaPlaylist playlist = ((HlsManifest) manifest).mediaPlaylist;
                // From the current playing frame to end time of chunk
                long timeToEndChunk = player.getDuration() - player.getCurrentPosition();
                long extProgramDateTime = getProgramDateTimeValue(playlist, timeToEndChunk);

                if (extProgramDateTime == INVALID_PROGRAM_DATE_TIME) {
                    uzVideoView.hideTextLiveStreamLatency();
                    return;
                }

                long elapsedTime = SystemClock.elapsedRealtime() - LocalData.getLastElapsedTime();
                long currentTime = LocalData.getLastServerTime() + elapsedTime;

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
            if (uzVideoView != null && uzVideoView.eventListener != null)
                uzVideoView.eventListener.onTracksChanged(trackGroups, trackSelections);
        }

        //This is called when ExoPlayer starts or stops loading sources(TS files, fMP4 files…)
        @Override
        public void onLoadingChanged(boolean isLoading) {
            if (uzVideoView != null && uzVideoView.eventListener != null)
                uzVideoView.eventListener.onLoadingChanged(isLoading);
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
                                            System.currentTimeMillis() - timestampRebufferStart);
                            timestampRebufferStart = 0;
                        } else {
                            timestampRebufferStart = System.currentTimeMillis();
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
                    break;
            }
            notifyUpdateButtonVisibility();
            if (progressListener != null)
                progressListener.onPlayerStateChanged(playWhenReady, playbackState);
            if (uzVideoView != null && uzVideoView.eventListener != null)
                uzVideoView.eventListener.onPlayerStateChanged(playWhenReady, playbackState);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            if (uzVideoView != null && uzVideoView.eventListener != null)
                uzVideoView.eventListener.onRepeatModeChanged(repeatMode);
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            if (uzVideoView != null && uzVideoView.eventListener != null)
                uzVideoView.eventListener.onShuffleModeEnabledChanged(shuffleModeEnabled);
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

            error.printStackTrace();
            exoPlaybackException = error;
            notifyUpdateButtonVisibility();
            if (uzVideoView == null)
                return;
            uzVideoView.handleError(ErrorUtils.exceptionPlayback());
            //LLog.d(TAG, "onPlayerError isConnected: " + ConnectivityUtils.isConnected(context));
            if (ConnectivityUtils.isConnected(context))
                uzVideoView.tryNextLinkPlay();
            else
                uzVideoView.pause();
            if (uzVideoView != null && uzVideoView.eventListener != null)
                uzVideoView.eventListener.onPlayerError(error);
        }

        //This is called when a position discontinuity occurs without a change to the timeline
        @Override
        public void onPositionDiscontinuity(int reason) {
            if (uzVideoView != null && uzVideoView.eventListener != null)
                uzVideoView.eventListener.onPositionDiscontinuity(reason);
        }

        //This is called when the current playback parameters change
        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            if (uzVideoView != null && uzVideoView.eventListener != null)
                uzVideoView.eventListener.onPlaybackParametersChanged(playbackParameters);
        }

        //This is called when seek finishes
        @Override
        public void onSeekProcessed() {
            if (uzVideoView != null && uzVideoView.eventListener != null)
                uzVideoView.eventListener.onSeekProcessed();
        }
    }
}
