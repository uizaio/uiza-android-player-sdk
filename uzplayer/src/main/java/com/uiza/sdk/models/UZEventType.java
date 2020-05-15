package com.uiza.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UZEventType {
    @JsonProperty("playerready")
    PLAYER_READY, // playerready
    @JsonProperty("loadstart")
    LOAD_START, // loadstart
    @JsonProperty("viewstart")
    VIEW_START, //viewstart
    @JsonProperty("pause")
    PAUSE, // pause
    @JsonProperty("play")
    PLAY, // play
    @JsonProperty("playing")
    PLAYING,
    @JsonProperty("seeking")
    SEEKING,
    @JsonProperty("seeked")
    SEEKED,
    @JsonProperty("waiting")
    WAITING,
    @JsonProperty("ratechange")
    RATE_CHANGE,
    @JsonProperty("rebufferstart")
    REBUFFER_START,
    @JsonProperty("rebufferend")
    REBUFFER_END,
    @JsonProperty("volumechange")
    VOLUME_CHANGE,
    @JsonProperty("fullscreenchange")
    FULLSCREEN_CHANGE,
    @JsonProperty("viewended")
    VIEW_END,
    @JsonProperty("error")
    ERROR,
    @JsonProperty("watching")
    WATCHING,
    @JsonProperty("view")
    VIEW
}
