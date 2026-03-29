package com.example.habitpal.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.data.local.UserPreferencesDataSource
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.Quote
import com.example.habitpal.domain.model.Weather
import com.example.habitpal.domain.usecase.habit.CompleteHabitUseCase
import com.example.habitpal.domain.usecase.habit.GetHabitsUseCase
import com.example.habitpal.domain.usecase.quote.GetQuoteUseCase
import com.example.habitpal.domain.usecase.weather.GetWeatherUseCase
import com.example.habitpal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val getWeatherUseCase: GetWeatherUseCase,
    private val getQuoteUseCase: GetQuoteUseCase,
    private val userPreferences: UserPreferencesDataSource
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _weather = MutableStateFlow<Resource<Weather>>(Resource.Loading)
    val weather: StateFlow<Resource<Weather>> = _weather.asStateFlow()

    private val _quote = MutableStateFlow<Resource<Quote>>(Resource.Loading)
    val quote: StateFlow<Resource<Quote>> = _quote.asStateFlow()

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        loadHabits()
        loadQuote()
        loadUserName()
    }

    private fun loadUserName() {
        viewModelScope.launch {
            userPreferences.userName.collect { _userName.value = it }
        }
    }

    private fun loadHabits() {
        viewModelScope.launch {
            getHabitsUseCase().collect { _habits.value = it }
        }
    }

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weather.value = Resource.Loading
            _weather.value = getWeatherUseCase(lat, lon)
        }
    }

    private fun loadQuote() {
        viewModelScope.launch {
            _quote.value = Resource.Loading
            _quote.value = getQuoteUseCase()
        }
    }

    fun completeHabit(habitId: Int) {
        viewModelScope.launch {
            completeHabitUseCase(habitId)
        }
    }
}

