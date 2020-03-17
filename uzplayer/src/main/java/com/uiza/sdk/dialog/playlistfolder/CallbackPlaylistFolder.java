package com.uiza.sdk.dialog.playlistfolder;


import com.uiza.sdk.models.UZPlayback;

/**
 * Created by loitp on 3/30/2018.
 */

public interface CallbackPlaylistFolder {
    void onClickItem(UZPlayback playback, int position);

    void onFocusChange(UZPlayback playback, int position);

    void onDismiss();
}
