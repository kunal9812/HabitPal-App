package com.example.habitpal.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.data.local.UserPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginEvent {
    object OnboardingComplete : LoginEvent()
    data class Error(val message: String) : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userPreferences: UserPreferencesDataSource
) : ViewModel() {

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    fun completeOnboarding(name: String) {
        if (name.isBlank()) {
            viewModelScope.launch {
                _events.emit(LoginEvent.Error("Please enter your name"))
            }
            return
        }
        viewModelScope.launch {
            userPreferences.saveUserName(name.trim())
            userPreferences.saveHasOnboarded(true)
            _events.emit(LoginEvent.OnboardingComplete)
        }
    }
}
