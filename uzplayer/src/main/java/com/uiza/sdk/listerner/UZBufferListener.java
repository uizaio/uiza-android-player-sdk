package com.uiza.sdk.listerner;

public interface UZBufferListener {
    void onBufferChanged(long bufferedDurationUs, float playbackSpeed);
}
