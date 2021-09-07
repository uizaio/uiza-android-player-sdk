package com.uiza.sampleplayer.pip;

import android.app.PendingIntent;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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

import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class FragmentPlayer extends Fragment implements UZPlayerCallback {
    private UZVideoView uzVideo;
    private EditText etLinkPlay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CompositeDisposable disposables;

    private final String BROADCAST_ACTION_1 = "BROADCAST_ACTION_1";
    private final String BROADCAST_ACTION_2 = "BROADCAST_ACTION_2";
    private final String BROADCAST_ACTION_3 = "BROADCAST_ACTION_3";
    private final int REQUEST_CODE = 1221;
    private final ArrayList<RemoteAction> actions = new ArrayList<>();
    private BroadcastReceiver receiver;

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
        super.onViewCreated(view, savedInstanceState);
        setupActions();
        setupViews(view);
    }

    private void setupViews(View view) {
        uzVideo = view.findViewById(R.id.uz_video_view);
        etLinkPlay = view.findViewById(R.id.et_link_play);
        uzVideo.setPlayerCallback(this);
        uzVideo.setEnablePictureInPicture(true);
        uzVideo.setAutoMoveToLiveEdge(true);
        uzVideo.setActions(actions);

        etLinkPlay.setText("https://hls.ted.com/talks/2639.m3u8?preroll=Thousands");
//        etLinkPlay.setText("https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8");

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

    private void setupActions() {
        //You only customer the PIP controller if android SDK >= Android O
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            actions.clear();

            Intent actionIntent1 = new Intent(BROADCAST_ACTION_1);
            final PendingIntent pendingIntent1 = PendingIntent.getBroadcast(getContext(), REQUEST_CODE, actionIntent1, 0);
            final Icon icon1 = Icon.createWithResource(getContext(), android.R.drawable.ic_dialog_info);
            RemoteAction remoteAction1 = new RemoteAction(icon1, "Info", "Some info", pendingIntent1);
            actions.add(remoteAction1);

            Intent actionIntent2 = new Intent(BROADCAST_ACTION_2);
            final PendingIntent pendingIntent2 = PendingIntent.getBroadcast(getContext(), REQUEST_CODE, actionIntent2, 0);
            final Icon icon2 = Icon.createWithResource(getContext(), android.R.drawable.ic_btn_speak_now);
            RemoteAction remoteAction2 = new RemoteAction(icon2, "Speak", "Speak info", pendingIntent2);
            actions.add(remoteAction2);

            Intent actionIntent3 = new Intent(BROADCAST_ACTION_3);
            final PendingIntent pendingIntent3 = PendingIntent.getBroadcast(getContext(), REQUEST_CODE, actionIntent3, 0);
            final Icon icon3 = Icon.createWithResource(getContext(), R.drawable.ic_transparent);
            RemoteAction remoteAction3 = new RemoteAction(icon3, "Hello", "Hello", pendingIntent3);
            remoteAction3.setEnabled(false);//set false in case you want to disable this icon
            actions.add(remoteAction3);

        } else {
            Toast.makeText(getContext(), "Not supported", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPlay() {
        final UZPlayback playback = new UZPlayback();
        playback.addLinkPlay(etLinkPlay.getText().toString());
        uzVideo.play(playback);
    }

    @Override
    public void playerViewCreated(UZPlayerView playerView) {
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

        if (isInPictureInPictureMode) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BROADCAST_ACTION_1);
            filter.addAction(BROADCAST_ACTION_2);
            filter.addAction(BROADCAST_ACTION_3);
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    switch (intent.getAction()) {
                        case BROADCAST_ACTION_1:
                            Toast.makeText(context, "BROADCAST_ACTION_1", Toast.LENGTH_SHORT).show();
                            break;
                        case BROADCAST_ACTION_2:
                            Toast.makeText(context, "BROADCAST_ACTION_2 ", Toast.LENGTH_SHORT).show();
                            break;
                        case BROADCAST_ACTION_3:
                            Toast.makeText(context, "BROADCAST_ACTION_3 ", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };
            if (getContext() != null) {
                getContext().registerReceiver(receiver, filter);
            }
        } else {
            if (receiver != null && getContext() != null) {
                getContext().unregisterReceiver(receiver);
            }
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
