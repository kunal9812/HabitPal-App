package com.example.habitpal.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.domain.model.HabitRingStats
import com.example.habitpal.domain.model.WeeklyStats
import com.example.habitpal.domain.usecase.progress.GetHabitRingStatsUseCase
import com.example.habitpal.domain.usecase.progress.GetProgressStatsUseCase
import com.example.habitpal.domain.usecase.progress.GetWeeklyStatsUseCase
import com.example.habitpal.domain.usecase.progress.ProgressStats
import com.example.habitpal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressStatsUseCase: GetProgressStatsUseCase,
    private val getWeeklyStatsUseCase: GetWeeklyStatsUseCase,
    private val getHabitRingStatsUseCase: GetHabitRingStatsUseCase
) : ViewModel() {

    private val _stats = MutableStateFlow<Resource<ProgressStats>>(Resource.Loading)
    val stats: StateFlow<Resource<ProgressStats>> = _stats.asStateFlow()

    private val _weeklyStats = MutableStateFlow<List<WeeklyStats>>(emptyList())
    val weeklyStats: StateFlow<List<WeeklyStats>> = _weeklyStats.asStateFlow()

    private val _habitRingStats = MutableStateFlow<List<HabitRingStats>>(emptyList())
    val habitRingStats: StateFlow<List<HabitRingStats>> = _habitRingStats.asStateFlow()

    private val _selectedHabitIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedHabitIds: StateFlow<Set<Int>> = _selectedHabitIds.asStateFlow()

    init {
        loadStats()
        loadWeeklyStats()
        loadHabitRingStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _stats.value = Resource.Loading
            try {
                getProgressStatsUseCase.observe().collect { stats ->
                    _stats.value = Resource.Success(stats)
                }
            } catch (e: Exception) {
                _stats.value = Resource.Error(e.localizedMessage ?: "Failed to load stats")
            }
        }
    }

    private fun loadWeeklyStats() {
        viewModelScope.launch {
            getWeeklyStatsUseCase().collect { _weeklyStats.value = it }
        }
    }

    private fun loadHabitRingStats() {
        viewModelScope.launch {
            getHabitRingStatsUseCase().collect { rings ->
                _habitRingStats.value = rings
                // auto select first 3 by default
                if (_selectedHabitIds.value.isEmpty()) {
                    _selectedHabitIds.value = rings.take(3).map { it.habit.id }.toSet()
                }
            }
        }
    }

    fun toggleHabitSelection(habitId: Int) {
        val current = _selectedHabitIds.value.toMutableSet()
        if (current.contains(habitId)) {
            if (current.size > 1) current.remove(habitId)
        } else {
            current.add(habitId)
        }
        _selectedHabitIds.value = current
    }
}