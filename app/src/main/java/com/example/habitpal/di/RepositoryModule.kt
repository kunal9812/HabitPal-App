package com.example.habitpal.di

import com.example.habitpal.data.repository.HabitRepositoryImpl
import com.example.habitpal.data.repository.QuoteRepositoryImpl
import com.example.habitpal.data.repository.WeatherRepositoryImpl
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.domain.repository.QuoteRepository
import com.example.habitpal.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindQuoteRepository(impl: QuoteRepositoryImpl): QuoteRepository
}

