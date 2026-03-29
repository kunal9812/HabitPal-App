package com.example.habitpal.domain.usecase.weather

import com.example.habitpal.domain.model.Weather
import com.example.habitpal.domain.repository.WeatherRepository
import com.example.habitpal.util.Resource
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double): Resource<Weather> =
        weatherRepository.getCurrentWeather(lat, lon)
}

