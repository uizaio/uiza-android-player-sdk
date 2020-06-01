package com.uiza.sdk.utils;

import android.content.pm.ResolveInfo;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.uiza.sdk.R;
import com.uiza.sdk.chromecast.Casty;
import com.uiza.sdk.models.UZPlaybackInfo;
import com.uiza.sdk.models.UZPlayback;

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
    private int uzPlayerSkinLayoutId = R.layout.uzplayer_skin_1;//id of layout xml
    private Casty casty;
    private UZPlayback playback;
    private String urlIMAAd = "";
    //start singleton data if play playlist folder
    private List<UZPlayback> playList;
    private UZPlaybackInfo playbackInfo;
    private int currentPositionOfPlayList = 0;
    private boolean useUZDragView;
    //dialog share
    private List<ResolveInfo> resolveInfoList;
    private boolean isSettingPlayer;

    private UZData() {
    }

    public static UZData getInstance() {
        return UDataHelper.INSTANCE;
    }

    public int getUZPlayerSkinLayoutId() {
        return uzPlayerSkinLayoutId;
    }

    public void setUZPlayerSkinLayoutId(@LayoutRes int uzPlayerSkinLayoutId) {
        this.uzPlayerSkinLayoutId = uzPlayerSkinLayoutId;
    }

    @Nullable
    public Casty getCasty() {
        if (casty == null) {
            Timber.e("You must init Casty with activity before using Chromecast. Tips: put 'UZPlayer.setCasty(this);' to your onStart() or onCreate()");
        }
        return casty;
    }

    public void setCasty(Casty casty) {
        this.casty = casty;
    }

    public UZPlayback getPlayback() {
        return playback;
    }

    public boolean isCurrentLive(){
        return (playback != null && playback.isLive());
    }

    public UZPlaybackInfo getPlaybackInfo() {
        return this.playbackInfo;
    }

    public void setPlayback(@NonNull UZPlayback playback) {
        this.playback = playback;
        this.playbackInfo = playback.getPlaybackInfo();
    }

    public String getUrlIMAAd() {
        return urlIMAAd;
    }

    public void setUrlIMAAd(String urlIMAAd) {
        this.urlIMAAd = urlIMAAd;
    }


    public void clear() {
        this.playback = null;
        this.playbackInfo = null;
        this.urlIMAAd = null;
    }

    @Nullable
    public String getEntityId() {
        return (playback == null) ? null : playback.getId();
    }

    @Nullable
    public String getEntityName() {
        return (playback == null) ? null : playback.getName();
    }

    @Nullable
    public String getHost() {
        if (playback == null) return null;
        URL url = playback.getPlayUrl();
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

    public List<UZPlayback> getPlayList() {
        return playList;
    }

    public void setPlayList(List<UZPlayback> playlist) {
        this.playList = playlist;
    }

    public int getCurrentPositionOfPlayList() {
        return currentPositionOfPlayList;
    }

    public void setCurrentPositionOfPlayList(int currentPositionOfPlayList) {
        this.currentPositionOfPlayList = currentPositionOfPlayList;
        UZPlayback currentInfo = playList.get(currentPositionOfPlayList);
        if (currentInfo != null) {
            this.playback = currentInfo;
            this.playbackInfo = currentInfo.getPlaybackInfo();
        }

    }

    public void clearDataForPlaylistFolder() {
        playList = null;
        currentPositionOfPlayList = 0;
    }

    public boolean isUseUZDragView() {
        return useUZDragView;
    }

    public void setUseWithUZDragView(boolean useUZDragView) {
        this.useUZDragView = useUZDragView;
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
