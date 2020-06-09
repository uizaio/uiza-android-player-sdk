package com.uiza.sdk.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by namnd.bka@gmail.com on 15/03/2020.
 */
public class UZPlayback implements Parcelable {

    public static final Creator<UZPlayback> CREATOR = new Creator<UZPlayback>() {
        @Override
        public UZPlayback createFromParcel(Parcel in) {
            return new UZPlayback(in);
        }

        @Override
        public UZPlayback[] newArray(int size) {
            return new UZPlayback[size];
        }
    };
    private String id;
    private String name;
    private String description;
    private String thumbnail;
    private float duration;
    private String logo;
    private Date createdAt;
    private String mpd;
    private String hls;
    private String other;

    public UZPlayback() {
    }

    public UZPlayback(String id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    protected UZPlayback(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        thumbnail = in.readString();
        logo = in.readString();
        duration = in.readFloat();
        createdAt = new Date(in.readLong());
        mpd = in.readString();
        hls = in.readString();
        other = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(thumbnail);
        dest.writeString(logo);
        dest.writeFloat(duration);
        dest.writeLong(createdAt.getTime());
        dest.writeString(mpd);
        dest.writeString(hls);
        dest.writeString(other);
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public void setMpd(String mpd) {
        this.mpd = mpd;
    }

    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLogo() {
        return logo;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public float getDuration() {
        return duration;
    }

    public String getHls() {
        return hls;
    }

    public void setHls(String hls) {
        this.hls = hls;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean canPlay() {
        return !TextUtils.isEmpty(mpd) || !TextUtils.isEmpty(hls) || !TextUtils.isEmpty(other);
    }

    public List<String> getUrls() {
        List<String> urls = new ArrayList<>();
        if (!TextUtils.isEmpty(this.mpd))
            urls.add(this.mpd);
        if (!TextUtils.isEmpty(this.hls))
            urls.add(this.hls);
        if (!TextUtils.isEmpty(this.other))
            urls.add(this.other);
        return urls;
    }

    public void setLinkPlay(String linkPlay) {
        if (linkPlay.toLowerCase().indexOf(UZMediaExtension.MPD) > 0) {
            setMpd(linkPlay);
        } else if (linkPlay.toLowerCase().indexOf(UZMediaExtension.M3U8) > 0) {
            setHls(linkPlay);
        } else   {
            setOther(linkPlay);
        }
    }

    /**
     * default: dash -> hls -> single file
     * @return string of url
     */
    public String getDefaultLinkPlay() {
        if (!TextUtils.isEmpty(mpd)) {
            return mpd;
        } else if (!TextUtils.isEmpty(hls)) {
            return hls;
        } else {
            return other;
        }
    }

    public int size() {
        return getUrls().size();
    }

    public URL getDefaultPlayUrl() {
        try {
            return new URL(getDefaultLinkPlay());
        } catch (MalformedURLException e) {
            Timber.w(e);
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "UZPlayback(id: %s, name: %s, description: %s, thumbnail: %s, mpd: %s, hls: %s, other: %s)",
                id, name, description, thumbnail,mpd, hls, other);
    }
}
