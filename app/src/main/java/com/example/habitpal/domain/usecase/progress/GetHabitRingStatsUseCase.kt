package com.example.habitpal.domain.usecase.progress

import com.example.habitpal.domain.model.HabitRingStats
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.util.startOfDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetHabitRingStatsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val getHabitStreakUseCase: GetHabitStreakUseCase
) {
    operator fun invoke(): Flow<List<HabitRingStats>> {
        return combine(
            habitRepository.getAllHabits(),
            habitRepository.getAllLogs()
        ) { habits, logs ->
            val todayStart = startOfDay()
            val todayEnd = todayStart + TimeUnit.DAYS.toMillis(1)

            // calculate completion over last 7 days
            val weekStart = todayStart - TimeUnit.DAYS.toMillis(6)

            habits.map { habit ->
                val logsForHabit = logs.filter { it.habitId == habit.id }

                val daysCompleted = (0..6).count { offset ->
                    val dayStart = todayStart - TimeUnit.DAYS.toMillis(offset.toLong())
                    val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)
                    logsForHabit.any { it.completedAt in dayStart until dayEnd }
                }

                val completionPercent = (daysCompleted / 7f) * 100f
                val streak = getHabitStreakUseCase(habit.id)

                HabitRingStats(
                    habit = habit,
                    completionPercent = completionPercent,
                    currentStreak = streak
                )
            }
        }
    }
}