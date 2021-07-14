package com.uiza.sampleplayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.uiza.api.UZApi;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZPlayerCallback;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZDragView;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;
import com.uiza.sdk.widget.UZToast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Demo UZPlayer with UZDragView
 */

public class PlayerActivity extends AppCompatActivity implements UZPlayerCallback, UZDragView.Callback,
        UZPlayerView.ControllerStateCallback {

    HorizontalScrollView llBottom;
    private UZVideoView uzVideo;
    private UZDragView uzDragView;
    private EditText etLinkPlay;
    private List<UZPlayback> playlist;
    Button btPlay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CompositeDisposable disposables;

    public static void setLastCursorEditText(@NonNull EditText editText) {
        if (!editText.getText().toString().isEmpty()) {
            editText.setSelection(editText.getText().length());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        UZPlayer.setUseWithUZDragView(true);
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_default);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        uzVideo = findViewById(R.id.uz_video_view);
        uzDragView = findViewById(R.id.vdhv);
        llBottom = findViewById(R.id.hsv_bottom);
        etLinkPlay = findViewById(R.id.et_link_play);
        btPlay = findViewById(R.id.bt_play);
        uzDragView.setCallback(this);
        uzDragView.setScreenRotate(false);
        uzVideo.setPlayerCallback(this);
//        uzVideo.getPlayerView().setControllerStateCallback(this);
        disposables = new CompositeDisposable();
        // If linkplay is livestream, it will auto move to live edge when onResume is called
        uzVideo.setAutoMoveToLiveEdge(true);
        UZPlayback playbackInfo = null;
        if (getIntent() != null) {
            playbackInfo = getIntent().getParcelableExtra("extra_playback_info");
            if (playbackInfo != null) {
                llBottom.setVisibility(View.GONE);
                etLinkPlay.setVisibility(View.GONE);
            } else {
                llBottom.setVisibility(View.VISIBLE);
                etLinkPlay.setVisibility(View.VISIBLE);
                initPlaylist();
            }
        } else {
            llBottom.setVisibility(View.VISIBLE);
            etLinkPlay.setVisibility(View.VISIBLE);
            initPlaylist();
        }
        findViewById(R.id.bt_0).setOnClickListener(view -> updateView(0));
        findViewById(R.id.bt_1).setOnClickListener(view -> updateView(1));
        findViewById(R.id.bt_2).setOnClickListener(view -> updateView(2));
        findViewById(R.id.bt_3).setOnClickListener(view -> updateView(3));
        findViewById(R.id.bt_4).setOnClickListener(view -> {
            etLinkPlay.setVisibility(View.GONE);
            btPlay.setVisibility(View.GONE);
            uzVideo.play(playlist);
        });
        btPlay.setOnClickListener(view -> onPlay());
        if (playbackInfo != null) {
            boolean isInitSuccess = uzVideo.play(playbackInfo);
            if (!isInitSuccess)
                UZToast.show(this, "Init failed");
        }
        (new Handler()).postDelayed(() -> {
            updateView(0);
            onPlay();
        }, 1000);
    }

    private void updateView(int index) {
        etLinkPlay.setVisibility(View.VISIBLE);
        btPlay.setVisibility(View.VISIBLE);
        etLinkPlay.setText(LSApplication.urls[index]);
        setLastCursorEditText(etLinkPlay);
    }

    private void initPlaylist() {
        playlist = new ArrayList<>();
        int i = 0;
        for (String url : LSApplication.urls) {
            UZPlayback playback = new UZPlayback();
            playback.addLinkPlay(url);
            playlist.add(playback);
            i++;
        }
    }

    private void onPlay() {
        final UZPlayback playback = new UZPlayback();
        playback.setPoster(LSApplication.thumbnailUrl);
        playback.addLinkPlay(etLinkPlay.getText().toString());
        uzVideo.play(playback);

    }

    @Override
    public void isInitResult(String linkPlay) {
        Timber.e("LinkPlay: %s", linkPlay);
        uzDragView.setInitResult(true);
        getLiveViewsTimer(true);
    }

    @Override
    public void onTimeShiftChange(boolean timeShiftOn) {
        runOnUiThread(() -> UZToast.show(this, "TimeShiftOn: "+timeShiftOn));
    }

    @Override
    public void onScreenRotate(boolean isLandscape) {
        if (!isLandscape) {
            int w = UZViewUtils.getScreenWidth();
            int h = w * 9 / 16;
            uzVideo.setFreeSize(false);
            uzVideo.setSize(w, h);
        }
        uzDragView.setScreenRotate(isLandscape);
    }

    @Override
    public void onError(UZException e) {
        runOnUiThread(() -> UZToast.show(this, e.getLocalizedMessage()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uzVideo.onDestroyView();
        UZPlayer.setUseWithUZDragView(false);
        if (disposables != null)
            disposables.dispose();
        handler = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (uzDragView.isAppear())
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

    private void updateUIRevertMaxChange(boolean isEnableRevertMaxSize) {
        if (isEnableRevertMaxSize && uzDragView.isAppear()) {
        }
    }

    @Override
    public void onOverScroll(UZDragView.State state, UZDragView.Part part) {
        uzVideo.pause();
        uzDragView.disappear();
    }

    @Override
    public void onEnableRevertMaxSize(boolean isEnableRevertMaxSize) {
        updateUIRevertMaxChange(!isEnableRevertMaxSize);
    }

    @Override
    public void onAppear(boolean isAppear) {
        updateUIRevertMaxChange(uzDragView.isEnableRevertMaxSize());
    }

    @Override
    public void onVisibilityChange(boolean isShow) {
        uzDragView.setVisibilityChange(isShow);
    }

    private void getLiveViewsTimer(boolean firstRun) {
        final UZPlayback playback = UZPlayer.getCurrentPlayback();
        if (handler != null && playback != null)
            handler.postDelayed(() -> {
                Disposable d = UZApi.getLiveViewers(playback.getFirstLinkPlay(), res -> {
                    uzVideo.setLiveViewers(res.getViews());
                }, Timber::e);
                if (d != null) {
                    disposables.add(d);
                }
                getLiveViewsTimer(false);
            }, firstRun ? 0 : 5000);
    }
}
