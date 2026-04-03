package com.example.habitpal.presentation.edithabit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.domain.usecase.habit.GetHabitsUseCase
import com.example.habitpal.domain.usecase.habit.UpdateHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditHabitEvent {
    object HabitSaved : EditHabitEvent()
    data class Error(val message: String) : EditHabitEvent()
}

@HiltViewModel
class EditHabitViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val updateHabitUseCase: UpdateHabitUseCase
) : ViewModel() {

    private val _habit = MutableStateFlow<Habit?>(null)
    val habit: StateFlow<Habit?> = _habit.asStateFlow()

    private val _events = MutableSharedFlow<EditHabitEvent>()
    val events: SharedFlow<EditHabitEvent> = _events.asSharedFlow()

    fun loadHabit(habitId: Int) {
        viewModelScope.launch {
            getHabitsUseCase().collect { habits ->
                _habit.value = habits.find { it.id == habitId }
            }
        }
    }

    fun saveHabit(
        title: String,
        description: String,
        frequency: HabitFrequency,
        color: Int
    ) {
        val current = _habit.value ?: return
        if (title.isBlank()) {
            viewModelScope.launch {
                _events.emit(EditHabitEvent.Error("Title cannot be empty"))
            }
            return
        }
        viewModelScope.launch {
            try {
                updateHabitUseCase(
                    current.copy(
                        title = title.trim(),
                        description = description.trim(),
                        frequency = frequency,
                        color = color
                    )
                )
                _events.emit(EditHabitEvent.HabitSaved)
            } catch (e: Exception) {
                _events.emit(EditHabitEvent.Error(e.localizedMessage ?: "Failed to save habit"))
            }
        }
    }
}
