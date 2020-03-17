package com.uiza.sdk.profile;

import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.os.Parcel;
import android.os.Parcelable;

import com.uiza.sdk.util.ValidValues;

public class AudioAttributes implements Parcelable {
    public static final Creator<AudioAttributes> CREATOR = new Creator<AudioAttributes>() {
        @Override
        public AudioAttributes createFromParcel(Parcel in) {
            return new AudioAttributes(in);
        }

        @Override
        public AudioAttributes[] newArray(int size) {
            return new AudioAttributes[size];
        }
    };
    /**
     * AAC in kb.
     * The bitrate value for the encoding process.
     * default value 64*1024 (64kps)
     */
    private int bitRate = 64 * 1024;
    /**
     * The sampleRate value for the audio encoding process.
     * default value 44100 (44.1 KHz)
     */
    private int sampleRate = 44100; // Hz
    /**
     * true stereo, false mono.
     * default value true (stereo)
     */
    private boolean stereo = true;
    /**
     * true enable echo canceler, false disable.
     */
    private boolean echoCanceler;
    /**
     * true enable noise suppressor, false  disable.
     */
    private boolean noiseSuppressor;

    private AudioAttributes() {
    }

    private AudioAttributes(Parcel in) {
        bitRate = in.readInt();
        sampleRate = in.readInt();
        stereo = in.readBoolean();
        echoCanceler = in.readBoolean();
        noiseSuppressor = in.readBoolean();
    }

    private AudioAttributes(int bitRate, int sampleRate, boolean stereo, boolean echoCanceler, boolean noiseSuppressor) {
        ValidValues.check(bitRate, 1, 256 * 1024); // max 256 Kbps
        ValidValues.check(sampleRate, 1, 48000); // max 48 KHz
        this.bitRate = bitRate;
        this.sampleRate = sampleRate;
        this.stereo = stereo;
        this.echoCanceler = echoCanceler;
        this.noiseSuppressor = noiseSuppressor;
    }

    public static AudioAttributes create(int bitRate, int sampleRate, boolean stereo) {
        return new AudioAttributes(bitRate, sampleRate, stereo, AcousticEchoCanceler.isAvailable(), NoiseSuppressor.isAvailable());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bitRate);
        dest.writeInt(sampleRate);
        dest.writeBoolean(stereo);
        dest.writeBoolean(echoCanceler);
        dest.writeBoolean(noiseSuppressor);
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
     * Sets the bitrate value for the encoding process.
     *
     * @param bitRate The bitrate value for the encoding process.
     * @return this instance
     */
    public AudioAttributes setBitRate(Integer bitRate) {
        this.bitRate = bitRate;
        return this;
    }

    /**
     * Returns the samplingRate value for the encoding process.
     *
     * @return the sampleRate value for the encoding process.
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Sets the samplingRate value for the encoding process.
     *
     * @param sampleRate The samplingRate value for the encoding process.
     * @return this instance
     */
    public AudioAttributes setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    /**
     * Returns the channels value (1=mono, 2=stereo, 4=quad) for the encoding process.
     *
     * @return The channels value (1=mono, 2=stereo, 4=quad) for the encoding process.
     */
    public boolean isStereo() {
        return stereo;
    }

    /**
     * Sets the value (false=mono, true=stereo) for the encoding process.
     *
     * @param stereo The value (false=mono, true=stereo) for the encoding
     *               process.
     * @return this instance
     */
    public AudioAttributes setStereo(boolean stereo) {
        this.stereo = stereo;
        return this;
    }

    public boolean isEchoCanceler() {
        return echoCanceler;
    }

    /**
     * see {@link AcousticEchoCanceler#isAvailable()}
     *
     * @param echoCanceler boolean value
     * @return this instance
     */
    public AudioAttributes setEchoCanceler(boolean echoCanceler) {
        this.echoCanceler = echoCanceler && AcousticEchoCanceler.isAvailable();
        return this;
    }

    public boolean isNoiseSuppressor() {
        return noiseSuppressor;
    }
    ///

    /**
     * see {@link NoiseSuppressor#isAvailable()}
     *
     * @param noiseSuppressor boolean value
     * @return this instance
     */
    public AudioAttributes setNoiseSuppressor(boolean noiseSuppressor) {
        this.noiseSuppressor = noiseSuppressor && NoiseSuppressor.isAvailable();
        return this;
    }
}
