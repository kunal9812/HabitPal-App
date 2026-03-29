package com.example.habitpal.domain.usecase.habit

import com.example.habitpal.domain.model.HabitLog
import com.example.habitpal.domain.repository.HabitRepository
import javax.inject.Inject

class CompleteHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habitId: Int, notes: String? = null): Long {
        val log = HabitLog(
            habitId = habitId,
            completedAt = System.currentTimeMillis(),
            notes = notes
        )
        return habitRepository.logHabitCompletion(log)
    }
}

