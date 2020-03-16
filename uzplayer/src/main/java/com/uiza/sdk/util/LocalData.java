package com.uiza.sdk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.core.content.ContextCompat;

import com.uiza.sdk.R;

/**
 * Created by loitp on 16/1/2019.
 */

public class LocalData {

    //=============================================================================START PREF
    private final static String PREFERENCES_FILE_NAME = "com.uiza.sdk::uzplayer";
    private final static String CLICKED_PIP = "CLICKED_PIP";
    //private final static String CLASS_NAME_OF_PLAYER = "CLASS_NAME_OF_PLAYER";
    private final static String IS_INIT_PLAYLIST_FOLDER = "IS_INIT_PLAYLIST_FOLDER";
    private final static String VIDEO_WIDTH = "VIDEO_WIDTH";
    private final static String VIDEO_HEIGHT = "VIDEO_HEIGHT";
    private final static String MINI_PLAYER_COLOR_VIEW_DESTROY = "MINI_PLAYER_COLOR_VIEW_DESTROY";
    private final static String MINI_PLAYER_TAP_TO_FULL_PLAYER = "MINI_PLAYER_TAP_TO_FULL_PLAYER";
    private final static String MINI_PLAYER_EZ_DESTROY = "MINI_PLAYER_EZ_DESTROY";
    private final static String MINI_PLAYER_ENABLE_VIBRATION = "MINI_PLAYER_ENABLE_VIBRATION";
    private final static String MINI_PLAYER_ENABLE_SMOOTH_SWITCH = "MINI_PLAYER_ENABLE_SMOOTH_SWITCH";
    private final static String MINI_PLAYER_AUTO_SIZE = "MINI_PLAYER_AUTO_SIZE";
    //private final static String MINI_PLAYER_CONTENT_POSITION_WHEN_SWITCH_TO_FULL_PLAYER = "MINI_PLAYER_CONTENT_POSITION_WHEN_SWITCH_TO_FULL_PLAYER";
    private final static String MINI_PLAYER_FIRST_POSITION_X = "MINI_PLAYER_FIRST_POSITION_X";
    private final static String MINI_PLAYER_FIRST_POSITION_Y = "MINI_PLAYER_FIRST_POSITION_Y";
    private final static String MINI_PLAYER_MARGIN_L = "MINI_PLAYER_MARGIN_L";
    private final static String MINI_PLAYER_MARGIN_T = "MINI_PLAYER_MARGIN_T";
    private final static String MINI_PLAYER_MARGIN_R = "MINI_PLAYER_MARGIN_R";

    //=============================================================================END FOR UIZA V3
    private final static String MINI_PLAYER_MARGIN_B = "MINI_PLAYER_MARGIN_B";
    private final static String MINI_PLAYER_SIZE_WIDTH = "MINI_PLAYER_SIZE_WIDTH";
    private final static String MINI_PLAYER_SIZE_HEIGHT = "MINI_PLAYER_SIZE_HEIGHT";
    private final static String LAST_SYNCED_SERVER_TIME = "LAST_SYNCED_SERVER_TIME";
    private final static String LAST_ELAPSED_TIME = "LAST_ELAPSED_TIME";
    private final static String STABLE_PIP_TOP_POSITION = "STABLE_PIP_TOP_POSITION";
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static SharedPreferences sharedPreferences;

    private LocalData() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void init(Context context) {
        LocalData.context = context.getApplicationContext();
    }

    public static Context getContext() {
        if (context != null) return context;
        throw new NullPointerException("u should init first");
    }

