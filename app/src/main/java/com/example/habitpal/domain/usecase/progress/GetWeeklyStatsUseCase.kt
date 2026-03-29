package com.example.habitpal.domain.usecase.progress

import com.example.habitpal.domain.model.WeeklyStats
import com.example.habitpal.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetWeeklyStatsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    operator fun invoke(): Flow<List<WeeklyStats>> {
        return combine(
            habitRepository.getAllHabits(),
            habitRepository.getAllLogs()
        ) { habits, logs ->
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val today = Calendar.getInstance()
            val todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK)

            (0..6).map { offset ->
                val cal = Calendar.getInstance()
                // get start of this week (Monday)
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.add(Calendar.DAY_OF_MONTH, offset)

                val dayStart = cal.timeInMillis
                val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)

                val completedCount = logs
                    .filter { it.completedAt in dayStart until dayEnd }
                    .map { it.habitId }
                    .distinct()
                    .size

                val totalHabits = habits.size
                val percent = if (totalHabits > 0)
                    (completedCount.toFloat() / totalHabits) * 100f
                else 0f

                val isToday = cal.get(Calendar.DAY_OF_WEEK) == todayDayOfWeek

                WeeklyStats(
                    dayName = dayNames[offset],
                    completionPercent = percent,
                    isToday = isToday
                )
            }
        }
    }
}