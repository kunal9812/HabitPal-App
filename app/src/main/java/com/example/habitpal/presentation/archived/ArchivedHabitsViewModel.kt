package com.example.habitpal.presentation.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.domain.usecase.ArchiveHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchivedHabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val archiveHabitUseCase: ArchiveHabitUseCase
) : ViewModel() {

    val archivedHabits: StateFlow<List<Habit>> = habitRepository.getArchivedHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreHabit(habitId: Int) {
        viewModelScope.launch {
            archiveHabitUseCase.restore(habitId)
        }
    }
}
