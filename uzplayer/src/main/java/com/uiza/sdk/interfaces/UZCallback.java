package com.uiza.sdk.interfaces;

import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.models.UZPlayback;

public interface UZCallback {
    //when video init done with result
    //isInitSuccess onStateReadyFirst
    void isInitResult(boolean isInitSuccess, UZPlayback playback);

    //when skin is changed
    void onSkinChange();

    //when screen rotate
    void onScreenRotate(boolean isLandscape);

    //when UZVideoView had an error
    void onError(UZException e);

}