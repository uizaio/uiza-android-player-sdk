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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.Rational;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.uiza.sdk.listerner.UZTVFocusChangeListener;
import com.uiza.sdk.models.UZAnalyticInfo;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.models.UZTrackingData;
import com.uiza.sdk.observers.SensorOrientationChangeNotifier;
import com.uiza.sdk.observers.UZConnectifyService;
import com.uiza.sdk.utils.ConnectivityUtils;
import com.uiza.sdk.utils.Constants;
import com.uiza.sdk.utils.ConvertUtils;
import com.uiza.sdk.utils.DebugUtils;
import com.uiza.sdk.utils.ImageUtils;
import com.uiza.sdk.utils.JacksonUtils;
import com.uiza.sdk.utils.ListUtils;
import com.uiza.sdk.utils.StringUtils;
import com.uiza.sdk.utils.TmpParamData;
import com.uiza.sdk.utils.UZAppUtils;
import com.uiza.sdk.utils.UZData;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.widget.UZPreviewTimeBar;
import com.uiza.sdk.widget.UZImageButton;
import com.uiza.sdk.widget.UZTextView;
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
public class UZVideoView extends VideoViewBase
        implements PreviewView.OnPreviewChangeListener, View.OnClickListener, View.OnFocusChangeListener,
        UZPlayerView.ControllerStateCallback, SensorOrientationChangeNotifier.Listener {

    private static final String HYPHEN = "-";
    //===================================================================START FOR PLAYLIST/FOLDER
    protected AudioListener audioListener;
    protected MetadataOutput metadataOutput;
    protected Player.EventListener eventListener;
    protected VideoListener videoListener;
    protected TextOutput textOutput;
    Handler handler = new Handler();
    private int DEFAULT_VALUE_BACKWARD_FORWARD = 10000;//10000 mls
    private int DEFAULT_VALUE_CONTROLLER_TIMEOUT = 8000;//8000 mls
    private boolean isLiveStream;
    private View bkg;
    private RelativeLayout rootView, rlChromeCast;
    private AbstractPlayerManager uzPlayerManager;
    private ProgressBar progressBar;
    private LinearLayout llTop, debugRootView;
    private RelativeLayout rlMsg, rlLiveInfo, rlEndScreen;
    private FrameLayout previewFrameLayout;
    private UZPreviewTimeBar uzTimebar;
    private ImageView ivThumbnail, ivVideoCover, ivLogo;
    private UZTextView tvPosition, tvDuration;
    private TextView tvMsg, tvTitle, tvLiveStatus, tvLiveView, tvLiveTime;
    private UZImageButton ibFullscreenIcon, ibPauseIcon, ibPlayIcon, ibReplayIcon, ibRewIcon, ibFfwdIcon, ibBackScreenIcon, ibVolumeIcon,
            ibSettingIcon, ibCcIcon, ibPlaylistFolderIcon //playlist folder
            , ibHearingIcon, ibPictureInPictureIcon, ibSkipPreviousIcon, ibSkipNextIcon, ibSpeedIcon, ivLiveTime, ivLiveView, ibsCast;
    private TextView tvEndScreenMsg;
    private UZPlayerView uzPlayerView;
    private long startTime = -1;
    private boolean isSetUZTimebarBottom;
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
    private ProgressListener progressListener;
    private PreviewView.OnPreviewChangeListener onPreviewChangeListener;
    private UZVideoViewItemClick uzVideoViewItemClick;
    private UZCallback uzCallback;
    private UZTVFocusChangeListener uzTVFocusChangeListener;
    private UZPlayerView.ControllerStateCallback controllerStateCallback;
    //    private boolean isGetClickedPip;
    private long timestampInitDataSource;
    //=============================================================================================START EVENTBUS
    private boolean isCalledFromConnectionEventBus = false;
    //last current position lúc từ exoplayer switch sang cast player
    private long lastCurrentPosition;
    private boolean isCastPlayerPlayingFirst;
    private StatsForNerdsView statsForNerdsView;
    private UZAdPlayerCallback videoAdPlayerCallback;
    private String viewerSessionId;
    private CompositeDisposable disposables;

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


    /**
     * Call one time from {@link #onAttachedToWindow}
     * Note: you must call inflate in this method
     */
    @Override
    public void onCreateView() {
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
        return (uzPlayerView != null) && uzPlayerView.getControllerAutoShow();
    }

    public void setControllerAutoShow(boolean isAutoShowController) {
        this.isAutoShowController = isAutoShowController;
        if (uzPlayerView != null) {
            uzPlayerView.setControllerAutoShow(isAutoShowController);
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

    // Lay pixel dung cho custom UI like youtube, uzTimebar bottom of player controller
    public int getPixelAdded() {
        return isSetUZTimebarBottom ? (getHeightUZTimeBar() / 2) : 0;
    }

    //return pixel
    public int getHeightUZTimeBar() {
        return UZViewUtils.heightOfView(uzTimebar);
    }

    //The current position of playing. the window means playable region, which is all of the content if vod, and some portion of the content if live.
    @Override
    public long getCurrentPosition() {
        return (uzPlayerManager == null) ? -1 : uzPlayerManager.getCurrentPosition();
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
        return (uzPlayerManager == null) ? 0 : uzPlayerManager.getVideoProfileW();
    }

    public int getVideoProfileH() {
        return (uzPlayerManager == null) ? 0 : uzPlayerManager.getVideoProfileH();
    }

    public void setResizeMode(int resizeMode) {
        if (uzPlayerView != null) {
            try {
                uzPlayerView.setResizeMode(resizeMode);
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

    public void setPlayerControllerAlwayVisible() {
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

    @Override
    public SimpleExoPlayer getPlayer() {
        return (uzPlayerManager == null) ? null : uzPlayerManager.getPlayer();
    }

    @Override
    public void seekTo(long positionMs) {
        if (uzPlayerManager != null)
            uzPlayerManager.seekTo(positionMs);
    }

    /**
     * Play with custom playback
     *
     * @return true if not error
     */
    @Override
    public boolean play() {
        if (UZData.getInstance().getPlayback() == null) {
            Timber.e(ErrorConstant.ERR_14);
            return false;
        }
        Context context = getContext();
        if (!ConnectivityUtils.isConnected(context)) {
            Timber.e(ErrorConstant.ERR_0);
            return false;
        }
        UZPlayback playbackInfo = UZData.getInstance().getPlayback();
        initPlayback(playbackInfo, true);
        return true;
    }

    /**
     * Play with {@link UZPlayback}
     *
     * @param playback PlaybackInfo nonnull
     * @return true if not error
     */
    @Override
    public boolean play(@NonNull UZPlayback playback) {
        Context context = getContext();
        if (!ConnectivityUtils.isConnected(context)) {
            Timber.e(ErrorConstant.ERR_0);
            return false;
        }
        UZData.getInstance().setSettingPlayer(false);
        //
        UZData.getInstance().setPlayback(playback);
        post(() -> initPlayback(playback, true));
        UZData.getInstance().clear();
        return true;
    }

    /**
     * Play with {@link UZPlayback}
     *
     * @param playlist List of PlaybackInfo
     * @return true if not error
     */
    @Override
    public boolean play(List<UZPlayback> playlist) {
        // TODO: Check how to get subtitle of a custom link play, because we have no idea about entityId or appId
        if (!ConnectivityUtils.isConnected(getContext())) {
            notifyError(ErrorUtils.exceptionNoConnection());
            return false;
        }
        if (ListUtils.isEmpty(playlist)) {
            Timber.d("initPlaylist::playlist is Empty");
            return false;
        } else {
            UZData.getInstance().clearDataForPlaylistFolder();
            UZData.getInstance().setPlayList(playlist);
            playPlaylistPosition(UZData.getInstance().getCurrentPositionOfPlayList());
        }
        isHasError = false;
        return true;
    }


    @Override
    public void resume() {
        TmpParamData.getInstance().setPlayerIsPaused(false);
        if (isCastingChromecast) {
            Casty casty = UZData.getInstance().getCasty();
            if (casty != null)
                casty.getPlayer().play();
        } else if (uzPlayerManager != null) {
            uzPlayerManager.resume();
        }
        UZViewUtils.goneViews(ibPlayIcon);
        if (ibPauseIcon != null) {
            UZViewUtils.visibleViews(ibPauseIcon);
            ibPauseIcon.requestFocus();
        }
    }

    @Override
    public void pause() {
        TmpParamData.getInstance().setPlayerIsPaused(true);
        if (isCastingChromecast) {
            Casty casty = UZData.getInstance().getCasty();
            if (casty != null)
                casty.getPlayer().pause();
        } else if (uzPlayerManager != null) {
            uzPlayerManager.pause();
        }
        UZViewUtils.goneViews(ibPauseIcon);
        if (ibPlayIcon != null) {
            UZViewUtils.visibleViews(ibPlayIcon);
            ibPlayIcon.requestFocus();
        }
        // tracking here
    }

    @Override
    public int getVideoWidth() {
        return (uzPlayerManager == null) ? 0 : uzPlayerManager.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return (uzPlayerManager == null) ? 0 : uzPlayerManager.getVideoHeight();
    }

    private void initPlayback(@NonNull UZPlayback playback, boolean isClearDataPlaylistFolder) {
        if (isClearDataPlaylistFolder) {
            UZData.getInstance().clearDataForPlaylistFolder();
        }
        timestampBeforeInitNewSession = System.currentTimeMillis();
        isCalledFromChangeSkin = false;
        handlePlayPlayListFolderUI();
        hideLayoutMsg();
        setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
        isOnPlayerEnded = false;
        updateUIEndScreen();
        isHasError = false;
        viewerSessionId = UUID.randomUUID().toString();
        this.isLiveStream = playback.isLive();
        if (isLiveStream)
            startTime = -1;
        if (uzPlayerManager != null) {
            releaseUZPlayerManager();
            resetCountTryLinkPlayError();
            showProgress();
        }
        updateUIDependOnLivestream();
        initDataSource(playback.getLinkPlay(), UZData.getInstance().getUrlIMAAd(), playback.getThumbnail());
        if (uzCallback != null)
            uzCallback.isInitResult(false, UZData.getInstance().getPlayback());
        initUZPlayerManager();
        disposables = new CompositeDisposable();
        trackWatchingTimer(true);
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
        if (isLiveStream) {
            // try to play 5 times
            if (countTryLinkPlayError >= 5) {
                if (uzLiveContentCallback != null)
                    uzLiveContentCallback.onLiveStreamUnAvailable();
                return;
            }
            // if entity is livestreaming, dont try to next link play
            Timber.e("tryNextLinkPlay isLivestream true -> try to replay = count %d", countTryLinkPlayError);
            if (uzPlayerManager != null) {
                uzPlayerManager.initWithoutReset();
                uzPlayerManager.setRunnable();
            }
            countTryLinkPlayError++;
            return;
        }
        countTryLinkPlayError++;
        releaseUZPlayerManager();
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
        releasePlayerAnalytic();
        releaseUZPlayerManager();
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

    private void releasePlayerAnalytic() {
        if (getPlayer() != null)
            getPlayer().removeAnalyticsListener(statsForNerdsView);
    }

    private void releaseUZPlayerManager() {
        if (uzPlayerManager != null)
            uzPlayerManager.release();
    }

    public void onResumeView() {
        SensorOrientationChangeNotifier.getInstance(getContext()).addListener(this);
        if (isCastingChromecast)
            return;
        activityIsPausing = false;
        if (uzPlayerManager != null) {
            if (ibPlayIcon == null || ibPlayIcon.getVisibility() != VISIBLE)
                uzPlayerManager.resume();
        }
        // try to move to the edge of livestream video
        if (autoMoveToLiveEdge && isLiveStream())
            seekToLiveEdge();
        else if (positionPIPPlayer > 0L && isInPipMode) {
            seekTo(positionPIPPlayer);
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
        if (isLiveStream() && getPlayer() != null)
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
        if (uzPlayerManager != null && !enablePIP())
            uzPlayerManager.pause();
    }

    public boolean enablePIP() {
        return (ibPictureInPictureIcon != null) && (ibPictureInPictureIcon.getVisibility() == VISIBLE) && UZAppUtils.hasSupportPIP(getContext());
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
        if (uzPlayerManager != null && !isCastingChromecast) {
            uzPlayerManager.seekTo(progress);
            uzPlayerManager.resume();
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
            UZViewUtils.goneViews(ibPictureInPictureIcon);
        } else {
            UZViewUtils.showDefaultControls((Activity) getContext());
            isLandscape = false;
            UZViewUtils.setUIFullScreenIcon(ibFullscreenIcon, false);
            if (!isCastingChromecast() && UZAppUtils.hasSupportPIP(getContext()))
                UZViewUtils.visibleViews(ibPictureInPictureIcon);
        }
        TmpParamData.getInstance().setPlayerIsFullscreen(isLandscape);
        setMarginPreviewTimeBar();
        setMarginRlLiveInfo();
        updateUISizeThumbnail();
        updateUIPositionOfProgressBar();
        if (isSetUZTimebarBottom)
            setMarginDependOnUZTimeBar(uzPlayerView.getVideoSurfaceView());
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
            handleClickSetting();
        else if (v == ibCcIcon)
            handleClickCC();
        else if (v == ibPlaylistFolderIcon)
            handleClickPlaylistFolder();
        else if (v == ibHearingIcon)
            handleClickHearing();
        else if (v == ibPictureInPictureIcon)
            enterPIPMode();
        else if (v.getParent() == debugRootView)
            showUZTrackSelectionDialog(v, true);
        else if (v == rlChromeCast)
            Timber.e("dangerous to remove");
        else if (v == ibFfwdIcon) {
            if (isCastingChromecast) {
                Casty casty = UZData.getInstance().getCasty();
                if (casty != null)
                    casty.getPlayer().seekToForward(DEFAULT_VALUE_BACKWARD_FORWARD);
            } else if (uzPlayerManager != null)
                uzPlayerManager.seekToForward(DEFAULT_VALUE_BACKWARD_FORWARD);
        } else if (v == ibRewIcon) {
            if (isCastingChromecast) {
                Casty casty = UZData.getInstance().getCasty();
                if (casty != null)
                    casty.getPlayer().seekToBackward(DEFAULT_VALUE_BACKWARD_FORWARD);
            } else if (uzPlayerManager != null) {
                uzPlayerManager.seekToBackward(DEFAULT_VALUE_BACKWARD_FORWARD);
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
        if (UZAppUtils.hasSupportPIP(getContext())) {
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
        return (uzPlayerView == null) ? -1 : uzPlayerView.getControllerShowTimeoutMs();
    }

    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        DEFAULT_VALUE_CONTROLLER_TIMEOUT = controllerShowTimeoutMs;
        post(() -> uzPlayerView.setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT));
    }

    public boolean isPlayerControllerShowing() {
        return (uzPlayerView != null) && uzPlayerView.isControllerVisible();

    }

    public void showController() {
        if (uzPlayerView != null)
            uzPlayerView.showController();
    }

    public void hideController() {
        if (isPlayerControllerAlwayVisible) return;
        if (uzPlayerView != null && !isCastingChromecast)//dont hide if is casting chromecast
            uzPlayerView.hideController();
    }

    public void setHideControllerOnTouch(boolean isHide) {
        if (uzPlayerView != null) {
            this.isHideOnTouch = isHide;
            uzPlayerView.setControllerHideOnTouch(isHide);
        }
    }

    public boolean getControllerHideOnTouch() {
        return (uzPlayerView != null) && uzPlayerView.getControllerHideOnTouch();
    }

    public boolean isUseController() {
        return useController;
    }

    public void setUseController(boolean useController) {
        this.useController = useController;
        if (uzPlayerView != null)
            uzPlayerView.setUseController(useController);
    }

    protected boolean isPlayPlaylistFolder() {
        return UZData.getInstance().getPlayList() != null;
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

                    @Override
                    public void onFocusChange(UZPlayback playback, int position) {
                    }

                    public void onDismiss() {
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

    public boolean replay() {
        if (uzPlayerManager == null) return false;
        TmpParamData.getInstance().addPlayerViewCount();
        //TODO Chỗ này đáng lẽ chỉ clear value của tracking khi đảm bảo rằng seekTo(0) true
        boolean result = uzPlayerManager.seekTo(0);
        if (result) {
            isSetFirstRequestFocusDone = false;
            isOnPlayerEnded = false;
            updateUIEndScreen();
            handlePlayPlayListFolderUI();
        }
        if (isCastingChromecast)
            replayChromeCast();
        return result;
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
                    ibVolumeIcon.setImageResource(isMute ? R.drawable.ic_volume_off_white_48 : R.drawable.ic_volume_up_white_48);
            }
        } else if (uzPlayerManager != null)
            uzPlayerManager.toggleVolumeMute(ibVolumeIcon);
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
        if (ListUtils.isEmpty(uzPlayerManager.getSubtitleList())) {
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

    public int getDefaultValueBackwardForward() {
        return DEFAULT_VALUE_BACKWARD_FORWARD;
    }

    public void setDefaultValueBackwardForward(int mls) {
        DEFAULT_VALUE_BACKWARD_FORWARD = mls;
    }

    /*
     ** Seek tu vi tri hien tai cong them bao nhieu mls
     */
    public void seekToForward(int mls) {
        setDefaultValueBackwardForward(mls);
        ibFfwdIcon.performClick();
    }

    /*
     ** Seek tu vi tri hien tai tru di bao nhieu mls
     */
    public void seekToBackward(int mls) {
        setDefaultValueBackwardForward(mls);
        ibRewIcon.performClick();
    }

    //chi toggle show hide controller khi video da vao dc onStateReadyFirst();
    public void toggleShowHideController() {
        if (uzPlayerView != null)
            uzPlayerView.toggleShowHideController();
    }

    public void togglePlayPause() {
        if (uzPlayerManager == null || getPlayer() == null) return;
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

    public PlayerView getUZPlayerView() {
        return uzPlayerView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public ImageView getIvThumbnail() {
        return ivThumbnail;
    }

    public boolean isLiveStream() {
        return isLiveStream;
    }

    public AbstractPlayerManager getUZPlayerManager() {
        return uzPlayerManager;
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

    public UZImageButton getIbPictureInPictureIcon() {
        return ibPictureInPictureIcon;
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

    public TextView getTvLiveView() {
        return tvLiveView;
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

    public UZPreviewTimeBar getUZTimeBar() {
        return uzTimebar;
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
        return (uzPlayerManager == null) ? -1 : uzPlayerManager.getVolume();
    }

    public void setVolume(float volume) {
        if (uzPlayerManager != null)
            uzPlayerManager.setVolume(volume);
    }

    @Override
    public void onVisibilityChange(boolean isShow) {
        if (ivLogo != null)
            ivLogo.setClickable(!isShow);
        if (controllerStateCallback != null)
            controllerStateCallback.onVisibilityChange(isShow);
    }

    public void setSpeed(float speed) {
        if (isLiveStream)
            throw new IllegalArgumentException(getContext().getString(R.string.error_speed_live_content));
        if (speed > 3 || speed < -3)
            throw new IllegalArgumentException(getContext().getString(R.string.error_speed_illegal));
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
        uzPlayerView.setControllerStateCallback(this);
        uzTimebar = uzPlayerView.findViewById(R.id.exo_progress);
        previewFrameLayout = uzPlayerView.findViewById(R.id.preview_frame_layout);
        if (uzTimebar != null) {
            if (uzTimebar.getTag() == null) {
                isSetUZTimebarBottom = false;
                uzPlayerView.setVisibility(VISIBLE);
            } else {
                if (uzTimebar.getTag().toString().equals(getContext().getString(R.string.use_bottom_uz_timebar))) {
                    isSetUZTimebarBottom = true;
                    setMarginDependOnUZTimeBar(uzPlayerView.getVideoSurfaceView());
                } else {
                    isSetUZTimebarBottom = false;
                    uzPlayerView.setVisibility(VISIBLE);
                }
            }
            uzTimebar.addOnPreviewChangeListener(this);
            uzTimebar.setOnFocusChangeListener(this);
        } else
            uzPlayerView.setVisibility(VISIBLE);
        ivThumbnail = uzPlayerView.findViewById(R.id.image_view_thumbnail);
        tvPosition = uzPlayerView.findViewById(R.id.uz_position);
        if (tvPosition != null) {
            tvPosition.setText(StringUtils.convertMlsecondsToHMmSs(0));
        }
        tvDuration = uzPlayerView.findViewById(R.id.uz_duration);
        if (tvDuration != null) {
            tvDuration.setText("-:-");
        }
        ibFullscreenIcon = uzPlayerView.findViewById(R.id.exo_fullscreen_toggle_icon);
        tvTitle = uzPlayerView.findViewById(R.id.tv_title);
        ibPauseIcon = uzPlayerView.findViewById(R.id.exo_pause);
        ibPlayIcon = uzPlayerView.findViewById(R.id.exo_play);
        //If auto start true, show button play and gone button pause
        UZViewUtils.goneViews(ibPlayIcon);
        ibReplayIcon = uzPlayerView.findViewById(R.id.exo_replay);
        ibRewIcon = uzPlayerView.findViewById(R.id.exo_rew);
        if (ibRewIcon != null)
            ibRewIcon.setSrcDrawableDisabled();
        ibFfwdIcon = uzPlayerView.findViewById(R.id.exo_ffwd);
        ibBackScreenIcon = uzPlayerView.findViewById(R.id.exo_back_screen);
        ibVolumeIcon = uzPlayerView.findViewById(R.id.exo_volume);
        ibSettingIcon = uzPlayerView.findViewById(R.id.exo_setting);
        ibCcIcon = uzPlayerView.findViewById(R.id.exo_cc);
        ibPlaylistFolderIcon = uzPlayerView.findViewById(R.id.exo_playlist_folder);
        ibHearingIcon = uzPlayerView.findViewById(R.id.exo_hearing);
        ibPictureInPictureIcon = uzPlayerView.findViewById(R.id.exo_picture_in_picture);
        ibSkipNextIcon = uzPlayerView.findViewById(R.id.exo_skip_next);
        ibSkipPreviousIcon = uzPlayerView.findViewById(R.id.exo_skip_previous);
        ibSpeedIcon = uzPlayerView.findViewById(R.id.exo_speed);
        if (!UZAppUtils.hasSupportPIP(getContext()))
            UZViewUtils.goneViews(ibPictureInPictureIcon);
        LinearLayout debugLayout = findViewById(R.id.debug_layout);
        debugRootView = findViewById(R.id.controls_root);
        if (BuildConfig.DEBUG) {
            debugLayout.setVisibility(View.VISIBLE);
        } else {
            debugLayout.setVisibility(View.GONE);
        }
        rlLiveInfo = uzPlayerView.findViewById(R.id.rl_live_info);
        tvLiveStatus = uzPlayerView.findViewById(R.id.tv_live);
        tvLiveView = uzPlayerView.findViewById(R.id.tv_live_view);
        tvLiveTime = uzPlayerView.findViewById(R.id.tv_live_time);
        ivLiveView = uzPlayerView.findViewById(R.id.iv_live_view);
        ivLiveTime = uzPlayerView.findViewById(R.id.iv_live_time);
        UZViewUtils.setFocusableViews(false, ivLiveView, ivLiveTime);
        rlEndScreen = uzPlayerView.findViewById(R.id.rl_end_screen);
        UZViewUtils.goneViews(rlEndScreen);
        tvEndScreenMsg = uzPlayerView.findViewById(R.id.tv_end_screen_msg);
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
                ibCcIcon, ibPlaylistFolderIcon, ibHearingIcon, ibPictureInPictureIcon, ibFfwdIcon,
                ibRewIcon, ibPlayIcon, ibPauseIcon, ibReplayIcon, ibSkipNextIcon, ibSkipPreviousIcon, ibSpeedIcon);
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
        if (TextUtils.isEmpty(urlImgThumbnail))
            return;
        if (ivVideoCover == null)
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
        if (ivVideoCover.getVisibility() != GONE) {
            ivVideoCover.setVisibility(GONE);
            ivVideoCover.invalidate();
            if (isLiveStream) {
                if (tvLiveTime != null)
                    tvLiveTime.setText(HYPHEN);
                if (tvLiveView != null)
                    tvLiveView.setText(HYPHEN);
            }
            if (!isFromHandleError)
                onStateReadyFirst();
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
        if (uzPlayerView == null || progressBar == null)
            return;
        uzPlayerView.post(() -> {
            int marginL = uzPlayerView.getMeasuredWidth() / 2 - progressBar.getMeasuredWidth() / 2;
            int marginT = uzPlayerView.getMeasuredHeight() / 2 - progressBar.getMeasuredHeight() / 2;
            UZViewUtils.setMarginPx(progressBar, marginL, marginT, 0, 0);
        });
    }

    private void addPlayerView() {
        uzPlayerView = null;
        int skinId = UZData.getInstance().getUZPlayerSkinLayoutId();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            uzPlayerView = (UZPlayerView) inflater.inflate(skinId, null);
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            uzPlayerView.setLayoutParams(lp);
            uzPlayerView.setVisibility(GONE);
            rootView.addView(uzPlayerView);
            setControllerAutoShow(isAutoShowController);
        } else
            Timber.e("inflater is null");
    }

    /*
     ** change skin of player (realtime)
     * return true if success
     */
    public boolean changeSkin(@LayoutRes int skinId) {
        if (uzPlayerManager == null) return false;
        if (UZData.getInstance().isUseUZDragView())
            throw new IllegalArgumentException(getContext().getString(R.string.error_change_skin_with_uzdragview));
        if (uzPlayerManager.isPlayingAd()) {
            notifyError(ErrorUtils.exceptionChangeSkin());
            return false;
        }
        UZData.getInstance().setUZPlayerSkinLayoutId(skinId);
        isRefreshFromChangeSkin = true;
        isCalledFromChangeSkin = true;
        rootView.removeView(uzPlayerView);
        rootView.requestLayout();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            uzPlayerView = (UZPlayerView) inflater.inflate(skinId, null);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            uzPlayerView.setLayoutParams(lp);
            rootView.addView(uzPlayerView);
            rootView.requestLayout();
            findViews();
            resizeContainerView();
            updateUIEachSkin();
            setMarginPreviewTimeBar();
            setMarginRlLiveInfo();
            //setup chromecast
            if (UZAppUtils.checkChromeCastAvailable())
                setupChromeCast();
            currentPositionBeforeChangeSkin = getCurrentPosition();
            releaseUZPlayerManager();
            updateUIDependOnLivestream();
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
        uzChromeCast.setUZChromeCastListener(new UZChromeCast.UZChromeCastListener() {
            @Override
            public void onConnected() {
                if (uzPlayerManager != null)
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
                    llTop.addView(uzChromeCast.getUZMediaRouteButton());
                addUIChromecastLayer();
            }
        });
        uzChromeCast.setupChromeCast(getContext());
    }

    private void updateTvDuration() {
        if (tvDuration != null)
            if (isLiveStream)
                tvDuration.setText(StringUtils.convertMlsecondsToHMmSs(0));
            else
                tvDuration.setText(StringUtils.convertMlsecondsToHMmSs(getDuration()));
    }

    public void setProgressSeekBar(@NonNull UZVerticalSeekBar uzVerticalSeekBar, int progressSeekBar) {
        uzVerticalSeekBar.setProgress(progressSeekBar);
    }

    private void setTextPosition(long currentMls) {
        if (tvPosition == null) return;
        if (isLiveStream) {
            long duration = getDuration();
            long past = duration - currentMls;
            tvPosition.setText(HYPHEN + StringUtils.convertMlsecondsToHMmSs(past));
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
        if (isLiveStream) return;
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
        if (uzTimebar != null)
            if (isLandscape)
                UZViewUtils.setMarginDimen(uzTimebar, 5, 0, 5, 0);
            else
                UZViewUtils.setMarginDimen(uzTimebar, 0, 0, 0, 0);
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

    private void updateUIDependOnLivestream() {
        if (isCastingChromecast)
            UZViewUtils.goneViews(ibPictureInPictureIcon);
        else if (UZAppUtils.isTablet(getContext()) && UZAppUtils.isTV(getContext()))//only hide ibPictureInPictureIcon if device is TV
            UZViewUtils.goneViews(ibPictureInPictureIcon);

        if (isLiveStream) {
            UZViewUtils.visibleViews(rlLiveInfo);
            //TODO why set gone not work?
            setUIVisible(false, ibSpeedIcon, ibRewIcon, ibFfwdIcon);
        } else {
            UZViewUtils.goneViews(rlLiveInfo);
            //TODO why set visible not work?
            setUIVisible(true, ibSpeedIcon, ibRewIcon, ibFfwdIcon);
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
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = uzPlayerManager.getTrackSelector().getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null)
            return;
        for (int i = 0; i < mappedTrackInfo.getRendererCount(); i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                Button button = new Button(getContext());
                button.setSoundEffectsEnabled(false);
                int label;
                switch (uzPlayerManager.getPlayer().getRendererType(i)) {
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
        if (!isLiveStream || getContext() == null) return;
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
            Timber.i("Video or Stream is ended !");
            setVisibilityOfPlayPauseReplay(true);
            showController();
            if (uzPlayerView != null) {
                uzPlayerView.setControllerShowTimeoutMs(0);
                uzPlayerView.setControllerHideOnTouch(false);
            }
        } else {
            setVisibilityOfPlayPauseReplay(false);
            if (uzPlayerView != null)
                uzPlayerView.setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
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
        //Có play kiểu gì đi nữa thì cũng phải ibPlayIcon GONE và ibPauseIcon VISIBLE và ibReplayIcon GONE
        setVisibilityOfPlayPauseReplay(false);
    }

    public void hideUzTimebar() {
        UZViewUtils.goneViews(previewFrameLayout, ivThumbnail, uzTimebar);
    }

    private List<UZItem> showUZTrackSelectionDialog(final View view, boolean showDialog) {
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = uzPlayerManager.getTrackSelector().getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            CharSequence title = ((Button) view).getText();
            int rendererIndex = (int) view.getTag();
            final Pair<AlertDialog, UZTrackSelectionView> dialogPair = UZTrackSelectionView.getDialog(getContext(), title, uzPlayerManager.getTrackSelector(), rendererIndex);
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

    public void setUZTimebarBottom() {
        if (uzPlayerView == null)
            throw new NullPointerException("uzPlayerView cannot be null");
        if (uzTimebar == null)
            throw new NullPointerException("uzTimebar cannot be null");
        if (uzPlayerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
    }

    public int getHeightUZVideo() {
        if (rootView == null) {
            return 0;
        }
        if (isSetUZTimebarBottom) {
            int hRootView = UZViewUtils.heightOfView(rootView);
            int hUZTimebar = getHeightUZTimeBar();
            return hRootView - hUZTimebar / 2;
        } else
            return UZViewUtils.heightOfView(rootView);
    }

    public void setBackgroundColorBkg(int color) {
        if (bkg != null)
            bkg.setBackgroundColor(color);
    }

    public void setBackgroundColorUZVideoRootView(int color) {
        RelativeLayout uzVideoRootView = findViewById(R.id.root_view_uz_video);
        if (uzVideoRootView != null)
            uzVideoRootView.setBackgroundColor(color);
    }

    public void setMarginDependOnUZTimeBar(View view) {
        if (view == null || uzTimebar == null) return;
        int heightUZTimebar;
        if (isLandscape)
            UZViewUtils.setMarginPx(view, 0, 0, 0, 0);
        else {
            heightUZTimebar = getHeightUZTimeBar();
            UZViewUtils.setMarginPx(view, 0, 0, 0, heightUZTimebar / 2);
        }
    }

    public void hideProgress() {
        if (uzPlayerManager != null)
            uzPlayerManager.hideProgress();
    }

    public void showProgress() {
        if (uzPlayerManager != null)
            uzPlayerManager.showProgress();
    }

    private void updateUIPlayerInfo() {
        if (uzPlayerView == null) return;
        UZPlayback info = UZData.getInstance().getPlayback();
        if (info == null) return;
        if (TextUtils.isEmpty(info.getLogo()))
            return;
        ivLogo = new ImageView(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ConvertUtils.dp2px(50f), ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        if (uzPlayerView.getOverlayFrameLayout() != null)
            uzPlayerView.getOverlayFrameLayout().addView(ivLogo, layoutParams);
        ivLogo.setOnClickListener(this);
        ImageUtils.load(ivLogo, info.getLogo());
    }

    /**
     * @param uzLiveContentCallback
     */
    public void setUZLiveContentCallback(UZLiveContentCallback uzLiveContentCallback) {
        this.uzLiveContentCallback = uzLiveContentCallback;
    }

    /**
     * @param uzCallback
     */
    public void setUZCallback(UZCallback uzCallback) {
        this.uzCallback = uzCallback;
    }

    public void setUizaTVFocusChangeListener(UZTVFocusChangeListener uizaTVFocusChangeListener) {
        this.uzTVFocusChangeListener = uizaTVFocusChangeListener;
        handleFirstViewHasFocus();
    }

    public void setProgressListener(ProgressListener progressListener) {
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
        if (uzPlayerView != null)
            uzPlayerView.setOnTouchEvent(onTouchEvent);
    }

    public void setOnSingleTap(UZPlayerView.OnSingleTap onSingleTap) {
        if (uzPlayerView != null)
            uzPlayerView.setOnSingleTap(onSingleTap);
    }

    public void setOnDoubleTap(UZPlayerView.OnDoubleTap onDoubleTap) {
        if (uzPlayerView != null)
            uzPlayerView.setOnDoubleTap(onDoubleTap);
    }

    public void setOnLongPressed(UZPlayerView.OnLongPressed onLongPressed) {
        if (uzPlayerView != null)
            uzPlayerView.setOnLongPressed(onLongPressed);
    }

    public void setAudioListener(AudioListener audioListener) {
        this.audioListener = audioListener;
    }

    public void setPlayerEventListener(Player.EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void setVideoListener(VideoListener videoListener) {
        this.videoListener = videoListener;
    }

    public void setMetadataOutput(MetadataOutput metadataOutput) {
        this.metadataOutput = metadataOutput;
    }

    public void setTextOutput(TextOutput textOutput) {
        this.textOutput = textOutput;
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
        isLiveStream = playback.isLive();
        if (uzPlayerManager != null) {
            releaseUZPlayerManager();
            resetCountTryLinkPlayError();
            showProgress();
        }
        setTitle();
    }

    private void checkToSetUpResource() {
        UZPlayback playbackInfo = UZData.getInstance().getPlayback();
        if (playbackInfo != null) {
            List<String> listLinkPlay = playbackInfo.getLinkPlays();
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
                    playbackInfo.getThumbnail());
            if (uzCallback != null)
                uzCallback.isInitResult(false, playbackInfo);
            initUZPlayerManager();
        } else
            handleError(ErrorUtils.exceptionSetup());
    }

    private void initDataSource(String linkPlay, String urlIMAAd, String urlThumbnailsPreviewSeekBar) {
        // hide the cc (subtitle) button
        UZViewUtils.goneViews(ibCcIcon);
        timestampInitDataSource = System.currentTimeMillis();
        Timber.d("-------------------->initDataSource linkPlay %s", linkPlay);
        TmpParamData.getInstance().setEntitySourceUrl(linkPlay);
        uzPlayerManager = new UZPlayerManager(this, linkPlay, urlIMAAd, urlThumbnailsPreviewSeekBar);
        ((UZPlayerManager) uzPlayerManager).addAdPlayerCallback(new UZAdPlayerCallback() {
            @Override
            public void onPlay() {
                updateTvDuration();
                if (videoAdPlayerCallback != null) videoAdPlayerCallback.onPlay();
            }

            @Override
            public void onVolumeChanged(int i) {
                if (videoAdPlayerCallback != null) videoAdPlayerCallback.onVolumeChanged(i);
            }

            @Override
            public void onPause() {
                if (videoAdPlayerCallback != null) videoAdPlayerCallback.onPause();
            }

            @Override
            public void onLoaded() {
                if (videoAdPlayerCallback != null) videoAdPlayerCallback.onLoaded();
            }

            @Override
            public void onResume() {
                if (videoAdPlayerCallback != null) videoAdPlayerCallback.onResume();
            }

            @Override
            public void onEnded() {
                updateTvDuration();
                if (videoAdPlayerCallback != null) videoAdPlayerCallback.onEnded();
            }

            @Override
            public void onError() {
                updateTvDuration();
                if (videoAdPlayerCallback != null) videoAdPlayerCallback.onError();
            }

            @Override
            public void onBuffering() {
                if (videoAdPlayerCallback != null) videoAdPlayerCallback.onBuffering();
            }
        });
        if (uzTimebar != null) {
            boolean disable = TextUtils.isEmpty(urlThumbnailsPreviewSeekBar);
            uzTimebar.setEnabled(!disable);
            uzTimebar.setPreviewLoader(uzPlayerManager);
        }
        uzPlayerManager.setProgressListener(new VideoViewBase.ProgressListener() {
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
                updateUIIbRewIconDependOnProgress(currentMls, false);
                Timber.e("onVideoProgress: %d, %d, %d, %d", currentMls, s, duration, percent);
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
                Timber.e("onBufferProgress: %d, %d, %d", bufferedPosition, bufferedPercentage, duration);
                if (progressListener != null)
                    progressListener.onBufferProgress(bufferedPosition, bufferedPercentage, duration);
            }
        });
        uzPlayerManager.setDebugCallback(this::updateUIButtonVisibilities);

        uzPlayerManager.setBufferCallback((bufferedDurationUs, playbackSpeed) -> statsForNerdsView.setBufferedDurationUs(bufferedDurationUs));
    }

    protected void onStateReadyFirst() {
        long pageLoadTime = System.currentTimeMillis() - timestampBeforeInitNewSession;
        TmpParamData.getInstance().setPageLoadTime(pageLoadTime);
        TmpParamData.getInstance().setViewStart(System.currentTimeMillis());
        TmpParamData.getInstance().setViewTimeToFirstFrame(System.currentTimeMillis());
        updateTvDuration();
        updateUIButtonPlayPauseDependOnIsAutoStart();
        updateUIDependOnLivestream();
        if (isSetUZTimebarBottom)
            UZViewUtils.visibleViews(uzPlayerView);
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

    private void initUZPlayerManager() {
        if (uzPlayerManager != null) {
            uzPlayerManager.init(this);
            if (isRefreshFromChangeSkin) {
                uzPlayerManager.seekTo(currentPositionBeforeChangeSkin);
                isRefreshFromChangeSkin = false;
                currentPositionBeforeChangeSkin = 0;
            }
            if (isCalledFromConnectionEventBus) {
                uzPlayerManager.setRunnable();
                isCalledFromConnectionEventBus = false;
            }
            // Always using this options
            initStatsForNerds();
        }
    }
    //=============================================================================================END EVENTBUS

    //=============================================================================================START CHROMECAST

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN_ORDERED)
    public void onMessageEvent(EventBusData.ConnectEvent event) {
        if (event == null || uzPlayerManager == null) return;
        if (!event.isConnected()) notifyError(ErrorUtils.exceptionNoConnection());
        else {
            if (uzPlayerManager.getExoPlaybackException() == null) {
                hideController();
                hideLayoutMsg();
            } else {
                isCalledFromConnectionEventBus = true;
                uzPlayerManager.setResumeIfConnectionError();
                if (!activityIsPausing) {
                    uzPlayerManager.init(this);
                    if (isCalledFromConnectionEventBus) {
                        uzPlayerManager.setRunnable();
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
        if (UZData.getInstance().getPlayback() == null || uzPlayerManager == null || uzPlayerManager.getPlayer() == null) {
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

        MediaInfo mediaInfo = new MediaInfo.Builder(uzPlayerManager.getLinkPlay())
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
                    uzPlayerManager.seekTo(currentPosition);
            }, 1000);
        }

    }

    /* khi click vào biểu tượng casting
     * thì sẽ pause local player và bắt đầu loading lên cast player
     * khi disconnect thì local player sẽ resume*/
    private void updateUIChromecast() {
        if (uzPlayerManager == null || rlChromeCast == null || UZAppUtils.isTV(getContext()))
            return;
        if (isCastingChromecast) {
            uzPlayerManager.pause();
            uzPlayerManager.setVolume(0f);
            UZViewUtils.visibleViews(rlChromeCast, ibPlayIcon);
            UZViewUtils.goneViews(ibPauseIcon);
            //casting player luôn play first với volume not mute
            //UizaData.getInstance().getCasty().setVolume(0.99);
            if (uzPlayerView != null)
                uzPlayerView.setControllerShowTimeoutMs(0);
        } else {
            uzPlayerManager.resume();
            uzPlayerManager.setVolume(0.99f);
            UZViewUtils.goneViews(rlChromeCast, ibPlayIcon);
            UZViewUtils.visibleViews(ibPauseIcon);
            //TODO iplm volume mute on/off o cast player
            //khi quay lại exoplayer từ cast player thì mặc định sẽ bật lại âm thanh (dù cast player đang mute hay !mute)
            //uzPlayerManager.setVolume(0.99f)
            if (uzPlayerView != null)
                uzPlayerView.setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
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
        getPlayer().addAnalyticsListener(statsForNerdsView);
    }

    public UZChromeCast getUZChromeCast() {
        return uzChromeCast;
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
        if (UZAppUtils.isAdsDependencyAvailable())
            this.videoAdPlayerCallback = uzAdPlayerCallback;
        else
            throw new NoClassDefFoundError(ErrorConstant.ERR_506);
    }

    protected void updateLiveStreamLatency(long latency) {
        statsForNerdsView.showTextLiveStreamLatency();
        statsForNerdsView.setTextLiveStreamLatency(StringUtils.groupingSeparatorLong(latency));
    }

    protected void hideTextLiveStreamLatency() {
        statsForNerdsView.hideTextLiveStreamLatency();
    }

    private void trackWatchingTimer(boolean firstRun) {
        UZAnalyticInfo ai = UZData.getInstance().getAnalyticInfo();
        if (ai != null && handler != null) {
            UZTrackingData data = new UZTrackingData(ai, viewerSessionId);
            handler.postDelayed(() -> trackWatching(data)
                    , firstRun ? 0 : 5000); // 5s
        } else {
            Timber.e("Do not track watching");
        }
    }

    private void trackWatching(final UZTrackingData data) {
        if (isPlaying()) {
            disposables.add(UZAnalytic.pushEvent(data, responseBody -> {
                Timber.d("send track watching: %s, response: %s", viewerSessionId, responseBody.string());
            }, error -> {
                Timber.e("send track watching error: %s", error.getMessage());
            }, () -> {
                // onComplete
                trackWatchingTimer(false);
            }));
        } else {
            trackWatchingTimer(false);
        }
    }
}
