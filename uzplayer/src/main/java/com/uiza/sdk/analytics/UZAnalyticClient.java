package com.uiza.sdk.analytics;

import com.uiza.sdk.analytics.helps.NullOnEmptyConverterFactory;
import com.uiza.sdk.analytics.interceptors.RestRequestInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import timber.log.Timber;

public class UZAnalyticClient {
    private final static String BASE_URL = "https://tracking-dev.uizadev.io";
    private Interceptor restRequestInterceptor;
    private static final int HTTP_TIMEOUT_TIME = 10; //10s
    private Retrofit retrofit;

    private static class UZRestClientHelper {
        private static final UZAnalyticClient INSTANCE = new UZAnalyticClient();
    }

    public static UZAnalyticClient getInstance() {
        return UZRestClientHelper.INSTANCE;
    }

    private UZAnalyticClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(provideHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .addConverterFactory(NullOnEmptyConverterFactory.create())
                .build();
    }

    private OkHttpClient provideHttpClient() {
        return new OkHttpClient.Builder()
                .readTimeout(HTTP_TIMEOUT_TIME, TimeUnit.SECONDS)
                .connectTimeout(HTTP_TIMEOUT_TIME, TimeUnit.SECONDS)
                .writeTimeout(HTTP_TIMEOUT_TIME, TimeUnit.SECONDS)
                .addInterceptor(provideInterceptor())
                .retryOnConnectionFailure(true)
                .addInterceptor(provideLogging())  // <-- this is the important line!
                .build();
    }

    private Interceptor provideLogging() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(Timber::d);
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logging;
    }

    private Interceptor provideInterceptor() {
        if (restRequestInterceptor == null)
            restRequestInterceptor = new RestRequestInterceptor();
        return restRequestInterceptor;
    }

    AnalyticAPI createAnalyticAPI() {
        if (retrofit == null) {
            throw new IllegalStateException("Must call init() before using");
        }
        return retrofit.create(AnalyticAPI.class);
    }
}
