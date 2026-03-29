package com.example.habitpal.domain.usecase.habit

import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.util.startOfDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    operator fun invoke(): Flow<List<Habit>> {
        return combine(
            habitRepository.getAllHabits(),
            habitRepository.getAllLogs()
        ) { habits, logs ->

            habits
                .map { habit ->
                    val windowStart = getWindowStart(habit.frequency)
                    val windowEnd = System.currentTimeMillis()

                    val isCompletedInWindow = logs.any {
                        it.habitId == habit.id && it.completedAt in windowStart..windowEnd
                    }

                    habit.copy(isCompletedToday = isCompletedInWindow)
                }
                .sortedWith(compareBy({ it.isCompletedToday }, { it.createdAt }))
        }
    }

    private fun getWindowStart(frequency: HabitFrequency): Long {
        val calendar = Calendar.getInstance()

        return when (frequency) {
            HabitFrequency.DAILY -> {
                startOfDay()
            }
            HabitFrequency.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            HabitFrequency.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
        }
    }
}

