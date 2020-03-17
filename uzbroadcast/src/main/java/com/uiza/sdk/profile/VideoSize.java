package com.uiza.sdk.profile;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Size;

public class VideoSize implements Parcelable {
    public static final Creator<VideoSize> CREATOR = new Creator<VideoSize>() {
        @Override
        public VideoSize createFromParcel(Parcel in) {
            return new VideoSize(in);
        }

        @Override
        public VideoSize[] newArray(int size) {
            return new VideoSize[size];
        }
    };
    private int mWidth;
    private int mHeight;


    private VideoSize(int w, int h) {
        this.mWidth = w;
        this.mHeight = h;
    }

    private VideoSize(Parcel in) {
        this.mWidth = in.readInt();
        this.mHeight = in.readInt();
    }

    public static VideoSize fromSize(Camera.Size size) {
        return new VideoSize(size.width, size.height);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static VideoSize fromSize(Size size) {
        return new VideoSize(size.getWidth(), size.getHeight());
    }

    private static NumberFormatException invalidSize(String s) {
        throw new NumberFormatException("Invalid Size: \"" + s + "\"");
    }

    /**
     * Parses the specified string as a size value.
     * <p>
     * The ASCII characters {@code \}{@code u002a} ('*') and
     * {@code \}{@code u0078} ('x') are recognized as separators between
     * the width and height.</p>
     * <p>
     * For any {@code Size s}: {@code Size.parseSize(s.toString()).equals(s)}.
     * However, the method also handles sizes expressed in the
     * following forms:</p>
     * <p>
     * "<i>width</i>{@code x}<i>height</i>" or
     * "<i>width</i>{@code *}<i>height</i>" {@code => new Size(width, height)},
     * where <i>width</i> and <i>height</i> are string integers potentially
     * containing a sign, such as "-10", "+7" or "5".</p>
     *
     * <pre>{@code
     * Size.parseSize("3*+6").equals(new Size(3, 6)) == true
     * Size.parseSize("-3x-6").equals(new Size(-3, -6)) == true
     * Size.parseSize("4 by 3") => throws NumberFormatException
     * }</pre>
     *
     * @param string the string representation of a size value.
     * @return the size value represented by {@code string}.
     * @throws NumberFormatException if {@code string} cannot be parsed
     *                               as a size value.
     * @throws NullPointerException  if {@code string} was {@code null}
     */
    public static VideoSize parseSize(String string)
            throws NumberFormatException {
        int sep_ix = string.indexOf('*');
        if (sep_ix < 0) {
            sep_ix = string.indexOf('x');
        }
        if (sep_ix < 0) {
            throw invalidSize(string);
        }
        try {
            return new VideoSize(Integer.parseInt(string.substring(0, sep_ix)),
                    Integer.parseInt(string.substring(sep_ix + 1)));
        } catch (NumberFormatException e) {
            throw invalidSize(string);
        }
    }

    /**
     * ------------ Some Special Size ----------
     */
    public static VideoSize FHD_1080p() {
        return new VideoSize(1920, 1080);
    }

    public static VideoSize HD_720p() {
        return new VideoSize(1280, 720);
    }

    public static VideoSize SD_480p() {
        return new VideoSize(854, 480);
    }

    public static VideoSize SD_360p() {
        return new VideoSize(640, 360);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mWidth);
        dest.writeInt(mHeight);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Check if this size is equal to another size.
     * <p>
     * Two sizes are equal if and only if both their widths and heights are
     * equal.
     * </p>
     * <p>
     * A size object is never equal to any other type of object.
     * </p>
     *
     * @return {@code true} if the objects were equal, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof VideoSize) {
            VideoSize other = (VideoSize) obj;
            return mWidth == other.mWidth && mHeight == other.mHeight;
        }
        return false;
    }

    /**
     * Return the size represented as a string with the format {@code "WxH"}
     *
     * @return string representation of the size
     */
    @Override
    public String toString() {
        return mWidth + "x" + mHeight;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public boolean isValid() {
        return mWidth <= 1920 && mWidth >= 0 && mHeight <= 1080 && mHeight >= 0;
    }

    public boolean isHighResolution() {
        return Math.min(mWidth, mHeight) >= 720;
    }
}
