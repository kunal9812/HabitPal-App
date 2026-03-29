package com.example.habitpal.data.remote.api

import com.example.habitpal.data.remote.dto.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weathercode,windspeed_10m,relative_humidity_2m",
        @Query("timezone") timezone: String = "auto"
    ): WeatherDto
}