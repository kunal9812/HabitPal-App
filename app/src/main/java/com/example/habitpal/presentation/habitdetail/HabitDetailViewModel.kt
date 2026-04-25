package com.example.habitpal.presentation.habitdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.domain.usecase.ArchiveHabitUseCase
import com.example.habitpal.domain.usecase.habit.CompleteHabitUseCase
import com.example.habitpal.domain.usecase.habit.GetHabitsUseCase
import com.example.habitpal.domain.usecase.habit.GetHabitStatsUseCase
import com.example.habitpal.domain.usecase.habit.HabitStats
import com.example.habitpal.domain.usecase.progress.GetHabitStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val getHabitsUseCase: GetHabitsUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val archiveHabitUseCase: ArchiveHabitUseCase,
    private val getHabitStreakUseCase: GetHabitStreakUseCase,
    private val getHabitStatsUseCase: GetHabitStatsUseCase
) : ViewModel() {

    private val _habit = MutableStateFlow<Habit?>(null)
    val habit: StateFlow<Habit?> = _habit.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _isArchived = MutableStateFlow(false)
    val isArchived: StateFlow<Boolean> = _isArchived.asStateFlow()

    private val _completionMap = MutableStateFlow<Map<LocalDate, Boolean>>(emptyMap())
    val completionMap: StateFlow<Map<LocalDate, Boolean>> = _completionMap.asStateFlow()

    private val _stats = MutableStateFlow<HabitStats?>(null)
    val stats: StateFlow<HabitStats?> = _stats.asStateFlow()

    fun loadHabit(habitId: Int) {
        viewModelScope.launch {
            getHabitsUseCase().collect { habits ->
                _habit.value = habits.find { it.id == habitId }
                _streak.value = getHabitStreakUseCase(habitId)
            }
        }
        loadHistory(habitId)
    }

    fun loadHistory(habitId: Int) {
        viewModelScope.launch {
            val end = LocalDate.now()
            val start = end.minusMonths(6)
            habitRepository.getCompletionsInRange(habitId, start.toString(), end.toString())
                .collect { dates ->
                _completionMap.value = dates.associateWith { true }
                _stats.value = getHabitStatsUseCase.calculate(
                    dates,
                    _habit.value?.createdAt ?: System.currentTimeMillis()
                )
            }
        }
    }

    fun completeHabit(habitId: Int, note: String? = null) {
        viewModelScope.launch {
            completeHabitUseCase(habitId, note)
            _streak.value = getHabitStreakUseCase(habitId)
        }
    }

    fun archiveHabit() {
        viewModelScope.launch {
            _habit.value?.let {
                archiveHabitUseCase.archive(it.id)
                _isArchived.value = true
            }
        }
    }
}


