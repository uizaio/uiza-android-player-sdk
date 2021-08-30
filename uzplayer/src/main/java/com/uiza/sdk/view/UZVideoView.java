package com.uiza.sdk.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.util.Rational;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsManifest;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.uiza.sdk.BuildConfig;
import com.uiza.sdk.R;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.analytics.UZAnalytic;
import com.uiza.sdk.animations.AnimationUtils;
import com.uiza.sdk.chromecast.Casty;
import com.uiza.sdk.dialog.hq.UZItem;
import com.uiza.sdk.dialog.hq.UZTrackSelectionView;
import com.uiza.sdk.dialog.info.UZDlgInfoV1;
import com.uiza.sdk.dialog.playlistfolder.CallbackPlaylistFolder;
import com.uiza.sdk.dialog.playlistfolder.UZPlaylistFolderDialog;
import com.uiza.sdk.dialog.setting.SettingAdapter;
import com.uiza.sdk.dialog.setting.SettingItem;
import com.uiza.sdk.dialog.speed.UZSpeedDialog;
import com.uiza.sdk.events.ConnectEvent;
import com.uiza.sdk.exceptions.ErrorConstant;
import com.uiza.sdk.exceptions.ErrorUtils;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZAdPlayerCallback;
import com.uiza.sdk.interfaces.UZManagerObserver;
import com.uiza.sdk.interfaces.UZPlayerCallback;
import com.uiza.sdk.listerner.UZChromeCastListener;
import com.uiza.sdk.listerner.UZProgressListener;
import com.uiza.sdk.listerner.UZTVFocusChangeListener;
import com.uiza.sdk.models.UZEventType;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.models.UZPlaybackInfo;
import com.uiza.sdk.models.UZTrackingData;
import com.uiza.sdk.observers.ConnectivityReceiver;
import com.uiza.sdk.observers.SensorOrientationChangeNotifier;
import com.uiza.sdk.utils.ConnectivityUtils;
import com.uiza.sdk.utils.Constants;
import com.uiza.sdk.utils.ConvertUtils;
import com.uiza.sdk.utils.DebugUtils;
import com.uiza.sdk.utils.ImageUtils;
import com.uiza.sdk.utils.ListUtils;
import com.uiza.sdk.utils.StringUtils;
import com.uiza.sdk.utils.UZAppUtils;
import com.uiza.sdk.utils.UZData;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.widget.UZImageButton;
import com.uiza.sdk.widget.UZPreviewTimeBar;
import com.uiza.sdk.widget.UZTextView;
import com.uiza.sdk.widget.previewseekbar.PreviewLoader;
import com.uiza.sdk.widget.previewseekbar.PreviewView;
import com.uiza.sdk.widget.seekbar.UZVerticalSeekBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * View of UZPlayer
 */
