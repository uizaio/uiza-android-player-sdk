package com.uiza.sdk.profile;

//

import android.media.MediaCodecInfo;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.uiza.sdk.util.ValidValues;

import java.util.Locale;

/**
 * Video Profile for Live
 * Codec H264
 */
public class VideoAttributes implements Parcelable {

    public static final Creator<VideoAttributes> CREATOR = new Creator<VideoAttributes>() {
        @Override
        public VideoAttributes createFromParcel(Parcel in) {
            return new VideoAttributes(in);
        }

        @Override
        public VideoAttributes[] newArray(int size) {
            return new VideoAttributes[size];
        }
    };

    private int MAX_BITRATE = 6000000; // 6000 kps

    /**
     * The video size for the encoding process.
     * default 1080p.
     */
    private VideoSize size = VideoSize.FHD_1080p();
    /**
     * The bitrate value for the encoding process.
     * default 6000000 (6MB)
     */
    private int bitRate = MAX_BITRATE;
    /**
     * The frame rate value for the encoding process.
     * default 30 fps
     */
    private int frameRate = 30;

    /**
     * The frame interval for the encoding process.
     * default 2 seconds.
     */
    private int frameInterval = 2; // sec

    private VideoAttributes() {
    }

    private VideoAttributes(Parcel in) {
        size = in.readParcelable(VideoSize.class.getClassLoader());
        bitRate = in.readInt();
        frameRate = in.readInt();
        frameInterval = in.readInt();
    }

    private VideoAttributes(VideoSize size, int frameRate, int bitRate, int frameInterval) {
        if (!size.isValid()) {
            throw new IllegalArgumentException("You must set size in [0,0] to [1920, 1080]");
        }
        ValidValues.check(frameRate, 1, 60);
        ValidValues.check(bitRate, 1, 6000000); // max 6MB
        ValidValues.check(frameInterval, 1, 10);
        this.size = size;
        this.bitRate = bitRate;
        this.frameRate = frameRate;
        this.frameInterval = frameInterval;
    }

    public static VideoAttributes FHD_1080p(int frameRate, int bitRate) {
        return new VideoAttributes(VideoSize.FHD_1080p(), frameRate, bitRate, 2);
    }

    public static VideoAttributes FHD_1080p(int frameRate, int bitRate, int frameInterval) {
        return new VideoAttributes(VideoSize.FHD_1080p(), frameRate, bitRate, frameInterval);
    }

    public static VideoAttributes HD_720p(int frameRate, int bitRate) {
        return new VideoAttributes(VideoSize.HD_720p(), frameRate, bitRate, 2);
    }

    public static VideoAttributes HD_720p(int frameRate, int bitRate, int frameInterval) {
        return new VideoAttributes(VideoSize.HD_720p(), frameRate, bitRate, frameInterval);
    }

    public static VideoAttributes SD_480p(int frameRate, int bitRate) {
        return new VideoAttributes(VideoSize.SD_480p(), frameRate, bitRate, 2);
    }

    public static VideoAttributes SD_480p(int frameRate, int bitRate, int frameInterval) {
        return new VideoAttributes(VideoSize.SD_480p(), frameRate, bitRate, frameInterval);
    }

    public static VideoAttributes SD_360p(int frameRate, int bitRate) {
        return new VideoAttributes(VideoSize.SD_360p(), frameRate, bitRate, 2);
    }

    public static VideoAttributes SD_360p(int frameRate, int bitRate, int frameInterval) {
        return new VideoAttributes(VideoSize.SD_360p(), frameRate, bitRate, frameInterval);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(size, flags);
        dest.writeInt(bitRate);
        dest.writeInt(frameRate);
        dest.writeInt(frameInterval);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Returns the bitrate value for the encoding process.
     *
     * @return The bitrate value for the encoding process.
     */
    public int getBitRate() {
        return bitRate;
    }

    /**
     * Sets the bitrate value for the encoding process. If null or not specified
     * a default value will be picked.
     *
     * @param bitRate The bitrate value for the encoding process.
     * @return this instance
     */
    public VideoAttributes setBitRate(int bitRate) {
        this.bitRate = bitRate;
        return this;
    }

    /**
     * Returns the video size for the encoding process.
     *
     * @return The video size for the encoding process.
     */
    public VideoSize getSize() {
        return size;
    }

    /**
     * Sets the video size for the encoding process. If null or not specified
     * the source video size will not be modified.
     *
     * @param size he video size for the encoding process.
     * @return this instance
     */
    public VideoAttributes setSize(VideoSize size) {
        this.size = size;
        return this;
    }

    /**
     * Returns the frame rate value for the encoding process.
     *
     * @return The frame rate value for the encoding process.
     */
    public int getFrameRate() {
        return frameRate;
    }

    /**
     * Sets the frame rate value for the encoding process. If null or not
     * specified a default value will be picked.
     *
     * @param frameRate The frame rate value for the encoding process.
     * @return this instance
     */
    public VideoAttributes setFrameRate(int frameRate) {
        this.frameRate = frameRate;
        return this;
    }

    /**
     * @return the Frame Interval value for the encoding process.
     */
    public int getFrameInterval() {
        return frameInterval;
    }

    public VideoAttributes setFrameInterval(Integer frameInterval) {
        this.frameInterval = frameInterval;
        return this;
    }

    public int getAVCProfile() {
        return size.isHighResolution() ? MediaCodecInfo.CodecProfileLevel.AVCProfileHigh : MediaCodecInfo.CodecProfileLevel.AVCProfileMain;
    }

    public int getAVCProfileLevel() {
        return size.isHighResolution() ? MediaCodecInfo.CodecProfileLevel.AVCLevel4 : MediaCodecInfo.CodecProfileLevel.AVCLevel31;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "VideoAttributes (res: %s, fps: %d, bitrate: %d, iFrameInterval: %d)", size.toString(), frameRate, bitRate, frameInterval);
    }
}