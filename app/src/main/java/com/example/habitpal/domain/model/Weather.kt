package com.example.habitpal.domain.model

data class Weather(
    val cityName: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val iconEmoji: String
)

