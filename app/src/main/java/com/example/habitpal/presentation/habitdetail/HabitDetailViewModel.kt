package com.example.habitpal.presentation.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitLog
import com.example.habitpal.domain.usecase.habit.CompleteHabitUseCase
import com.example.habitpal.domain.usecase.habit.DeleteHabitUseCase
import com.example.habitpal.domain.usecase.habit.GetHabitsUseCase
import com.example.habitpal.domain.usecase.progress.GetHabitStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val getHabitStreakUseCase: GetHabitStreakUseCase
) : ViewModel() {

    private val _habit = MutableStateFlow<Habit?>(null)
    val habit: StateFlow<Habit?> = _habit.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()

    fun loadHabit(habitId: Int) {
        viewModelScope.launch {
            getHabitsUseCase().collect { habits ->
                _habit.value = habits.find { it.id == habitId }
                _streak.value = getHabitStreakUseCase(habitId)
            }
        }
    }

    fun completeHabit(habitId: Int) {
        viewModelScope.launch {
            completeHabitUseCase(habitId)
            _streak.value = getHabitStreakUseCase(habitId)
        }
    }

    fun deleteHabit() {
        viewModelScope.launch {
            _habit.value?.let {
                deleteHabitUseCase(it)
                _isDeleted.value = true
            }
        }
    }
}

