package com.heetch.technicaltest.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.heetch.technicaltest.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier

class NetworkManager {

    class DO_NOT_VERIFY_IMP: javax.net.ssl.HostnameVerifier {
        override fun verify(p0: String?, p1: javax.net.ssl.SSLSession?): Boolean {
            return true
        }
    }

    companion object {
        const val BASE_URL = "https://hiring.heetch.com/mobile/"
    }

    fun getRepository() : ApiInterface {
        val loggingInterceptor = provideLoggingInterceptor()
        val httpClient = provideLoggingCapableHttpClient(loggingInterceptor)
        val gson = provideGsonConverter()
        val rxJavaCallAdapterFactory = provideRxJavaCallAdapterFactory()
        val retrofit = provideRetrofitBuilder(httpClient, gson, rxJavaCallAdapterFactory)
        return provideRestService(retrofit, BASE_URL)
    }

    private fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            Log.d("Retrofit logging", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }


    private fun provideLoggingCapableHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {

        val DO_NOT_VERIFY = DO_NOT_VERIFY_IMP()


        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .hostnameVerifier(DO_NOT_VERIFY)
            .build()
    }

    private fun provideRetrofitBuilder(
        okHttpClient: OkHttpClient,
        gson: Gson,
        rxJavaCallAdapterFactory: RxJava2CallAdapterFactory
    ): Retrofit.Builder {

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(rxJavaCallAdapterFactory)
            .client(okHttpClient)
    }

    private fun provideRestService(retrofitBuilder: Retrofit.Builder, baseUrl: String): ApiInterface {
        return retrofitBuilder.baseUrl(baseUrl)
            .build()
            .create(ApiInterface::class.java)
    }

    private fun provideGsonConverter(): Gson {
        return GsonBuilder().create()
    }

    private fun provideRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.create()
    }

}