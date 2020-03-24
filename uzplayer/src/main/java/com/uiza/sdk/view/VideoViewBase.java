package com.uiza.sdk.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.uiza.sdk.models.UZPlayback;

import java.util.List;


public abstract class VideoViewBase extends RelativeLayout {

    private boolean isInit = false;

    public VideoViewBase(Context context) {
        this(context, null);
    }

    public VideoViewBase(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoViewBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInitView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoViewBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        onInitView();
    }

    /**
     * constructors call 2 times, use isInit like a flag
     */
    private void onInitView() {
//        if (!isInit) {
//            onCreateView();
//            isInit = true;
//        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInit) {
            onCreateView();
            isInit = true;
        }
    }

    public abstract void onCreateView();

    /**
     * Play from custom Playback
     *
     * @return true if success init
     */
    public abstract boolean play();

    /**
     * Play a playback
     *
     * @param playback
     * @return true if success init
     */
    public abstract boolean play(@NonNull UZPlayback playback);

    /**
     * play a playlist
     *
     * @param playlist
     * @return true if success init
     */
    public abstract boolean play(List<UZPlayback> playlist);

    public abstract long getCurrentPosition();

    public abstract void seekTo(long positionMs);

    public abstract void pause();

    public abstract void resume();

    public abstract int getVideoWidth();

    public abstract int getVideoHeight();

    public abstract SimpleExoPlayer getPlayer();

    public interface ProgressListener {
        void onAdProgress(int s, int duration, int percent);

        void onAdEnded();

        void onVideoProgress(long currentMls, int s, long duration, int percent);

        void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        void onBufferProgress(long bufferedPosition, int bufferedPercentage, long duration);
    }
}
