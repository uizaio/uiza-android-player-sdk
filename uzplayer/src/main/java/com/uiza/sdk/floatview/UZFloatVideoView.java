package com.uiza.sdk.floatview;

/**
 * Created by www.muathu@gmail.com on 14/1/2019.
 */

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.uiza.sdk.R;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.util.TmpParamData;
import com.uiza.sdk.util.UZAppUtils;
import com.uiza.sdk.util.UZData;
import com.uiza.sdk.util.UZViewUtils;
import com.uiza.sdk.view.VideoViewBase;

import java.util.List;

import timber.log.Timber;

public class UZFloatVideoView extends VideoViewBase {
    private PlayerView playerView;
    private UZFloatPlayerManagerAbs fuzUizaPlayerManager;
    private ProgressBar progressBar;
    private String linkPlay;
    private boolean isLiveStream;
    private long contentPosition;
    private boolean isOnStateReadyFirst;
    private long timestampRebufferStart;
    private int oldPercent = -1;
    private VideoViewBase.ProgressListener progressListener;

    private Callback callback;
    private boolean init = false;

    public UZFloatVideoView(Context context) {
        this(context, null);
    }

    public UZFloatVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UZFloatVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        onInitView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public UZFloatVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInitView();
    }

    private void onInitView(){
        if(!init) {
            inflate(getContext(), R.layout.uz_float_video_view, this);
            progressBar = findViewById(R.id.pb);
            playerView = findViewById(R.id.player_view);
            init = true;
        }
    }

    @Override
    public void onCreateView() {
        // Nothing
    }

    //=============================================================================================================START CONFIG

    @Override
    public long getCurrentPosition() {
        return (getPlayer() == null) ? 0 : getPlayer().getCurrentPosition();
    }

    public long getContentBufferedPosition() {
        return (getPlayer() == null) ? 0 : getPlayer().getContentBufferedPosition();
    }

    @Override
    public void seekTo(long position) {
        if (fuzUizaPlayerManager != null) {
            fuzUizaPlayerManager.seekTo(position);
        }
    }

    //return true if toggleResume
    //return false if togglePause
    protected boolean togglePauseResume() {
        return (fuzUizaPlayerManager != null) && fuzUizaPlayerManager.togglePauseResume();
    }

    @Override
    public void pause() {
        if (fuzUizaPlayerManager != null)
            fuzUizaPlayerManager.pauseVideo();
    }

    @Override
    public void resume() {
        if (fuzUizaPlayerManager != null)
            fuzUizaPlayerManager.resumeVideo();
    }

    @Override
    public int getVideoWidth() {
        return (fuzUizaPlayerManager == null) ? 0 : fuzUizaPlayerManager.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return (fuzUizaPlayerManager == null) ? 0 : fuzUizaPlayerManager.getVideoHeight();
    }

    @Override
    public SimpleExoPlayer getPlayer() {
        return (fuzUizaPlayerManager == null) ? null : fuzUizaPlayerManager.getPlayer();
    }

    private void releasePlayerManager() {
        if (fuzUizaPlayerManager != null)
            fuzUizaPlayerManager.release();
    }

    @Override
    public boolean play() {
        UZPlayback playback = UZData.getInstance().getPlayback();
        if (playback != null) {
            init(playback.getLinkPlay(), playback.isLive(), 0, null);
            return true;
        } else
            return false;
    }

    @Override
    public boolean play(@NonNull UZPlayback playback) {
        init(playback.getLinkPlay(), playback.isLive(), 0, null);
        return true;
    }

    @Override
    public boolean play(List<UZPlayback> playlist) {
        return false;
    }

    //=============================================================================================================END CONFIG

    //=============================================================================================================START VIEW
    public PlayerView getPlayerView() {
        return playerView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    protected void onVideoSizeChanged(int width, int height) {
        if (callback != null) {
            callback.onVideoSizeChanged(width, height);
        }
    }

    protected void hideProgress() {
        progressBar.setVisibility(GONE);
    }
    //=============================================================================================================END VIEW

    protected void showProgress() {
        progressBar.setVisibility(VISIBLE);
    }

    public void init(String linkPlay, boolean isLiveStream, long contentPosition, Callback callback) {
        if (TextUtils.isEmpty(linkPlay)) {
            Timber.e("init failed: linkPlay == null || linkPlay.isEmpty()");
            return;
        }
        this.linkPlay = linkPlay;
        this.isLiveStream = isLiveStream;
        this.contentPosition = contentPosition;
        showProgress();
//        UZViewUtils.setColorProgressBar(progressBar, Color.WHITE);
        isOnStateReadyFirst = false;
        Timber.d("init linkPlay: %s, isLiveStream: %s", linkPlay, isLiveStream);
        this.callback = callback;
        releasePlayerManager();
        checkToSetUp();
    }

    private void checkToSetUp() {
        initData(linkPlay, null, UZAppUtils.isAdsDependencyAvailable());
        onResume();
    }

    public void initData(String linkPlay, String urlIMAAd, boolean includeAd) {
        if (includeAd) {
            fuzUizaPlayerManager =
                    new UZFloatPlayerManager(this, linkPlay, urlIMAAd);
            fuzUizaPlayerManager.setProgressListener(new VideoViewBase.ProgressListener() {
                @Override
                public void onAdEnded() {
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
                    if (progressListener != null) {
                        progressListener.onVideoProgress(currentMls, s, duration, percent);
                    }
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                }

                @Override
                public void onBufferProgress(long bufferedPosition, int bufferedPercentage, long duration) {
                }
            });
        } else {
            fuzUizaPlayerManager =
                    new UZFloatNoAdsPlayerManager(this, linkPlay);
        }
    }

    //=============================================================================================================START LIFE CIRCLE
    public void onDestroy() {
        releasePlayerManager();
    }

    public void onResume() {
        if (fuzUizaPlayerManager != null) {
            fuzUizaPlayerManager.init(isLiveStream, contentPosition);
        }
    }
    //=============================================================================================================END LIFE CIRCLE

    public void onPause() {
        if (fuzUizaPlayerManager != null) {
            fuzUizaPlayerManager.reset();
        }
    }

    //================================ START CALLBACK
    public void setProgressListener(VideoViewBase.ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    protected void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case Player.STATE_BUFFERING:
                showProgress();
                timestampRebufferStart = System.currentTimeMillis();
                TmpParamData.getInstance().addViewRebufferCount();
                break;
            case Player.STATE_ENDED:
                timestampRebufferStart = 0;
                break;
            case Player.STATE_IDLE:
                timestampRebufferStart = 0;
                showProgress();
                break;
            case Player.STATE_READY:
                if (timestampRebufferStart != 0) {
                    TmpParamData.getInstance().setViewRebufferDuration(System.currentTimeMillis() - timestampRebufferStart);
                }
                timestampRebufferStart = 0;
                if (!isOnStateReadyFirst) {
                    onStateReadyFirst();
                    isOnStateReadyFirst = true;
                }
                hideProgress();
                break;
        }
        if (callback != null) {
            callback.onPlayerStateChanged(playWhenReady, playbackState);
        }
    }

    protected void onPlayerError(UZException error) {
        if (callback != null)
            callback.onPlayerError(error);

    }

    private void onStateReadyFirst() {
        if (callback != null)
            callback.isInitResult(true);
    }

    protected void getLinkPlayOfNextItem(CallbackGetNextPlayback callback) {
        if (callback == null) return;
        if (UZData.getInstance().getPlayList() == null) {
            Timber.e("playPlaylistPosition error: incorrect position");
            callback.onSuccess(null);
            return;
        }
        int currentPositionOfDataList = UZData.getInstance().getCurrentPositionOfPlayList();
        int position = currentPositionOfDataList + 1;
        if (position < 0) {
            Timber.e("This is the first item");
            callback.onSuccess(null);
            return;
        }
        if (position > UZData.getInstance().getPlayList().size() - 1) {
            Timber.e("This is the last item");
            callback.onSuccess(null);
            return;
        }
        UZData.getInstance().setCurrentPositionOfPlayList(position);
        callback.onSuccess(UZData.getInstance().getPlayback());
    }


    public interface CallbackGetNextPlayback {
        void onSuccess(UZPlayback playback);
    }

    public interface Callback {
        void isInitResult(boolean isInitSuccess);

        void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        void onVideoSizeChanged(int width, int height);

        void onPlayerError(UZException error);
    }
    //=============================================================================================================END CALLBACK
}