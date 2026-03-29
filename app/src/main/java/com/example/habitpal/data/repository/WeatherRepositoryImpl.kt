package com.example.habitpal.data.repository

import com.example.habitpal.data.mapper.toDomain
import com.example.habitpal.data.remote.api.WeatherApi
import com.example.habitpal.domain.model.Weather
import com.example.habitpal.domain.repository.WeatherRepository
import com.example.habitpal.util.Resource
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApi
) : WeatherRepository {

    override suspend fun getCurrentWeather(lat: Double, lon: Double): Resource<Weather> {
        return try {
            val dto = weatherApi.getCurrentWeather(lat, lon)
            Resource.Success(dto.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Unable to fetch weather", e)
        }
    }
}

