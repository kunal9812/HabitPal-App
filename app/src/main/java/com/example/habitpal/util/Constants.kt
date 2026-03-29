package com.example.habitpal.util

object Constants {
    const val DATABASE_NAME = "habit_pal_db"


    const val WEATHER_BASE_URL = "https://api.open-meteo.com/v1/"

    // Geocoding (to get city name from coordinates, also free + no key)
    const val GEOCODING_BASE_URL = "https://nominatim.openstreetmap.org/"

    // ZenQuotes (no API key needed)
    const val QUOTES_BASE_URL = "https://zenquotes.io/api/"

    // Database
    const val DATABASE_NAME_VALUE = "habit_pal_db"

    // Date formats
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"

    // Habit frequencies
    const val FREQUENCY_DAILY = "DAILY"
    const val FREQUENCY_WEEKLY = "WEEKLY"
    const val FREQUENCY_MONTHLY = "MONTHLY"

    // Shared preferences
    const val PREFS_NAME = "habit_pal_prefs"
    const val PREF_USER_NAME = "user_name"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val PREF_THEME = "theme"

    // Weather defaults
    const val DEFAULT_LATITUDE = 28.6139  // Delhi as fallback
    const val DEFAULT_LONGITUDE = 77.2090
}
