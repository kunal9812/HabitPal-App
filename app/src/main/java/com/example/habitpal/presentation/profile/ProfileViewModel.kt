package com.example.habitpal.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.data.local.UserPreferencesDataSource
import com.example.habitpal.domain.usecase.progress.GetProgressStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProgressStatsUseCase: GetProgressStatsUseCase,
    private val userPreferences: UserPreferencesDataSource
) : ViewModel() {

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _totalHabits = MutableStateFlow(0)
    val totalHabits: StateFlow<Int> = _totalHabits.asStateFlow()

    private val _totalCompletions = MutableStateFlow(0)
    val totalCompletions: StateFlow<Int> = _totalCompletions.asStateFlow()

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(false)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    init {
        loadStats()
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            userPreferences.userName.collect { _userName.value = it }
        }
        viewModelScope.launch {
            userPreferences.notificationsEnabled.collect { _notificationsEnabled.value = it }
        }
        viewModelScope.launch {
            userPreferences.darkModeEnabled.collect { _darkModeEnabled.value = it }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            getProgressStatsUseCase.observe().collect { stats ->
                _totalHabits.value = stats.totalHabits
                _totalCompletions.value = stats.totalCompletions
                _longestStreak.value = stats.longestStreak
            }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            userPreferences.saveUserName(name)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveNotificationsEnabled(enabled)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveDarkModeEnabled(enabled)
        }
    }
}

