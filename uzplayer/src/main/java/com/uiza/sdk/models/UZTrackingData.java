package com.uiza.sdk.models;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.uiza.sdk.analytics.UZAnalytic;
import com.uiza.sdk.analytics.helps.JsonDateSerializer;
import com.uiza.sdk.utils.JacksonUtils;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UZTrackingData {
    @JsonProperty("app_id")
    private String appId;
    @JsonProperty("entity_id")
    private String entityId;
    @JsonProperty("entity_source")
    private String entitySource;
    @JsonProperty("viewer_user_id")
    private String viewerUserId; // android id
    @JsonProperty("viewer_session_id")
    private String viewerSessionId;
    @JsonProperty("event")
    private UZEventType eventType;
    @JsonProperty("timestamp")
    @JsonSerialize(using = JsonDateSerializer.class)
    private Date timestamp;

    public UZTrackingData(UZPlaybackInfo info, String viewerSessionId) {
        this(info.getAppId(), info.getEntityId(), info.getEntitySource(), viewerSessionId);
    }

    public UZTrackingData(String appId, String entityId, String entitySource, String viewerSessionId) {
        this.appId = appId;
        this.entityId = entityId;
        this.entitySource = entitySource;
        this.viewerSessionId = viewerSessionId;
        this.timestamp = new Date();
        this.viewerUserId = UZAnalytic.getDeviceId();
    }

    public String getAppId() {
        return appId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityId() {
        return entityId;
    }

    /**
     * @param entitySource: source of entity. ex: live
     */
    public void setEntitySource(String entitySource) {
        this.entitySource = entitySource;
    }

    public String getEntitySource() {
        return entitySource;
    }

    public String getViewerUserId() {
        return viewerUserId;
    }

    /**
     * @param eventType: type of event. ex: watching
     */
    public void setEventType(UZEventType eventType) {
        this.eventType = eventType;
    }

    public UZEventType getEventType() {
        return eventType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getViewerSessionId() {
        return viewerSessionId;
    }

    @NonNull
    @Override
    public String toString() {
        return JacksonUtils.toJson(this);
    }
}
