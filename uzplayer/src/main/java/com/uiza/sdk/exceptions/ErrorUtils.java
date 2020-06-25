package com.uiza.sdk.exceptions;

public class ErrorUtils {

    public static UZException exceptionNoConnection() {
        return new UZException(ErrorConstant.ERR_CODE_0, ErrorConstant.ERR_0);
    }

    public static UZException exceptionNoLinkPlay() {
        return new UZException(ErrorConstant.ERR_CODE_5, ErrorConstant.ERR_5);
    }

    public static UZException exceptionTryAllLinkPlay() {
        return new UZException(ErrorConstant.ERR_CODE_6, ErrorConstant.ERR_6);
    }

    public static UZException exceptionSetup() {
        return new UZException(ErrorConstant.ERR_CODE_7, ErrorConstant.ERR_7);
    }

    public static UZException exceptionChangeSkin() {
        return new UZException(ErrorConstant.ERR_CODE_9, ErrorConstant.ERR_9);
    }

    public static UZException exceptionListHQ() {
        return new UZException(ErrorConstant.ERR_CODE_10, ErrorConstant.ERR_10);
    }

    public static UZException exceptionListAudio() {
        return new UZException(ErrorConstant.ERR_CODE_11, ErrorConstant.ERR_11);
    }

    public static UZException exceptionShowPip() {
        return new UZException(ErrorConstant.ERR_CODE_19, ErrorConstant.ERR_19);
    }

    public static UZException exceptionPlayback() {
        return new UZException(ErrorConstant.ERR_CODE_24, ErrorConstant.ERR_24);
    }

    public static UZException exceptionNoPlaylist() {
        return new UZException(ErrorConstant.ERR_CODE_25, ErrorConstant.ERR_25);
    }

    public static UZException exceptionPlaylistFolderItemFirst() {
        return new UZException(ErrorConstant.ERR_CODE_25, ErrorConstant.ERR_25);
    }

    public static UZException exceptionPlaylistFolderItemLast() {
        return new UZException(ErrorConstant.ERR_CODE_26, ErrorConstant.ERR_26);
    }
}
