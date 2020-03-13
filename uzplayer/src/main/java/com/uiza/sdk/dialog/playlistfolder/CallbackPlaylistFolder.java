package com.uiza.sdk.dialog.playlistfolder;


import com.uiza.sdk.models.UZPlaybackInfo;

/**
 * Created by loitp on 3/30/2018.
 */

public interface CallbackPlaylistFolder {
    void onClickItem(UZPlaybackInfo playback, int position);

    void onFocusChange(UZPlaybackInfo playback, int position);

    void onDismiss();
}
