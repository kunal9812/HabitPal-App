package com.example.habitpal.domain.repository

import com.example.habitpal.domain.model.Weather
import com.example.habitpal.util.Resource

interface WeatherRepository {
    suspend fun getCurrentWeather(lat: Double, lon: Double): Resource<Weather>
}

