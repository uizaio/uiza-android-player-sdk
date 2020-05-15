package com.uiza.sdk.analytics;

import com.uiza.sdk.models.UZLiveCounter;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LiveViewCountAPI {

    // Get live viewers count
    @GET("/v1/analytics/live_viewers")
    Observable<UZLiveCounter> getLiveViewers(@Query("app_id") String appId,
                                             @Query("entity_id") String entityId);
}