    private static SharedPreferences getPrivatePreference(Context context) {
        if (sharedPreferences == null)
            sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE_NAME, 0);
        return sharedPreferences;
    }

    /////////////////////////////////BOOLEAN
    public static Boolean isInitPlaylistFolder() {
        return (Boolean) SharedPrefUtils.get(getPrivatePreference(context), IS_INIT_PLAYLIST_FOLDER, false);
    }

    public static void setIsInitPlaylistFolder(Boolean value) {
        SharedPrefUtils.put(getPrivatePreference(context), IS_INIT_PLAYLIST_FOLDER, value);
    }

    public static Boolean getClickedPip() {
        return (Boolean) SharedPrefUtils.get(getPrivatePreference(context), CLICKED_PIP, false);
    }

    public static void setClickedPip(Boolean value) {
        SharedPrefUtils.put(getPrivatePreference(context), CLICKED_PIP, value);
    }

    public static Boolean getMiniPlayerEzDestroy() {
        return (Boolean) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_EZ_DESTROY, false);
    }

    public static void setMiniPlayerEzDestroy(Boolean value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_EZ_DESTROY, value);
    }

    public static Boolean getMiniPlayerEnableVibration() {
        return (Boolean) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_ENABLE_VIBRATION, false);
    }

    public static void setMiniPlayerEnableVibration(Boolean value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_ENABLE_VIBRATION, value);
    }

    public static Boolean getMiniPlayerTapToFullPlayer() {
        return (Boolean) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_TAP_TO_FULL_PLAYER, true);
    }

    public static void setMiniPlayerTapToFullPlayer(Boolean value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_TAP_TO_FULL_PLAYER, value);
    }

    public static Boolean getMiniPlayerEnableSmoothSwitch() {
        return (Boolean) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_ENABLE_SMOOTH_SWITCH, true);
    }

    public static void setMiniPlayerEnableSmoothSwitch(Boolean value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_ENABLE_SMOOTH_SWITCH, value);
    }

    public static Boolean getMiniPlayerAutoSize() {
        return (Boolean) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_AUTO_SIZE, true);
    }

    private static void setMiniPlayerAutoSize(Boolean value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_AUTO_SIZE, value);
    }

    /////////////////////////////////INT
    public static int getVideoWidth() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), VIDEO_WIDTH, 16);
    }

    public static void setVideoWidth(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), VIDEO_WIDTH, value);
    }

    public static int getVideoHeight() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), VIDEO_HEIGHT, 9);
    }

    public static void setVideoHeight(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), VIDEO_HEIGHT, value);
    }

    public static int getMiniPlayerColorViewDestroy() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_COLOR_VIEW_DESTROY, ContextCompat.getColor(context, R.color.black_65));
    }

    public static void setMiniPlayerColorViewDestroy(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_COLOR_VIEW_DESTROY, value);
    }

    public static int getMiniPlayerFirstPositionX() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_FIRST_POSITION_X, -1);
    }

    private static void setMiniPlayerFirstPositionX(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_FIRST_POSITION_X, value);
    }

    public static int getMiniPlayerFirstPositionY() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_FIRST_POSITION_Y, -1);
    }

    private static void setMiniPlayerFirstPositionY(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_FIRST_POSITION_Y, value);
    }

    public static void setMiniPlayerFirstPosition(int firstPositionX, int firstPositionY) {
        setMiniPlayerFirstPositionX(firstPositionX);
        setMiniPlayerFirstPositionY(firstPositionY);
    }

    public static int getMiniPlayerMarginL() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_MARGIN_L, 0);
    }

    private static void setMiniPlayerMarginL(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_MARGIN_L, value);
    }

    public static int getMiniPlayerMarginT() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_MARGIN_T, 0);
    }

    private static void setMiniPlayerMarginT(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_MARGIN_T, value);
    }

    public static int getMiniPlayerMarginR() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_MARGIN_R, 0);
    }

    private static void setMiniPlayerMarginR(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_MARGIN_R, value);
    }

    public static int getMiniPlayerMarginB() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_MARGIN_B, 0);
    }

    private static void setMiniPlayerMarginB(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_MARGIN_B, value);
    }

    public static int getMiniPlayerSizeWidth() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_SIZE_WIDTH, Constants.W_320);
    }

    private static void setMiniPlayerSizeWidth(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_SIZE_WIDTH, value);
    }

    public static int getMiniPlayerSizeHeight() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), MINI_PLAYER_SIZE_HEIGHT, Constants.W_180);
    }

    private static void setMiniPlayerSizeHeight(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), MINI_PLAYER_SIZE_HEIGHT, value);
    }

    public static boolean setMiniPlayerMarginDp(float marginL, float marginT, float marginR, float marginB) {
        int pxL = ConvertUtils.dp2px(marginL);
        int pxT = ConvertUtils.dp2px(marginT);
        int pxR = ConvertUtils.dp2px(marginR);
        int pxB = ConvertUtils.dp2px(marginB);
        return setMiniPlayerMarginPixel(pxL, pxT, pxR, pxB);
    }

    public static boolean setMiniPlayerMarginPixel(int marginL, int marginT, int marginR, int marginB) {
        int screenW = UZViewUtils.getScreenWidth();
        int screenH = UZViewUtils.getScreenHeight();
        int rangeMarginW = screenW / 5;
        int rangeMarginH = screenH / 5;
        if (marginL < 0 || marginL > rangeMarginW)
            throw new IllegalArgumentException("Error: marginL is invalid, the right value must from 0px to " + rangeMarginW + "px or 0dp to " + ConvertUtils.px2dp(rangeMarginW) + "dp");
        if (marginT < 0 || marginT > rangeMarginH)
            throw new IllegalArgumentException("Error: marginT is invalid, the right value must from 0px to " + rangeMarginH + "px or 0dp to " + ConvertUtils.px2dp(rangeMarginH) + "dp");
        if (marginR < 0 || marginR > rangeMarginW)
            throw new IllegalArgumentException("Error: marginR is invalid, the right value must from 0px to " + rangeMarginW + "px or 0dp to " + ConvertUtils.px2dp(rangeMarginW) + "dp");
        if (marginB < 0 || marginB > rangeMarginH)
            throw new IllegalArgumentException("Error: marginB is invalid, the right value must from 0px to " + rangeMarginH + "px or 0dp to " + ConvertUtils.px2dp(rangeMarginH) + "dp");

        setMiniPlayerMarginL(marginL);
        setMiniPlayerMarginT(marginT);
        setMiniPlayerMarginR(marginR);
        setMiniPlayerMarginB(marginB);
        return true;
    }

    public static boolean setMiniPlayerSizeDp(boolean isAutoSize, int videoWidthDp, int videoHeightDp) {
        int pxW = ConvertUtils.dp2px(videoWidthDp);
        int pxH = ConvertUtils.dp2px(videoHeightDp);
        return setMiniPlayerSizePixel(isAutoSize, pxW, pxH);
    }

    public static boolean setMiniPlayerSizePixel(boolean isAutoSize, int videoWidthPx, int videoHeightPx) {
        setMiniPlayerAutoSize(isAutoSize);
        if (isAutoSize) {
            setMiniPlayerSizeWidth(Constants.W_320);
            setMiniPlayerSizeHeight(Constants.W_180);
            return true;
        }
        int screenWPx = UZViewUtils.getScreenWidth();
        int screenHPx = UZViewUtils.getScreenHeight();
        if (videoWidthPx < 0 || videoWidthPx > screenWPx)
            throw new IllegalArgumentException("Error: videoWidthPx is invalid, the right value must from 0px to " + screenWPx + "px or 0dp to " + ConvertUtils.px2dp(screenWPx) + "dp");
        if (videoHeightPx < 0 || videoHeightPx > screenHPx)
            throw new IllegalArgumentException("Error: videoHeightPx is invalid, the right value must from 0px to " + screenHPx + "px or 0dp to " + ConvertUtils.px2dp(screenHPx) + "dp");

        setMiniPlayerSizeWidth(videoWidthPx);
        setMiniPlayerSizeHeight(videoHeightPx);
        return true;
    }

    public static int getStablePipTopPosition() {
        return (Integer) SharedPrefUtils.get(getPrivatePreference(context), STABLE_PIP_TOP_POSITION, 0);
    }

    public static void setStablePipTopPosition(int value) {
        SharedPrefUtils.put(getPrivatePreference(context), STABLE_PIP_TOP_POSITION, value);
    }

    public static void saveLastServerTime(long currentTimeMillis) {
        SharedPrefUtils.put(getPrivatePreference(context), LAST_SYNCED_SERVER_TIME, currentTimeMillis);
    }

    public static long getLastServerTime() {
        return (long) SharedPrefUtils.get(getPrivatePreference(context), LAST_SYNCED_SERVER_TIME,
                System.currentTimeMillis());
    }

    public static void saveLastElapsedTime(long elapsedTime) {
        SharedPrefUtils.put(getPrivatePreference(context), LAST_ELAPSED_TIME, elapsedTime);
    }

    public static long getLastElapsedTime() {
        return (long) SharedPrefUtils.get(getPrivatePreference(context), LAST_ELAPSED_TIME,
                SystemClock.elapsedRealtime());
    }
}
