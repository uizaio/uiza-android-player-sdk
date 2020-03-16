package com.uiza.sampleplayer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.uiza.sdk.models.UZPlaybackInfo;
import com.uiza.sdk.util.LocalData;
import com.uiza.sdk.util.UZViewUtils;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;
import com.uiza.sdk.view.VDHView;
import com.uiza.sdk.widget.UZToast;

/**
 * Created by loitp on 9/1/2019.
 */

public class PlayerActivity extends AppCompatActivity implements UZCallback, VDHView.Callback, UZPlayerView.OnTouchEvent, UZVideoViewItemClick,
        UZPlayerView.ControllerStateCallback {
    private static final String[] urls = new String[]{
            "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd",
            "https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            "https://s3-ap-southeast-1.amazonaws.com/cdnetwork-test/drm_sample_byterange/manifest.mpd"};
    HorizontalScrollView llBottom;
    private UZVideoView uzVideo;
    private VDHView vdhv;
    private EditText etLinkPlay;
    private Button btPlay;
    private boolean isLive = false;

    public static void setLastCursorEditText(@NonNull EditText editText) {
        if (!editText.getText().toString().isEmpty()) {
            editText.setSelection(editText.getText().length());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        UZPlayer.setUseWithVDHView(true);
//        UizaCoreSDK.setCasty(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        uzVideo = findViewById(R.id.uiza_video);
        vdhv = findViewById(R.id.vdhv);
        llBottom = findViewById(R.id.hsv_bottom);
        etLinkPlay = findViewById(R.id.et_link_play);
        btPlay = findViewById(R.id.bt_play);
        vdhv.setCallback(this);
        vdhv.setOnTouchEvent(this);
        vdhv.setScreenRotate(false);
        uzVideo.setUZCallback(this);
        uzVideo.setUZVideoViewItemClick(this);
        uzVideo.addControllerStateCallback(this);
        // If linkplay is livestream, it will auto move to live edge when onResume is called
        uzVideo.setAutoMoveToLiveEdge(true);
        UZPlaybackInfo playbackInfo = null;
        if (getIntent() != null) {
            playbackInfo = getIntent().getParcelableExtra("extra_playback_info");
            if (playbackInfo != null) {
                llBottom.setVisibility(View.GONE);
                etLinkPlay.setVisibility(View.GONE);
                btPlay.setVisibility(View.GONE);
            } else {
                llBottom.setVisibility(View.VISIBLE);
                etLinkPlay.setVisibility(View.VISIBLE);
                btPlay.setVisibility(View.VISIBLE);
            }
        } else {
            llBottom.setVisibility(View.VISIBLE);
            etLinkPlay.setVisibility(View.VISIBLE);
            btPlay.setVisibility(View.VISIBLE);
        }
        btPlay.setEnabled(playbackInfo != null);
        etLinkPlay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence == null || charSequence.toString().isEmpty()) {
                    btPlay.setEnabled(false);
                } else {
                    btPlay.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        findViewById(R.id.bt_0).setOnClickListener(view -> {
            etLinkPlay.setText(urls[0]);
            setLastCursorEditText(etLinkPlay);
        });
        findViewById(R.id.bt_1).setOnClickListener(view -> {
            etLinkPlay.setText(urls[1]);
            isLive = true;
            setLastCursorEditText(etLinkPlay);
        });
        findViewById(R.id.bt_2).setOnClickListener(view -> {
            etLinkPlay.setText(urls[2]);
            isLive = true;
            setLastCursorEditText(etLinkPlay);
        });
        findViewById(R.id.bt_3).setOnClickListener(view -> {
            etLinkPlay.setText(urls[3]);
            setLastCursorEditText(etLinkPlay);
        });

        btPlay.setOnClickListener(view -> {
            final UZPlaybackInfo playback = new UZPlaybackInfo();
            playback.setHls(etLinkPlay.getText().toString());
            playback.setLive(isLive);
            UZPlayer.setCurrentPlaybackInfo(playback);
            boolean isInitSuccess = uzVideo.initCustomLinkPlay();
            if (!isInitSuccess) {
                UZToast.show(this, "Init failed");
            }
        });
        findViewById(R.id.bt_stats_for_nerds).setOnClickListener(v -> {
            if (uzVideo != null) {
                uzVideo.toggleStatsForNerds();
            }
        });
        if (playbackInfo != null) {
            boolean isInitSuccess = uzVideo.play(playbackInfo);
            if (!isInitSuccess) {
                UZToast.show(this, "Init failed");
            }
        }
        if (LocalData.getClickedPip()) {
            btPlay.performClick();
        }
    }

    @Override
    public void isInitResult(boolean isInitSuccess, boolean isGetDataSuccess, UZPlaybackInfo data) {
        vdhv.setInitResult(isInitSuccess);
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
        if (isInitMiniPlayerSuccess) {
            onBackPressed();
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
        vdhv.setScreenRotate(isLandscape);
    }

    @Override
    public void onError(UZException e) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uzVideo.onDestroyView();
        UZPlayer.setUseWithVDHView(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        uzVideo.onResumeView();
    }

    @Override
    public void onPause() {
        super.onPause();
        vdhv.onPause();
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
        if (isEnableRevertMaxSize && vdhv.isAppear()) {
            // todo
        }
    }

    @Override
    public void onViewSizeChange(boolean isMaximizeView) {

    }

    @Override
    public void onStateChange(VDHView.State state) {

    }

    @Override
    public void onPartChange(VDHView.Part part) {

    }

    @Override
    public void onViewPositionChanged(int left, int top, float dragOffset) {

    }

    @Override
    public void onOverScroll(VDHView.State state, VDHView.Part part) {
        uzVideo.pause();
        vdhv.dissappear();
    }

    @Override
    public void onEnableRevertMaxSize(boolean isEnableRevertMaxSize) {
        updateUIRevertMaxChange(!isEnableRevertMaxSize);
    }

    @Override
    public void onAppear(boolean isAppear) {
        updateUIRevertMaxChange(vdhv.isEnableRevertMaxSize());
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
        vdhv.setVisibilityChange(isShow);
    }
}
