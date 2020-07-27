package com.uiza.sdk.interfaces;

import com.uiza.sdk.exceptions.UZException;

public interface UZPlayerCallback {
    //when video init done with result
    //isInitSuccess onStateReadyFirst
    default void isInitResult(String linkPlay) {
    }

    //when skin is changed
    default void onSkinChange() {
    }

    default void onShowUserController(boolean show) {

    }

    default void onTimeShiftChange(boolean timeShiftOn) {
    }

    //when screen rotate
    default void onScreenRotate(boolean isLandscape) {
    }

    //when UZVideoView had an error
    default void onError(UZException e) {
    }

}