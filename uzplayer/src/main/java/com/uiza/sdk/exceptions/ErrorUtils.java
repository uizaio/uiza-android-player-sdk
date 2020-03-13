package com.uiza.sdk.exceptions;

public class ErrorUtils {

    public static UZException exceptionNoConnection() {
        return new UZException(ErrorConstant.ERR_CODE_0, ErrorConstant.ERR_0);
    }

    public static UZException exceptionCannotGetDetailEntity() {
        return new UZException(ErrorConstant.ERR_CODE_1, ErrorConstant.ERR_1);
    }

    public static UZException exceptionNoTokenStreaming() {
        return new UZException(ErrorConstant.ERR_CODE_2, ErrorConstant.ERR_2);
    }

    public static UZException exceptionCannotGetLinkPlayLive() {
        return new UZException(ErrorConstant.ERR_CODE_3, ErrorConstant.ERR_3);
    }

    public static UZException exceptionCannotGetLinkPlayVOD() {
        return new UZException(ErrorConstant.ERR_CODE_4, ErrorConstant.ERR_4);
    }

    public static UZException exceptionEntityId() {
        return new UZException(ErrorConstant.ERR_CODE_5, ErrorConstant.ERR_5);
    }

    public static UZException exceptionTryAllLinkPlay() {
        return new UZException(ErrorConstant.ERR_CODE_6, ErrorConstant.ERR_6);
    }

    public static UZException exceptionSetup() {
        return new UZException(ErrorConstant.ERR_CODE_7, ErrorConstant.ERR_7);
    }

    public static UZException exceptionListAllEntity() {
        return new UZException(ErrorConstant.ERR_CODE_8, ErrorConstant.ERR_8);
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

    public static UZException exceptionRetrieveALiveEvent() {
        return new UZException(ErrorConstant.ERR_CODE_21, ErrorConstant.ERR_21);
    }

    public static UZException exceptionPlayback() {
        return new UZException(ErrorConstant.ERR_CODE_24, ErrorConstant.ERR_24);
    }

    public static UZException exceptionPlaylistFolderItemFirst() {
        return new UZException(ErrorConstant.ERR_CODE_25, ErrorConstant.ERR_25);
    }

    public static UZException exceptionPlaylistFolderItemLast() {
        return new UZException(ErrorConstant.ERR_CODE_26, ErrorConstant.ERR_26);
    }

    public static UZException exceptionPlayerInfo() {
        return new UZException(ErrorConstant.ERR_CODE_27, ErrorConstant.ERR_27);
    }
}
