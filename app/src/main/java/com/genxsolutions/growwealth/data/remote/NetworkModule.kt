package com.genxsolutions.growwealth.data.remote

import android.util.Log
import com.genxsolutions.growwealth.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private val baseUrl: String = BuildConfig.API_BASE_URL

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val timingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val startNs = System.nanoTime()
        val response = chain.proceed(request)
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        if (tookMs >= 1500L) {
            Log.w("GrowWealthNetwork", "Slow request ${request.method} ${request.url.encodedPath} took ${tookMs}ms")
        }
        response
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(timingInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    val api: GrowWealthApi = retrofit.create(GrowWealthApi::class.java)
}
