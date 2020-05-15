package com.uiza.sdk.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.uiza.sdk.utils.JacksonUtils;
import com.uiza.sdk.utils.ListUtils;

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
    private String hls;
    private String hlsTs;
    private String mpd;
    private boolean live;

    public UZPlayback() {
        this.live = false;
    }

    public UZPlayback(String id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
        this.live = false;
    }

    protected UZPlayback(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        thumbnail = in.readString();
        logo = in.readString();
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
        dest.writeString(logo);
        dest.writeFloat(duration);
        dest.writeLong(createdAt.getTime());
        dest.writeString(hls);
        dest.writeString(hlsTs);
        dest.writeString(mpd);
        dest.writeInt(live ? 1 : 0);
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
        return !TextUtils.isEmpty(hls) || !TextUtils.isEmpty(hlsTs) || !TextUtils.isEmpty(mpd);
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

    public List<String> getLinkPlays() {
        if (live) {
            //Bat buoc dung linkplay m3u8 cho nay, do bug cua system
            return ListUtils.filter(this.getUrls(), url -> url.toLowerCase().indexOf(UZMediaExtension.M3U8) > 0);
        } else {
            List<String> listLinkPlay = new ArrayList<>();
            List<String> urls = getUrls();
            listLinkPlay.addAll(ListUtils.filter(urls, url -> url.toLowerCase().indexOf(UZMediaExtension.MPD) > 0));
            listLinkPlay.addAll(ListUtils.filter(urls, url -> url.toLowerCase().indexOf(UZMediaExtension.M3U8) > 0));
            return listLinkPlay;
        }
    }


    public String getLinkPlay() {
        if (!TextUtils.isEmpty(mpd)) {
            return mpd;
        } else if (!TextUtils.isEmpty(hls)) {
            return hls;
        } else {
            return hlsTs;
        }
    }

    public UZAnalyticInfo getAnalyticInfo() {
        String url = getLinkPlay();
        int index = url.indexOf("?cm=");
        if (index > 0) {
            try {
                String json = new String(Base64.decode(url.substring(index + 4), Base64.DEFAULT));
                return JacksonUtils.fromJson(json, UZAnalyticInfo.class);
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return null;
    }

    public URL getPlayUrl() {
        try {
            return new URL(getLinkPlay());
        } catch (MalformedURLException e) {
            Timber.w(e);
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "UZPlayback(id: %s, name: %s, description: %s, thumbnail: %s , hls: %s, hls_ts: %s, mpd: %s, live: %b)",
                id, name, description, thumbnail, hls, hlsTs, mpd, live);
    }
}
