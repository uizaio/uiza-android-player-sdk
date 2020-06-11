package com.uiza.sdk.utils;

import android.content.res.Resources;
import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;

public class ConvertUtils {

    private static final String EXT_X_PROGRAM_DATE_TIME = "#EXT-X-PROGRAM-DATE-TIME:";
    private static final String EXTINF = "#EXTINF:";

    private ConvertUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static int dp2px(float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(float pxValue) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static long getProgramDateTime(HlsMediaPlaylist playlist, long timeToEndChunk) {
        if (playlist == null || ListUtils.isEmpty(playlist.tags))
            return C.INDEX_UNSET;
        final String emptyStr = "";
        final int tagSize = playlist.tags.size();
        long totalTime = 0;
        int playingIndex = tagSize;
        // Find the playing frame index
        while (playingIndex > 0) {
            String tag = playlist.tags.get(playingIndex - 1);
            if (tag.contains(EXTINF)) {
                totalTime +=
                        Double.parseDouble(tag.replace(",", emptyStr).replace(EXTINF, emptyStr)) * 1000;
                if (totalTime >= timeToEndChunk) {
                    break;
                }
            }
            playingIndex--;
        }
        if (playingIndex >= tagSize) {
            // That means the livestream latency is larger than 1 segment (duration).
            // we should skip to calc latency in this case
            return C.INDEX_UNSET;
        }
        // Find the playing frame EXT_X_PROGRAM_DATE_TIME
        String playingDateTime = emptyStr;
        for (int i = playingIndex; i < tagSize; i++) {
            String tag = playlist.tags.get(i);
            if (tag.contains(EXT_X_PROGRAM_DATE_TIME)) {
                playingDateTime = tag.replace(EXT_X_PROGRAM_DATE_TIME, emptyStr);
                break;
            }
        }

        if (TextUtils.isEmpty(playingDateTime)) {
            // That means something wrong with the format, check with server
            // we should skip to calc latency in this case
            return C.INDEX_UNSET;
        }
        // int list of frame, we get the EXT_X_PROGRAM_DATE_TIME of current playing frame
        return StringUtils.convertUTCMs(playingDateTime);
    }
}