public class UZVideoView extends RelativeLayout
        implements UZManagerObserver, PreviewLoader, PreviewView.OnPreviewChangeListener, View.OnClickListener, View.OnFocusChangeListener,
        SensorOrientationChangeNotifier.Listener {

    private static final String HYPHEN = "-";
    private static final long FAST_FORWARD_REWIND_INTERVAL = 10000L; // 10s
    /**
     * The timeout in milliseconds. A non-positive value will cause the
     * controller to remain visible indefinitely.
     */
    private static final int DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS = 8000; // 8s
    private static final long DEFAULT_VALUE_TRACKING_LOOP = 5000L;  // 5s
    public static final long DEFAULT_TARGET_DURATION_MLS = 2000L; // 2s

    //===================================================================START FOR PLAYLIST/FOLDER
    //
    private long targetDurationMls = DEFAULT_TARGET_DURATION_MLS;
    private Handler handler = new Handler(Looper.getMainLooper());
    private RelativeLayout rootView, rlChromeCast;
    private UZPlayerManager playerManager;
    private ProgressBar progressBar;
    private LinearLayout llTop, debugRootView;
    private RelativeLayout rlMsg;
    private RelativeLayout rlLiveInfo;
    private FrameLayout previewFrameLayout;
    private UZPreviewTimeBar timeBar;
    private ImageView ivThumbnail, ivVideoCover;
    private UZTextView tvPosition, tvDuration;
    private TextView tvTitle;
    private TextView tvLiveStatus;
    private TextView tvLiveView;
    private TextView tvLiveTime;
    private UZImageButton ibFullscreenIcon;
    private UZImageButton ibPauseIcon;
    private UZImageButton ibPlayIcon;
    private UZImageButton ibReplayIcon;
    private UZImageButton ibRewIcon;
    private UZImageButton ibFfwdIcon;
    private UZImageButton ibBackScreenIcon;
    private UZImageButton ibVolumeIcon;
    private UZImageButton ibSettingIcon;
    private UZImageButton ibCcIcon;
    private UZImageButton ibPlaylistFolderIcon; //playlist folder
    private UZImageButton ibHearingIcon;
    private UZImageButton pipIcon;
    private UZImageButton ibSkipPreviousIcon;
    private UZImageButton ibSkipNextIcon;
    private UZImageButton ibSpeedIcon;
    private UZImageButton ivLiveTime;
    private UZImageButton ivLiveView;
    private TextView tvEndScreenMsg;
    private UZPlayerView playerView;
    private long defaultSeekValue = FAST_FORWARD_REWIND_INTERVAL;
    private boolean timeBarAtBottom;
    private UZChromeCast uzChromeCast;
    private boolean isCastingChromecast = false;
    private boolean autoMoveToLiveEdge;
    private boolean isInPipMode = false;
    private boolean isPIPModeEnabled = true; //Has the user disabled PIP mode in AppOpps?
    private long positionPIPPlayer;
    //========================================================================START CONFIG
    private boolean isAutoStart = Constants.DF_PLAYER_IS_AUTO_START;
    private boolean isAutoSwitchItemPlaylistFolder = true;
    private boolean isAutoShowController;
    private boolean isFreeSize;
    private boolean isPlayerControllerAlwayVisible;
    private boolean isSetFirstRequestFocusDone;
    private boolean isHasError;
    private int countTryLinkPlayError = 0;
    private boolean activityIsPausing = false;
    private long timestampOnStartPreview;
    private boolean isOnPreview;
    private long maxSeekLastDuration;
    private boolean isLandscape;//current screen is landscape or portrait
    private boolean isAlwaysPortraitScreen = false;
    private boolean isHideOnTouch = true;
    private boolean useController = true;
    private boolean isOnPlayerEnded;
    private boolean alwaysHideLiveViewers = false;
    private boolean enablePictureInPicture = false;
    //========================================================================END CONFIG
    /*
     **Change skin via skin id resources
     * changeSkin(R.layout.uzplayer_skin_1);
     */
    //TODO improve this func
    private boolean isRefreshFromChangeSkin;
    private long currentPositionBeforeChangeSkin;
    private boolean isCalledFromChangeSkin;
    private View firstViewHasFocus;
    /**
     * ======== START EVENT =====
     */
    private PreviewView.OnPreviewChangeListener onPreviewChangeListener;
    private UZPlayerCallback playerCallback;
    private UZTVFocusChangeListener uzTVFocusChangeListener;
    private UZAdPlayerCallback adPlayerCallback;
    boolean isFirstStateReady = false;
    //=============================================================================================START EVENTBUS
    private boolean isCalledFromConnectionEventBus = false;
    //last current position lúc từ exoplayer switch sang cast player
    private long lastCurrentPosition;
    private boolean isCastPlayerPlayingFirst;
    private StatsForNerdsView statsForNerdsView;
    private String viewerSessionId;
    private CompositeDisposable disposables;
    private boolean viewCreated = false;
    private final ConnectivityReceiver connectivityReceiver = new ConnectivityReceiver();

    public UZVideoView(Context context) {
        super(context);
    }

    public UZVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UZVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UZVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!viewCreated) {
            onCreateView();
        }
    }

    public boolean isViewCreated() {
        return viewCreated;
    }

    /**
     * Call one time from {@link #onAttachedToWindow}
     * Note: you must call inflate in this method
     */
    private void onCreateView() {
        if (UZAppUtils.checkChromeCastAvailable())
            setupChromeCast();
        inflate(getContext(), R.layout.uz_ima_video_core_rl, this);
        rootView = findViewById(R.id.root_view);
        int skinId = UZData.getInstance().getUZPlayerSkinLayoutId();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            playerView = (UZPlayerView) inflater.inflate(skinId, null);
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            playerView.setLayoutParams(lp);
            playerView.setVisibility(GONE);
            if (playerView.getVideoSurfaceView() instanceof SurfaceView) {
                ((SurfaceView) playerView.getVideoSurfaceView()).getHolder().addCallback(new SurfaceHolder.Callback2() {
                    @Override
                    public void surfaceRedrawNeeded(SurfaceHolder holder) {
                    }

                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                        if (isInPipMode) {
                            if (UZAppUtils.hasSupportPIP(getContext(), enablePictureInPicture)) {
                                if (getContext() instanceof Activity) {
                                    ((Activity) getContext()).finishAndRemoveTask();
                                }
                            }
                        }
                    }
                });
            }
            rootView.addView(playerView);
            setControllerAutoShow(isAutoShowController);
            findViews();
            resizeContainerView();
        } else {
            throw new NullPointerException("Can not inflater view");
        }
        updateUIEachSkin();
        setMarginPreviewTimeBar();
        setMarginRlLiveInfo();
        updateUISizeThumbnail();
        viewCreated = true;
        if (playerCallback != null) {
            playerCallback.playerViewCreated(playerView);
        }
    }

    private void resizeContainerView() {
        if (!isFreeSize) {
            setSize(getVideoWidth(), getVideoHeight());
        } else {
            setSize(this.getWidth(), this.getHeight());
        }
    }

    /**
     * register connection internet listener
     */
    private void registerConnectifyReceiver() {
        getContext().registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        EventBus.getDefault().register(this);
    }

    /**
     * register connection internet listener
     */
    private void unregisterConnectifyReceiver() {
        getContext().unregisterReceiver(connectivityReceiver);
        EventBus.getDefault().unregister(this);
    }

    public boolean isAutoStart() {
        return isAutoStart;
    }

    public void setAutoStart(boolean isAutoStart) {
        this.isAutoStart = isAutoStart;
        updateUIButtonPlayPauseDependOnIsAutoStart();
    }

    public boolean isAutoSwitchItemPlaylistFolder() {
        return isAutoSwitchItemPlaylistFolder;
    }

    public void setAutoSwitchItemPlaylistFolder(boolean isAutoSwitchItemPlaylistFolder) {
        this.isAutoSwitchItemPlaylistFolder = isAutoSwitchItemPlaylistFolder;
    }

    public boolean getControllerAutoShow() {
        return (playerView != null) && playerView.getControllerAutoShow();
    }

    public void setControllerAutoShow(boolean isAutoShowController) {
        this.isAutoShowController = isAutoShowController;
        if (playerView != null) {
            playerView.setControllerAutoShow(isAutoShowController);
        }
    }

    // Lay pixel dung cho custom UI like youtube, timeBar bottom of player controller
    public int getPixelAdded() {
        return timeBarAtBottom ? (getHeightTimeBar() / 2) : 0;
    }

    //return pixel
    public int getHeightTimeBar() {
        return UZViewUtils.heightOfView(timeBar);
    }

    private long getDuration() {
        return (getPlayer() == null) ? -1 : getPlayer().getDuration();
    }

    private long getCurrentPosition() {
        return (getPlayer() == null) ? -1 : getPlayer().getCurrentPosition();
    }

    @Nullable
    public Format getVideoFormat() {
        return (getPlayer() == null) ? null : getPlayer().getVideoFormat();
    }

    @Nullable
    public Format getAudioFormat() {
        return (getPlayer() == null) ? null : getPlayer().getAudioFormat();
    }

    public int getVideoProfileW() {
        return (playerManager == null) ? 0 : playerManager.getVideoProfileW();
    }

    public int getVideoProfileH() {
        return (playerManager == null) ? 0 : playerManager.getVideoProfileH();
    }

    public void setResizeMode(int resizeMode) {
        if (playerView != null) {
            try {
                playerView.setResizeMode(resizeMode);
            } catch (java.lang.IllegalStateException e) {
                Timber.e(e);
            }
        }
    }

    public void setSize(int width, int height) {
        UZViewUtils.resizeLayout(rootView, ivVideoCover, getPixelAdded(), width, height, isFreeSize);
    }

    public void setFreeSize(boolean isFreeSize) {
        this.isFreeSize = isFreeSize;
        resizeContainerView();
    }

    public void setPlayerControllerAlwaysVisible() {
        setControllerAutoShow(true);
        setHideControllerOnTouch(false);
        setControllerShowTimeoutMs(0);
        isPlayerControllerAlwayVisible = true;
    }

    protected void handleError(UZException uzException) {
        if (uzException == null) {
            return;
        }
        notifyError(uzException);
        // Capture by Sentry, in uzException already contains Message, Error Code
        Timber.e(uzException);
        if (isHasError) {
            return;
        }
        isHasError = true;
        UZData.getInstance().setSettingPlayer(false);
    }

    private void notifyError(UZException exception) {
        if (playerCallback != null)
            playerCallback.onError(exception);
    }

    private void handlePlayPlayListFolderUI() {
        setVisibilityOfPlaylistFolderController(isPlayPlaylistFolder() ? VISIBLE : GONE);
    }

    public SimpleExoPlayer getPlayer() {
        return (playerManager == null) ? null : playerManager.getPlayer();
    }

    public void seekTo(long positionMs) {
        if (playerManager != null)
            playerManager.seekTo(positionMs);
    }

    /**
     * Play with custom playback
     *
     * @return true if not error
     */
    public boolean play() {
        UZPlayback playback = UZData.getInstance().getPlayback();
        if (playback == null) {
            Timber.e(ErrorConstant.ERR_14);
            return false;
        }
        if (!ConnectivityUtils.isConnected(getContext())) {
            Timber.e(ErrorConstant.ERR_0);
            return false;
        }
        initPlayback(playback, true);
        return true;
    }

    /**
     * Play with {@link UZPlayback}
     *
     * @param playback PlaybackInfo nonnull
     * @return true if not error
     */
    public boolean play(@NonNull UZPlayback playback) {
        if (!ConnectivityUtils.isConnected(getContext())) {
            notifyError(ErrorUtils.exceptionNoConnection());
            return false;
        }
        UZData.getInstance().setPlayback(playback);
        initPlayback(playback, true);
        return true;
    }

    /**
     * Play with {@link UZPlayback}
     *
     * @param playlist List of PlaybackInfo
     * @return true if not error
     */
    public boolean play(List<UZPlayback> playlist) {
        // TODO: Check how to get subtitle of a custom link play, because we have no idea about entityId or appId
        if (!ConnectivityUtils.isConnected(getContext())) {
            handleError(ErrorUtils.exceptionNoConnection());
            return false;
        }
        if (ListUtils.isEmpty(playlist)) {
            handleError(ErrorUtils.exceptionPlaylistFolderItemFirst());
            return false;
        } else {
            UZData.getInstance().clearDataForPlaylistFolder();
            UZData.getInstance().setPlayList(playlist);
            playPlaylistPosition(UZData.getInstance().getCurrentPositionOfPlayList());
        }
        isHasError = false;
        return true;
    }


    public void resume() {
        if (isCastingChromecast) {
            Casty casty = UZData.getInstance().getCasty();
            if (casty != null)
                casty.getPlayer().play();
        } else if (playerManager != null) {
            playerManager.resume();
        }
        UZViewUtils.goneViews(ibPlayIcon);
        if (ibPauseIcon != null) {
            UZViewUtils.visibleViews(ibPauseIcon);
            ibPauseIcon.requestFocus();
        }
        setKeepScreenOn(true);
    }

    public void pause() {
        if (isCastingChromecast) {
            Casty casty = UZData.getInstance().getCasty();
            if (casty != null)
                casty.getPlayer().pause();
        } else if (playerManager != null) {
            playerManager.pause();
        }
        UZViewUtils.goneViews(ibPauseIcon);
        setKeepScreenOn(false);
        if (ibPlayIcon != null) {
            UZViewUtils.visibleViews(ibPlayIcon);
            ibPlayIcon.requestFocus();
        }
        // tracking here
    }

    public int getVideoWidth() {
        return (playerManager == null) ? 0 : playerManager.getVideoWidth();
    }

    public int getVideoHeight() {
        return (playerManager == null) ? 0 : playerManager.getVideoHeight();
    }

    private void initPlayback(@NonNull UZPlayback playback, boolean isClearDataPlaylistFolder) {
        if (isClearDataPlaylistFolder) {
            UZData.getInstance().clearDataForPlaylistFolder();
        }
        isCalledFromChangeSkin = false;
        handlePlayPlayListFolderUI();
        hideLayoutMsg();
        setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS);
        isOnPlayerEnded = false;
        updateUIEndScreen();
        isHasError = false;
        viewerSessionId = UUID.randomUUID().toString();
        if (playerManager != null) {
            releasePlayerManager();
            resetCountTryLinkPlayError();
            showProgress();
        }
        updateUIDependOnLiveStream();
        disposables = new CompositeDisposable();
        String linkPlay = playback.getFirstLinkPlay();
        if (TextUtils.isEmpty(linkPlay)) {
            handleError(ErrorUtils.exceptionNoLinkPlay());
            return;
        }
        initDataSource(linkPlay, UZData.getInstance().getUrlIMAAd(), playback.getPoster());
        if (playerCallback != null)
            playerCallback.isInitResult(linkPlay);
        trackWatchingTimer(true);
        initPlayerManager();
    }

    private void initPlayerManager() {
        if (playerManager != null) {
            playerManager.register(this);
            if (isRefreshFromChangeSkin) {
                playerManager.seekTo(currentPositionBeforeChangeSkin);
                isRefreshFromChangeSkin = false;
                currentPositionBeforeChangeSkin = 0;
            }
            if (isCalledFromConnectionEventBus) {
                playerManager.setRunnable();
                isCalledFromConnectionEventBus = false;
            }
            // Always using this options
            initStatsForNerds();
        }
    }

    public void toggleStatsForNerds() {
        if (getPlayer() == null) return;
        boolean isEnableStatsForNerds =
                statsForNerdsView == null || statsForNerdsView.getVisibility() != View.VISIBLE;
        if (isEnableStatsForNerds)
            UZViewUtils.visibleViews(statsForNerdsView);
        else
            UZViewUtils.goneViews(statsForNerdsView);
    }

    protected void tryNextLinkPlay() {
        if (isLIVE()) {
            // try to play 5 times
            if (countTryLinkPlayError >= UZData.getInstance().getPlayback().getSize()) {
                return;
            }
            // if entity is livestreaming, dont try to next link play
            if (playerManager != null) {
                playerManager.initWithoutReset();
                playerManager.setRunnable();
            }
            countTryLinkPlayError++;
            isFirstStateReady = false;
            return;
        }
        countTryLinkPlayError++;
        isFirstStateReady = false;
        releasePlayerManager();
        checkToSetUpResource();
    }

    //khi call api callAPIGetLinkPlay nhung json tra ve ko co data
    //se co gang choi video da play gan nhat
    //neu co thi se play
    //khong co thi bao loi
    private void handleErrorNoData() {
        removeVideoCover(true);
        if (playerCallback != null) {
            UZData.getInstance().setSettingPlayer(false);
            handleError(ErrorUtils.exceptionNoLinkPlay());
        }
    }

    protected void resetCountTryLinkPlayError() {
        countTryLinkPlayError = 0;
    }

    public boolean onBackPressed() {
        if (isLandscape()) {
            toggleFullscreen();
            return true;
        }
        return false;
    }

    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @Nullable Configuration newConfig) {
        positionPIPPlayer = getCurrentPosition();
        isInPipMode = isInPictureInPictureMode;
        // Hide the full-screen UI (controls, etc.) while in picture-in-picture mode.
        // Restore the full-screen UI.
        setUseController(!isInPictureInPictureMode);
    }

    public void onDestroyView() {
        releasePlayerStats();
        releasePlayerManager();
        UZData.getInstance().setSettingPlayer(false);
        isCastingChromecast = false;
        isCastPlayerPlayingFirst = false;
        if (UZAppUtils.hasSupportPIP(getContext(), enablePictureInPicture)) {
            ((Activity) getContext()).finishAndRemoveTask();
        }
        if (playerManager != null) {
            playerManager.unregister();
        }
        if (disposables != null)
            disposables.dispose();
        handler = null;
    }

    private void releasePlayerStats() {
        if (getPlayer() != null)
            getPlayer().removeAnalyticsListener(statsForNerdsView);
    }

    private void releasePlayerManager() {
        if (playerManager != null)
            playerManager.release();
    }

    public void onResumeView() {
        SensorOrientationChangeNotifier.getInstance(getContext()).addListener(this);
        registerConnectifyReceiver();
        if (isCastingChromecast)
            return;
        activityIsPausing = false;
        if (playerManager != null) {
//            if (ibPlayIcon == null || ibPlayIcon.getVisibility() != VISIBLE)
//                playerManager.resume();
            playerManager.resume();
        }

        if (positionPIPPlayer > 0L && isInPipMode) {
            seekTo(positionPIPPlayer);
        } else if (autoMoveToLiveEdge && isLIVE()) {
            // try to move to the edge of livestream video
            seekToLiveEdge();
        }
        //Makes sure that the media controls pop up on resuming and when going between PIP and non-PIP states.
        setUseController(true);
    }


    public boolean isPlaying() {
        return (getPlayer() != null) && getPlayer().getPlayWhenReady();
    }

    /**
     * Set auto move the the last window of livestream, default is false
     *
     * @param autoMoveToLiveEdge true if always seek to last livestream video, otherwise false
     */
    public void setAutoMoveToLiveEdge(boolean autoMoveToLiveEdge) {
        this.autoMoveToLiveEdge = autoMoveToLiveEdge;
    }

    /**
     * Seek to live edge of a streaming video
     */
    public void seekToLiveEdge() {
        if (isLIVE() && getPlayer() != null)
            getPlayer().seekToDefaultPosition();
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putLong("ARG_VIDEO_POSITION", getCurrentPosition());
    }

    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        positionPIPPlayer = savedInstanceState.getLong("ARG_VIDEO_POSITION");
    }

    public void onPauseView() {
        activityIsPausing = true;
        positionPIPPlayer = getCurrentPosition();
        SensorOrientationChangeNotifier.getInstance(getContext()).remove(this);
        unregisterConnectifyReceiver();
        // in PIP to continue
        if (playerManager != null && !isInPipMode) {
            playerManager.pause();
        }
    }

    @Override
    public boolean isPIPEnable() {
        return (pipIcon != null)
                && !isCastingChromecast()
                && UZAppUtils.hasSupportPIP(getContext(), enablePictureInPicture)
                && !UZData.getInstance().isUseUZDragView();
    }

    @Override
    public void onStartPreview(PreviewView previewView, int progress) {
        timestampOnStartPreview = System.currentTimeMillis();
        if (onPreviewChangeListener != null)
            onPreviewChangeListener.onStartPreview(previewView, progress);
    }

    @Override
    public void onPreview(PreviewView previewView, int progress, boolean fromUser) {
        isOnPreview = true;
        updateUIIbRewIconDependOnProgress(progress, true);
        if (onPreviewChangeListener != null)
            onPreviewChangeListener.onPreview(previewView, progress, fromUser);
    }

    @Override
    public void onStopPreview(PreviewView previewView, int progress) {
        if (isCastingChromecast) {
            Casty casty = UZData.getInstance().getCasty();
            if (casty != null) casty.getPlayer().seek(progress);
        }
        long seekLastDuration = System.currentTimeMillis() - timestampOnStartPreview;
        if (maxSeekLastDuration < seekLastDuration) {
            maxSeekLastDuration = seekLastDuration;
        }
        isOnPreview = false;
        onStopPreview(progress);
        if (onPreviewChangeListener != null)
            onPreviewChangeListener.onStopPreview(previewView, progress);
    }

    public void onStopPreview(int progress) {
        if (playerManager != null && !isCastingChromecast) {
            playerManager.seekTo(progress);
            playerManager.resume();
            isOnPlayerEnded = false;
            updateUIEndScreen();
        }
    }

    @Override
    public void onFocusChange(View view, boolean isFocus) {
        if (uzTVFocusChangeListener != null)
            uzTVFocusChangeListener.onFocusChange(view, isFocus);
        else if (firstViewHasFocus == null)
            firstViewHasFocus = view;
    }

    public void setAdPlayerCallback(UZAdPlayerCallback callback) {
        this.adPlayerCallback = callback;
        if (UZAppUtils.isAdsDependencyAvailable()) {
            if (playerManager != null)
                playerManager.setAdPlayerCallback(callback);
        } else
            throw new NoClassDefFoundError(ErrorConstant.ERR_506);
    }

    @Override
    public UZAdPlayerCallback getAdPlayerCallback() {
        return adPlayerCallback;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    @Override
    public void onOrientationChange(int orientation) {
        //270 land trai
        //0 portrait duoi
        //90 land phai
        //180 portrait tren
        boolean isDeviceAutoRotation = UZViewUtils.isRotationPossible(getContext());
        if (orientation == 90 || orientation == 270) {
            if (isDeviceAutoRotation && !isLandscape)
                if (!isAlwaysPortraitScreen) {
                    UZViewUtils.changeScreenLandscape((Activity) getContext(), orientation);
                }
        } else {
            if (isDeviceAutoRotation && isLandscape)
                if (!isAlwaysPortraitScreen) {
                    UZViewUtils.changeScreenPortrait((Activity) getContext());
                }
        }
    }

    //===================================================================END FOR PLAYLIST/FOLDER

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (playerView == null) return;
        resizeContainerView();
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!isInPipMode) {
                UZViewUtils.hideSystemUiFullScreen(playerView);
            }
