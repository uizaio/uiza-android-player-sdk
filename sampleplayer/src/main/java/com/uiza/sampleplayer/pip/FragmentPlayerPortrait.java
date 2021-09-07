package com.uiza.sampleplayer.pip;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.uiza.sampleplayer.R;
import com.uiza.sdk.UZPlayer;
import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.interfaces.UZPlayerCallback;
import com.uiza.sdk.models.UZPlayback;
import com.uiza.sdk.utils.UZViewUtils;
import com.uiza.sdk.view.UZPlayerView;
import com.uiza.sdk.view.UZVideoView;

import timber.log.Timber;

public class FragmentPlayerPortrait extends Fragment implements UZPlayerCallback {
    private UZVideoView uzVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_default);
        return inflater.inflate(R.layout.fragment_player_portrait, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupViews(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupViews(View view) {
        uzVideo = view.findViewById(R.id.uz_video_view);
        uzVideo.setPlayerCallback(this);
        uzVideo.setEnablePictureInPicture(true);
        uzVideo.setAutoMoveToLiveEdge(true);
        uzVideo.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
//                uzVideo.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                uzVideo.setFreeSize(true);
                uzVideo.setSize(UZViewUtils.getScreenWidth(), UZViewUtils.getScreenHeight());
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        });

        (new Handler()).postDelayed(this::onPlay, 100);

        if (getActivity() != null) {
            getActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (!uzVideo.onBackPressed()) {
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    }
                }
            });
        }
    }

    private void onPlay() {
        final UZPlayback playback = new UZPlayback();
        playback.addLinkPlay("https://assets.mixkit.co/videos/preview/mixkit-portrait-of-a-fashion-woman-with-silver-makeup-39875-large.mp4");
        uzVideo.play(playback);
    }

    @Override
    public void playerViewCreated(UZPlayerView playerView) {
        playerView.setControllerStateCallback(visible -> {
        });
    }

    @Override
    public void isInitResult(String linkPlay) {

    }

    @Override
    public void onError(UZException e) {
        Timber.e(e);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        uzVideo.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            uzVideo.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onScreenRotate(boolean isLandscape) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        uzVideo.onPictureInPictureModeChanged(isInPictureInPictureMode, null);
        if (!isInPictureInPictureMode) {
            uzVideo.postDelayed(() -> {
//                uzVideo.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                uzVideo.setFreeSize(true);
//                uzVideo.setSize(UZViewUtils.getScreenWidth(), UZViewUtils.getScreenHeight());
            }, 10);
        }
    }

    void onUserLeaveHint() {
        try {
            if (!uzVideo.isLandscape()) {
                uzVideo.enterPIPMode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
