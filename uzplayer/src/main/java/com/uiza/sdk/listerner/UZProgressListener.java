package com.uiza.sdk.listerner;

public interface UZProgressListener {
    default void onAdProgress(int s, int duration, int percent) {
    }

    default void onAdEnded() {
    }

    default void onVideoProgress(long currentMls, int s, long duration, int percent) {
    }

    default void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
    }

    default void onBufferProgress(long bufferedPosition, int bufferedPercentage, long duration) {
    }
}
