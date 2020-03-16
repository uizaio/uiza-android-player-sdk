package com.uiza.sdk.util;

import android.content.pm.ResolveInfo;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.uiza.sdk.R;
import com.uiza.sdk.chromecast.Casty;
import com.uiza.sdk.models.UZPlaybackInfo;

import java.net.URL;
import java.util.List;

import timber.log.Timber;

public class UZData {
    private static final String TEXT = "text";
    private static final String VIDEO = "video";
    private static final String AUDIO = "audio";
    private static final String CAPTIONS = "captions";
    private static final String SUBTITLE = "subtitle";
    //
    @LayoutRes
    private int currentPlayerId = R.layout.uz_player_skin_1;//id of layout xml
    private Casty casty;
    private UZPlaybackInfo playbackInfo;
    private String urlIMAAd = "";
    //start singleton data if play playlist folder
    private List<UZPlaybackInfo> playList;
    private int currentPositionOfPlayList = 0;
    private boolean isUseWithVDHView;
    //dialog share
    private List<ResolveInfo> resolveInfoList;
    private boolean isSettingPlayer;

    private UZData() {
    }

    public static UZData getInstance() {
        return UDataHelper.INSTANCE;
    }

    @LayoutRes
    public int getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(@LayoutRes int currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    @Nullable
    public Casty getCasty() {
        if (casty == null) {
            Timber.e("You must init Casty with activity before using Chromecast. Tips: put 'UizaCoreSDK.setCasty(this);' to your onStart() or onCreate()");
        }
        return casty;
    }

    public void setCasty(Casty casty) {
        this.casty = casty;
    }

    public UZPlaybackInfo getPlaybackInfo() {
        return playbackInfo;
    }

    public void setPlaybackInfo(UZPlaybackInfo playbackInfo) {
        this.playbackInfo = playbackInfo;
    }

    public String getUrlIMAAd() {
        return urlIMAAd;
    }

    public void setUrlIMAAd(String urlIMAAd) {
        this.urlIMAAd = urlIMAAd;
    }


    public void clear() {
        this.playbackInfo = null;
        this.urlIMAAd = null;
    }

    @Nullable
    public String getEntityId() {
        return (playbackInfo == null) ? null : playbackInfo.getId();
    }

    public boolean isLiveStream() {
        return (playbackInfo != null) && playbackInfo.isLive();
    }

    @Nullable
    public String getEntityName() {
        return (playbackInfo == null) ? null : playbackInfo.getName();
    }

    @Nullable
    public String getThumbnail() {
        return (playbackInfo == null) ? null : playbackInfo.getThumbnail();
    }

    @Nullable
    public String getChannelName() {
        return (playbackInfo == null) ? null : playbackInfo.getChannelName();
    }

    public String getHost() {
        if (playbackInfo == null) return null;
        URL url = playbackInfo.getLinkPlayUrl();
        if (url == null) return null;
        return url.getHost();
    }

    /**
     * true neu playlist folder
     * tra ve false neu play entity
     */
    public boolean isPlayWithPlaylistFolder() {
        return playList != null;
    }

    public List<UZPlaybackInfo> getPlayList() {
        return playList;
    }

    public void setPlayList(List<UZPlaybackInfo> playlist) {
        this.playList = playlist;
    }

    public int getCurrentPositionOfPlayList() {
        return currentPositionOfPlayList;
    }

    public void setCurrentPositionOfPlayList(int currentPositionOfPlayList) {
        this.currentPositionOfPlayList = currentPositionOfPlayList;
        UZPlaybackInfo currentInfo = playList.get(currentPositionOfPlayList);
        if (currentInfo != null) this.playbackInfo = currentInfo;

    }
    //end singleton data if play playlist folder

    public UZPlaybackInfo getDataWithPositionOfPlayList(int position) {
        return ListUtils.isEmpty(playList) ? null : playList.get(position);
    }

    public void clearDataForPlaylistFolder() {
        playList = null;
        currentPositionOfPlayList = 0;
    }

    public boolean isUseWithVDHView() {
        return isUseWithVDHView;
    }

    public void setUseWithVDHView(boolean isUseWithVDHView) {
        this.isUseWithVDHView = isUseWithVDHView;
    }

    public List<ResolveInfo> getResolveInfoList() {
        return resolveInfoList;
    }

    public void setResolveInfoList(List<ResolveInfo> resolveInfoList) {
        this.resolveInfoList = resolveInfoList;
    }
    //end dialog share

    public boolean isSettingPlayer() {
        return isSettingPlayer;
    }

    public void setSettingPlayer(boolean settingPlayer) {
        isSettingPlayer = settingPlayer;
    }

    // Bill Pugh Singleton Implementation
    private static class UDataHelper {
        private static final UZData INSTANCE = new UZData();
    }
}
