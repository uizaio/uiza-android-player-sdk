package com.uiza.sdk.interfaces;

import com.uiza.sdk.exceptions.UZException;
import com.uiza.sdk.models.UZPlaybackInfo;

public interface UZCallback {
    //when video init done with result
    //isInitSuccess onStateReadyFirst
    //isGetDataSuccess da co data ResultGetLinkPlay va Data
    void isInitResult(boolean isInitSuccess, boolean isGetDataSuccess, UZPlaybackInfo playback);

    //when pip video is init success
    void onStateMiniPlayer(boolean isInitMiniPlayerSuccess);

    //when skin is changed
    void onSkinChange();

    //when screen rotate
    void onScreenRotate(boolean isLandscape);

    //when uiimavideo had an error
    void onError(UZException e);

}