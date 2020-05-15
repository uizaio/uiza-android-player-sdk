package com.uiza.sdk.analytics.interceptors;


import java.io.IOException;

import androidx.annotation.NonNull;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RestRequestInterceptor implements Interceptor {

    private static final String CONTENT_TYPE = "application/json";

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("Content-Type", CONTENT_TYPE);
        return chain.proceed(builder.build());
    }
}
