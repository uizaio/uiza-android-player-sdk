package com.uiza.sdk.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UZPlaybackInfo implements Serializable {
    @JsonProperty("app_id")
    private String appId;
    @JsonProperty("entity_id")
    private String entityId;
    @JsonProperty("entity_source")
    private String entitySource;

    public UZPlaybackInfo() {
    }

    public UZPlaybackInfo(String appId, String entityId, String entitySource) {
        this.appId = appId;
        this.entityId = entityId;
        this.entitySource = entitySource;
    }

    public String getAppId() {
        return appId;
    }

    public String getEntitySource() {
        return entitySource;
    }

    public String getEntityId() {
        return entityId;
    }
}
