package com.uiza.sampleplayer;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZCallback;
import com.uiza.sdk.interfaces.UZVideoViewItemClick;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZDragView;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;
import com.uiza.sdk.widget.UZToast;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo UZPlayer with UZDragView
 */

public class PlayerActivity extends AppCompatActivity implements UZCallback, UZDragView.Callback, UZPlayerView.OnSingleTap, UZVideoViewItemClick,
        UZPlayerView.ControllerStateCallback {

    HorizontalScrollView llBottom;
    private UZVideoView uzVideo;
    private UZDragView uzDragView;
    private EditText etLinkPlay;
    private List<UZPlayback> playlist;
    Button btPlay, btnStarts;
    private boolean isLive = false;

    public static void setLastCursorEditText(@NonNull EditText editText) {
        if (!editText.getText().toString().isEmpty()) {
            editText.setSelection(editText.getText().length());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        UZPlayer.setUseWithUZDragView(true);
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_custom);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        uzVideo = findViewById(R.id.uz_video_view);
        uzDragView = findViewById(R.id.vdhv);
        llBottom = findViewById(R.id.hsv_bottom);
        etLinkPlay = findViewById(R.id.et_link_play);
        btPlay = findViewById(R.id.bt_play);
        btnStarts = findViewById(R.id.bt_stats_for_nerds);
        uzDragView.setCallback(this);
        uzDragView.setOnSingleTap(this);
        uzDragView.setScreenRotate(false);
        uzVideo.setUZCallback(this);
        uzVideo.setUZVideoViewItemClick(this);
        uzVideo.setControllerStateCallback(this);
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
        btnStarts.setVisibility(View.GONE);
        findViewById(R.id.bt_0).setOnClickListener(view -> updateView(LSApplication.urls[0], true));
        findViewById(R.id.bt_1).setOnClickListener(view -> updateView(LSApplication.urls[1], false));
        findViewById(R.id.bt_2).setOnClickListener(view -> updateView(LSApplication.urls[2], false));
        findViewById(R.id.bt_3).setOnClickListener(view -> updateView(LSApplication.urls[3], false));
        findViewById(R.id.bt_4).setOnClickListener(view -> {
            etLinkPlay.setVisibility(View.GONE);
            btPlay.setVisibility(View.GONE);
            uzVideo.play(playlist);
        });
        btPlay.setOnClickListener(view -> onPlay());
        btnStarts.setOnClickListener(v -> {
            if (uzVideo != null)
                uzVideo.toggleStatsForNerds();
        });
        if (playbackInfo != null) {
            boolean isInitSuccess = uzVideo.play(playbackInfo);
            if (!isInitSuccess)
                UZToast.show(this, "Init failed");
        }

        (new Handler()).postDelayed(() -> {
            updateView(LSApplication.urls[0], false);
            onPlay();
        }, 1000);
    }

    private void updateView(String url, boolean live) {
        etLinkPlay.setVisibility(View.VISIBLE);
        btPlay.setVisibility(View.VISIBLE);
        etLinkPlay.setText(url);
        isLive = live;
        setLastCursorEditText(etLinkPlay);
    }

    private void initPlaylist() {
        playlist = new ArrayList<>();
        for (String url : LSApplication.urls) {
            UZPlayback playback = new UZPlayback();
            playback.setHls(url);
            playlist.add(playback);
        }
    }

    private void onPlay() {
        final UZPlayback playback = new UZPlayback();
        playback.setThumbnail(LSApplication.thumbnailUrl);
        playback.setHls(etLinkPlay.getText().toString());
        playback.setLive(isLive);
        UZPlayer.setCurrentPlayback(playback);
        boolean isInitSuccess = uzVideo.play();
        if (!isInitSuccess) {
            UZToast.show(this, "Init failed");
        }
    }

    @Override
    public void isInitResult(boolean isInitSuccess, UZPlayback data) {
        btnStarts.setVisibility(View.VISIBLE);
        uzDragView.setInitResult(isInitSuccess);
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
            // todo
        }
    }

    @Override
    public void onViewSizeChange(boolean isMaximizeView) {

    }

    @Override
    public void onStateChange(UZDragView.State state) {

    }

    @Override
    public void onPartChange(UZDragView.Part part) {

    }

    @Override
    public void onViewPositionChanged(int left, int top, float dragOffset) {

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
    public void onSingleTapConfirmed(float x, float y) {
        if (uzDragView.isMaximizeView())
            uzDragView.post(() -> uzVideo.toggleShowHideController());
    }

    @Override
    public void onVisibilityChange(boolean isShow) {
        uzDragView.setVisibilityChange(isShow);
    }
}
