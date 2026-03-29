package com.example.habitpal.data.mapper

import com.example.habitpal.data.remote.dto.WeatherDto
import com.example.habitpal.domain.model.Weather

fun WeatherDto.toDomain(): Weather = Weather(
    cityName = timezone,
    temperature = current.temperature,
    description = weatherCodeToDescription(current.weatherCode),
    humidity = current.humidity,
    iconEmoji = weatherCodeToIcon(current.weatherCode)
)

private fun weatherCodeToDescription(code: Int): String {
    return when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rainy"
        71, 73, 75 -> "Snowy"
        80, 81, 82 -> "Rain showers"
        95 -> "Thunderstorm"
        else -> "Unknown"
    }
}

private fun weatherCodeToIcon(code: Int): String {
    return when (code) {
        0 -> "☀️"
        1, 2, 3 -> "⛅"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63, 65 -> "🌧️"
        71, 73, 75 -> "❄️"
        80, 81, 82 -> "🌧️"
        95 -> "⛈️"
        else -> "🌡️"
    }
}
