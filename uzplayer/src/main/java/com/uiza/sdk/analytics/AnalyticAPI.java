package com.uiza.sdk.analytics;

import com.uiza.sdk.models.UZTrackingBody;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AnalyticAPI {
    //UZ Tracking API
    @POST("/v1/events")
    Observable<ResponseBody> pushEvents(@Body UZTrackingBody trackingBody);

}
