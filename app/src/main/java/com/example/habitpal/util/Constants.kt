package com.example.habitpal.util

object Constants {
    const val DATABASE_NAME = "habit_pal_db"

    const val WEATHER_BASE_URL = "https://api.open-meteo.com/v1/"
    const val GEOCODING_BASE_URL = "https://nominatim.openstreetmap.org/"
    const val QUOTES_BASE_URL = "https://zenquotes.io/api/"

    // Date formats
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    // Habit frequencies
    const val FREQUENCY_DAILY = "DAILY"
    const val FREQUENCY_WEEKLY = "WEEKLY"
    const val FREQUENCY_MONTHLY = "MONTHLY"

    // Weather defaults (Delhi as fallback)
    const val DEFAULT_LATITUDE = 28.6139
    const val DEFAULT_LONGITUDE = 77.2090
}
