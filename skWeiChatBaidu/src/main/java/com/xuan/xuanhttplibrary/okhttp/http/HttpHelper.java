package com.xuan.xuanhttplibrary.okhttp.http;


import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.ydd.yanshi.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by lizheng on 2018/5/11.
 */

public class HttpHelper {
    public final static String URL = Api.BASE_API;

    public static Api getApiService(final String apiUrl) {
        Retrofit mRetrofit = new Retrofit.Builder()
                .client(getOkHttpClient())
                .baseUrl(apiUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Api task = mRetrofit.create(Api.class);
        return task;
    }

    public static Api getApiService() {
        return getApiService(URL);
    }


//    private static SSLSocketFactory sslSocketFactory;

    public static OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        /*try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
        }*/


        builder
                .retryOnConnectionFailure(true)
                .connectTimeout(0, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .addInterceptor(getHttpLoggingInterceptor())
                .addNetworkInterceptor(new StethoInterceptor()) //配置接口调试工具
//                .sslSocketFactory(sslSocketFactory)
                .build();

        return builder.build();
    }


    public static Interceptor getHttpLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return interceptor;
    }
}
