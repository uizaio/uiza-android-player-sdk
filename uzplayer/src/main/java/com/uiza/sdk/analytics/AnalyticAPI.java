package com.uiza.sdk.analytics;

import io.reactivex.Observable;

import com.uiza.sdk.models.UZTrackingBody;

import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AnalyticAPI {
    @GET("/v1/analytics/{app_id}/usage/daily")
    Observable<String> usageDaily(@Path("app_id") String appId,
                                  @Query("start_date") String startDate,
                                  @Query("end_date") String endDate,
                                  @Query("page_size") int pageSize,
                                  @Query("page_number") int pageNumber);

    @GET("/v1/analytics/usage/summary")
    Observable<String> usageSummary(@Header("Authorization") String authorization,
                                    @Header("X-Customer-Custom-ID") String appId);

    //Uiza Tracking API
    @POST("/v1/events")
    Observable<ResponseBody> pushEvents(@Body UZTrackingBody trackingBody);


}
