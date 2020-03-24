package com.uiza.sampleplayer;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import com.uiza.sdk.util.UZViewUtils;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;
import com.uiza.sdk.widget.UZToast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loitp on 9/1/2019.
 */

public class PipPlayerActivity extends AppCompatActivity implements UZCallback, UZPlayerView.OnSingleTap, UZVideoViewItemClick {
    private static final String[] urls = new String[]{
            "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd",
            "https://mnmedias.api.telequebec.tv/m3u8/29880.m3u8",
            "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            "https://s3-ap-southeast-1.amazonaws.com/cdnetwork-test/drm_sample_byterange/manifest.mpd"};
    HorizontalScrollView llBottom;
    private UZVideoView uzVideo;
    private EditText etLinkPlay;
    private List<UZPlayback> playlist;

    public static void setLastCursorEditText(@NonNull EditText editText) {
        if (!editText.getText().toString().isEmpty()) {
            editText.setSelection(editText.getText().length());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        UZPlayer.setCasty(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pip_player);
        uzVideo = findViewById(R.id.uz_video_view);
        llBottom = findViewById(R.id.hsv_bottom);
        etLinkPlay = findViewById(R.id.et_link_play);
        Button btPlaylist = findViewById(R.id.bt_playlist);
        uzVideo.setUZCallback(this);
        uzVideo.setUZVideoViewItemClick(this);
        uzVideo.setOnSingleTap(this);
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

        btPlaylist.setOnClickListener(view -> uzVideo.play(playlist));
        findViewById(R.id.bt_stats_for_nerds).setOnClickListener(v -> {
            if (uzVideo != null)
                uzVideo.toggleStatsForNerds();
        });
        if (playbackInfo != null) {
            boolean isInitSuccess = uzVideo.play(playbackInfo);
            if (!isInitSuccess)
                UZToast.show(this, "Init failed");
        }
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

}
