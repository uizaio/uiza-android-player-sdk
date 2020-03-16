package com.uiza.sdk.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
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
import com.uiza.sdk.R;
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
import com.uiza.sdk.floatview.UZFloatVideoService;
import com.uiza.sdk.interfaces.UZAdPlayerCallback;
import com.uiza.sdk.interfaces.UZCallback;
import com.uiza.sdk.interfaces.UZLiveContentCallback;
import com.uiza.sdk.interfaces.UZVideoViewItemClick;
import com.uiza.sdk.listerner.UZTVFocusChangeListener;
import com.uiza.sdk.models.UZPlaybackInfo;
import com.uiza.sdk.observers.SensorOrientationChangeNotifier;
import com.uiza.sdk.observers.UZConnectifyService;
import com.uiza.sdk.util.ConnectivityUtils;
import com.uiza.sdk.util.Constants;
import com.uiza.sdk.util.ConvertUtils;
import com.uiza.sdk.util.DebugUtils;
import com.uiza.sdk.util.ImageUtils;
import com.uiza.sdk.util.ListUtils;
import com.uiza.sdk.util.LocalData;
import com.uiza.sdk.util.StringUtils;
import com.uiza.sdk.util.TmpParamData;
import com.uiza.sdk.util.UZAppUtils;
import com.uiza.sdk.util.UZData;
import com.uiza.sdk.util.UZViewUtils;
import com.uiza.sdk.widget.UZPreviewTimeBar;
import com.uiza.sdk.widget.autosize.UZImageButton;
import com.uiza.sdk.widget.autosize.UZTextView;
import com.uiza.sdk.widget.previewseekbar.PreviewView;
import com.uiza.sdk.widget.seekbar.UZVerticalSeekBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by loitp on 2/27/2019.
 */

