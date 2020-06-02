package com.uiza.sampleplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.uiza.api.UZApi;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZCallback;
import com.uiza.sdk.interfaces.UZVideoViewItemClick;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;
import com.uiza.sdk.widget.UZToast;

import java.util.Locale;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Demo UZPlayer with Picture In Picture
 */
public class PipPlayerActivity extends AppCompatActivity implements UZCallback, UZPlayerView.OnSingleTap, UZVideoViewItemClick {

    private UZVideoView uzVideo;
    private EditText etLinkPlay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CompositeDisposable disposables;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_default);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip_player);
        uzVideo = findViewById(R.id.uz_video_view);
        etLinkPlay = findViewById(R.id.et_link_play);
        uzVideo.setUZCallback(this);
        uzVideo.setUZVideoViewItemClick(this);
        uzVideo.setOnSingleTap(this);
        // If linkplay is livestream, it will auto move to live edge when onResume is called
        uzVideo.setAutoMoveToLiveEdge(true);
        UZPlayback playbackInfo = null;
        if (getIntent() != null) {
            playbackInfo = getIntent().getParcelableExtra("extra_playback_info");
        }
        if (playbackInfo != null)
            etLinkPlay.setText(playbackInfo.getLinkPlay());
        else
            etLinkPlay.setText(LSApplication.urls[0]);

        findViewById(R.id.btn_play).setOnClickListener(view -> onPlay());

        findViewById(R.id.bt_stats_for_nerds).setOnClickListener(v -> {
            if (uzVideo != null)
                uzVideo.toggleStatsForNerds();
        });

        disposables = new CompositeDisposable();
        (new Handler()).postDelayed(this::onPlay, 1000);
    }

    private void onPlay() {
        final UZPlayback playback = new UZPlayback();
        playback.setThumbnail(LSApplication.thumbnailUrl);
        playback.setLinkPlay(etLinkPlay.getText().toString());
        playback.setLive(true);
        uzVideo.play(playback);
    }

    @Override
    public void isInitResult(boolean isInitSuccess, UZPlayback data) {
        if (isInitSuccess) {
            getLiveViewsTimer(true);
        } else {
            UZToast.show(this, "Init failed");
        }
    }

    @Override
    public void onItemClick(View view) {
        if (view.getId() == R.id.exo_back_screen) {
            if (!uzVideo.isLandscape()) {
                onBackPressed();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        uzVideo.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        uzVideo.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSkinChange() {
    }

    @Override
    public void onScreenRotate(boolean isLandscape) {
        if (!isLandscape) {
            int w = UZViewUtils.getScreenWidth();
            int h = w * 9 / 16;
            uzVideo.setFreeSize(false);
            uzVideo.setSize(w, h);
        }
    }

    @Override
    public void onError(UZException e) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uzVideo.onDestroyView();
        if (disposables != null)
            disposables.dispose();
        handler = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        uzVideo.onResumeView();
    }

    @Override
    public void onPause() {
        super.onPause();
        uzVideo.onPauseView();
    }

    @Override
    public void onBackPressed() {
        if (!uzVideo.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        if (newConfig != null)
            uzVideo.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        uzVideo.enterPIPMode();
    }

    @Override
    public void onSingleTapConfirmed(float x, float y) {
        uzVideo.toggleShowHideController();
    }

    private void getLiveViewsTimer(boolean firstRun) {
        final UZPlayback playback = UZPlayer.getCurrentPlayback();
        if (handler != null && playback != null)
            handler.postDelayed(() -> {
                Disposable d = UZApi.getLiveViewers(playback.getLinkPlay(), res -> {
                    uzVideo.setLiveViewers(res.getViews());
                }, Timber::e);
                if (d != null) {
                    disposables.add(d);
                }
                getLiveViewsTimer(false);
            }, firstRun ? 0 : 5000);
    }
}
