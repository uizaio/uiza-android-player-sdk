package com.uiza.sampleplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;

import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZCallback;
import com.uiza.sdk.interfaces.UZVideoViewItemClick;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.util.LocalData;
import com.uiza.sdk.util.UZViewUtils;
import com.uiza.sdk.view.UZDragView;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;
import com.uiza.sdk.widget.UZToast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loitp on 9/1/2019.
 */

public class PlayerActivity extends AppCompatActivity implements UZCallback, UZDragView.Callback, UZPlayerView.OnTouchEvent, UZVideoViewItemClick,
        UZPlayerView.ControllerStateCallback {
    private static final String[] urls = new String[]{
            "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd",
            "https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            "https://s3-ap-southeast-1.amazonaws.com/cdnetwork-test/drm_sample_byterange/manifest.mpd"};
    HorizontalScrollView llBottom;
    private UZVideoView uzVideo;
    private UZDragView uzDragView;
    private EditText etLinkPlay;
    private Button btPlaylist;
    private List<UZPlayback> playlist;

    public static void setLastCursorEditText(@NonNull EditText editText) {
        if (!editText.getText().toString().isEmpty()) {
            editText.setSelection(editText.getText().length());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        UZPlayer.setUseWithUZDragView(true);
//        UZPlayer.setCasty(this);
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_custom);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        uzVideo = findViewById(R.id.uz_video_view);
        uzDragView = findViewById(R.id.vdhv);
        llBottom = findViewById(R.id.hsv_bottom);
        etLinkPlay = findViewById(R.id.et_link_play);
        btPlaylist = findViewById(R.id.bt_playlist);
        uzDragView.setCallback(this);
        uzDragView.setOnTouchEvent(this);
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

        findViewById(R.id.bt_0).setOnClickListener(view -> {
            etLinkPlay.setText(urls[0]);
            setLastCursorEditText(etLinkPlay);
            onPlay(false);
        });
        findViewById(R.id.bt_1).setOnClickListener(view -> {
            etLinkPlay.setText(urls[1]);
            setLastCursorEditText(etLinkPlay);
            onPlay(false);
        });
        findViewById(R.id.bt_2).setOnClickListener(view -> {
            etLinkPlay.setText(urls[2]);
            setLastCursorEditText(etLinkPlay);
            onPlay(false);
        });
        findViewById(R.id.bt_3).setOnClickListener(view -> {
            etLinkPlay.setText(urls[3]);
            setLastCursorEditText(etLinkPlay);
            onPlay(true);
        });

        btPlaylist.setOnClickListener(view -> {
            uzVideo.play(playlist);
        });
        findViewById(R.id.bt_stats_for_nerds).setOnClickListener(v -> {
            if (uzVideo != null)
                uzVideo.toggleStatsForNerds();
        });
        if (playbackInfo != null) {
            boolean isInitSuccess = uzVideo.play(playbackInfo);
            if (!isInitSuccess)
                UZToast.show(this, "Init failed");
        }
        if (LocalData.getClickedPip())
            btPlaylist.performClick();
    }

    private void initPlaylist() {
        playlist = new ArrayList<>();
        for (String url : urls) {
            UZPlayback playback = new UZPlayback();
            playback.setHls(url);
            playlist.add(playback);
        }
    }

    private void onPlay(boolean live) {
        final UZPlayback playback = new UZPlayback();
        playback.setHls(etLinkPlay.getText().toString());
        playback.setLive(live);
        UZPlayer.setCurrentPlayback(playback);
        boolean isInitSuccess = uzVideo.play();
        if (!isInitSuccess) {
            UZToast.show(this, "Init failed");
        }
    }

    @Override
    public void isInitResult(boolean isInitSuccess, UZPlayback data) {
        uzDragView.setInitResult(isInitSuccess);
    }

    @Override
    public void onItemClick(View view) {
        switch (view.getId()) {
            case R.id.exo_back_screen:
                if (!uzVideo.isLandscape()) {
                    onBackPressed();
                }
                break;
        }
    }

    @Override
    public void onStateMiniPlayer(boolean isInitMiniPlayerSuccess) {
        if (isInitMiniPlayerSuccess)
            onBackPressed();
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
        uzVideo.onResumeView();
    }

    @Override
    public void onPause() {
        super.onPause();
        uzDragView.onPause();
        uzVideo.onPauseView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        uzVideo.onActivityResult(resultCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (uzVideo.isLandscape()) {
            uzVideo.toggleFullscreen();
        } else {
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
        uzVideo.toggleShowHideController();
    }

    @Override
    public void onLongPress(float x, float y) {

    }

    @Override
    public void onDoubleTap(float x, float y) {

    }

    @Override
    public void onSwipeRight() {

    }

    @Override
    public void onSwipeLeft() {

    }

    @Override
    public void onSwipeBottom() {

    }

    @Override
    public void onSwipeTop() {

    }

    @Override
    public void onVisibilityChange(boolean isShow) {
        uzDragView.setVisibilityChange(isShow);
    }
}
