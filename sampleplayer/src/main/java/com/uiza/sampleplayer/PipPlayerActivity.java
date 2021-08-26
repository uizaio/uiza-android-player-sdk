package com.uiza.sampleplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.uiza.api.UZApi;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZPlayerCallback;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Demo UZPlayer with Picture In Picture
 */
public class PipPlayerActivity extends AppCompatActivity implements UZPlayerCallback {

    private UZVideoView uzVideo;
    private EditText etLinkPlay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CompositeDisposable disposables;
    private boolean inPip = false;

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_default);
        super.onCreate(savedState);
        setContentView(R.layout.activity_pip_player);
        uzVideo = findViewById(R.id.uz_video_view);
        etLinkPlay = findViewById(R.id.et_link_play);
        uzVideo.setPlayerCallback(this);
        uzVideo.setEnablePictureInPicture(true);
        // If linkplay is livestream, it will auto move to live edge when onResume is called
        uzVideo.setAutoMoveToLiveEdge(true);
        UZPlayback playbackInfo = null;
        if (getIntent() != null) {
            playbackInfo = getIntent().getParcelableExtra("extra_playback_info");
        }
        if (playbackInfo != null)
            etLinkPlay.setText(playbackInfo.getFirstLinkPlay());
        else
            etLinkPlay.setText(LSApplication.urls[0]);

//        etLinkPlay.setText("https://hls.ted.com/talks/2639.m3u8?preroll=Thousands");

        findViewById(R.id.btn_play).setOnClickListener(view -> onPlay());
        disposables = new CompositeDisposable();
        (new Handler()).postDelayed(this::onPlay, 100);
    }

    private void onPlay() {
        final UZPlayback playback = new UZPlayback();
        playback.addLinkPlay(etLinkPlay.getText().toString());
        uzVideo.play(playback);
    }

    @Override
    public void playerViewCreated(UZPlayerView playerView) {
        playerView.setControllerStateCallback(visible -> {
            // nothing to do
        });
    }

    @Override
    public void isInitResult(String linkPlay) {
        getLiveViewsTimer(true);
    }

    @Override
    public void onError(UZException e) {
        Timber.e(e);
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
    public void onScreenRotate(boolean isLandscape) {
        if (!isLandscape) {
            int w = UZViewUtils.getScreenWidth();
            int h = w * 9 / 16;
            uzVideo.setFreeSize(false);
            uzVideo.setSize(w, h);
        }
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
        if (inPip) {
            inPip = false;
        } else {
            uzVideo.onResumeView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!inPip) {
            uzVideo.onPauseView();
        }
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
        inPip = true;
    }

    private void getLiveViewsTimer(boolean firstRun) {
        final UZPlayback playback = UZPlayer.getCurrentPlayback();
        if (handler != null && playback != null && uzVideo != null)
            handler.postDelayed(() -> {
                Disposable d = UZApi.getLiveViewers(playback.getFirstLinkPlay(),
                        res -> uzVideo.setLiveViewers(res.getViews()), Timber::e);
                if (d != null) {
                    disposables.add(d);
                }
                getLiveViewsTimer(false);
            }, firstRun ? 0 : 5000);
    }
}
