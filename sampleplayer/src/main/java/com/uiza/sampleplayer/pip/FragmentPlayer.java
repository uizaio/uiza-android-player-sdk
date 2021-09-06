package com.uiza.sampleplayer.pip;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.uiza.api.UZApi;
import com.uiza.sampleplayer.R;
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

public class FragmentPlayer extends Fragment implements UZPlayerCallback {
    private UZVideoView uzVideo;
    private EditText etLinkPlay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CompositeDisposable disposables;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UZPlayer.setUZPlayerSkinLayoutId(R.layout.uzplayer_skin_default);
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupViews(view);
        super.onViewCreated(view, savedInstanceState);
    }

    private void setupViews(View view) {
        uzVideo = view.findViewById(R.id.uz_video_view);
        etLinkPlay = view.findViewById(R.id.et_link_play);
        uzVideo.setPlayerCallback(this);
        uzVideo.setEnablePictureInPicture(true);
        uzVideo.setAutoMoveToLiveEdge(true);

        etLinkPlay.setText("https://hls.ted.com/talks/2639.m3u8?preroll=Thousands");

        view.findViewById(R.id.btn_play).setOnClickListener(v -> onPlay());
        disposables = new CompositeDisposable();
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
        if (!isLandscape) {
            int w = UZViewUtils.getScreenWidth();
            int h = w * 9 / 16;
            uzVideo.setFreeSize(false);
            uzVideo.setSize(w, h);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        uzVideo.onPictureInPictureModeChanged(isInPictureInPictureMode, null);
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
