package com.uiza.sdk.interfaces;

public interface UZLiveContentCallback {
    default void onUpdateLiveInfoTimeStartLive(long duration, String hhmmss) {
    }

    default void onUpdateLiveInfoCurrentView(long watchNow) {
    }

    default void onLiveStreamUnAvailable() {
    }
}
