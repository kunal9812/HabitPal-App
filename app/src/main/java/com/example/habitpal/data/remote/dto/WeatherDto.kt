package com.example.habitpal.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherDto(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "timezone") val timezone: String,
    @Json(name = "current") val current: CurrentWeatherDto
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "temperature_2m") val temperature: Double,
    @Json(name = "weathercode") val weatherCode: Int,
    @Json(name = "windspeed_10m") val windSpeed: Double,
    @Json(name = "relative_humidity_2m") val humidity: Int
)