public class UZVideoView extends VideoViewBase
        implements PreviewView.OnPreviewChangeListener, View.OnClickListener, View.OnFocusChangeListener,
        UZPlayerView.ControllerStateCallback, SensorOrientationChangeNotifier.Listener {
    private static final String M3U8_EXTENSION = ".m3u8";
    private static final String MPD_EXTENSION = ".mpd";
    private static final String PLAY_THROUGH_100 = "100";
    private static final String PLAY_THROUGH_75 = "75";
    private static final String PLAY_THROUGH_50 = "50";
    private static final String PLAY_THROUGH_25 = "25";
    private static final String HYPHEN = "-";
    private final int DELAY_FIRST_TO_GET_LIVE_INFORMATION = 100;
    private final int DELAY_TO_GET_LIVE_INFORMATION = 15000;
    //===================================================================START FOR PLAYLIST/FOLDER
    private final int pfLimit = 100;
    private final String pfOrderBy = "createdAt";
    private final String pfOrderType = "DESC";
    private final String publishToCdn = "success";
    protected AudioListener audioListener;
    protected MetadataOutput metadataOutput;
    protected Player.EventListener eventListener;
    protected VideoListener videoListener;
    protected TextOutput textOutput;
    Handler handler = new Handler();
    private int DEFAULT_VALUE_BACKWARD_FORWARD = 10000;//10000 mls
    private int DEFAULT_VALUE_CONTROLLER_TIMEOUT = 8000;//8000 mls
    private boolean isLivestream;
    private View bkg;
    private RelativeLayout rootView, rlChromeCast;
    private IUizaPlayerManager uzPlayerManager;
    private ProgressBar progressBar;
    private LinearLayout llTop, debugRootView;
    private RelativeLayout rlMsg, rlLiveInfo, rlEndScreen;
    private FrameLayout previewFrameLayout;
    private UZPreviewTimeBar uzTimebar;
    private ImageView ivThumbnail, ivVideoCover, ivLogo;
    private UZTextView tvPosition, tvDuration;
    private TextView tvMsg, tvTitle, tvLiveStatus, tvLiveView, tvLiveTime;
    private UZImageButton ibFullscreenIcon, ibPauseIcon, ibPlayIcon, ibReplayIcon, ibRewIcon, ibFfwdIcon, ibBackScreenIcon, ibVolumeIcon,
            ibSettingIcon, ibCcIcon, ibPlaylistFolderIcon//danh sach playlist folder
            , ibHearingIcon, ibPictureInPictureIcon, ibSkipPreviousIcon, ibSkipNextIcon, ibSpeedIcon, ivLiveTime, ivLiveView, ibsCast;
    private TextView tvEndScreenMsg;
    private UZPlayerView uzPlayerView;
    //    private ResultGetLinkPlay mResultGetLinkPlay;
//    private ResultGetTokenStreaming mResultGetTokenStreaming;
    private String urlIMAAd = null;
    private long startTime = -1;
    private boolean isSetUZTimebarBottom;
    private UZChromeCast uZChromeCast;
    private boolean isCastingChromecast = false;
    private boolean autoMoveToLiveEdge;
    private @LayoutRes
    int pipControlSkin;
    //========================================================================START CONFIG
    private boolean isAutoStart = Constants.DF_PLAYER_IS_AUTO_START;
    private boolean isAutoSwitchItemPlaylistFolder = true;
    private boolean isAutoShowController;
    private boolean isFreeSize;
    private boolean isPlayerControllerAlwayVisible;
    private boolean isSetFirstRequestFocusDone;
    private boolean isHasError;
    private long timestampBeforeInitNewSession;
    private boolean isInitCustomLinkPlay;//user pass any link (not use entityId or metadataId)
    private int countTryLinkPlayError = 0;
    private boolean activityIsPausing = false;
    private long timestampOnStartPreview;
    private boolean isOnPreview;
    private long maxSeekLastDuration;
    private boolean isLandscape;//current screen is landscape or portrait
    private boolean isInitMiniPlayerSuccess = true;
    private boolean isHideOnTouch = true;
    private boolean isDefaultUseController = true;
    private int pfPage = 0;
    private int pfTotalPage = Integer.MAX_VALUE;
    private boolean isOnPlayerEnded;
    private boolean isClickedSkipNextOrSkipPrevious;
    private String urlImgThumbnail;
    //========================================================================END CONFIG
    /*
     **Change skin via skin id resources
     * changeSkin(R.layout.uz_player_skin_1);
     */
    //TODO improve this func
    private boolean isRefreshFromChangeSkin;
    private long currentPositionBeforeChangeSkin;
    private boolean isCalledFromChangeSkin;
    private View firstViewHasFocus;
    private int progressBarColor = Color.WHITE;
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
    private boolean isGetClickedPip;
    private long timestampInitDataSource;
    //=============================================================================================START EVENTBUS
    private boolean isCalledFromConnectionEventBus = false;
    private long positionMiniPlayer;
    //last current position lúc từ exoplayer switch sang cast player
    private long lastCurrentPosition;
    private boolean isCastPlayerPlayingFirst;
    private StatsForNerdsView statsForNerdsView;
    private UZAdPlayerCallback videoAdPlayerCallback;

    public UZVideoView(Context context) {
        super(context);
        initView(null, 0);
    }

    public UZVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs, 0);
    }

    public UZVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UZVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(attrs, defStyleAttr);
    }

    /**
     * Call twice time
     * Node: Don't call inflate in this method
     */
    private void initView(AttributeSet attrs, int defStyleAttr) {
        // nothing
    }

    /**
     * Call one time from {@link #onAttachedToWindow}
     * Note: you must call inflate in this method
     */
    @Override
    public void onCreateView() {
        if (UZAppUtils.checkChromeCastAvailable()) {
            setupChromeCast();
        }
        EventBus.getDefault().register(this);
        startConnectifyService();
        inflate(getContext(), R.layout.v3_uiza_ima_video_core_rl, this);
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
        if (uzCallback != null) {
            uzCallback.onError(exception);
        }
    }

    public boolean initCustomLinkPlay() {
        Context context = getContext();
        if (UZData.getInstance().getPlaybackInfo() == null) {
            Timber.e(ErrorConstant.ERR_14);
            return false;
        }
        if (!ConnectivityUtils.isConnected(context)) {
            Timber.e(ErrorConstant.ERR_0);
            return false;
        }
        UZPlaybackInfo playbackInfo = UZData.getInstance().getPlaybackInfo();
        if (!LocalData.getClickedPip()) {
            UZAppUtils.stopMiniPlayer(context);
        }
        initPlayback(playbackInfo.getLinkPlay(), playbackInfo.isLive());
        LocalData.setIsInitPlaylistFolder(false);
        return true;
    }

    private void handlePlayPlayListFolderUI() {
        if (isPlayPlaylistFolder()) {
            setVisibilityOfPlaylistFolderController(VISIBLE);
        } else {
            setVisibilityOfPlaylistFolderController(GONE);
        }
    }

    @Override
    public SimpleExoPlayer getPlayer() {
        return (uzPlayerManager == null) ? null : uzPlayerManager.getPlayer();
    }

    @Override
    public void seekTo(long positionMs) {
        if (uzPlayerManager != null) {
            uzPlayerManager.seekTo(positionMs);
        }
    }

    /**
     * Play with {@link UZPlaybackInfo}
     *
     * @param playback PlaybackInfo nonnull
     * @return true if not error
     */
    @Override
    public boolean play(@NonNull UZPlaybackInfo playback) {
        Context context = getContext();
        LocalData.setClickedPip(false);
        if (!ConnectivityUtils.isConnected(context)) {
            Timber.e(ErrorConstant.ERR_0);
            return false;
        }
        UZAppUtils.stopMiniPlayer(context);
        UZData.getInstance().setSettingPlayer(false);
        //
        UZData.getInstance().setPlaybackInfo(playback);
        post(() -> initPlayback(playback.getLinkPlay(), playback.isLive()));
        LocalData.setIsInitPlaylistFolder(false);
        UZData.getInstance().clear();
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

    public void initPlayback(@NonNull String linkPlay, boolean isLiveStream) {
        Timber.d("init linkPlay %s", linkPlay);
        isInitCustomLinkPlay = true;
        isCalledFromChangeSkin = false;
        setVisibilityOfPlaylistFolderController(GONE);
        urlIMAAd = null;
        UZData.getInstance().setSettingPlayer(true);
        hideLayoutMsg();
        setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
        isOnPlayerEnded = false;
        updateUIEndScreen();
        isHasError = false;
        this.isLivestream = isLiveStream;
        if (isLivestream) {
            startTime = -1;
        }
        if (uzPlayerManager != null) {
            releaseUzPlayerManager();
            resetCountTryLinkPlayError();
            showProgress();
        }
        updateUIDependOnLivestream();
        // TODO: Check how to get subtitle of a custom link play, because we have no idea about entityId or appId
        if (!ConnectivityUtils.isConnected(getContext())) {
            notifyError(ErrorUtils.exceptionNoConnection());
            return;
        }
        initDataSource(linkPlay, UZData.getInstance().getUrlIMAAd(), UZData.getInstance().getThumbnail(), UZAppUtils.isAdsDependencyAvailable());
        if (uzCallback != null) {
            uzCallback.isInitResult(false, true, UZData.getInstance().getPlaybackInfo());
        }
        initUizaPlayerManager();
    }

    public void initPlaylistFolder(String metadataId) {
        if (metadataId == null) {
            Timber.d("initPlaylistFolder metadataId null -> called from PIP: %b", isGetClickedPip);
        } else {
            Timber.d("initPlaylistFolder metadataId %s, -> called from PIP: %b", metadataId, isGetClickedPip);
            UZData.getInstance().clearDataForPlaylistFolder();
        }
        isHasError = false;
        isClickedSkipNextOrSkipPrevious = false;
    }

    public void toggleStatsForNerds() {
        if (getPlayer() == null) return;
        boolean isEnableStatsForNerds =
                statsForNerdsView == null || statsForNerdsView.getVisibility() != View.VISIBLE;
        if (isEnableStatsForNerds) {
            UZViewUtils.visibleViews(statsForNerdsView);
        } else {
            UZViewUtils.goneViews(statsForNerdsView);
        }
    }

    protected void tryNextLinkPlay() {
        if (isLivestream) {
            // try to play 5 times
            if (countTryLinkPlayError >= 5) {
                if (uzLiveContentCallback != null) {
                    uzLiveContentCallback.onLiveStreamUnAvailable();
                }
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
        Timber.e("%s: %d", getContext().getString(R.string.cannot_play_will_try), countTryLinkPlayError);
        releaseUzPlayerManager();
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
            uzCallback.isInitResult(false, false, null);
        }
    }

    protected void resetCountTryLinkPlayError() {
        countTryLinkPlayError = 0;
    }

    public void onDestroyView() {
        //cannot use isGetClickedPip (global variable), must use UizaUtil.getClickedPip(activity)
        if (LocalData.getClickedPip()) {
            UZAppUtils.stopMiniPlayer(getContext());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getContext().stopService(new Intent(getContext(), UZConnectifyService.class));
        }
        releasePlayerAnalytic();
        releaseUzPlayerManager();
        UZData.getInstance().setSettingPlayer(false);
        isCastingChromecast = false;
        isCastPlayerPlayingFirst = false;
        EventBus.getDefault().unregister(this);
    }

    private void releasePlayerAnalytic() {
        if (getPlayer() != null) {
            getPlayer().removeAnalyticsListener(statsForNerdsView);
        }
    }

    private void releaseUzPlayerManager() {
        if (uzPlayerManager != null) {
            uzPlayerManager.release();
        }
    }

    public void onResumeView() {
        SensorOrientationChangeNotifier.getInstance(getContext()).addListener(this);
        if (isCastingChromecast) {
            return;
        }
        activityIsPausing = false;
        if (uzPlayerManager != null) {
            if (ibPlayIcon == null || ibPlayIcon.getVisibility() != VISIBLE) {
                uzPlayerManager.resume();
            }
        }
        // try to move to the edge of livestream video
        if (autoMoveToLiveEdge && isLivestream()) {
            seekToLiveEdge();
        }
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
        if (isLivestream() && getPlayer() != null) {
            getPlayer().seekToDefaultPosition();
        }
    }

    public void onPauseView() {
        activityIsPausing = true;
        SensorOrientationChangeNotifier.getInstance(getContext()).remove(this);
        if (uzPlayerManager != null) {
            uzPlayerManager.pause();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isCastingChromecast()) {
            Timber.e("Error: handleClickPictureInPicture isCastingChromecast -> return");
            return;
        }
        if (UZViewUtils.isCanOverlay(getContext())) {
            initializePiP();
        }
    }

    @Override
    public void onStartPreview(PreviewView previewView, int progress) {
        timestampOnStartPreview = System.currentTimeMillis();
        if (onPreviewChangeListener != null) {
            onPreviewChangeListener.onStartPreview(previewView, progress);
        }
    }

    @Override
    public void onPreview(PreviewView previewView, int progress, boolean fromUser) {
        isOnPreview = true;
        updateUIIbRewIconDependOnProgress(progress, true);
        if (onPreviewChangeListener != null) {
            onPreviewChangeListener.onPreview(previewView, progress, fromUser);
        }
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
        if (onPreviewChangeListener != null) {
            onPreviewChangeListener.onStopPreview(previewView, progress);
        }
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
            if (isDeviceAutoRotation && !isLandscape) {
                UZViewUtils.changeScreenLandscape((Activity) getContext(), orientation);
            }
        } else {
            if (isDeviceAutoRotation && isLandscape) {
                UZViewUtils.changeScreenPortrait((Activity) getContext());
            }
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
            if (!isCastingChromecast()) {
                UZViewUtils.visibleViews(ibPictureInPictureIcon);
            }
        }
        TmpParamData.getInstance().setPlayerIsFullscreen(isLandscape);
        setMarginPreviewTimeBar();
        setMarginRlLiveInfo();
        updateUISizeThumbnail();
        updateUIPositionOfProgressBar();
        if (isSetUZTimebarBottom) {
            setMarginDependOnUZTimeBar(uzPlayerView.getVideoSurfaceView());
        }
        if (uzCallback != null) {
            uzCallback.onScreenRotate(isLandscape);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == rlMsg) {
            AnimationUtils.play(v, Techniques.Pulse);
        } else if (v == ibFullscreenIcon) {
            toggleFullscreen();
        } else if (v == ibBackScreenIcon) {
            handleClickBackScreen();
        } else if (v == ibVolumeIcon) {
            handleClickBtVolume();
        } else if (v == ibSettingIcon) {
            handleClickSetting();
        } else if (v == ibCcIcon) {
            handleClickCC();
        } else if (v == ibPlaylistFolderIcon) {
            handleClickPlaylistFolder();
        } else if (v == ibHearingIcon) {
            handleClickHearing();
        } else if (v == ibPictureInPictureIcon) {
            handleClickPictureInPicture();
        } else if (v.getParent() == debugRootView) {
            showUZTrackSelectionDialog(v, true);
        } else if (v == rlChromeCast) {
            Timber.e("dangerous to remove");
        } else if (v == ibFfwdIcon) {
            if (isCastingChromecast) {
                Casty casty = UZData.getInstance().getCasty();
                if (casty != null)
                    casty.getPlayer().seekToForward(DEFAULT_VALUE_BACKWARD_FORWARD);
            } else {
                if (uzPlayerManager != null) {
                    uzPlayerManager.seekToForward(DEFAULT_VALUE_BACKWARD_FORWARD);
                }
            }
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
        } else if (v == ibPauseIcon) {
            pause();
        } else if (v == ibPlayIcon) {
            resume();
        } else if (v == ibReplayIcon) {
            replay();
        } else if (v == ibSkipNextIcon) {
            handleClickSkipNext();
        } else if (v == ibSkipPreviousIcon) {
            handleClickSkipPrevious();
        } else if (v == ibSpeedIcon) {
            showSpeed();
        } else if (v == tvEndScreenMsg) {
            AnimationUtils.play(v, Techniques.Pulse);
        } else if (v == ivLogo) {
            AnimationUtils.play(v, Techniques.Pulse);
            UZPlaybackInfo info = UZData.getInstance().getPlaybackInfo();
            if (info == null || TextUtils.isEmpty(info.getThumbnail())) {
                return;
            }
            UZAppUtils.openUrlInBrowser(getContext(), info.getThumbnail());
        }
        /*có trường hợp đang click vào các control thì bị ẩn control ngay lập tức, trường hợp này ta có thể xử lý khi click vào control thì reset count down để ẩn control ko
        default controller timeout là 8s, vd tới s thứ 7 bạn tương tác thì tới s thứ 8 controller sẽ bị ẩn*/
        if (isDefaultUseController) {
            if (rlMsg == null || rlMsg.getVisibility() != VISIBLE) {
                if (isPlayerControllerShowing()) {
                    showController();
                }
            }
        }
        if (uzVideoViewItemClick != null)
            uzVideoViewItemClick.onItemClick(v);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void handleClickPictureInPicture() {
        if (!isInitMiniPlayerSuccess) {
            //dang init 1 instance mini player roi, khong cho init nua
            notifyError(ErrorUtils.exceptionShowPip());
            return;
        }
        if (isCastingChromecast()) {
            notifyError(ErrorUtils.exceptionShowPip());
            return;
        }
        if (UZViewUtils.isCanOverlay(getContext())) {
            initializePiP();
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            ((Activity) getContext()).startActivityForResult(intent, Constants.CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }

    public void initializePiP() {
        if (uzPlayerManager == null || TextUtils.isEmpty(uzPlayerManager.getLinkPlay())) {
            notifyError(ErrorUtils.exceptionShowPip());
            return;
        }
        UZViewUtils.goneViews(ibPictureInPictureIcon);
        if (uzCallback != null) {
            isInitMiniPlayerSuccess = false;
            uzCallback.onStateMiniPlayer(false);
        }
        LocalData.setVideoWidth(getVideoWidth());
        LocalData.setVideoHeight(getVideoHeight());
        Intent intent = new Intent(getContext(), UZFloatVideoService.class);
        intent.putExtra(Constants.FLOAT_CONTENT_POSITION, getCurrentPosition());
        intent.putExtra(Constants.FLOAT_LINK_PLAY, uzPlayerManager.getLinkPlay());
        intent.putExtra(Constants.FLOAT_IS_LIVESTREAM, isLivestream);
        intent.putExtra(Constants.FLOAT_PROGRESS_BAR_COLOR, progressBarColor);
        intent.putExtra(Constants.FLOAT_CONTROL_SKIN_ID, pipControlSkin);
        getContext().startService(intent);
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
        if (uzPlayerView != null) {
            uzPlayerView.showController();
        }
    }

    public void hideController() {
        if (isPlayerControllerAlwayVisible) {
            return;
        }
        if (!isCastingChromecast) {//dont hide if is casting chromecast
            if (uzPlayerView != null) {
                uzPlayerView.hideController();
            }
        }
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

    public boolean isDefaultUseController() {
        return isDefaultUseController;
    }

    public void setDefaultUseController(boolean isDefaultUseController) {
        this.isDefaultUseController = isDefaultUseController;
        setUseController(this.isDefaultUseController);
    }

    protected void setUseController(final boolean isUseController) {
        if (uzPlayerView != null) {
            uzPlayerView.setUseController(isUseController);
        }
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
        urlImgThumbnail = null;
        pause();
        hideController();
        //update UI for skip next and skip previous button
        if (position == 0) {
            setSrcDrawableEnabledForViews(ibSkipPreviousIcon, ibSkipNextIcon);
        } else if (position == UZData.getInstance().getPlayList().size() - 1) {
            setSrcDrawableEnabledForViews(ibSkipPreviousIcon, ibSkipNextIcon);
        } else {
            setSrcDrawableEnabledForViews(ibSkipPreviousIcon, ibSkipNextIcon);
        }
        //set disabled prevent double click, will enable onStateReadyFirst()
        setClickableForViews(false, ibSkipPreviousIcon, ibSkipNextIcon);
        //end update UI for skip next and skip previous button
        UZData.getInstance().setCurrentPositionOfPlayList(position);
        UZPlaybackInfo data = UZData.getInstance().getDataWithPositionOfPlayList(position);
        if (data == null || TextUtils.isEmpty(data.getId())) {
            Timber.e("playPlaylistPosition error: data null or cannot get id");
            return;
        }
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
        UZPlaylistFolderDialog uizaPlaylistFolderDlg = new UZPlaylistFolderDialog(getContext(), isLandscape, UZData.getInstance().getPlayList(), UZData.getInstance().getCurrentPositionOfPlayList(), new CallbackPlaylistFolder() {
            @Override
            public void onClickItem(UZPlaybackInfo playback, int position) {
                UZData.getInstance().setCurrentPositionOfPlayList(position);
                playPlaylistPosition(position);
            }

            @Override
            public void onFocusChange(UZPlaybackInfo playback, int position) {
            }

            @Override
            public void onDismiss() {
            }
        });
        UZViewUtils.showDialog(uizaPlaylistFolderDlg);
    }

    private void handleClickSkipNext() {
        isClickedSkipNextOrSkipPrevious = true;
        isOnPlayerEnded = false;
        updateUIEndScreen();
        autoSwitchNextVideo();
    }

    private void handleClickSkipPrevious() {
        isClickedSkipNextOrSkipPrevious = true;
        isOnPlayerEnded = false;
        updateUIEndScreen();
        autoSwitchPreviousLinkVideo();
    }

    public boolean replay() {
        if (uzPlayerManager == null) {
            return false;
        }
        TmpParamData.getInstance().addPlayerViewCount();
        //TODO Chỗ này đáng lẽ chỉ clear value của tracking khi đảm bảo rằng seekTo(0) true
        boolean result = uzPlayerManager.seekTo(0);
        if (result) {
            isSetFirstRequestFocusDone = false;
            isOnPlayerEnded = false;
            updateUIEndScreen();
            handlePlayPlayListFolderUI();
        }
        if (isCastingChromecast) {
            replayChromeCast();
        }
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
                if (ibVolumeIcon != null) {
                    if (isMute) {
                        ibVolumeIcon.setImageResource(R.drawable.ic_volume_off_white_48);
                    } else {
                        ibVolumeIcon.setImageResource(R.drawable.ic_volume_up_white_48);
                    }
                }
            }
        } else if (uzPlayerManager != null) {
            uzPlayerManager.toggleVolumeMute(ibVolumeIcon);

        }
    }

    private void handleClickBackScreen() {
        if (isLandscape) {
            toggleFullscreen();
        }
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
            if (view != null) {
                view.performClick();
            }
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

    /*
     ** Hiển thị picture in picture và close video view hiện tại
     * Chỉ work nếu local player đang không casting
     * Device phải là tablet
     */
    public void showPip() {
        if (isCastingChromecast()) {
            Timber.e(ErrorConstant.ERR_19);
            notifyError(ErrorUtils.exceptionShowPip());
        } else {
            // [Re-check]: Why use performClick?
            // UIUtils.performClick(ibPictureInPictureIcon);
            handleClickPictureInPicture();
        }
    }

    public void setPipControlSkin(@LayoutRes int skinId) {
        this.pipControlSkin = skinId;
    }

    public void showSpeed() {
        if (getPlayer() == null) return;
        final UZSpeedDialog uzDlgSpeed = new UZSpeedDialog(getContext(), getPlayer().getPlaybackParameters().speed,
                speed -> {
                    if (speed != null) {
                        setSpeed(speed.getValue());
                    }
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

    public PlayerView getUzPlayerView() {
        return uzPlayerView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public ImageView getIvThumbnail() {
        return ivThumbnail;
    }

    public boolean isLivestream() {
        return isLivestream;
    }

    public IUizaPlayerManager getUzPlayerManager() {
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
        if (uzPlayerManager != null) {
            uzPlayerManager.setVolume(volume);
        }
    }

    @Override
    public void onVisibilityChange(boolean isShow) {
        if (ivLogo != null) {
            ivLogo.setClickable(!isShow);
        }
        if (controllerStateCallback != null) {
            controllerStateCallback.onVisibilityChange(isShow);
        }
    }

    public void setSpeed(float speed) {
        if (getContext() == null) {
            return;
        }
        if (isLivestream) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_speed_live_content));
        }
        if (speed > 3 || speed < -3) {
            throw new IllegalArgumentException(getContext().getString(R.string.error_speed_illegal));
        }
        PlaybackParameters playbackParameters = new PlaybackParameters(speed);
        if (getPlayer() != null) {
            getPlayer().setPlaybackParameters(playbackParameters);
        }
    }

    //=============================================================================================START UI
    private void findViews() {
        bkg = findViewById(R.id.bkg);
        rlMsg = findViewById(R.id.rl_msg);
        rlMsg.setOnClickListener(this);
        tvMsg = findViewById(R.id.tv_msg);
        if (tvMsg != null) {
            UZViewUtils.setTextShadow(tvMsg, Color.BLACK);
        }
        ivVideoCover = findViewById(R.id.iv_cover);
        llTop = findViewById(R.id.ll_top);
        progressBar = findViewById(R.id.pb);
        UZViewUtils.setColorProgressBar(progressBar, progressBarColor);
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
        } else {
            uzPlayerView.setVisibility(VISIBLE);
        }
        ivThumbnail = uzPlayerView.findViewById(R.id.image_view_thumnail);
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
        ibPauseIcon = uzPlayerView.findViewById(R.id.exo_pause_uiza);
        ibPlayIcon = uzPlayerView.findViewById(R.id.exo_play_uiza);
        //If auto start true, show button play and gone button pause
        UZViewUtils.goneViews(ibPlayIcon);
        ibReplayIcon = uzPlayerView.findViewById(R.id.exo_replay_uiza);
        ibRewIcon = uzPlayerView.findViewById(R.id.exo_rew);
        if (ibRewIcon != null) {
            ibRewIcon.setSrcDrawableDisabled();
        }
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
        LinearLayout debugLayout = findViewById(R.id.debug_layout);
        debugRootView = findViewById(R.id.controls_root);
//        if (uizacoresdk.BuildConfig.DEBUG) {
//            debugLayout.setVisibility(View.VISIBLE);
//        } else {
//            debugLayout.setVisibility(View.GONE);
//        }
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
        if (TextUtils.isEmpty(urlImgThumbnail)) {
            return;
        }
        this.urlImgThumbnail = urlImgThumbnail;
        if (ivVideoCover == null) {
            return;
        }
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
            if (TextUtils.isEmpty(urlImgThumbnail)) {
                UZPlaybackInfo info = UZData.getInstance().getPlaybackInfo();
                if (info == null || TextUtils.isEmpty(info.getThumbnail())) {
                    urlCover = Constants.URL_IMG_THUMBNAIL_BLACK;
                } else {
                    urlCover = info.getThumbnail();
                }
            } else {
                urlCover = urlImgThumbnail;
            }
            TmpParamData.getInstance().setEntityPosterUrl(urlCover);
            ImageUtils.load(ivVideoCover, urlCover, R.drawable.background_black);
        }
    }

    protected void removeVideoCover(boolean isFromHandleError) {
        if (ivVideoCover.getVisibility() != GONE) {
            ivVideoCover.setVisibility(GONE);
            ivVideoCover.invalidate();
            if (isLivestream) {
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
        int currentPlayerId = UZData.getInstance().getCurrentPlayerId();
        if (currentPlayerId == R.layout.uz_player_skin_2 || currentPlayerId == R.layout.uz_player_skin_3) {
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
        int skinId = UZData.getInstance().getCurrentPlayerId();
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
        if (uzPlayerManager == null)
            return false;

        if (UZData.getInstance().isUseWithVDHView())
            throw new IllegalArgumentException(getContext().getString(R.string.error_change_skin_with_vdhview));

        if (uzPlayerManager.isPlayingAd()) {
            notifyError(ErrorUtils.exceptionChangeSkin());
            return false;
        }
        UZData.getInstance().setCurrentPlayerId(skinId);
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
            releaseUzPlayerManager();
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
        uZChromeCast = new UZChromeCast();
        uZChromeCast.setUZChromeCastListener(new UZChromeCast.UZChromeCastListener() {
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
                    llTop.addView(uZChromeCast.getUzMediaRouteButton());
                addUIChromecastLayer();
            }
        });
        uZChromeCast.setupChromeCast(getContext());
    }

    private void updateTvDuration() {
        if (tvDuration != null)
            if (isLivestream)
                tvDuration.setText(StringUtils.convertMlsecondsToHMmSs(0));
            else
                tvDuration.setText(StringUtils.convertMlsecondsToHMmSs(getDuration()));
    }

    public void setProgressSeekBar(@NonNull UZVerticalSeekBar uzVerticalSeekBar, int progressSeekBar) {
        uzVerticalSeekBar.setProgress(progressSeekBar);
    }

    private void setTextPosition(long currentMls) {
        if (tvPosition == null) return;
        if (isLivestream) {
            long duration = getDuration();
            long past = duration - currentMls;
            tvPosition.setText(HYPHEN + StringUtils.convertMlsecondsToHMmSs(past));
        } else
            tvPosition.setText(StringUtils.convertMlsecondsToHMmSs(currentMls));
    }

    private void updateUIIbRewIconDependOnProgress(long currentMls, boolean isCalledFromUZTimebarEvent) {
        if (isCalledFromUZTimebarEvent)
            setTextPosition(currentMls);
        else {
            if (!isOnPreview) //uzTimebar is displaying
                setTextPosition(currentMls);
            return;
        }
        if (isLivestream) return;
        if (ibRewIcon != null && ibFfwdIcon != null) {
            if (currentMls == 0) {
                if (ibRewIcon.isSetSrcDrawableEnabled()) {
                    ibRewIcon.setSrcDrawableDisabled();
                }
                if (!ibFfwdIcon.isSetSrcDrawableEnabled()) {
                    ibFfwdIcon.setSrcDrawableEnabled();
                }
            } else if (currentMls == getDuration()) {
                if (!ibRewIcon.isSetSrcDrawableEnabled()) {
                    ibRewIcon.setSrcDrawableEnabled();
                }
                if (ibFfwdIcon.isSetSrcDrawableEnabled()) {
                    ibFfwdIcon.setSrcDrawableDisabled();
                }
            } else {
                if (!ibRewIcon.isSetSrcDrawableEnabled()) {
                    ibRewIcon.setSrcDrawableEnabled();
                }
                if (!ibFfwdIcon.isSetSrcDrawableEnabled()) {
                    ibFfwdIcon.setSrcDrawableEnabled();
                }
            }
        }
    }

    //FOR TV
    public void updateUIFocusChange(@NonNull View view, boolean isFocus) {
        if (isFocus) {
            if (view instanceof UZImageButton) {
                UZViewUtils.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
                ((UZImageButton) view).setColorFilter(Color.GRAY);
            } else if (view instanceof Button) {
                UZViewUtils.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
            } else if (view instanceof UZPreviewTimeBar) {
                UZViewUtils.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus_uz_timebar, R.drawable.bkg_tv_no_focus_uz_timebar);
            }
        } else {
            if (view instanceof UZImageButton) {
                UZViewUtils.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
                ((UZImageButton) view).clearColorFilter();
            } else if (view instanceof Button) {
                UZViewUtils.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus, R.drawable.bkg_tv_no_focus);
            } else if (view instanceof UZPreviewTimeBar) {
                UZViewUtils.updateUIFocusChange(view, isFocus, R.drawable.bkg_tv_has_focus_uz_timebar, R.drawable.bkg_tv_no_focus_uz_timebar);
            }
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
            if (isLandscape) {
                UZViewUtils.setMarginDimen(uzTimebar, 5, 0, 5, 0);
            } else {
                UZViewUtils.setMarginDimen(uzTimebar, 0, 0, 0, 0);
            }
    }

    private void setMarginRlLiveInfo() {
        if (rlLiveInfo != null)
            if (isLandscape) {
                UZViewUtils.setMarginDimen(rlLiveInfo, 50, 0, 50, 0);
            } else {
                UZViewUtils.setMarginDimen(rlLiveInfo, 5, 0, 5, 0);
            }
    }

    private void setTitle() {
        if (tvTitle != null) {
            tvTitle.setText(UZData.getInstance().getEntityName());
        }
    }

    /**
     * ======== END UI =========
     */

    private void updateUIDependOnLivestream() {
        if (isCastingChromecast) {
            UZViewUtils.goneViews(ibPictureInPictureIcon);
        } else {
            if (UZAppUtils.isTablet(getContext()) && UZAppUtils.isTV(getContext())) {//only hide ibPictureInPictureIcon if device is TV
                UZViewUtils.goneViews(ibPictureInPictureIcon);
            }
        }
        if (isLivestream) {
            UZViewUtils.visibleViews(rlLiveInfo);
            //TODO why set gone not work?
            setUIVisible(false, ibSpeedIcon, ibRewIcon, ibFfwdIcon);
        } else {
            UZViewUtils.goneViews(rlLiveInfo);
            //TODO why set visible not work?
            setUIVisible(true, ibSpeedIcon, ibRewIcon, ibFfwdIcon);
        }
        if (UZAppUtils.isTV(getContext())) {
            UZViewUtils.goneViews(ibFullscreenIcon);
        }
    }

    private void setUIVisible(boolean visible, UZImageButton... views) {
        for (UZImageButton v : views) {
            if (v != null) {
                v.setUIVisible(visible);
            }
        }
    }

    protected void updateUIButtonVisibilities() {
        if (debugRootView != null) {
            debugRootView.removeAllViews();
        }
        if (getPlayer() == null) {
            return;
        }
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = uzPlayerManager.getTrackSelector().getCurrentMappedTrackInfo();
        if (mappedTrackInfo == null) {
            return;
        }
        for (int i = 0; i < mappedTrackInfo.length; i++) {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);
            if (trackGroups.length != 0) {
                if (getContext() == null) {
                    return;
                }
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
                if (debugRootView != null) {
                    debugRootView.addView(button);
                }
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
        if (!isLivestream || getContext() == null) {
            return;
        }
        long now = System.currentTimeMillis();
        long duration = now - startTime;
        String s = StringUtils.convertMlsecondsToHMmSs(duration);
        if (tvLiveTime != null) {
            tvLiveTime.setText(s);
        }
        if (uzLiveContentCallback != null) {
            uzLiveContentCallback.onUpdateLiveInfoTimeStartLive(duration, s);
        }
//        callAPIUpdateLiveInfoTimeStartLive(DELAY_TO_GET_LIVE_INFORMATION);
    }

    private void updateUIEndScreen() {
        if (getContext() == null) {
            return;
        }
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
            if (uzPlayerView != null) {
                uzPlayerView.setControllerShowTimeoutMs(DEFAULT_VALUE_CONTROLLER_TIMEOUT);
            }
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
            boolean allowAdaptiveSelections = false;
            final Pair<AlertDialog, UZTrackSelectionView> dialogPair = UZTrackSelectionView.getDialog(getContext(), title, uzPlayerManager.getTrackSelector(), rendererIndex);
            dialogPair.second.setShowDisableOption(false);
            dialogPair.second.setAllowAdaptiveSelections(allowAdaptiveSelections);
            dialogPair.second.setCallback(() -> handler.postDelayed(() -> {
                {
                    if (dialogPair.first == null) {
                        return;
                    }
                    dialogPair.first.cancel();
                }
            }, 300));
            if (showDialog) {
                UZViewUtils.showDialog(dialogPair.first);
            }
            return dialogPair.second.getUZItemList();
        }
        return null;
    }

    public void setUzTimebarBottom() {
        if (uzPlayerView == null) {
            throw new NullPointerException("uzPlayerView cannot be null");
        }
        if (uzTimebar == null) {
            throw new NullPointerException("uzTimebar cannot be null");
        }
        if (uzPlayerView.getResizeMode() != AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT) {
            setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
        }
    }

    public int getHeightUZVideo() {
        if (rootView == null) {
            return 0;
        }
        if (isSetUZTimebarBottom) {
            int hRootView = UZViewUtils.heightOfView(rootView);
            int hUZTimebar = getHeightUZTimeBar();
            return hRootView - hUZTimebar / 2;
        } else {
            return UZViewUtils.heightOfView(rootView);
        }
    }

    public void setBackgroundColorBkg(int color) {
        if (bkg != null) {
            bkg.setBackgroundColor(color);
        }
    }

    public void setBackgroundColorUZVideoRootView(int color) {
        RelativeLayout uzVideoRootView = findViewById(R.id.root_view_uz_video);
        if (uzVideoRootView != null) {
            uzVideoRootView.setBackgroundColor(color);
        }
    }

    public void setMarginDependOnUZTimeBar(View view) {
        if (view == null || uzTimebar == null) {
            return;
        }
        int heightUZTimebar;
        if (isLandscape) {
            UZViewUtils.setMarginPx(view, 0, 0, 0, 0);
        } else {
            heightUZTimebar = getHeightUZTimeBar();
            UZViewUtils.setMarginPx(view, 0, 0, 0, heightUZTimebar / 2);
        }
    }

    public void setProgressBarColor(int progressBarColor) {
        if (progressBar != null) {
            this.progressBarColor = progressBarColor;
            UZViewUtils.setColorProgressBar(progressBar, progressBarColor);
        }
    }

    public void hideProgress() {
        if (uzPlayerManager != null) {
            uzPlayerManager.hideProgress();
        }
    }

    public void showProgress() {
        if (uzPlayerManager != null) {
            uzPlayerManager.showProgress();
        }
    }

    private void updateUIPlayerInfo() {
        if (uzPlayerView == null) return;
        UZPlaybackInfo info = UZData.getInstance().getPlaybackInfo();
        if (info == null) return;

        if (TextUtils.isEmpty(info.getThumbnail())) {
            return;
        }

        ivLogo = new ImageView(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ConvertUtils.dp2px(50f), ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        if (uzPlayerView.getOverlayFrameLayout() != null)
            uzPlayerView.getOverlayFrameLayout().addView(ivLogo, layoutParams);
        ivLogo.setOnClickListener(this);
        ImageUtils.load(ivLogo, info.getThumbnail());
    }

    public void addUZLiveContentCallback(UZLiveContentCallback uzLiveContentCallback) {
        this.uzLiveContentCallback = uzLiveContentCallback;
    }

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

    public void addControllerStateCallback(final UZPlayerView.ControllerStateCallback controllerStateCallback) {
        this.controllerStateCallback = controllerStateCallback;
    }

    public void addOnTouchEvent(UZPlayerView.OnTouchEvent onTouchEvent) {
        if (uzPlayerView != null) {
            uzPlayerView.setOnTouchEvent(onTouchEvent);
        }
    }

    public void addAudioListener(AudioListener audioListener) {
        this.audioListener = audioListener;
    }

    public void addPlayerEventListener(Player.EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void addVideoListener(VideoListener videoListener) {
        this.videoListener = videoListener;
    }

    public void addMetadataOutput(MetadataOutput metadataOutput) {
        this.metadataOutput = metadataOutput;
    }

    public void addTextOutput(TextOutput textOutput) {
        this.textOutput = textOutput;
    }

    private void checkData() {
        UZData.getInstance().setSettingPlayer(true);
        isHasError = false;
        if (UZData.getInstance().getEntityId() == null || UZData.getInstance().getEntityId().isEmpty()) {
            Timber.e("checkData getEntityId null or empty -> return");
            handleError(ErrorUtils.exceptionEntityId());
            UZData.getInstance().setSettingPlayer(false);
            return;
        }
        isLivestream = UZData.getInstance().isLiveStream();
        isGetClickedPip = LocalData.getClickedPip();
        Timber.d("checkData isLivestream: %b, isGetClickedPip: %b", isLivestream, isGetClickedPip);
        if (uzPlayerManager != null) {
            releaseUzPlayerManager();
//            mResultGetLinkPlay = null;
            resetCountTryLinkPlayError();
            showProgress();
        }
        setTitle();
    }

    private void checkToSetUpResource() {
        UZPlaybackInfo info = UZData.getInstance().getPlaybackInfo();
        if (info != null) {
            List<String> listLinkPlay = new ArrayList<>();
            List<String> urlList = info.getUrls();
            if (isLivestream) {
                //Bat buoc dung linkplay m3u8 cho nay, do bug cua system
                for (String url : urlList) {
                    if (url.toLowerCase().endsWith(M3U8_EXTENSION)) {
                        listLinkPlay.add(url);
                    }
                }
            } else {
                for (String url : urlList) {
                    if (url.toLowerCase().endsWith(MPD_EXTENSION)) {
                        listLinkPlay.add(url);
                    }
                }
                for (String url : urlList) {
                    if (url.toLowerCase().endsWith(M3U8_EXTENSION)) {
                        listLinkPlay.add(url);
                    }
                }
            }
            if (listLinkPlay.isEmpty()) {
                handleErrorNoData();
                return;
            }
            if (countTryLinkPlayError >= listLinkPlay.size()) {
                if (ConnectivityUtils.isConnected(getContext())) {
                    handleError(ErrorUtils.exceptionTryAllLinkPlay());
                } else {
                    notifyError(ErrorUtils.exceptionNoConnection());
                }
                return;
            }
            String linkPlay = listLinkPlay.get(countTryLinkPlayError);
            boolean isAdsDevendency = UZAppUtils.isAdsDependencyAvailable();
            if (isCalledFromChangeSkin) {
                //if called from func changeSkin(), dont initDataSource with uilIMA Ad.
                initDataSource(linkPlay, null, UZData.getInstance().getThumbnail(), isAdsDevendency);
            } else {
                initDataSource(linkPlay, UZData.getInstance().getUrlIMAAd(), UZData.getInstance().getThumbnail(), isAdsDevendency);
            }
            if (uzCallback != null) {
                uzCallback.isInitResult(false, true, UZData.getInstance().getPlaybackInfo());
            }
            initUizaPlayerManager();
        } else {
            handleError(ErrorUtils.exceptionSetup());
        }
    }

    private void initDataSource(String linkPlay, String urlIMAAd, String urlThumbnailsPreviewSeekbar, boolean includeAds) {

        // hide the cc (subtitle) button
        UZViewUtils.goneViews(ibCcIcon);

        timestampInitDataSource = System.currentTimeMillis();
        Timber.d("-------------------->initDataSource linkPlay %s", linkPlay);
        TmpParamData.getInstance().setEntitySourceUrl(linkPlay);
        TmpParamData.getInstance().setTimeFromInitEntityIdToAllApiCalledSuccess(System.currentTimeMillis() - timestampBeforeInitNewSession);
        if (includeAds) {
            uzPlayerManager = new UZPlayerManager(this, linkPlay, urlIMAAd, urlThumbnailsPreviewSeekbar);
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
        } else {
            uzPlayerManager =
                    new UZPlayerNoAdsManager(this, linkPlay, urlThumbnailsPreviewSeekbar);
        }
        if (uzTimebar != null) {
            boolean disable = TextUtils.isEmpty(urlThumbnailsPreviewSeekbar);
            uzTimebar.setEnabled(!disable);
            uzTimebar.setPreviewLoader(uzPlayerManager);
        }
        uzPlayerManager.setProgressListener(new VideoViewBase.ProgressListener() {
            @Override
            public void onAdEnded() {
                setDefaultUseController(isDefaultUseController());
                if (progressListener != null) {
                    progressListener.onAdEnded();
                }
            }

            @Override
            public void onAdProgress(int s, int duration, int percent) {
                if (progressListener != null) {
                    progressListener.onAdProgress(s, duration, percent);
                }
            }

            @Override
            public void onVideoProgress(long currentMls, int s, long duration, int percent) {
                TmpParamData.getInstance().setPlayerPlayheadTime(s);
                updateUIIbRewIconDependOnProgress(currentMls, false);
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
        if (isSetUZTimebarBottom) {
            UZViewUtils.visibleViews(uzPlayerView);
        }
        resizeContainerView();
        //enable from playPlaylistPosition() prevent double click
        setClickableForViews(true, ibSkipPreviousIcon, ibSkipNextIcon);
        if (isGetClickedPip) {
            Timber.d("getClickedPip true -> setPlayWhenReady true");
            uzPlayerManager.getPlayer().setPlayWhenReady(true);
        }
        if (uzCallback != null) {
            Timber.d("onStateReadyFirst ===> isInitResult");
            uzCallback.isInitResult(true, true, UZData.getInstance().getPlaybackInfo());
        }
        if (isCastingChromecast)
            replayChromeCast();
        updateUIPlayerInfo();
        TmpParamData.getInstance().setSessionStart(System.currentTimeMillis());
        long playerStartUpTime = System.currentTimeMillis() - timestampInitDataSource;
        TmpParamData.getInstance().setPlayerStartupTime(playerStartUpTime);
        UZData.getInstance().setSettingPlayer(false);
    }

    private void initUizaPlayerManager() {
        if (uzPlayerManager != null) {
            uzPlayerManager.init();
            if (isGetClickedPip && !isPlayPlaylistFolder())
                uzPlayerManager.getPlayer().setPlayWhenReady(false);
            else {
                if (isRefreshFromChangeSkin) {
                    uzPlayerManager.seekTo(currentPositionBeforeChangeSkin);
                    isRefreshFromChangeSkin = false;
                    currentPositionBeforeChangeSkin = 0;
                }
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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventBusData.ConnectEvent event) {
        if (event == null || uzPlayerManager == null)
            return;
        if (!event.isConnected())
            notifyError(ErrorUtils.exceptionNoConnection());
        else {
            if (uzPlayerManager.getExoPlaybackException() == null) {
                hideController();
                hideLayoutMsg();
            } else {
                isCalledFromConnectionEventBus = true;
                uzPlayerManager.setResumeIfConnectionError();
                if (!activityIsPausing) {
                    uzPlayerManager.init();
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
        if (positionMiniPlayer != 0) {
            seekTo(positionMiniPlayer);
            resume();
            sendEventInitSuccess();
            positionMiniPlayer = 0;
            return false;
        } else {
            setUrlImgThumbnail(urlImgThumbnail);
            pause();
            showProgress();
            positionMiniPlayer = 0;
            return true;
        }
    }

    //listen msg from service FloatUizaVideoService
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CommunicateMng.MsgFromService msg) {
        if (msg == null || uzPlayerManager == null)
            return;
        //click open app of mini player
        if (msg instanceof CommunicateMng.MsgFromServiceOpenApp) {
            Timber.d("miniplayer STEP 6");
            try {
                positionMiniPlayer = ((CommunicateMng.MsgFromServiceOpenApp) msg).getPositionMiniPlayer();
                Class classNamePfPlayer = Class.forName(((Activity) getContext()).getClass().getName());
                Intent intent = new Intent(getContext(), classNamePfPlayer);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.KEY_UIZA_ENTITY_ID, UZData.getInstance().getEntityId());
                getContext().startActivity(intent);
            } catch (ClassNotFoundException e) {
                Timber.e(e, "onMessageEvent open app ClassNotFoundException");
            }
            return;
        }
        //when pip float view init success
        if (uzCallback != null && msg instanceof CommunicateMng.MsgFromServiceIsInitSuccess) {
            //Ham nay duoc goi khi player o FloatUizaVideoService da init xong
            //Nhiem vu la minh se gui vi tri hien tai sang cho FloatUizaVideoService no biet
            Timber.d("miniplayer STEP 3 UZVideo biet FloatUizaVideoService da init xong -> gui lai content position cua UZVideo cho FloatUizaVideoService");
            CommunicateMng.MsgFromActivityPosition msgFromActivityPosition = new CommunicateMng.MsgFromActivityPosition(null);
            msgFromActivityPosition.setPosition(getCurrentPosition());
            CommunicateMng.postFromActivity(msgFromActivityPosition);
            isInitMiniPlayerSuccess = true;
            uzCallback.onStateMiniPlayer(((CommunicateMng.MsgFromServiceIsInitSuccess) msg).isInitSuccess());
        }
    }

    public void sendEventInitSuccess() {
        CommunicateMng.MsgFromActivityIsInitSuccess msgFromActivityIsInitSuccess = new CommunicateMng.MsgFromActivityIsInitSuccess(null);
        msgFromActivityIsInitSuccess.setInitSuccess(true);
        CommunicateMng.postFromActivity(msgFromActivityIsInitSuccess);
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
        if (UZData.getInstance().getPlaybackInfo() == null || uzPlayerManager == null || uzPlayerManager.getPlayer() == null) {
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

        //play chromecast with full screen control
        //UizaData.getInstance().getCasty().getPlayer().loadMediaAndPlay(mediaInfo, true, lastCurrentPosition);

        //play chromecast without screen control
        UZData.getInstance().getCasty().getPlayer().loadMediaAndPlayInBackground(mediaInfo, true, lastCurrentPosition);

        UZData.getInstance().getCasty().getPlayer().getRemoteMediaClient().addProgressListener((currentPosition, duration1) -> {
            if (currentPosition >= lastCurrentPosition && !isCastPlayerPlayingFirst) {
                hideProgress();
                isCastPlayerPlayingFirst = true;
            }
            if (currentPosition > 0) {
                uzPlayerManager.seekTo(currentPosition);
            }
        }, 1000);

    }

    /* khi click vào biểu tượng casting
     * thì sẽ pause local player và bắt đầu loading lên cast player
     * khi disconnect thì local player sẽ resume*/
    private void updateUIChromecast() {
        if (uzPlayerManager == null || rlChromeCast == null || UZAppUtils.isTV(getContext())) {
            return;
        }
        if (isCastingChromecast) {
            uzPlayerManager.pause();
            uzPlayerManager.setVolume(0f);
            UZViewUtils.visibleViews(rlChromeCast, ibPlayIcon);
            UZViewUtils.goneViews(ibPauseIcon);
//            UIUtils.goneViews(ibSettingIcon, ibCcIcon, ibBackScreenIcon, ibPlayIcon, ibPauseIcon, ibVolumeIcon);
            //casting player luôn play first với volume not mute
            //UizaData.getInstance().getCasty().setVolume(0.99);

            if (uzPlayerView != null) {
                uzPlayerView.setControllerShowTimeoutMs(0);
            }
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
        if (getContext() == null) {
            return;
        }
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
                if (jobScheduler != null) {
                    jobScheduler.schedule(myJob);
                }
            } catch (NoClassDefFoundError e) {

            }
        }
    }

    // ===== Stats For Nerds =====
    private void initStatsForNerds() {
        getPlayer().addAnalyticsListener(statsForNerdsView);
    }

    public UZChromeCast getuZChromeCast() {
        return uZChromeCast;
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
            if (llTop.getParent() instanceof ViewGroup) {
                ((RelativeLayout) llTop.getParent()).addView(rlChromeCast, 0);
            }
        } else if (rlLiveInfo != null) {
            if (rlLiveInfo.getParent() instanceof ViewGroup) {
                ((RelativeLayout) rlLiveInfo.getParent()).addView(rlChromeCast, 0);
            }
        }
    }

    public void addVideoAdPlayerCallback(UZAdPlayerCallback uzAdPlayerCallback) {
        if (UZAppUtils.isAdsDependencyAvailable()) {
            this.videoAdPlayerCallback = uzAdPlayerCallback;
        } else {
            throw new NoClassDefFoundError(ErrorConstant.ERR_506);
        }
    }

    protected void updateLiveStreamLatency(long latency) {
        statsForNerdsView.showTextLiveStreamLatency();
        statsForNerdsView.setTextLiveStreamLatency(StringUtils.groupingSeparatorLong(latency));
    }

    protected void hideTextLiveStreamLatency() {
        statsForNerdsView.hideTextLiveStreamLatency();
    }


}
