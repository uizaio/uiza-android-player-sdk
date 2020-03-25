package com.uiza.sampleplayer;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZCallback;
import com.uiza.sdk.interfaces.UZVideoViewItemClick;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;
import com.uiza.sdk.widget.UZToast;

/**
 * Created by loitp on 9/1/2019.
 */

public class PipPlayerActivity extends AppCompatActivity implements UZCallback, UZPlayerView.OnSingleTap, UZVideoViewItemClick {

    private UZVideoView uzVideo;
    private EditText etLinkPlay;

    public static void setLastCursorEditText(@NonNull EditText editText) {
        if (!editText.getText().toString().isEmpty()) {
            editText.setSelection(editText.getText().length());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        UZPlayer.setCasty(this);
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_1);
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
            etLinkPlay.setText(LSApplication.urls[1]);

        findViewById(R.id.btn_play).setOnClickListener(view -> {
            onPlay(false);
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
    }

    private void onPlay(boolean live) {
        final UZPlayback playback = new UZPlayback();
        playback.setThumbnail("https://i.insider.com/5ae1e2b3bd96711e008b4704?width=1100&format=jpeg&auto=webp");
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
