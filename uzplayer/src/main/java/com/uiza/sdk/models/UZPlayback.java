package com.uiza.sdk.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

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
    private float duration;
    private String poster;
    private Date createdAt;
    private List<String> linkPlays;

    public UZPlayback() {
        linkPlays = new ArrayList<>();
    }

    public UZPlayback(String id, String name, String description, String poster) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.poster = poster;
        linkPlays = new ArrayList<>();
    }

    protected UZPlayback(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        poster = in.readString();
        duration = in.readFloat();
        createdAt = new Date(in.readLong());
        in.readStringList(linkPlays);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(poster);
        dest.writeFloat(duration);
        dest.writeLong(createdAt.getTime());
        dest.writeStringList(linkPlays);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getPoster() {
        return poster;
    }

    public float getDuration() {
        return duration;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean canPlay() {
        return !ListUtils.isEmpty(linkPlays);
    }

    public void addLinkPlay(String linkPlay) {
        if (!TextUtils.isEmpty(linkPlay)) {
            linkPlays.add(linkPlay);
        }
    }

    public List<String> getLinkPlays() {
        return linkPlays;
    }

    public int getSize() {
        return linkPlays.size();
    }

    public String getLinkPlay(int pos) {
        if (ListUtils.isEmpty(linkPlays) || pos >= linkPlays.size())
            return null;
        return linkPlays.get(pos);
    }

    /**
     * default: dash -> hls -> single file
     *
     * @return string of url
     */
    public String getFirstLinkPlay() {
        if (ListUtils.isEmpty(linkPlays)) return null;
        return linkPlays.get(0);
    }

    public URL getFirstPlayUrl() {
        try {
            return new URL(getFirstLinkPlay());
        } catch (MalformedURLException e) {
            Timber.w(e);
            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "UZPlayback(id: %s, name: %s, description: %s, poster: %s)",
                id, name, description, poster);
    }
}
