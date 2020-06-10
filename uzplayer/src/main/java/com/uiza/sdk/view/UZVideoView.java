package com.uiza.sdk.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.Rational;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
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
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.video.VideoListener;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.uiza.sdk.BuildConfig;
import com.uiza.sdk.R;
import com.uiza.sdk.analytics.UZAnalytic;
import com.uiza.sdk.animations.AnimationUtils;
import com.uiza.sdk.chromecast.Casty;
import com.uiza.sdk.dialog.hq.UZItem;
import com.uiza.sdk.dialog.hq.UZTrackSelectionView;
import com.uiza.sdk.dialog.info.UZDlgInfoV1;
import com.uiza.sdk.dialog.playlistfolder.CallbackPlaylistFolder;
import com.uiza.sdk.dialog.playlistfolder.UZPlaylistFolderDialog;
import com.uiza.sdk.dialog.speed.UZSpeedDialog;
import com.uiza.sdk.events.EventBusData;
import com.uiza.sdk.exceptions.ErrorConstant;
import com.uiza.sdk.exceptions.ErrorUtils;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZAdPlayerCallback;
import com.uiza.sdk.interfaces.UZCallback;
import com.uiza.sdk.interfaces.UZLiveContentCallback;
import com.uiza.sdk.interfaces.UZVideoViewItemClick;
import com.uiza.sdk.listerner.UZChromeCastListener;
import com.uiza.sdk.listerner.UZProgressListener;
import com.uiza.sdk.listerner.UZTVFocusChangeListener;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.models.UZPlaybackInfo;
import com.uiza.sdk.models.UZTrackingData;
import com.uiza.sdk.observers.SensorOrientationChangeNotifier;
import com.uiza.sdk.observers.UZConnectifyService;
import com.uiza.sdk.utils.ConnectivityUtils;
import com.uiza.sdk.utils.Constants;
import com.uiza.sdk.utils.ConvertUtils;
import com.uiza.sdk.utils.DebugUtils;
import com.uiza.sdk.utils.ImageUtils;
import com.uiza.sdk.utils.ListUtils;
import com.uiza.sdk.utils.StringUtils;
import com.uiza.sdk.utils.TmpParamData;
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
        implements PreviewLoader, PreviewView.OnPreviewChangeListener, View.OnClickListener, View.OnFocusChangeListener,
        UZPlayerView.ControllerStateCallback, SensorOrientationChangeNotifier.Listener {

    private static final String HYPHEN = "-";
    private static final long DEFAULT_VALUE_BACKWARD_FORWARD = 10000L; // 10s
    /**
     * The timeout in milliseconds. A non-positive value will cause the
     * controller to remain visible indefinitely.
     */
    private static final int DEFAULT_VALUE_CONTROLLER_TIMEOUT_MLS = 8000; // 8s
    private static final long DEFAULT_VALUE_TRACKING_LOOP = 5000L;  // 5s

    //===================================================================START FOR PLAYLIST/FOLDER
    private VideoListener videoListener;
    private AudioListener audioListener;
    private Player.EventListener eventListener;
    private MetadataOutput metadataOutput;
    private TextOutput textOutput;

    private Handler handler = new Handler(Looper.getMainLooper());
    private View bkg;
    private RelativeLayout rootView, rlChromeCast;
    private UZPlayerManager playerManager;
    private ProgressBar progressBar;
    private LinearLayout llTop, debugRootView;
    private RelativeLayout rlMsg, rlLiveInfo, rlEndScreen;
    private FrameLayout previewFrameLayout;
    private UZPreviewTimeBar timeBar;
    private ImageView ivThumbnail, ivVideoCover, ivLogo;
    private UZTextView tvPosition, tvDuration;
    private TextView tvMsg, tvTitle, tvLiveStatus, tvLiveView, tvLiveTime;
    private UZImageButton ibFullscreenIcon, ibPauseIcon, ibPlayIcon, ibReplayIcon, ibRewIcon, ibFfwdIcon, ibBackScreenIcon, ibVolumeIcon,
            ibSettingIcon, ibCcIcon, ibPlaylistFolderIcon //playlist folder
            , ibHearingIcon, pipIcon, ibSkipPreviousIcon, ibSkipNextIcon, ibSpeedIcon, ivLiveTime, ivLiveView, ibsCast;
    private SwitchCompat toggleTimeShift;
    private TextView tvEndScreenMsg;
    private UZPlayerView playerView;
    private long startTime = -1;
    private long defaultSeekValue = DEFAULT_VALUE_BACKWARD_FORWARD;
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
    private long timestampBeforeInitNewSession;
    private int countTryLinkPlayError = 0;
    private boolean activityIsPausing = false;
    private long timestampOnStartPreview;
    private boolean isOnPreview;
    private long maxSeekLastDuration;
    private boolean isLandscape;//current screen is landscape or portrait
    private boolean isHideOnTouch = true;
    private boolean useController = true;
    private boolean isOnPlayerEnded;
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
    private UZLiveContentCallback uzLiveContentCallback;
    private UZProgressListener progressListener;
    private PreviewView.OnPreviewChangeListener onPreviewChangeListener;
    private UZVideoViewItemClick uzVideoViewItemClick;
    private UZCallback uzCallback;
    private UZTVFocusChangeListener uzTVFocusChangeListener;
    private UZPlayerView.ControllerStateCallback controllerStateCallback;
    private long timestampInitDataSource;
    //=============================================================================================START EVENTBUS
    private boolean isCalledFromConnectionEventBus = false;
    //last current position lúc từ exoplayer switch sang cast player
    private long lastCurrentPosition;
    private boolean isCastPlayerPlayingFirst;
    private StatsForNerdsView statsForNerdsView;
    private String viewerSessionId;
    private CompositeDisposable disposables;
    private boolean isInit = false;

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
        if (!isInit) {
            onCreateView();
        }
    }

    /**
     * Call one time from {@link #onAttachedToWindow}
     * Note: you must call inflate in this method
     */
    private void onCreateView() {
        if (UZAppUtils.checkChromeCastAvailable())
            setupChromeCast();
        try {
            EventBus.getDefault().register(this);
        } catch (NoClassDefFoundError e) {
            Timber.e(e);
        }
        startConnectifyService();
        inflate(getContext(), R.layout.uz_ima_video_core_rl, this);
        rootView = findViewById(R.id.root_view);
        addPlayerView();
        findViews();
        resizeContainerView();
        updateUIEachSkin();
        setMarginPreviewTimeBar();
        setMarginRlLiveInfo();
        updateUISizeThumbnail();
        scheduleJob();
        isInit = true;
    }

    private void resizeContainerView() {
        if (!isFreeSize) {
            setSize(getVideoWidth(), getVideoHeight());
        } else {
            setSize(this.getWidth(), this.getHeight());
        }
    }

    private void startConnectifyService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Intent startServiceIntent = new Intent(getContext(), UZConnectifyService.class);
                getContext().startService(startServiceIntent);
            } catch (NoClassDefFoundError e) {
                Timber.e(e);
            }
        }
    }

    public boolean isAutoStart() {
        return isAutoStart;
    }

    public void setAutoStart(boolean isAutoStart) {
        this.isAutoStart = isAutoStart;
        TmpParamData.getInstance().setPlayerAutoplayOn(isAutoStart);
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

    public long getDuration() {
        return (getPlayer() == null) ? -1 : getPlayer().getDuration();
    }

    // An estimate of the position in the current window up to which data is buffered.
    // If the length of the content is 100,00 ms, and played 50,000 ms already with extra 50,000 ms~ 60,000 ms buffered,
    // it returns 60,000 ms.
    public long getBufferedPosition() {
        return (getPlayer() == null) ? -1 : getPlayer().getBufferedPosition();
    }

    // An estimate of the percentage in the current window up to which data is buffered.
    // If the length of the content is 100,00 ms, and played 50,000 ms already with extra 50,000 ms~ 60,000 ms buffered,
    // it returns 60(%).
    public int getBufferedPercentage() {
        return (getPlayer() == null) ? -1 : getPlayer().getBufferedPercentage();
    }

    // Lay pixel dung cho custom UI like youtube, timeBar bottom of player controller
    public int getPixelAdded() {
        return timeBarAtBottom ? (getHeightTimeBar() / 2) : 0;
    }

    //return pixel
    public int getHeightTimeBar() {
        return UZViewUtils.heightOfView(timeBar);
    }

    //The current position of playing. the window means playable region, which is all of the content if vod, and some portion of the content if live.
    public long getCurrentPosition() {
        return (playerManager == null) ? -1 : playerManager.getCurrentPosition();
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
        if (uzCallback != null)
            uzCallback.onError(exception);
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
            Timber.e(ErrorConstant.ERR_0);
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
            notifyError(ErrorUtils.exceptionNoConnection());
            return false;
        }
        if (ListUtils.isEmpty(playlist)) {
            Timber.d("play::playlist is Empty");
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
        TmpParamData.getInstance().setPlayerIsPaused(false);
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
    }

    public void pause() {
        TmpParamData.getInstance().setPlayerIsPaused(true);
        if (isCastingChromecast) {
            Casty casty = UZData.getInstance().getCasty();
            if (casty != null)
                casty.getPlayer().pause();
        } else if (playerManager != null) {
            playerManager.pause();
        }
        UZViewUtils.goneViews(ibPauseIcon);
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
        timestampBeforeInitNewSession = System.currentTimeMillis();
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
            if (isLIVE()) {
                startTime = -1;
            }
        }
        updateUIDependOnLiveStream();
        disposables = new CompositeDisposable();
        initDataSource(playback.getDefaultLinkPlay(), UZData.getInstance().getUrlIMAAd(), playback.getThumbnail());
        if (uzCallback != null)
            uzCallback.isInitResult(true, UZData.getInstance().getPlayback());
        trackWatchingTimer(true);
        initPlayerManager();
    }

    private void initPlayerManager() {
        if (playerManager != null) {
            playerManager.init(this);
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
            if (countTryLinkPlayError >= 5) {
                if (uzLiveContentCallback != null)
                    uzLiveContentCallback.onLiveStreamUnAvailable();
                return;
            }
            // if entity is livestreaming, dont try to next link play
            Timber.e("tryNextLinkPlay isLivestream true -> try to replay = count %d", countTryLinkPlayError);
            if (playerManager != null) {
                playerManager.initWithoutReset();
                playerManager.setRunnable();
            }
            countTryLinkPlayError++;
            return;
        }
        countTryLinkPlayError++;
        releasePlayerManager();
        checkToSetUpResource();
    }

    //khi call api callAPIGetLinkPlay nhung json tra ve ko co data
    //se co gang choi video da play gan nhat
    //neu co thi se play
    //khong co thi bao loi
    private void handleErrorNoData() {
        removeVideoCover(true);
        if (uzCallback != null) {
            UZData.getInstance().setSettingPlayer(false);
            uzCallback.isInitResult(false, null);
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

    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        positionPIPPlayer = getCurrentPosition();
        isInPipMode = !isInPictureInPictureMode;
        if (!isPlaying()) {
            setUseController(true);
        }
    }

    public void onDestroyView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getContext().stopService(new Intent(getContext(), UZConnectifyService.class));
        releasePlayerStats();
        releasePlayerManager();
        UZData.getInstance().setSettingPlayer(false);
        isCastingChromecast = false;
        isCastPlayerPlayingFirst = false;
        EventBus.getDefault().unregister(this);
        if (UZAppUtils.hasSupportPIP(getContext())) {
            ((Activity) getContext()).finishAndRemoveTask();
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
        if (isCastingChromecast)
            return;
        activityIsPausing = false;
        if (playerManager != null) {
            if (ibPlayIcon == null || ibPlayIcon.getVisibility() != VISIBLE)
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
        // in PIP not pause
        if (playerManager != null && !isPIPEnable()) {
            playerManager.pause();
        }
    }

    public boolean isPIPEnable() {
        return (pipIcon != null)
                && !isCastingChromecast()
                && UZAppUtils.hasSupportPIP(getContext())
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
        TmpParamData.getInstance().addViewSeekCount();
        long seekLastDuration = System.currentTimeMillis() - timestampOnStartPreview;
        TmpParamData.getInstance().setViewSeekDuration(seekLastDuration);
        if (maxSeekLastDuration < seekLastDuration) {
            maxSeekLastDuration = seekLastDuration;
            TmpParamData.getInstance().setViewMaxSeekTime(maxSeekLastDuration);
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
                UZViewUtils.changeScreenLandscape((Activity) getContext(), orientation);
        } else {
            if (isDeviceAutoRotation && isLandscape)
                UZViewUtils.changeScreenPortrait((Activity) getContext());
        }
    }

    //===================================================================END FOR PLAYLIST/FOLDER

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resizeContainerView();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            UZViewUtils.hideDefaultControls((Activity) getContext());
            isLandscape = true;
            UZViewUtils.setUIFullScreenIcon(ibFullscreenIcon, true);
            UZViewUtils.goneViews(pipIcon);
        } else {
            UZViewUtils.showDefaultControls((Activity) getContext());
            isLandscape = false;
            UZViewUtils.setUIFullScreenIcon(ibFullscreenIcon, false);
            if (isPIPEnable())
                UZViewUtils.visibleViews(pipIcon);
        }
        TmpParamData.getInstance().setPlayerIsFullscreen(isLandscape);
        setMarginPreviewTimeBar();
        setMarginRlLiveInfo();
        updateUISizeThumbnail();
        updateUIPositionOfProgressBar();
        if (timeBarAtBottom)
            setMarginDependOnUZTimeBar(playerView.getVideoSurfaceView());
        if (uzCallback != null)
            uzCallback.onScreenRotate(isLandscape);
    }

    @Override
    public void onClick(View v) {
        if (v == rlMsg)
            AnimationUtils.play(v, Techniques.Pulse);
        else if (v == ibFullscreenIcon)
            toggleFullscreen();
        else if (v == ibBackScreenIcon)
            handleClickBackScreen();
        else if (v == ibVolumeIcon)
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
            showUZTrackSelectionDialog(v, true);
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
                    casty.getPlayer().seekToBackward(defaultSeekValue);
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
        else if (v == ivLogo) {
            AnimationUtils.play(v, Techniques.Pulse);
            UZPlayback info = UZData.getInstance().getPlayback();
            if (info == null || TextUtils.isEmpty(info.getThumbnail()))
                return;
            UZAppUtils.openUrlInBrowser(getContext(), info.getThumbnail());
        }
        /*có trường hợp đang click vào các control thì bị ẩn control ngay lập tức, trường hợp này ta có thể xử lý khi click vào control thì reset count down để ẩn control ko
        default controller timeout là 8s, vd tới s thứ 7 bạn tương tác thì tới s thứ 8 controller sẽ bị ẩn*/
        if (useController
                && (rlMsg == null || rlMsg.getVisibility() != VISIBLE)
                && isPlayerControllerShowing())
            showController();
        if (uzVideoViewItemClick != null)
            uzVideoViewItemClick.onItemClick(v);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void enterPIPMode() {
        if (isPIPEnable()) {
            positionPIPPlayer = getCurrentPosition();
            setUseController(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PictureInPictureParams.Builder params = new PictureInPictureParams.Builder();
                Rational aspectRatio = new Rational(getVideoWidth(), getVideoHeight());
                params.setAspectRatio(aspectRatio);
                ((Activity) getContext()).enterPictureInPictureMode(params.build());
            } else {
                ((Activity) getContext()).enterPictureInPictureMode();
            }
        }
        postDelayed(() -> {
            isPIPModeEnabled = ((Activity) getContext()).isInPictureInPictureMode();
            if (!isPIPModeEnabled) {
                enterPIPMode();
            }
        }, 50);
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
        setSrcDrawableEnabledForViews(ibSkipPreviousIcon, ibSkipNextIcon);
        //set disabled prevent double click, will enable onStateReadyFirst()
        setClickableForViews(false, ibSkipPreviousIcon, ibSkipNextIcon);
        //end update UI for skip next and skip previous button
        UZData.getInstance().setCurrentPositionOfPlayList(position);
        UZPlayback playback = UZData.getInstance().getPlayback();
        if (playback == null || !playback.canPlay()) {
            Timber.e("playPlaylistPosition error: playlist is null or can not play");
            return;
        }

        initPlayback(playback, false);
    }

    private void setSrcDrawableEnabledForViews(UZImageButton... views) {
        for (UZImageButton v : views) {
            if (v != null && !v.isFocused()) {
                v.setSrcDrawableEnabled();
            }
        }
    }

    private void setClickableForViews(boolean able, View... views) {
        for (View v : views) {
            if (v != null) {
                v.setClickable(able);
                v.setFocusable(able);
            }
        }
    }

    protected void onPlayerEnded() {
        if (isPlaying()) {
            isOnPlayerEnded = true;
            if (isPlayPlaylistFolder() && isAutoSwitchItemPlaylistFolder) {
                hideController();
                autoSwitchNextVideo();
            } else {
                updateUIEndScreen();
            }
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
        TmpParamData.getInstance().addPlayerViewCount();
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
        if (isLandscape)
            toggleFullscreen();
    }

    private void handleClickSetting() {
        View view = DebugUtils.getVideoButton(debugRootView);
        if (view != null)
            view.performClick();
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

    /*
     ** Seek tu vi tri hien tai cong them bao nhieu mls
     */
    public void seekToForward(int mls) {
        setDefaultSeekValue(mls);
        ibFfwdIcon.performClick();
    }

    public void seekToForward() {
        if (!isLIVE())
            ibFfwdIcon.performClick();
    }

    /*
     ** Seek tu vi tri hien tai tru di bao nhieu mls
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

    public void showCCPopup() {
        ibCcIcon.performClick();
    }

    public void showHQPopup() {
        ibSettingIcon.performClick();
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

    /*
     ** Bo video hien tai va choi tiep theo 1 video trong playlist/folder
     */
    public void skipNextVideo() {
        handleClickSkipNext();
    }

    /*
     * Bo video hien tai va choi lui lai 1 video trong playlist/folder
     */
    public void skipPreviousVideo() {
        handleClickSkipPrevious();
    }

    public PlayerView getPlayerView() {
        return playerView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public ImageView getIvThumbnail() {
        return ivThumbnail;
    }

    public boolean isLIVE() {
        return playerManager != null && playerManager.isLIVE();
    }

    public UZPlayerManager getPlayerManager() {
        return playerManager;
    }

    public TextView getTvMsg() {
        return tvMsg;
    }

    public ImageView getIvVideoCover() {
        return ivVideoCover;
    }

    public UZImageButton getIbFullscreenIcon() {
        return ibFullscreenIcon;
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public UZImageButton getIbPauseIcon() {
        return ibPauseIcon;
    }

    public UZImageButton getIbPlayIcon() {
        return ibPlayIcon;
    }

    public UZImageButton getIbReplayIcon() {
        return ibReplayIcon;
    }

    public UZImageButton getIbRewIcon() {
        return ibRewIcon;
    }

    public UZImageButton getIbFfwdIcon() {
        return ibFfwdIcon;
    }

    public UZImageButton getIbBackScreenIcon() {
        return ibBackScreenIcon;
    }

    public UZImageButton getIbVolumeIcon() {
        return ibVolumeIcon;
    }

    public UZImageButton getIbSettingIcon() {
        return ibSettingIcon;
    }

    public UZImageButton getIbCcIcon() {
        return ibCcIcon;
    }

    public UZImageButton getIbPlaylistFolderIcon() {
        return ibPlaylistFolderIcon;
    }

    public UZImageButton getIbHearingIcon() {
        return ibHearingIcon;
    }

    public UZImageButton getPIPIcon() {
        return pipIcon;
    }

    public UZImageButton getIbSkipPreviousIcon() {
        return ibSkipPreviousIcon;
    }

    public UZImageButton getIbSkipNextIcon() {
        return ibSkipNextIcon;
    }

    public UZImageButton getIbSpeedIcon() {
        return ibSpeedIcon;
    }

    public RelativeLayout getRlLiveInfo() {
        return rlLiveInfo;
    }

    public void setLiveViewers(int viewers) {
        if (tvLiveView != null) {
            if (viewers == 1) {
                tvLiveView.setText(getResources().getString(R.string.oneViewer));
            } else {
                tvLiveView.setText(getResources().getString(R.string.numberOfViewers, viewers));
            }
        }
    }

    public TextView getTvLiveTime() {
        return tvLiveTime;
    }

    public RelativeLayout getRlChromeCast() {
        return rlChromeCast;
    }

    public UZImageButton getIbsCast() {
        return ibsCast;
    }

    public UZTextView getTvPosition() {
        return tvPosition;
    }

    public UZTextView getTvDuration() {
        return tvDuration;
    }

    public TextView getTvLiveStatus() {
        return tvLiveStatus;
    }

    public UZImageButton getIvLiveTime() {
        return ivLiveTime;
    }

    public UZImageButton getIvLiveView() {
        return ivLiveView;
    }

    public RelativeLayout getRlEndScreen() {
        return rlEndScreen;
    }

    public TextView getTvEndScreenMsg() {
        return tvEndScreenMsg;
    }

    public UZPreviewTimeBar getPreviewTimeBar() {
        return timeBar;
    }

    public LinearLayout getLlTop() {
        return llTop;
    }

    public View getBkg() {
        return bkg;
    }

    public List<UZItem> getHQList() {
        View view = DebugUtils.getVideoButton(debugRootView);
        if (view == null) {
            Timber.e("Error getHQList null");
            notifyError(ErrorUtils.exceptionListHQ());
            return null;
        }
        return showUZTrackSelectionDialog(view, false);
    }

    public List<UZItem> getAudioList() {
        View view = DebugUtils.getAudioButton(debugRootView);
        if (view == null) {
            notifyError(ErrorUtils.exceptionListAudio());
            Timber.e("Error audio null");
            return null;
        }
        return showUZTrackSelectionDialog(view, false);
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

    @Override
    public void onVisibilityChange(boolean isShow) {
        if (ivLogo != null)
            ivLogo.setClickable(!isShow);
        if (controllerStateCallback != null)
            controllerStateCallback.onVisibilityChange(isShow);
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
        bkg = findViewById(R.id.bkg);
        rlMsg = findViewById(R.id.rl_msg);
        rlMsg.setOnClickListener(this);
        tvMsg = findViewById(R.id.tv_msg);
        if (tvMsg != null)
            UZViewUtils.setTextShadow(tvMsg, Color.BLACK);
        ivVideoCover = findViewById(R.id.iv_cover);
        llTop = findViewById(R.id.ll_top);
        progressBar = findViewById(R.id.pb);
        UZViewUtils.setColorProgressBar(progressBar, Color.WHITE);
        updateUIPositionOfProgressBar();
        playerView.setControllerStateCallback(this);
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
        if (!UZAppUtils.hasSupportPIP(getContext()) || UZData.getInstance().isUseUZDragView())
            UZViewUtils.goneViews(pipIcon);
        LinearLayout debugLayout = findViewById(R.id.debug_layout);
        debugRootView = findViewById(R.id.controls_root);
        if (BuildConfig.DEBUG) {
            debugLayout.setVisibility(View.VISIBLE);
        } else {
            debugLayout.setVisibility(View.GONE);
        }
        toggleTimeShift = playerView.findViewById(R.id.toggle_time_shift);
        rlLiveInfo = playerView.findViewById(R.id.rl_live_info);
        tvLiveStatus = playerView.findViewById(R.id.tv_live);
        tvLiveView = playerView.findViewById(R.id.tv_live_view);
        tvLiveTime = playerView.findViewById(R.id.tv_live_time);
        ivLiveView = playerView.findViewById(R.id.iv_live_view);
        ivLiveTime = playerView.findViewById(R.id.iv_live_time);
        UZViewUtils.setFocusableViews(false, ivLiveView, ivLiveTime);
        rlEndScreen = playerView.findViewById(R.id.rl_end_screen);
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
        if(toggleTimeShift != null){
            toggleTimeShift.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(!playerManager.switchTimeShift(isChecked)){
                    toggleTimeShift.setChecked(false);
                }
            });
        }
    }

    private void setEventForViews() {
        setClickAndFocusEventForViews(ibFullscreenIcon, ibBackScreenIcon, ibVolumeIcon, ibSettingIcon,
                ibCcIcon, ibPlaylistFolderIcon, ibHearingIcon, pipIcon, ibFfwdIcon,
                ibRewIcon, ibPlayIcon, ibPauseIcon, ibReplayIcon, ibSkipNextIcon, ibSkipPreviousIcon, ibSpeedIcon, tvLiveStatus, toggleTimeShift);
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

    public void setUrlImgThumbnail(String urlImgThumbnail) {
        if (TextUtils.isEmpty(urlImgThumbnail) || ivVideoCover == null)
            return;
        if (ivVideoCover.getVisibility() != VISIBLE) {
            ivVideoCover.setVisibility(VISIBLE);
            ImageUtils.load(ivVideoCover, urlImgThumbnail, R.drawable.background_black);
        }
    }

    private void setVideoCover() {
        if (ivVideoCover.getVisibility() != VISIBLE) {
            resetCountTryLinkPlayError();
            ivVideoCover.setVisibility(VISIBLE);
            ivVideoCover.invalidate();
            String urlCover;
            UZPlayback info = UZData.getInstance().getPlayback();
            if (info == null || TextUtils.isEmpty(info.getThumbnail()))
                urlCover = Constants.URL_IMG_THUMBNAIL_BLACK;
            else
                urlCover = info.getThumbnail();
            TmpParamData.getInstance().setEntityPosterUrl(urlCover);
            ImageUtils.load(ivVideoCover, urlCover, R.drawable.background_black);
        }
    }

    protected void removeVideoCover(boolean isFromHandleError) {
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
        if (playerView == null || progressBar == null)
            return;
        playerView.post(() -> {
            int marginL = playerView.getMeasuredWidth() / 2 - progressBar.getMeasuredWidth() / 2;
            int marginT = playerView.getMeasuredHeight() / 2 - progressBar.getMeasuredHeight() / 2;
            UZViewUtils.setMarginPx(progressBar, marginL, marginT, 0, 0);
        });
    }

    private void addPlayerView() {
        playerView = null;
        int skinId = UZData.getInstance().getUZPlayerSkinLayoutId();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            playerView = (UZPlayerView) inflater.inflate(skinId, null);
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            playerView.setLayoutParams(lp);
            playerView.setVisibility(GONE);
            rootView.addView(playerView);
            setControllerAutoShow(isAutoShowController);
        } else
            Timber.e("inflater is null");
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
        rootView.removeView(playerView);
        rootView.requestLayout();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
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
            if (uzCallback != null)
                uzCallback.onSkinChange();
            return true;
        }
        return false;

    }

    private void setupChromeCast() {
        uzChromeCast = new UZChromeCast();
        uzChromeCast.setUZChromeCastListener(new UZChromeCastListener() {
            @Override
            public void onConnected() {
                if (playerManager != null)
                    lastCurrentPosition = getCurrentPosition();
                handleConnectedChromecast();
            }

            @Override
            public void onDisconnected() {
                handleDisconnectedChromecast();
            }

            @Override
            public void addUIChromecast() {
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

    /**
     * ======== END UI =========
     */

    private void updateUIDependOnLiveStream() {
        if (isCastingChromecast)
            UZViewUtils.goneViews(pipIcon);
        else if (UZAppUtils.isTablet(getContext()) && UZAppUtils.isTV(getContext()))//only hide ibPictureInPictureIcon if device is TV
            UZViewUtils.goneViews(pipIcon);
        if (isLIVE()) {
            UZViewUtils.visibleViews(rlLiveInfo, tvLiveStatus, tvLiveTime, tvLiveView, ivLiveTime, ivLiveView, toggleTimeShift);
            UZViewUtils.goneViews(ibSpeedIcon, tvDuration, ibRewIcon, ibFfwdIcon);
            setUIVisible(false, ibRewIcon, ibFfwdIcon);
        } else {
            UZViewUtils.goneViews(rlLiveInfo, tvLiveStatus, tvLiveTime, tvLiveView, ivLiveTime, ivLiveView, toggleTimeShift);
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

    private void updateLiveInfoTimeStartLive() {
        if (!isLIVE() || getContext() == null) return;
        long now = System.currentTimeMillis();
        long duration = now - startTime;
        String s = StringUtils.convertMlsecondsToHMmSs(duration);
        if (tvLiveTime != null)
            tvLiveTime.setText(s);
        if (uzLiveContentCallback != null)
            uzLiveContentCallback.onUpdateLiveInfoTimeStartLive(duration, s);
    }

    private void updateUIEndScreen() {
        if (getContext() == null) return;
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

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (inflater != null)
            builder.setCustomTitle(inflater.inflate(R.layout.custom_header_dragview, null));
        // add a list
        int actionCount = debugRootView.getChildCount();
        if (actionCount < 1) return;
        String[] actions = new String[actionCount];
        for (int i = 0; i < actionCount; i++) {
            actions[i] = ((Button) debugRootView.getChildAt(i)).getText().toString();
        }

        builder.setAdapter(new ArrayAdapter<>(getContext(), R.layout.uz_setting_list_item, actions), (dialog, which) ->
                (debugRootView.getChildAt(which)).performClick()
        );
        UZViewUtils.showDialog(builder.create());
    }

    private List<UZItem> showUZTrackSelectionDialog(final View view, boolean showDialog) {
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

    public void setTimeBarBottom() {
        if (playerView == null)
            throw new NullPointerException("PlayerView cannot be null");
        if (timeBar == null)
            throw new NullPointerException("timeBar cannot be null");
        if (playerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
    }

    public int getHeightOfVideo() {
        if (rootView == null) {
            return 0;
        }
        if (timeBarAtBottom) {
            int hRootView = UZViewUtils.heightOfView(rootView);
            int hTimeBar = getHeightTimeBar();
            return hRootView - hTimeBar / 2;
        } else
            return UZViewUtils.heightOfView(rootView);
    }

    public void setBackgroundColorBkg(int color) {
        if (bkg != null)
            bkg.setBackgroundColor(color);
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
        if (playerManager != null)
            playerManager.hideProgress();
    }

    public void showProgress() {
        if (playerManager != null)
            playerManager.showProgress();
    }

    private void updateUIPlayerInfo() {
        if (playerView == null) return;
        UZPlayback info = UZData.getInstance().getPlayback();
        if (info == null) return;
        if (TextUtils.isEmpty(info.getLogo()))
            return;
        ivLogo = new ImageView(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ConvertUtils.dp2px(50f), ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        if (playerView.getOverlayFrameLayout() != null)
            playerView.getOverlayFrameLayout().addView(ivLogo, layoutParams);
        ivLogo.setOnClickListener(this);
        ImageUtils.load(ivLogo, info.getLogo());
    }

    /**
     * @param uzLiveContentCallback
     */
    public void setLiveContentCallback(UZLiveContentCallback uzLiveContentCallback) {
        this.uzLiveContentCallback = uzLiveContentCallback;
    }

    /**
     * @param uzCallback
     */
    public void setUZCallback(UZCallback uzCallback) {
        this.uzCallback = uzCallback;
    }

    public void setTVFocusChangeListener(UZTVFocusChangeListener uzTVFocusChangeListener) {
        this.uzTVFocusChangeListener = uzTVFocusChangeListener;
        handleFirstViewHasFocus();
    }

    public void setProgressListener(UZProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void setOnPreviewChangeListener(PreviewView.OnPreviewChangeListener onPreviewChangeListener) {
        this.onPreviewChangeListener = onPreviewChangeListener;
    }

    //=============================================================================================END EVENT

    public void setUZVideoViewItemClick(UZVideoViewItemClick uzVideoViewItemClick) {
        this.uzVideoViewItemClick = uzVideoViewItemClick;
    }

    public void setControllerStateCallback(UZPlayerView.ControllerStateCallback controllerStateCallback) {
        this.controllerStateCallback = controllerStateCallback;
    }

    public void setOnTouchEvent(UZPlayerView.OnTouchEvent onTouchEvent) {
        if (playerView != null)
            playerView.setOnTouchEvent(onTouchEvent);
    }

    public void setOnSingleTap(UZPlayerView.OnSingleTap onSingleTap) {
        if (playerView != null)
            playerView.setOnSingleTap(onSingleTap);
    }

    public void setOnDoubleTap(UZPlayerView.OnDoubleTap onDoubleTap) {
        if (playerView != null)
            playerView.setOnDoubleTap(onDoubleTap);
    }

    public void setOnLongPressed(UZPlayerView.OnLongPressed onLongPressed) {
        if (playerView != null)
            playerView.setOnLongPressed(onLongPressed);
    }

    public void setAudioListener(AudioListener audioListener) {
        this.audioListener = audioListener;
    }

    public AudioListener getAudioListener() {
        return audioListener;
    }

    public void setEventListener(Player.EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public Player.EventListener getEventListener() {
        return eventListener;
    }

    public void setVideoListener(VideoListener videoListener) {
        this.videoListener = videoListener;
    }

    public VideoListener getVideoListener() {
        return videoListener;
    }

    public void setMetadataOutput(MetadataOutput metadataOutput) {
        this.metadataOutput = metadataOutput;
    }

    public MetadataOutput getMetadataOutput() {
        return metadataOutput;
    }

    public void setTextOutput(TextOutput textOutput) {
        this.textOutput = textOutput;
    }

    public TextOutput getTextOutput() {
        return textOutput;
    }

    private void checkData() {
        UZData.getInstance().setSettingPlayer(true);
        isHasError = false;
        UZPlayback playback = UZData.getInstance().getPlayback();
        if (playback == null || !playback.canPlay()) {
            Timber.e("checkData playback null or link play invalid");
            handleError(ErrorUtils.exceptionTryAllLinkPlay());
            UZData.getInstance().setSettingPlayer(false);
            return;
        }
        if (playerManager != null) {
            releasePlayerManager();
            resetCountTryLinkPlayError();
            showProgress();
        }
        setTitle();
    }

    private void checkToSetUpResource() {
        UZPlayback playback = UZData.getInstance().getPlayback();
        if (playback != null) {
            List<String> listLinkPlay = playback.getUrls();
            if (listLinkPlay.isEmpty()) {
                handleErrorNoData();
                return;
            }
            if (countTryLinkPlayError >= listLinkPlay.size()) {
                if (ConnectivityUtils.isConnected(getContext()))
                    handleError(ErrorUtils.exceptionTryAllLinkPlay());
                else
                    notifyError(ErrorUtils.exceptionNoConnection());
                return;
            }
            String linkPlay = listLinkPlay.get(countTryLinkPlayError);
            initDataSource(linkPlay,
                    isCalledFromChangeSkin ? null : UZData.getInstance().getUrlIMAAd(),
                    playback.getThumbnail());
            if (uzCallback != null)
                uzCallback.isInitResult(false, playback);
            initPlayerManager();
        } else
            handleError(ErrorUtils.exceptionSetup());
    }

    private void initDataSource(String linkPlay, String urlIMAAd, String urlThumbnailsPreviewSeekBar) {
        timestampInitDataSource = System.currentTimeMillis();
        TmpParamData.getInstance().setEntitySourceUrl(linkPlay);
        playerManager = new UZPlayerManager.Builder(this)
                .withPlayUrl(linkPlay)
                .withIMAAdUrl(urlIMAAd)
                .build();
        playerManager.setAdPlayerCallback(new UZAdPlayerCallback() {
            @Override
            public void onPlay() {
                updateTvDuration();
            }

            @Override
            public void onEnded() {
                updateTvDuration();
            }

            @Override
            public void onError() {
                updateTvDuration();
            }

        });
        if (timeBar != null) {
            boolean disable = TextUtils.isEmpty(urlThumbnailsPreviewSeekBar);
            timeBar.setEnabled(!disable);
            timeBar.setPreviewLoader(this);
        }
        playerManager.setProgressListener(new UZProgressListener() {
            @Override
            public void onAdEnded() {
                setUseController(isUseController());
                if (progressListener != null)
                    progressListener.onAdEnded();
            }

            @Override
            public void onAdProgress(int s, int duration, int percent) {
                if (progressListener != null)
                    progressListener.onAdProgress(s, duration, percent);
            }

            @Override
            public void onVideoProgress(long currentMls, int s, long duration, int percent) {
                TmpParamData.getInstance().setPlayerPlayheadTime(s);
                post(() -> updateUIIbRewIconDependOnProgress(currentMls, false));
                if (isLIVE())
                    post(() -> updateLiveStatus(currentMls, duration));
                if (progressListener != null)
                    progressListener.onVideoProgress(currentMls, s, duration, percent);
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (progressListener != null)
                    progressListener.onPlayerStateChanged(playWhenReady, playbackState);
            }

            @Override
            public void onBufferProgress(long bufferedPosition, int bufferedPercentage, long duration) {
                if (progressListener != null)
                    progressListener.onBufferProgress(bufferedPosition, bufferedPercentage, duration);
            }
        });
        playerManager.setDebugCallback(this::updateUIButtonVisibilities);
        playerManager.setBufferCallback((bufferedDurationUs, playbackSpeed) -> statsForNerdsView.setBufferedDurationUs(bufferedDurationUs));
    }

    @Override
    public void loadPreview(long currentPosition, long max) {
        if (playerManager == null) return;
        playerManager.setPlayWhenReady(false);
        String thumbnailsUrl = UZData.getInstance().getThumbnailsUrl();
        if (!TextUtils.isEmpty(thumbnailsUrl) && ivThumbnail != null)
            ImageUtils.loadThumbnail(ivThumbnail, thumbnailsUrl, currentPosition);
    }

    protected void onStateReadyFirst() {
        long pageLoadTime = System.currentTimeMillis() - timestampBeforeInitNewSession;
        TmpParamData.getInstance().setPageLoadTime(pageLoadTime);
        TmpParamData.getInstance().setViewStart(System.currentTimeMillis());
        TmpParamData.getInstance().setViewTimeToFirstFrame(System.currentTimeMillis());
        updateTvDuration();
        updateUIButtonPlayPauseDependOnIsAutoStart();
        updateUIDependOnLiveStream();
        if (timeBarAtBottom)
            UZViewUtils.visibleViews(playerView);
        resizeContainerView();
        //enable from playPlaylistPosition() prevent double click
        setClickableForViews(true, ibSkipPreviousIcon, ibSkipNextIcon);
        if (uzCallback != null) {
            uzCallback.isInitResult(true, UZData.getInstance().getPlayback());
        }
        if (isCastingChromecast)
            replayChromeCast();
        updateUIPlayerInfo();
        TmpParamData.getInstance().setSessionStart(System.currentTimeMillis());
        long playerStartUpTime = System.currentTimeMillis() - timestampInitDataSource;
        TmpParamData.getInstance().setPlayerStartupTime(playerStartUpTime);
        UZData.getInstance().setSettingPlayer(false);
    }

    //=============================================================================================END EVENTBUS

    //=============================================================================================START CHROMECAST

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEvent(EventBusData.ConnectEvent event) {
        if (event == null || playerManager == null) return;
        if (!event.isConnected()) notifyError(ErrorUtils.exceptionNoConnection());
        else {
            if (playerManager.getExoPlaybackException() == null) {
                hideController();
                hideLayoutMsg();
            } else {
                isCalledFromConnectionEventBus = true;
                playerManager.setResumeIfConnectionError();
                if (!activityIsPausing) {
                    playerManager.init(this);
                    if (isCalledFromConnectionEventBus) {
                        playerManager.setRunnable();
                        isCalledFromConnectionEventBus = false;
                    }
                }
            }
            resume();
        }
    }

    public boolean isInitNewItem(String urlImgThumbnail) {
        if (positionPIPPlayer != 0) {
            seekTo(positionPIPPlayer);
            resume();
            positionPIPPlayer = 0;
            return false;
        } else {
            setUrlImgThumbnail(urlImgThumbnail);
            pause();
            showProgress();
            positionPIPPlayer = 0;
            return true;
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

    //=============================================================================================END CHROMECAST
    private void scheduleJob() {
        if (getContext() == null) return;
        JobInfo myJob;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                myJob = new JobInfo.Builder(0, new ComponentName(getContext(), UZConnectifyService.class))
                        .setRequiresCharging(true)
                        .setMinimumLatency(1000)
                        .setOverrideDeadline(2000)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPersisted(true)
                        .build();
                JobScheduler jobScheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                if (jobScheduler != null)
                    jobScheduler.schedule(myJob);
            } catch (NoClassDefFoundError e) {
                Timber.w(e);
            }
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
        ibsCast = new UZImageButton(getContext());
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

    public void setVideoAdPlayerCallback(UZAdPlayerCallback uzAdPlayerCallback) {
        if (UZAppUtils.isAdsDependencyAvailable()) {
            playerManager.setAdPlayerCallback(uzAdPlayerCallback);
        } else
            throw new NoClassDefFoundError(ErrorConstant.ERR_506);
    }

    protected long getTargetDurationMls() {
        return (playerManager != null) ? playerManager.getTargetDurationMls() : UZPlayerManager.DEFAULT_TARGET_DURATION_MLS;
    }

    private void updateLiveStatus(long currentMls, long duration) {
        if (tvLiveStatus == null) return;
        long timeToEndChunk = duration - currentMls;
        long targetDurationMls = getTargetDurationMls();
        if (timeToEndChunk <= targetDurationMls * 10) {
            tvLiveStatus.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            UZViewUtils.goneViews(tvPosition);
        } else {
            tvLiveStatus.setTextColor(Color.WHITE);
            UZViewUtils.visibleViews(tvPosition);
        }
    }

    private void seekToEndLive() {
        long timeToEndChunk = getDuration() - getCurrentPosition();
        long targetDurationMls = getTargetDurationMls();
        if (timeToEndChunk > targetDurationMls * 10) {
            seekToLiveEdge();
        }
    }

    private boolean isTimeShift(){
        return toggleTimeShift != null && toggleTimeShift.isChecked();
    }

    protected void updateLiveStreamLatency(long latency) {
        statsForNerdsView.showTextLiveStreamLatency();
        statsForNerdsView.setTextLiveStreamLatency(StringUtils.groupingSeparatorLong(latency));
    }

    protected void hideTextLiveStreamLatency() {
        statsForNerdsView.hideTextLiveStreamLatency();
    }

    private void trackWatchingTimer(boolean firstRun) {
        final UZPlaybackInfo pi = UZData.getInstance().getPlaybackInfo();
        if (pi != null && handler != null) {
            UZTrackingData data = new UZTrackingData(pi, viewerSessionId);
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
}
