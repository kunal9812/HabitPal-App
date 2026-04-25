package com.example.habitpal.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.data.local.UserPreferencesDataSource
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.domain.model.HabitTemplate
import com.example.habitpal.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val userPreferences: UserPreferencesDataSource
) : ViewModel() {

    val templates: List<HabitTemplate> = HabitTemplate.defaults
    private val selectedTemplates = mutableSetOf<HabitTemplate>()

    fun toggleTemplate(template: HabitTemplate) {
        if (!selectedTemplates.add(template)) {
            selectedTemplates.remove(template)
        }
    }

    fun finishOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            selectedTemplates.forEach { template ->
                habitRepository.addHabit(
                    Habit(
                        title = template.title,
                        description = "",
                        frequency = HabitFrequency.DAILY
                    )
                )
            }
            userPreferences.setOnboardingComplete()
            onComplete()
        }
    }

    fun skipOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            userPreferences.setOnboardingComplete()
            onComplete()
        }
    }
}



