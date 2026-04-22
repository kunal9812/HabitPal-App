package com.example.habitpal.domain.usecase.habit

import com.example.habitpal.domain.model.HabitLog
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.util.endOfDay
import com.example.habitpal.util.startOfDay
import javax.inject.Inject

class ToggleHabitCompletionUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: Int, note: String? = null) {
        val todayLogs = repository.getLogsForHabitInRange(
            habitId = habitId,
            startTime = startOfDay(),
            endTime = endOfDay()
        )

        if (todayLogs.isNotEmpty()) {
            repository.deleteLog(todayLogs.first())
        } else {
            repository.logHabitCompletion(
                HabitLog(
                    habitId = habitId,
                    completedAt = System.currentTimeMillis(),
                    notes = note
                )
            )
        }
    }
}

