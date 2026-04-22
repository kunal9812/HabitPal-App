package com.example.habitpal.domain.usecase

import com.example.habitpal.util.ReminderScheduler
import com.example.habitpal.domain.repository.HabitRepository
import javax.inject.Inject

class ArchiveHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend fun archive(habitId: Int) {
        repository.archiveHabit(habitId)
        reminderScheduler.cancel(habitId)
    }

    suspend fun restore(habitId: Int) {
        repository.restoreHabit(habitId)
    }
}
