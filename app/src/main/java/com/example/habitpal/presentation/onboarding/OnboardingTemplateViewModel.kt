package com.example.habitpal.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.data.local.UserPreferencesDataSource
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.domain.model.HabitTemplate
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.domain.usecase.SeedCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingTemplateViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val seedCategoriesUseCase: SeedCategoriesUseCase,
    private val userPreferences: UserPreferencesDataSource
) : ViewModel() {

    val templates = HabitTemplate.defaults
    private val selectedTemplates = mutableSetOf<HabitTemplate>()

    fun toggleTemplate(template: HabitTemplate) {
        if (template in selectedTemplates) selectedTemplates.remove(template)
        else selectedTemplates.add(template)
    }

    fun isSelected(template: HabitTemplate) = template in selectedTemplates

    fun finishOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            seedCategoriesUseCase.seedIfEmpty()
            selectedTemplates.forEach { template ->
                val habit = Habit(
                    title = template.name,
                    description = "",
                    frequency = template.defaultFrequency
                )
                habitRepository.addHabit(habit)
            }
            userPreferences.saveHasOnboarded(true)
            onComplete()
        }
    }
}