//            UZViewUtils.hideSystemUiFullScreen(playerView);
            isLandscape = true;
            UZViewUtils.setUIFullScreenIcon(ibFullscreenIcon, true);
            UZViewUtils.goneViews(pipIcon);
        } else {
            if (!isInPipMode) {
                UZViewUtils.hideSystemUi(playerView);
            }
//            UZViewUtils.hideSystemUi(playerView);
            isLandscape = false;
            UZViewUtils.setUIFullScreenIcon(ibFullscreenIcon, false);
            if (isPIPEnable())
                UZViewUtils.visibleViews(pipIcon);
        }
        setMarginPreviewTimeBar();
        setMarginRlLiveInfo();
        updateUISizeThumbnail();
        updateUIPositionOfProgressBar();
        if (timeBarAtBottom)
            setMarginDependOnUZTimeBar(playerView.getVideoSurfaceView());
        if (playerCallback != null)
            playerCallback.onScreenRotate(isLandscape);
    }

    @Override
    public void onClick(View v) {
        if (v == rlMsg)
            AnimationUtils.play(v, Techniques.Pulse);
        else if (v == ibFullscreenIcon) {
            toggleFullscreen();
        } else if (v == ibBackScreenIcon) {
            handleClickBackScreen();
        } else if (v == ibVolumeIcon)
            handleClickBtVolume();
        else if (v == ibSettingIcon)
            showSettingsDialog();
        else if (v == ibCcIcon)
            handleClickCC();
        else if (v == ibPlaylistFolderIcon)
            handleClickPlaylistFolder();
        else if (v == ibHearingIcon)
            handleClickHearing();
        else if (v == pipIcon)
            enterPIPMode();
        else if (v.getParent() == debugRootView)
            showTrackSelectionDialog(v, true);
        else if (v == rlChromeCast)
            Timber.e("dangerous to remove");
        else if (v == tvLiveStatus) {
            seekToEndLive();
        } else if (v == ibFfwdIcon) {
            if (isCastingChromecast) {
                Casty casty = UZData.getInstance().getCasty();
                if (casty != null)
                    casty.getPlayer().seekToForward(defaultSeekValue);
            } else if (playerManager != null)
                playerManager.seekToForward(defaultSeekValue);
        } else if (v == ibRewIcon) {
            if (isCastingChromecast) {
                Casty casty = UZData.getInstance().getCasty();
                if (casty != null)
                    casty.getPlayer().seekToRewind(defaultSeekValue);
            } else if (playerManager != null) {
                playerManager.seekToBackward(defaultSeekValue);
                if (isPlaying()) {
                    isOnPlayerEnded = false;
                    updateUIEndScreen();
                }
            }
        } else if (v == ibPauseIcon)
            pause();
        else if (v == ibPlayIcon)
            resume();
        else if (v == ibReplayIcon)
            replay();
        else if (v == ibSkipNextIcon)
            handleClickSkipNext();
        else if (v == ibSkipPreviousIcon)
            handleClickSkipPrevious();
        else if (v == ibSpeedIcon)
            showSpeed();
        else if (v == tvEndScreenMsg)
            AnimationUtils.play(v, Techniques.Pulse);
        /*có trường hợp đang click vào các control thì bị ẩn control ngay lập tức, trường hợp này ta có thể xử lý khi click vào control thì reset count down để ẩn control ko
        default controller timeout là 8s, vd tới s thứ 7 bạn tương tác thì tới s thứ 8 controller sẽ bị ẩn*/
        if (useController
                && (rlMsg == null || rlMsg.getVisibility() != VISIBLE)
                && isPlayerControllerShowing())
            showController();

    }

    @TargetApi(Build.VERSION_CODES.N)
    public void enterPIPMode() {
        if (isLandscape) {
            throw new IllegalArgumentException("Cannot enter PIP Mode if screen is landscape");
        }
        if (isPIPEnable()) {
            isInPipMode = true;
            positionPIPPlayer = getCurrentPosition();
            setUseController(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PictureInPictureParams.Builder params = new PictureInPictureParams.Builder();
                Rational aspectRatio = new Rational(getVideoWidth(), getVideoHeight());
                params.setAspectRatio(aspectRatio);
//                List actions = new ArrayList<RemoteAction>();
//                actions.add(new RemoteAction());
//                params.setActions(actions);
                ((Activity) getContext()).enterPictureInPictureMode(params.build());
            } else {
                ((Activity) getContext()).enterPictureInPictureMode();
            }
        }
//        postDelayed(() -> {
//            isPIPModeEnabled = ((Activity) getContext()).isInPictureInPictureMode();
//            if (!isPIPModeEnabled) {
//                enterPIPMode();
//            }
//        }, 50);
    }

    public int getControllerShowTimeoutMs() {
        return (playerView == null) ? -1 : playerView.getControllerShowTimeoutMs();
    }

    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        post(() -> playerView.setControllerShowTimeoutMs(controllerShowTimeoutMs));
    }

    public boolean isPlayerControllerShowing() {
        return (playerView != null) && playerView.isControllerVisible();

    }

    public void showController() {
        if (playerView != null)
            playerView.showController();
    }

    public void hideController() {
        if (isPlayerControllerAlwayVisible) return;
        if (playerView != null && !isCastingChromecast)//do not hide if is casting chromecast
            playerView.hideController();
    }

    public void setHideControllerOnTouch(boolean isHide) {
        if (playerView != null) {
            this.isHideOnTouch = isHide;
            playerView.setControllerHideOnTouch(isHide);
        }
    }

    public boolean getControllerHideOnTouch() {
        return (playerView != null) && playerView.getControllerHideOnTouch();
    }

    public boolean isUseController() {
        return useController;
    }

    public void setUseController(boolean useController) {
        this.useController = useController;
        if (playerView != null)
            playerView.setUseController(useController);
    }

    protected boolean isPlayPlaylistFolder() {
        return !ListUtils.isEmpty(UZData.getInstance().getPlayList());
    }

    private void playPlaylistPosition(int position) {
        if (!isPlayPlaylistFolder()) {
            Timber.e("playPlaylistPosition error: incorrect position");
            return;
        }
        Timber.d("playPlaylistPosition position: %d", position);
        if (position < 0) {
            Timber.e("This is the first item");
            notifyError(ErrorUtils.exceptionPlaylistFolderItemFirst());
            return;
        }
        if (position > UZData.getInstance().getPlayList().size() - 1) {
            Timber.e("This is the last item");
            notifyError(ErrorUtils.exceptionPlaylistFolderItemLast());
            return;
        }
        pause();
        hideController();
        //update UI for skip next and skip previous button
        UZViewUtils.setSrcDrawableEnabledForViews(ibSkipPreviousIcon, ibSkipNextIcon);
        //set disabled prevent double click, will enable onStateReadyFirst()
        UZViewUtils.setClickableForViews(false, ibSkipPreviousIcon, ibSkipNextIcon);
        //end update UI for skip next and skip previous button
        UZData.getInstance().setCurrentPositionOfPlayList(position);
        UZPlayback playback = UZData.getInstance().getPlayback();
        if (playback == null || !playback.canPlay()) {
            notifyError(ErrorUtils.exceptionNoLinkPlay());
            return;
        }
        initPlayback(playback, false);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        if (manifest instanceof HlsManifest) {
            HlsMediaPlaylist playlist = ((HlsManifest) manifest).mediaPlaylist;
            targetDurationMls = C.usToMs(playlist.targetDurationUs);
            // From the current playing frame to end time of chunk
            long timeToEndChunk = getDuration() - getCurrentPosition();
            long extProgramDateTime = ConvertUtils.getProgramDateTime(playlist, timeToEndChunk);
            if (extProgramDateTime == C.INDEX_UNSET) {
                hideTextLiveStreamLatency();
                return;
            }
            long elapsedTime = SystemClock.elapsedRealtime() - UZPlayer.getElapsedTime();
            long currentTime = System.currentTimeMillis() + elapsedTime;
            long latency = currentTime - extProgramDateTime;
            updateLiveStreamLatency(latency);
        } else
            hideTextLiveStreamLatency();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        hideProgress();
        handleError(ErrorUtils.exceptionPlayback());
        if (ConnectivityUtils.isConnected(getContext()))
            tryNextLinkPlay();
        else
            pause();
    }

    @Override
    public void onPlayerEnded() {
        if (isPlaying()) {
            setKeepScreenOn(false);
            isOnPlayerEnded = true;
            if (isPlayPlaylistFolder() && isAutoSwitchItemPlaylistFolder) {
                hideController();
                autoSwitchNextVideo();
            } else {
                updateUIEndScreen();
            }
        }
        hideProgress();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING: // more data needs to load
            case Player.STATE_IDLE: // nothing to play media
                showProgress();
                break;
            case Player.STATE_ENDED:
                onPlayerEnded();
                break;
            case Player.STATE_READY: // can start playback
                hideProgress();
                updateTvDuration();
                updateTimeBarWithTimeShiftStatus();
                if (playWhenReady) {
                    // media actually playing
                    hideLayoutMsg();
                    resetCountTryLinkPlayError();
                    if (timeBar != null)
                        timeBar.hidePreview();
                }
                ((Activity) getContext()).setResult(Activity.RESULT_OK);
                if (!isFirstStateReady) {
                    removeVideoCover(false);
                    isFirstStateReady = true;
                }
                break;
        }
    }

    private void autoSwitchNextVideo() {
        playPlaylistPosition(UZData.getInstance().getCurrentPositionOfPlayList() + 1);
    }

    private void autoSwitchPreviousLinkVideo() {
        playPlaylistPosition(UZData.getInstance().getCurrentPositionOfPlayList() - 1);
    }

    private void handleClickPlaylistFolder() {
        UZPlaylistFolderDialog uzPlaylistFolderDlg = new UZPlaylistFolderDialog(
                getContext(),
                isLandscape,
                UZData.getInstance().getPlayList(),
                UZData.getInstance().getCurrentPositionOfPlayList(),
                new CallbackPlaylistFolder() {
                    @Override
                    public void onClickItem(UZPlayback playback, int position) {
                        playPlaylistPosition(position);
                    }
                });
        UZViewUtils.showDialog(uzPlaylistFolderDlg);
    }

    private void handleClickSkipNext() {
        isOnPlayerEnded = false;
        updateUIEndScreen();
        autoSwitchNextVideo();
    }

    private void handleClickSkipPrevious() {
        isOnPlayerEnded = false;
        updateUIEndScreen();
        autoSwitchPreviousLinkVideo();
    }

    public void replay() {
        if (playerManager == null) return;
        //TODO Chỗ này đáng lẽ chỉ clear value của tracking khi đảm bảo rằng seekTo(0) true
        boolean result = playerManager.seekTo(0);
        if (result) {
            isSetFirstRequestFocusDone = false;
            isOnPlayerEnded = false;
            updateUIEndScreen();
            handlePlayPlayListFolderUI();
        }
        if (isCastingChromecast)
            replayChromeCast();
    }

    private void replayChromeCast() {
        lastCurrentPosition = 0;
        handleConnectedChromecast();
        showController();
    }

    /*Nếu đang casting thì button này sẽ handle volume on/off ở cast player
     * Ngược lại, sẽ handle volume on/off ở exo player*/
    private void handleClickBtVolume() {
        if (isCastingChromecast) {
            Casty casty = UZData.getInstance().getCasty();
            if (casty != null) {
                boolean isMute = casty.toggleMuteVolume();
                if (ibVolumeIcon != null)
                    ibVolumeIcon.setImageResource(isMute ? R.drawable.ic_volume_off_white_24 : R.drawable.ic_volume_up_white_24);
            }
        }
        toggleVolumeMute();
    }

    private void handleClickBackScreen() {
        if (isLandscape) {
            toggleFullscreen();
        } else {
            ((Activity) getContext()).onBackPressed();
        }
    }

    private void handleClickCC() {
        if (ListUtils.isEmpty(playerManager.getSubtitleList())) {
            UZDlgInfoV1 uzDlgInfoV1 = new UZDlgInfoV1(getContext(), getContext().getString(R.string.text), getContext().getString(R.string.no_caption));
            UZViewUtils.showDialog(uzDlgInfoV1);
        } else {
            View view = DebugUtils.getTextButton(debugRootView);
            if (view != null)
                view.performClick();
        }
    }

    private void handleClickHearing() {
        View view = DebugUtils.getAudioButton(debugRootView);
        if (view != null)
            view.performClick();
    }

    public void setDefaultSeekValue(int mls) {
        defaultSeekValue = mls;
    }

    /**
     * Seek tu vi tri hien tai cong them bao nhieu mls
     */
    public void seekToForward(int mls) {
        setDefaultSeekValue(mls);
        ibFfwdIcon.performClick();
    }

    public void seekToForward() {
        if (!isLIVE())
            ibFfwdIcon.performClick();
    }

    /**
     * Seek tu vi tri hien tai tru di bao nhieu mls
     */
    public void seekToBackward(int mls) {
        setDefaultSeekValue(mls);
        ibRewIcon.performClick();
    }

    public void seekToBackward() {
        ibRewIcon.performClick();
    }

    //chi toggle show hide controller khi video da vao dc onStateReadyFirst();
    public void toggleShowHideController() {
        if (playerView != null)
            playerView.toggleShowHideController();
    }

    public void togglePlayPause() {
        if (getPlayer() == null) return;
        if (getPlayer().getPlayWhenReady())
            pause();
        else
            resume();
    }

    public void toggleVolume() {
        ibVolumeIcon.performClick();
    }

    public void toggleFullscreen() {
        UZViewUtils.toggleScreenOrientation((Activity) getContext());
    }

    public void showSpeed() {
        if (getPlayer() == null) return;
        final UZSpeedDialog uzDlgSpeed = new UZSpeedDialog(getContext(), getPlayer().getPlaybackParameters().speed,
                speed -> {
                    if (speed != null)
                        setSpeed(speed.getValue());
                });
        UZViewUtils.showDialog(uzDlgSpeed);
    }

    /**
     * Bo video hien tai va choi tiep theo 1 video trong playlist/folder
     */
    public void skipNextVideo() {
        handleClickSkipNext();
    }

    /**
     * Bo video hien tai va choi lui lai 1 video trong playlist/folder
     */
    public void skipPreviousVideo() {
        handleClickSkipPrevious();
    }

    @Override
    public UZPlayerView getPlayerView() {
        return playerView;
    }

    public boolean isLIVE() {
        return playerManager != null && playerManager.isLIVE();
    }

    public void setLiveViewers(int viewers) {
        if (tvLiveView != null && !alwaysHideLiveViewers) {
            if (viewers == 1) {
                tvLiveView.setText(getResources().getString(R.string.oneViewer));
            } else {
                tvLiveView.setText(getResources().getString(R.string.numberOfViewers, viewers));
            }
        }
    }

    public float getVolume() {
        return (playerManager == null) ? -1 : playerManager.getVolume();
    }

    public void setVolume(float volume) {
        if (playerManager == null) return;
        playerManager.setVolume(volume);
        if (ibVolumeIcon != null) {
            if (playerManager.getVolume() != 0f) {
                ibVolumeIcon.setSrcDrawableEnabled();
            } else {
                ibVolumeIcon.setSrcDrawableDisabledCanTouch();
            }
        }
    }

    private float volumeToggle;

    public void toggleVolumeMute() {
        if (playerManager == null) return;
        if (playerManager.getVolume() == 0f) {
            setVolume(volumeToggle);
            ibVolumeIcon.setSrcDrawableEnabled();
        } else {
            volumeToggle = getVolume();
            setVolume(0f);
            ibVolumeIcon.setSrcDrawableDisabledCanTouch();
        }
    }

    public void setSpeed(float speed) {
        if (isLIVE())
            throw new IllegalArgumentException(getResources().getString(R.string.error_speed_live_content));
        if (speed > 3 || speed < -3)
            throw new IllegalArgumentException(getResources().getString(R.string.error_speed_illegal));
        PlaybackParameters playbackParameters = new PlaybackParameters(speed);
        if (getPlayer() != null)
            getPlayer().setPlaybackParameters(playbackParameters);
    }

    //=============================================================================================START UI
    private void findViews() {
        rlMsg = findViewById(R.id.rl_msg);
        rlMsg.setOnClickListener(this);
        TextView tvMsg = findViewById(R.id.tv_msg);
        if (tvMsg != null)
            UZViewUtils.setTextShadow(tvMsg, Color.BLACK);
        ivVideoCover = findViewById(R.id.iv_cover);
        llTop = findViewById(R.id.ll_top);
        progressBar = findViewById(R.id.pb);
        if (progressBar != null)
            UZViewUtils.setColorProgressBar(progressBar, Color.WHITE);
        updateUIPositionOfProgressBar();
        playerView.setOnDoubleTap(new UZPlayerView.OnDoubleTap() {
            @Override
            public void onDoubleTapProgressUp(float posX, float posY) {
                float halfScreen = UZViewUtils.getScreenWidth() / 2.0f;
                if (posX - 60.0f > halfScreen) {
                    seekToForward();
                } else if (posX + 60.0f < halfScreen) {
                    seekToBackward();
                }
            }
        });
        timeBar = playerView.findViewById(R.id.exo_progress);
        previewFrameLayout = playerView.findViewById(R.id.preview_frame_layout);
        if (timeBar != null) {
            if (timeBar.getTag() == null) {
                timeBarAtBottom = false;
                playerView.setVisibility(VISIBLE);
            } else {
                if (timeBar.getTag().toString().equals(getResources().getString(R.string.use_bottom_uz_timebar))) {
                    timeBarAtBottom = true;
                    setMarginDependOnUZTimeBar(playerView.getVideoSurfaceView());
                } else {
                    timeBarAtBottom = false;
                    playerView.setVisibility(VISIBLE);
                }
            }
            timeBar.addOnPreviewChangeListener(this);
            timeBar.setOnFocusChangeListener(this);
        } else
            playerView.setVisibility(VISIBLE);
        ivThumbnail = playerView.findViewById(R.id.image_view_thumbnail);
        tvPosition = playerView.findViewById(R.id.uz_position);
        if (tvPosition != null) {
            tvPosition.setText(StringUtils.convertMlsecondsToHMmSs(0));
        }
        tvDuration = playerView.findViewById(R.id.uz_duration);
        if (tvDuration != null) {
            tvDuration.setText("-:-");
        }
        ibFullscreenIcon = playerView.findViewById(R.id.exo_fullscreen_toggle_icon);
        tvTitle = playerView.findViewById(R.id.tv_title);
        ibPauseIcon = playerView.findViewById(R.id.exo_pause);
        ibPlayIcon = playerView.findViewById(R.id.exo_play);
        //If auto start true, show button play and gone button pause
        UZViewUtils.goneViews(ibPlayIcon);
        ibReplayIcon = playerView.findViewById(R.id.exo_replay);
        ibRewIcon = playerView.findViewById(R.id.exo_rew);
        if (ibRewIcon != null)
            ibRewIcon.setSrcDrawableDisabled();
        ibFfwdIcon = playerView.findViewById(R.id.exo_ffwd);
        ibBackScreenIcon = playerView.findViewById(R.id.exo_back_screen);
        ibVolumeIcon = playerView.findViewById(R.id.exo_volume);
        ibSettingIcon = playerView.findViewById(R.id.exo_setting);
        ibCcIcon = playerView.findViewById(R.id.exo_cc);
        ibPlaylistFolderIcon = playerView.findViewById(R.id.exo_playlist_folder);
        ibHearingIcon = playerView.findViewById(R.id.exo_hearing);
        pipIcon = playerView.findViewById(R.id.exo_picture_in_picture);
        ibSkipNextIcon = playerView.findViewById(R.id.exo_skip_next);
        ibSkipPreviousIcon = playerView.findViewById(R.id.exo_skip_previous);
        ibSpeedIcon = playerView.findViewById(R.id.exo_speed);
        if (!UZAppUtils.hasSupportPIP(getContext(), enablePictureInPicture) || UZData.getInstance().isUseUZDragView())
            UZViewUtils.goneViews(pipIcon);
        LinearLayout debugLayout = findViewById(R.id.debug_layout);
        debugRootView = findViewById(R.id.controls_root);
        if (BuildConfig.DEBUG) {
            debugLayout.setVisibility(View.VISIBLE);
        } else {
            debugLayout.setVisibility(View.GONE);
        }
        rlLiveInfo = playerView.findViewById(R.id.rl_live_info);
        tvLiveStatus = playerView.findViewById(R.id.tv_live);
        tvLiveView = playerView.findViewById(R.id.tv_live_view);
        tvLiveTime = playerView.findViewById(R.id.tv_live_time);
        ivLiveView = playerView.findViewById(R.id.iv_live_view);
        ivLiveTime = playerView.findViewById(R.id.iv_live_time);
        UZViewUtils.setFocusableViews(false, ivLiveView, ivLiveTime);
        RelativeLayout rlEndScreen = playerView.findViewById(R.id.rl_end_screen);
        UZViewUtils.goneViews(rlEndScreen);
        tvEndScreenMsg = playerView.findViewById(R.id.tv_end_screen_msg);
        if (tvEndScreenMsg != null) {
            UZViewUtils.setTextShadow(tvEndScreenMsg, Color.WHITE);
            tvEndScreenMsg.setOnClickListener(this);
        }
        setEventForViews();
        //set visibility first, so scared if removed
        setVisibilityOfPlaylistFolderController(GONE);
        statsForNerdsView = findViewById(R.id.stats_for_nerds);
    }

    private void setEventForViews() {
        setClickAndFocusEventForViews(ibFullscreenIcon, ibBackScreenIcon, ibVolumeIcon, ibSettingIcon,
                ibCcIcon, ibPlaylistFolderIcon, ibHearingIcon, pipIcon, ibFfwdIcon,
                ibRewIcon, ibPlayIcon, ibPauseIcon, ibReplayIcon, ibSkipNextIcon, ibSkipPreviousIcon, ibSpeedIcon, tvLiveStatus);
    }

    private void setClickAndFocusEventForViews(View... views) {
        for (View v : views) {
            if (v != null) {
                v.setOnClickListener(this);
                v.setOnFocusChangeListener(this);
            }
        }
    }

    //If auto start true, show button play and gone button pause
    //if not, gone button play and show button pause
    private void updateUIButtonPlayPauseDependOnIsAutoStart() {
        if (isAutoStart) {
            UZViewUtils.goneViews(ibPlayIcon);
            if (ibPauseIcon != null) {
                UZViewUtils.visibleViews(ibPauseIcon);
                if (!isSetFirstRequestFocusDone) {
                    ibPauseIcon.requestFocus();//set first request focus if using player for TV
                    isSetFirstRequestFocusDone = true;
                }
            }
        } else {
            if (isPlaying()) {
                UZViewUtils.goneViews(ibPlayIcon);
                if (ibPauseIcon != null) {
                    UZViewUtils.visibleViews(ibPauseIcon);
                    if (!isSetFirstRequestFocusDone) {
                        ibPauseIcon.requestFocus();//set first request focus if using player for TV
                        isSetFirstRequestFocusDone = true;
                    }
                }
            } else {
                if (ibPlayIcon != null) {
                    UZViewUtils.visibleViews(ibPlayIcon);
                    if (!isSetFirstRequestFocusDone) {
                        ibPlayIcon.requestFocus();//set first request focus if using player for TV
                        isSetFirstRequestFocusDone = true;
                    }
                }
                UZViewUtils.goneViews(ibPauseIcon);
            }
        }
    }

    private void removeVideoCover(boolean isFromHandleError) {
        if (!isFromHandleError)
            onStateReadyFirst();
        if (ivVideoCover.getVisibility() != GONE) {
            ivVideoCover.setVisibility(GONE);
            ivVideoCover.invalidate();
            if (isLIVE()) {
                if (tvLiveTime != null)
                    tvLiveTime.setText(HYPHEN);
                if (tvLiveView != null)
                    tvLiveView.setText(HYPHEN);
            }
        } else
            //goi changeskin realtime thi no ko vao if nen ko update tvDuration dc
            updateTvDuration();
    }

    private void updateUIEachSkin() {
        int curSkinLayoutId = UZData.getInstance().getUZPlayerSkinLayoutId();
        if (curSkinLayoutId == R.layout.uzplayer_skin_2 || curSkinLayoutId == R.layout.uzplayer_skin_3) {
            if (ibPlayIcon != null) {
                ibPlayIcon.setRatioLand(7);
                ibPlayIcon.setRatioPort(5);
            }
            if (ibPauseIcon != null) {
                ibPauseIcon.setRatioLand(7);
                ibPauseIcon.setRatioPort(5);
            }
            if (ibReplayIcon != null) {
                ibReplayIcon.setRatioLand(7);
                ibReplayIcon.setRatioPort(5);
            }
        }
    }

    private void updateUIPositionOfProgressBar() {
        if (progressBar == null)
            return;
        postDelayed(() -> {
            int marginL = playerView.getMeasuredWidth() / 2 - progressBar.getMeasuredWidth() / 2;
            int marginT = playerView.getMeasuredHeight() / 2 - progressBar.getMeasuredHeight() / 2;
            UZViewUtils.setMarginPx(progressBar, marginL, marginT, 0, 0);
        }, 10);
    }

    /*
     ** change skin of player (realtime)
     * return true if success
     */
    public boolean changeSkin(@LayoutRes int skinId) {
        if (playerManager == null) return false;
        if (UZData.getInstance().isUseUZDragView())
            throw new IllegalArgumentException(getResources().getString(R.string.error_change_skin_with_uzdragview));
        if (playerManager.isPlayingAd()) {
            notifyError(ErrorUtils.exceptionChangeSkin());
            return false;
        }
        UZData.getInstance().setUZPlayerSkinLayoutId(skinId);
        isRefreshFromChangeSkin = true;
        isCalledFromChangeSkin = true;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            rootView.removeView(playerView);
            rootView.requestLayout();
            playerView = (UZPlayerView) inflater.inflate(skinId, null);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            playerView.setLayoutParams(lp);
            rootView.addView(playerView);
            rootView.requestLayout();
            findViews();
            resizeContainerView();
            updateUIEachSkin();
            updateUIDependOnLiveStream();
            setMarginPreviewTimeBar();
            setMarginRlLiveInfo();
            //setup chromecast
            if (UZAppUtils.checkChromeCastAvailable())
                setupChromeCast();
            currentPositionBeforeChangeSkin = getCurrentPosition();
            releasePlayerManager();
            setTitle();
            checkToSetUpResource();
            updateUISizeThumbnail();
            if (playerCallback != null)
                playerCallback.onSkinChange();
            return true;
        }
        return false;

    }

    private void setupChromeCast() {
        uzChromeCast = new UZChromeCast();
        uzChromeCast.setUZChromeCastListener(new UZChromeCastListener() {
            @Override
            public void onConnected() {
                lastCurrentPosition = getCurrentPosition();
                handleConnectedChromecast();
            }

            @Override
            public void onDisconnected() {
                handleDisconnectedChromecast();
            }

            @Override
            public void addUIChromeCast() {
                if (llTop != null)
                    llTop.addView(uzChromeCast.getMediaRouteButton());
                addUIChromecastLayer();
            }
        });
        uzChromeCast.setupChromeCast(getContext());
    }

    private void updateTvDuration() {
        if (tvDuration != null)
            if (isLIVE())
                tvDuration.setText(StringUtils.convertMlsecondsToHMmSs(0));
            else
                tvDuration.setText(StringUtils.convertMlsecondsToHMmSs(getDuration()));
    }

    public void setProgressSeekBar(@NonNull UZVerticalSeekBar uzVerticalSeekBar, int progressSeekBar) {
        uzVerticalSeekBar.setProgress(progressSeekBar);
    }

    private void setTextPosition(long currentMls) {
        if (tvPosition == null) return;
        if (isLIVE()) {
            long duration = getDuration();
            long past = duration - currentMls;
            tvPosition.setText(String.format("%s%s", HYPHEN, StringUtils.convertMlsecondsToHMmSs(past)));
        } else
            tvPosition.setText(StringUtils.convertMlsecondsToHMmSs(currentMls));
    }


    private void updateUIIbRewIconDependOnProgress(long currentMls, boolean isCalledFromUZTimeBarEvent) {
        if (isCalledFromUZTimeBarEvent)
            setTextPosition(currentMls);
        else {
            if (!isOnPreview) //uzTimeBar is displaying
                setTextPosition(currentMls);
            return;
        }
        if (isLIVE()) return;
        if (ibRewIcon != null && ibFfwdIcon != null) {
            if (currentMls == 0) {
                if (ibRewIcon.isSetSrcDrawableEnabled())
                    ibRewIcon.setSrcDrawableDisabled();
                if (!ibFfwdIcon.isSetSrcDrawableEnabled())
                    ibFfwdIcon.setSrcDrawableEnabled();
            } else if (currentMls == getDuration()) {
                if (!ibRewIcon.isSetSrcDrawableEnabled())
                    ibRewIcon.setSrcDrawableEnabled();
                if (ibFfwdIcon.isSetSrcDrawableEnabled())
                    ibFfwdIcon.setSrcDrawableDisabled();
            } else {
                if (!ibRewIcon.isSetSrcDrawableEnabled())
                    ibRewIcon.setSrcDrawableEnabled();
                if (!ibFfwdIcon.isSetSrcDrawableEnabled())
                    ibFfwdIcon.setSrcDrawableEnabled();
            }
        }
    }

    //FOR TV
    public void updateUIFocusChange(@NonNull View view, boolean isFocus) {
        if (view instanceof UZImageButton) {
            UZViewUtils.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
            ((UZImageButton) view).clearColorFilter();
        } else if (view instanceof Button) {
            UZViewUtils.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
        } else if (view instanceof UZPreviewTimeBar) {
            UZViewUtils.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus_uz_timebar, R.drawable.bkg_tv_no_focus_uz_timebar);
        }
    }

    private void handleFirstViewHasFocus() {
        if (firstViewHasFocus != null && uzTVFocusChangeListener != null) {
            uzTVFocusChangeListener.onFocusChange(firstViewHasFocus, true);
            firstViewHasFocus = null;
        }
    }

    private void updateUISizeThumbnail() {
        int screenWidth = UZViewUtils.getScreenWidth();
        int widthIv = isLandscape ? screenWidth / 4 : screenWidth / 5;
        if (previewFrameLayout != null) {
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.width = widthIv;
            layoutParams.height = (int) (widthIv * Constants.RATIO_9_16);
            previewFrameLayout.setLayoutParams(layoutParams);
            previewFrameLayout.requestLayout();
        }
    }

    private void setMarginPreviewTimeBar() {
        if (timeBar != null)
            if (isLandscape)
                UZViewUtils.setMarginDimen(timeBar, 5, 0, 5, 0);
            else
                UZViewUtils.setMarginDimen(timeBar, 0, 0, 0, 0);
    }

    private void setMarginRlLiveInfo() {
        if (rlLiveInfo != null)
            if (isLandscape)
                UZViewUtils.setMarginDimen(rlLiveInfo, 50, 0, 50, 0);
            else
                UZViewUtils.setMarginDimen(rlLiveInfo, 5, 0, 5, 0);
    }

    private void setTitle() {
        if (tvTitle != null)
            tvTitle.setText(UZData.getInstance().getEntityName());
    }

    public void setAlwaysHideLiveViewers(boolean hide) {
        this.alwaysHideLiveViewers = hide;
    }

    /**
     * ======== END UI =========
     */

    private void updateUIDependOnLiveStream() {
        if (isCastingChromecast)
            UZViewUtils.goneViews(pipIcon);
        else if (UZAppUtils.isTablet(getContext()) && UZAppUtils.isTV(getContext()))//only hide ibPictureInPictureIcon if device is TV
            UZViewUtils.goneViews(pipIcon);
        if (isLIVE()) {
            if (alwaysHideLiveViewers) {
                UZViewUtils.visibleViews(rlLiveInfo, tvLiveStatus, tvLiveTime, ivLiveTime);
                UZViewUtils.goneViews(ivLiveTime, ivLiveView);
            } else {
                UZViewUtils.visibleViews(rlLiveInfo, tvLiveStatus, tvLiveTime, ivLiveTime, tvLiveView, ivLiveView);
            }
            UZViewUtils.goneViews(ibSpeedIcon, tvDuration, ibRewIcon, ibFfwdIcon);
            setUIVisible(false, ibRewIcon, ibFfwdIcon);
        } else {
            UZViewUtils.goneViews(rlLiveInfo, tvLiveStatus, tvLiveTime, tvLiveView, ivLiveTime, ivLiveView);
            UZViewUtils.visibleViews(ibSpeedIcon, tvDuration, ibFfwdIcon, ibRewIcon);
            setUIVisible(true, ibRewIcon, ibFfwdIcon);
            //TODO why set visible not work?
        }
        if (UZAppUtils.isTV(getContext()))
            UZViewUtils.goneViews(ibFullscreenIcon);
    }

    private void setUIVisible(boolean visible, UZImageButton... views) {
        for (UZImageButton v : views) {
            if (v != null)
                v.setUIVisible(visible);
        }
    }

    protected void updateUIButtonVisibilities() {
        if (getContext() == null) return;
        if (debugRootView != null) debugRootView.removeAllViews();
        if (getPlayer() == null) return;
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerManager.getTrackSelector().getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null)
            return;
        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {

            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                Button button = new Button(getContext());
                button.setSoundEffectsEnabled(false);
                int label;
                switch (playerManager.getPlayer().getRendererType(i)) {
                    case C.TRACK_TYPE_AUDIO:
                        label = R.string.audio;
                        break;
                    case C.TRACK_TYPE_VIDEO:
                        label = R.string.video;
                        break;
                    case C.TRACK_TYPE_TEXT:
                        label = R.string.text;
                        break;
                    default:
                        continue;
                }
                button.setText(label);
                button.setTag(i);
                button.setOnClickListener(this);
                if (debugRootView != null)
                    debugRootView.addView(button);
            }
        }
    }

    public void showLayoutMsg() {
        hideController();
        UZViewUtils.visibleViews(rlMsg);
    }

    public void hideLayoutMsg() {
        UZViewUtils.goneViews(rlMsg);
    }

    private void updateUIEndScreen() {
        if (isOnPlayerEnded) {
            setVisibilityOfPlayPauseReplay(true);
            showController();
            if (playerView != null) {
                playerView.setControllerShowTimeoutMs(0);
                playerView.setControllerHideOnTouch(false);
            }
        } else {
            setVisibilityOfPlayPauseReplay(false);
            if (playerView != null)
                playerView.setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS);
            setHideControllerOnTouch(isHideOnTouch);
        }
    }

    private void setVisibilityOfPlayPauseReplay(boolean isShowReplay) {
        if (isShowReplay) {
            UZViewUtils.goneViews(ibPlayIcon, ibPauseIcon);
            if (ibReplayIcon != null) {
                UZViewUtils.visibleViews(ibReplayIcon);
                ibReplayIcon.requestFocus();
            }
        } else {
            updateUIButtonPlayPauseDependOnIsAutoStart();
            UZViewUtils.goneViews(ibReplayIcon);
        }
    }

    private void setVisibilityOfPlaylistFolderController(int visibilityOfPlaylistFolderController) {
        UZViewUtils.setVisibilityViews(visibilityOfPlaylistFolderController, ibPlaylistFolderIcon,
                ibSkipNextIcon, ibSkipPreviousIcon);
        setVisibilityOfPlayPauseReplay(false);
    }

    Dialog dlg = null;

    /**
     * show Setting Dialog
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (inflater != null)
            builder.setCustomTitle(inflater.inflate(R.layout.custom_header_dragview, null));
        // add a list
        int actionCount = debugRootView.getChildCount();
        if (actionCount < 1) return;
        List<SettingItem> actions = new ArrayList<>();
        for (int i = 0; i < actionCount; i++) {
            actions.add(new SettingItem(((Button) debugRootView.getChildAt(i)).getText().toString()));
        }
        if (statsForNerdsView != null) {
            actions.add(new SettingItem(getResources().getString(R.string.stats), statsForNerdsView.getVisibility() == View.VISIBLE, isChecked -> {
                if (dlg != null) {
                    postDelayed(() -> dlg.dismiss(), 500);
                }
                if (isChecked)
                    UZViewUtils.visibleViews(statsForNerdsView);
                else
                    UZViewUtils.goneViews(statsForNerdsView);
                return true;
            }));
        }
        if (playerManager != null && playerManager.isTimeShiftSupport()) {
            actions.add(new SettingItem(getResources().getString(R.string.time_shift), playerManager.isTimeShiftOn(), isChecked -> {
                if (dlg != null) {
                    postDelayed(() -> dlg.dismiss(), 600);
                }
                boolean sw = playerManager.switchTimeShift(isChecked);
                if (playerCallback != null && sw)
                    playerCallback.onTimeShiftChange(playerManager.isTimeShiftOn());
                return sw;
            }));
        }
        builder.setAdapter(new SettingAdapter(getContext(), actions), (dialog, which) -> {
            if (which < actionCount)
                (debugRootView.getChildAt(which)).performClick();
        });
        dlg = builder.create();
        UZViewUtils.showDialog(dlg);
    }

    private List<UZItem> showTrackSelectionDialog(final View view, boolean showDialog) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerManager.getTrackSelector().getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            CharSequence title = ((Button) view).getText();
            int rendererIndex = (int) view.getTag();
            final Pair<AlertDialog, UZTrackSelectionView> dialogPair = UZTrackSelectionView.getDialog(getContext(), title, playerManager.getTrackSelector(), rendererIndex);
            dialogPair.second.setShowDisableOption(false);
            dialogPair.second.setAllowAdaptiveSelections(false);
            dialogPair.second.setCallback(() -> handler.postDelayed(() -> {
                        if (dialogPair.first == null)
                            return;
                        dialogPair.first.cancel();
                    }
                    , 300));
            if (showDialog)
                UZViewUtils.showDialog(dialogPair.first);
            return dialogPair.second.getUZItemList();
        }
        return null;
    }

    public void setBackgroundColor(int color) {
        RelativeLayout uzVideoRootView = findViewById(R.id.root_view_uz_video);
        if (uzVideoRootView != null)
            uzVideoRootView.setBackgroundColor(color);
    }

    public void setMarginDependOnUZTimeBar(View view) {
        if (view == null || timeBar == null) return;
        int heightTimeBar;
        if (isLandscape)
            UZViewUtils.setMarginPx(view, 0, 0, 0, 0);
        else {
            heightTimeBar = getHeightTimeBar();
            UZViewUtils.setMarginPx(view, 0, 0, 0, heightTimeBar / 2);
        }
    }

    public void hideProgress() {
        if (progressBar != null)
            progressBar.setVisibility(View.GONE);
    }

    public void showProgress() {
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * @param callback {@see UZPlayerCallback}
     */
    public void setPlayerCallback(UZPlayerCallback callback) {
        this.playerCallback = callback;
    }

    public void setTVFocusChangeListener(UZTVFocusChangeListener uzTVFocusChangeListener) {
        this.uzTVFocusChangeListener = uzTVFocusChangeListener;
        handleFirstViewHasFocus();
    }

    public void setOnPreviewChangeListener(PreviewView.OnPreviewChangeListener onPreviewChangeListener) {
        this.onPreviewChangeListener = onPreviewChangeListener;
    }

    //=============================================================================================END EVENT

    private void checkToSetUpResource() {
        UZPlayback playback = UZData.getInstance().getPlayback();
        if (playback != null) {
            List<String> listLinkPlay = playback.getLinkPlays();
            if (listLinkPlay.isEmpty()) {
                handleErrorNoData();
                return;
            }
            if (countTryLinkPlayError >= listLinkPlay.size()) {
                if (ConnectivityUtils.isConnected(getContext()))
                    handleError(ErrorUtils.exceptionTryAllLinkPlay());
                else
                    handleError(ErrorUtils.exceptionNoConnection());
                return;
            }
            String linkPlay = listLinkPlay.get(countTryLinkPlayError);
            if (TextUtils.isEmpty(linkPlay)) {
                handleError(ErrorUtils.exceptionNoLinkPlay());
                return;
            }
            initDataSource(linkPlay,
                    isCalledFromChangeSkin ? null : UZData.getInstance().getUrlIMAAd(),
                    playback.getPoster());
            if (playerCallback != null)
                playerCallback.isInitResult(linkPlay);
            initPlayerManager();
        } else
            handleError(ErrorUtils.exceptionSetup());
    }

    private void initDataSource(String linkPlay, String urlIMAAd, String urlThumbnailsPreviewSeekBar) {
        playerManager = new UZPlayerManager.Builder(getContext())
                .withPlayUrl(linkPlay)
                .withIMAAdUrl(urlIMAAd)
                .build();
        isFirstStateReady = false;
        if (timeBar != null) {
            boolean disable = TextUtils.isEmpty(urlThumbnailsPreviewSeekBar);
            timeBar.setEnabled(!disable);
            timeBar.setPreviewLoader(this);
        }
        playerManager.setProgressListener(new UZProgressListener() {
            @Override
            public void onVideoProgress(long currentMls, int s, long duration, int percent) {
                post(() -> updateUIIbRewIconDependOnProgress(currentMls, false));
                if (isLIVE())
                    post(() -> updateLiveStatus(currentMls, duration));
            }
        });
        playerManager.setDebugCallback(this::updateUIButtonVisibilities);
        playerManager.setBufferCallback((bufferedDurationUs, playbackSpeed) -> statsForNerdsView.setBufferedDurationUs(bufferedDurationUs));
    }

    @Override
    public void loadPreview(long currentPosition, long max) {
        if (playerManager == null) return;
        playerManager.setPlayWhenReady(false);
        String posterUrl = UZData.getInstance().getPosterUrl();
        if (!TextUtils.isEmpty(posterUrl) && ivThumbnail != null)
            ImageUtils.loadThumbnail(ivThumbnail, posterUrl, currentPosition);
    }

    private void onStateReadyFirst() {
        updateTvDuration();
        updateUIButtonPlayPauseDependOnIsAutoStart();
        updateUIDependOnLiveStream();
        if (timeBarAtBottom)
            UZViewUtils.visibleViews(playerView);
        resizeContainerView();
        //enable from playPlaylistPosition() prevent double click
        UZViewUtils.setClickableForViews(true, ibSkipPreviousIcon, ibSkipNextIcon);
        if (playerCallback != null) {
            playerCallback.isInitResult(UZData.getInstance().getPlayback().getLinkPlay(countTryLinkPlayError));
        }
        if (isCastingChromecast)
            replayChromeCast();
        if (timeBar != null) {
            timeBar.hidePreview();
        }
        UZData.getInstance().setSettingPlayer(false);
    }

    /**
     * When isLive = true, if not time shift then hide timber
     */
    private void updateTimeBarWithTimeShiftStatus() {
        if (playerManager != null && playerManager.isTimeShiftSupport()) {
            if (!playerManager.isTimeShiftOn()) {
                UZViewUtils.goneViews(timeBar);
            } else {
                UZViewUtils.visibleViews(timeBar);
            }
        }
    }

    //=============================================================================================START CHROMECAST

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(ConnectEvent event) {
        if (event == null || playerManager == null) return;
        if (!event.isConnected()) {
            notifyError(ErrorUtils.exceptionNoConnection());
        } else {
            if (playerManager.getExoPlaybackException() == null) {
                hideController();
                hideLayoutMsg();
            } else {
                isCalledFromConnectionEventBus = true;
                playerManager.setResumeIfConnectionError();
                if (!activityIsPausing) {
                    playerManager.register(this);
                    if (isCalledFromConnectionEventBus) {
                        playerManager.setRunnable();
                        isCalledFromConnectionEventBus = false;
                    }
                }
            }
            resume();
        }
    }

    private void handleConnectedChromecast() {
        isCastingChromecast = true;
        isCastPlayerPlayingFirst = false;
        playChromecast();
        updateUIChromecast();
    }

    private void handleDisconnectedChromecast() {
        isCastingChromecast = false;
        isCastPlayerPlayingFirst = false;
        updateUIChromecast();
    }

    public boolean isCastingChromecast() {
        return isCastingChromecast;
    }

    private void playChromecast() {
        if (UZData.getInstance().getPlayback() == null || playerManager == null || playerManager.getPlayer() == null) {
            return;
        }
        showProgress();
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
//        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, UizaData.getInstance().getPlayback().getDescription());
//        movieMetadata.putString(MediaMetadata.KEY_TITLE, UizaData.getInstance().getPlayback().getEntityName());
//        movieMetadata.addImage(new WebImage(Uri.parse(UizaData.getInstance().getPlayback().getThumbnail())));
        // NOTE: The receiver app (on TV) should Satisfy CORS requirements
        // https://developers.google.com/cast/docs/android_sender/media_tracks#satisfy_cors_requirements
        List<MediaTrack> mediaTrackList = new ArrayList<>();
        long duration = getDuration();
        if (duration < 0) {
            Timber.e("invalid duration -> cannot play chromecast");
            return;
        }

        MediaInfo mediaInfo = new MediaInfo.Builder(playerManager.getLinkPlay())
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType("videos/mp4")
                .setMetadata(movieMetadata)
                .setMediaTracks(mediaTrackList)
                .setStreamDuration(duration)
                .build();

        //play chromecast without screen control
        Casty casty = UZData.getInstance().getCasty();
        if (casty != null) {
            casty.getPlayer().loadMediaAndPlayInBackground(mediaInfo, true, lastCurrentPosition);
            casty.getPlayer().getRemoteMediaClient().addProgressListener((currentPosition, duration1) -> {
                if (currentPosition >= lastCurrentPosition && !isCastPlayerPlayingFirst) {
                    hideProgress();
                    isCastPlayerPlayingFirst = true;
                }
                if (currentPosition > 0)
                    playerManager.seekTo(currentPosition);
            }, 1000);
        }

    }

    /* khi click vào biểu tượng casting
     * thì sẽ pause local player và bắt đầu loading lên cast player
     * khi disconnect thì local player sẽ resume*/
    private void updateUIChromecast() {
        if (playerManager == null || rlChromeCast == null || UZAppUtils.isTV(getContext()))
            return;
        if (isCastingChromecast) {
            playerManager.pause();
            setVolume(0f);
            UZViewUtils.visibleViews(rlChromeCast, ibPlayIcon);
            UZViewUtils.goneViews(ibPauseIcon);
            //casting player luôn play first với volume not mute
            //UizaData.getInstance().getCasty().setVolume(0.99);
            if (playerView != null)
                playerView.setControllerShowTimeoutMs(0);
        } else {
            playerManager.resume();
            setVolume(0.99f);
            UZViewUtils.goneViews(rlChromeCast, ibPlayIcon);
            UZViewUtils.visibleViews(ibPauseIcon);
            //TODO iplm volume mute on/off o cast player
            //khi quay lại exoplayer từ cast player thì mặc định sẽ bật lại âm thanh (dù cast player đang mute hay !mute)
            if (playerView != null)
                playerView.setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS);
        }
    }

    // ===== Stats For Nerds =====
    private void initStatsForNerds() {
        if (getPlayer() != null)
            getPlayer().addAnalyticsListener(statsForNerdsView);
    }

    private void addUIChromecastLayer() {
        rlChromeCast = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams rlChromeCastParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rlChromeCast.setLayoutParams(rlChromeCastParams);
        rlChromeCast.setVisibility(GONE);
        rlChromeCast.setBackgroundColor(Color.BLACK);
        UZImageButton ibsCast = new UZImageButton(getContext());
        ibsCast.setBackgroundColor(Color.TRANSPARENT);
        ibsCast.setImageResource(R.drawable.cast);
        RelativeLayout.LayoutParams ibsCastParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ibsCastParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        ibsCast.setLayoutParams(ibsCastParams);
        ibsCast.setRatioPort(5);
        ibsCast.setRatioLand(5);
        ibsCast.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ibsCast.setColorFilter(Color.WHITE);
        rlChromeCast.addView(ibsCast);
        rlChromeCast.setOnClickListener(this);
        if (llTop != null) {
            if (llTop.getParent() instanceof ViewGroup)
                ((RelativeLayout) llTop.getParent()).addView(rlChromeCast, 0);
        } else if (rlLiveInfo != null) {
            if (rlLiveInfo.getParent() instanceof ViewGroup)
                ((RelativeLayout) rlLiveInfo.getParent()).addView(rlChromeCast, 0);
        }
    }

    private void updateLiveStatus(long currentMls, long duration) {
        if (tvLiveStatus == null) return;
        long timeToEndChunk = duration - currentMls;
        if (timeToEndChunk <= targetDurationMls * 10) {
            tvLiveStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.text_live_color_focus));
            UZViewUtils.goneViews(tvPosition);
        } else {
            tvLiveStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.text_live_color));
            UZViewUtils.visibleViews(tvPosition);
        }
    }

    private void seekToEndLive() {
        long timeToEndChunk = getDuration() - getCurrentPosition();
        if (timeToEndChunk > targetDurationMls * 10) {
            seekToLiveEdge();
        }
    }

    public void updateLiveStreamLatency(long latency) {
        statsForNerdsView.showTextLiveStreamLatency();
        statsForNerdsView.setTextLiveStreamLatency(StringUtils.groupingSeparatorLong(latency));
    }

    public void hideTextLiveStreamLatency() {
        statsForNerdsView.hideTextLiveStreamLatency();
    }

    private void trackWatchingTimer(boolean firstRun) {
        final UZPlaybackInfo pi = UZData.getInstance().getPlaybackInfo();
        if (pi != null && handler != null) {
            UZTrackingData data = new UZTrackingData(pi, viewerSessionId, UZEventType.WATCHING);
            handler.postDelayed(() -> {
                        if (isPlaying()) {
                            disposables.add(UZAnalytic.pushEvent(data, res -> Timber.d("send track watching: %s, response: %s", viewerSessionId, res.string()),
                                    error -> Timber.e("send track watching error: %s", error.getMessage())
                            ));
                        }
                        trackWatchingTimer(false);
                    }
                    , firstRun ? 0 : DEFAULT_VALUE_TRACKING_LOOP); // 5s
        } else {
            Timber.e("Do not track watching");
        }
    }

    public void setAlwaysPortraitScreen(boolean isAlwaysPortraitScreen) {
        this.isAlwaysPortraitScreen = isAlwaysPortraitScreen;
    }

    public boolean isAlwaysPortraitScreen() {
        return isAlwaysPortraitScreen;
    }

    public void setEnablePictureInPicture(boolean enablePictureInPicture) {
        this.enablePictureInPicture = enablePictureInPicture;
    }

    public boolean isEnablePictureInPicture() {
        return enablePictureInPicture;
    }
}
