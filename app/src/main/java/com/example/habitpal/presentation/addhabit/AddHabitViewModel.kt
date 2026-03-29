package com.example.habitpal.presentation.addhabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.domain.usecase.habit.AddHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AddHabitEvent {
    object HabitAdded : AddHabitEvent()
    data class Error(val message: String) : AddHabitEvent()
}

@HiltViewModel
class AddHabitViewModel @Inject constructor(
    private val addHabitUseCase: AddHabitUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<AddHabitEvent>()
    val events: SharedFlow<AddHabitEvent> = _events.asSharedFlow()

    fun addHabit(
        title: String,
        description: String,
        frequency: HabitFrequency = HabitFrequency.DAILY,
        reminderTime: String? = null,
        color: Int = 0,
        icon: String = "ic_habit_default"
    ) {
        if (title.isBlank()) {
            viewModelScope.launch {
                _events.emit(AddHabitEvent.Error("Title cannot be empty"))
            }
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                addHabitUseCase(
                    Habit(
                        title = title.trim(),
                        description = description.trim(),
                        frequency = frequency,
                        reminderTime = reminderTime,
                        color = color,
                        icon = icon
                    )
                )
                _events.emit(AddHabitEvent.HabitAdded)
            } catch (e: Exception) {
                _events.emit(AddHabitEvent.Error(e.localizedMessage ?: "Failed to add habit"))
            } finally {
                _isLoading.value = false
            }
        }
    }
}

