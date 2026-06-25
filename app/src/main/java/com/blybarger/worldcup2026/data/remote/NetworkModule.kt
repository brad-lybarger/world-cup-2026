package com.blybarger.worldcup2026.data.remote

import com.blybarger.worldcup2026.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Builds the configured [FootballDataApi]. */
object NetworkModule {

    private const val BASE_URL = "https://api.football-data.org/"

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun createApi(apiKey: String = BuildConfig.FOOTBALL_DATA_API_KEY): FootballDataApi {
        val client = OkHttpClient.Builder()
            // football-data.org authenticates via the X-Auth-Token request header.
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Auth-Token", apiKey)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FootballDataApi::class.java)
    }
}
