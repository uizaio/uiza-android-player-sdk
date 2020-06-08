package com.uiza.sdk.interfaces;

import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.models.UZPlayback;

public interface UZCallback {
    //when video init done with result
    //isInitSuccess onStateReadyFirst
    default void isInitResult(boolean isInitSuccess, UZPlayback playback) {
    }

    //when skin is changed
    default void onSkinChange() {
    }

    //when screen rotate
    default void onScreenRotate(boolean isLandscape) {
    }

    //when UZVideoView had an error
    default void onError(UZException e) {
    }

}