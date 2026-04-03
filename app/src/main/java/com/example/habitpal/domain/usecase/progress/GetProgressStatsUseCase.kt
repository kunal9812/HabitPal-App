package com.example.habitpal.domain.usecase.progress

import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.util.startOfDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ProgressStats(
    val totalHabits: Int,
    val completedToday: Int,
    val completionRatePercent: Int,
    val longestStreak: Int,
    val totalCompletions: Int
)

class GetProgressStatsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val getHabitStreakUseCase: GetHabitStreakUseCase
) {
    // One-shot snapshot — returns the first emission from the reactive flow.
    suspend operator fun invoke(): ProgressStats = observe().first()

    fun observe(): Flow<ProgressStats> {
        return combine(
            habitRepository.getAllHabits(),
            habitRepository.getAllLogs()
        ) { habits, allLogs ->
            val todayStart = startOfDay()
            val todayEnd = todayStart + TimeUnit.DAYS.toMillis(1)

            val completedTodayHabitIds = allLogs
                .filter { it.completedAt in todayStart until todayEnd }
                .map { it.habitId }
                .distinct()

            val totalHabits = habits.size
            val completedToday = completedTodayHabitIds.size
            val completionRate = if (totalHabits > 0) (completedToday * 100) / totalHabits else 0

            val longestStreak = habits.maxOfOrNull { habit ->
                getHabitStreakUseCase(habit.id)
            } ?: 0

            ProgressStats(
                totalHabits = totalHabits,
                completedToday = completedToday,
                completionRatePercent = completionRate,
                longestStreak = longestStreak,
                totalCompletions = allLogs.size
            )
        }
    }
}