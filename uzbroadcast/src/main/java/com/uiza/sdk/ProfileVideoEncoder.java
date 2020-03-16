package com.uiza.sdk;

//

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * Profile Encoding for Live
 * Codec H264
 */
public class ProfileVideoEncoder {
//    P1080(2800 * 1024, 1920, 1080), //bandwidth 2800 Kbps
//    P720(1400 * 1024, 1280, 720), // bandwidth 1400 Kbps
//    P480(1000 * 1024, 854, 480), //bandwidth 1000 Kbps
//    P360(600 * 1024, 640, 360); // //bandwidth 600 Kbps

    /**
     * H264 in kb.
     */
    private final int bitrate;
    /**
     * resolution in px.
     */
    private final int width;
    /**
     * resolution in px.
     */
    private final int height;

    /**
     * frame rate
     */
    private final int fps;
    /**
     * IFrameInterval
     */
    private final int frameInterval;

    private ProfileVideoEncoder(int width, int height, int fps, int bitrate, int frameInterval) {
        if (width > 1920 || width < 0) {
            throw new IllegalArgumentException("You must set with of resolution <= 1920");
        }
        if (height > 1080 || height < 0) {
            throw new IllegalArgumentException("You must set height of resolution <= 1080");
        }
        if (fps > 60 || fps <= 0) {
            throw new IllegalArgumentException("You must set fps <= 60 and fps > 0");
        }
        if (bitrate > 5500000 || bitrate <= 0) { // max 5.5MB
            throw new IllegalArgumentException("You must set bitrate <= 5.500.000 and bitrate > 0");
        }
        if (frameInterval > 10 || frameInterval <= 0) {
            throw new IllegalArgumentException("You must set frameInterval <= 10 and frameInterval > 0");
        }
        this.width = width;
        this.height = height;
        this.bitrate = bitrate;
        this.fps = fps;
        this.frameInterval = frameInterval;
    }

    public static ProfileVideoEncoder create1080p(int fps, int bitrate) {
        return new ProfileVideoEncoder(1920, 1080, fps, bitrate, 2);
    }

    public static ProfileVideoEncoder create1080p(int fps, int bitrate, int frameInterval) {
        return new ProfileVideoEncoder(1920, 1080, fps, bitrate, frameInterval);
    }

    public static ProfileVideoEncoder create720p(int fps, int bitrate) {
        return new ProfileVideoEncoder(1280, 720, fps, bitrate, 2);
    }

    public static ProfileVideoEncoder create720p(int fps, int bitrate, int frameInterval) {
        return new ProfileVideoEncoder(1280, 720, fps, bitrate, frameInterval);
    }

    public static ProfileVideoEncoder create480p(int fps, int bitrate) {
        return new ProfileVideoEncoder(854, 480, fps, bitrate, 2);
    }

    public static ProfileVideoEncoder create480p(int fps, int bitrate, int frameInterval) {
        return new ProfileVideoEncoder(854, 480, fps, bitrate, frameInterval);
    }

    public static ProfileVideoEncoder create360p(int fps, int bitrate) {
        return new ProfileVideoEncoder(640, 360, fps, bitrate, 2);
    }

    public static ProfileVideoEncoder create360p(int fps, int bitrate, int frameInterval) {
        return new ProfileVideoEncoder(640, 360, fps, bitrate, frameInterval);
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getFps() {
        return fps;
    }

    public int getFrameInterval() {
        return frameInterval;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),"Profile(resolution: %dx%d, fps: %d, bitrate: %d, iFrameInterval: %d)",width, height, fps, bitrate, frameInterval);
    }
}