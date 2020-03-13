package com.uiza.sdk.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class UZPlaybackInfo implements Parcelable {

    public static final Creator<UZPlaybackInfo> CREATOR = new Creator<UZPlaybackInfo>() {
        @Override
        public UZPlaybackInfo createFromParcel(Parcel in) {
            return new UZPlaybackInfo(in);
        }

        @Override
        public UZPlaybackInfo[] newArray(int size) {
            return new UZPlaybackInfo[size];
        }
    };
    @SerializedName("id")
    String id;
    @SerializedName("name")
    String name;
    @SerializedName("description")
    String description;
    @SerializedName("thumbnail")
    String thumbnail;
    @SerializedName("duration")
    float duration;
    @SerializedName("channel_name")
    String channelName;
    @SerializedName("last_feed_id")
    String lastFeedId;
    @SerializedName("created_at")
    Date createdAt;
    @SerializedName("hls")
    String hls;
    @SerializedName("hls_ts")
    String hlsTs;
    @SerializedName("mpd")
    String mpd;
    @SerializedName("is_live")
    boolean live;

    public UZPlaybackInfo() {
        this.live = false;
    }

    protected UZPlaybackInfo(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        thumbnail = in.readString();
        duration = in.readFloat();
        createdAt = new Date(in.readLong());
        hls = in.readString();
        hlsTs = in.readString();
        mpd = in.readString();
        live = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(thumbnail);
        dest.writeFloat(duration);
        dest.writeLong(createdAt.getTime());
        dest.writeString(hls);
        dest.writeString(hlsTs);
        dest.writeString(mpd);
        dest.writeInt(live ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<String> getUrls() {
        List<String> urls = new ArrayList<>();
        if (!TextUtils.isEmpty(this.hls))
            urls.add(this.hls);
        if (!TextUtils.isEmpty(this.hlsTs))
            urls.add(this.hlsTs);
        if (!TextUtils.isEmpty(this.mpd))
            urls.add(this.mpd);
        return urls;
    }

    public void setHlsTs(String hlsTs) {
        this.hlsTs = hlsTs;
    }

    public void setMpd(String mpd) {
        this.mpd = mpd;
    }

    public String getId() {
        return id;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public float getDuration() {
        return duration;
    }

    public String getLastFeedId() {
        return lastFeedId;
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
        return !TextUtils.isEmpty(hls) || !TextUtils.isEmpty(hlsTs) || !TextUtils.isEmpty(mpd);
    }

    public String getLinkPlay() {
        if (!TextUtils.isEmpty(hls)) {
            return hls;
        } else if (!TextUtils.isEmpty(hlsTs)) {
            return hlsTs;
        } else {
            return mpd;
        }
    }

    public URL getLinkPlayUrl() {
        try {
            return new URL(getLinkPlay());
        } catch (MalformedURLException e) {
            Timber.w(e);
            return null;
        }
    }
}
