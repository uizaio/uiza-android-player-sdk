package com.uiza.sampleplayer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZPlayerCallback;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;
import com.uiza.sdk.widget.UZToast;

public class CastPlayerActivity extends AppCompatActivity implements UZPlayerCallback, UZPlayerView.OnSingleTap {
    private UZVideoView uzVideo;
    private EditText etLinkPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UZPlayer.setCasty(this);
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cast_player);
        uzVideo = findViewById(R.id.uz_video_view);
        etLinkPlay = findViewById(R.id.et_link_play);
        UZPlayer.getCasty().setUpMediaRouteButton(findViewById(R.id.media_route_button));
        uzVideo.setPlayerCallback(this);
        uzVideo.setOnSingleTap(this);
        // If linkplay is livestream, it will auto move to live edge when onResume is called
        uzVideo.setAutoMoveToLiveEdge(true);
        etLinkPlay.setText(LSApplication.urls[0]);
        findViewById(R.id.btn_play).setOnClickListener(view -> onPlay());
    }

    private void onPlay() {
        final UZPlayback playback = new UZPlayback();
        playback.setPoster(LSApplication.thumbnailUrl);
        playback.addLinkPlay(etLinkPlay.getText().toString());
        uzVideo.play(playback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        UZPlayer.getCasty().addMediaRouteMenuItem(menu);
        return true;
    }

    @Override
    public void isInitResult(String linkPlay) {

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
        UZToast.show(this, e.getLocalizedMessage());
    }

    @Override
    public void onSingleTapConfirmed(float x, float y) {
        uzVideo.toggleShowHideController();
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
}
