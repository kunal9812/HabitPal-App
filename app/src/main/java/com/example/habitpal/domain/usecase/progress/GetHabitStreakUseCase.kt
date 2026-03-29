package com.example.habitpal.domain.usecase.progress

import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.util.startOfDay
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetHabitStreakUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    /**
     * Returns the current streak (consecutive days completed) for a given habit.
     */
    suspend operator fun invoke(habitId: Int): Int {
        val logs = habitRepository.getLogsForHabit(habitId).first()
        if (logs.isEmpty()) return 0

        val completedDays = logs
            .map { startOfDay(it.completedAt) }
            .distinct()
            .sortedDescending()

        var streak = 0
        var expectedDay = startOfDay(System.currentTimeMillis())

        for (day in completedDays) {
            if (day == expectedDay) {
                streak++
                expectedDay -= TimeUnit.DAYS.toMillis(1)
            } else if (day < expectedDay) {
                break
            }
        }
        return streak
    }
}

