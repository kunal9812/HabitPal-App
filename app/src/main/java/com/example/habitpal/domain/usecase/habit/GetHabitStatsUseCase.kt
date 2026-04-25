package com.example.habitpal.domain.usecase.habit

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HabitStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val allTimeRate: Float
)

class GetHabitStatsUseCase @Inject constructor() {
    fun calculate(dates: List<LocalDate>, createdAt: Long): HabitStats {
        if (dates.isEmpty()) return HabitStats(0, 0, 0, 0f)

        val sorted = dates.distinct().sortedDescending()
        var streak = 0
        var best = 0
        var current = 0
        var prev: LocalDate? = null

        for (date in sorted) {
            streak = if (prev == null) 1 else if (prev == date.plusDays(1)) streak + 1 else 1
            if (current == 0) current = streak
            if (streak > best) best = streak
            prev = date
        }

        val created = Instant.ofEpochMilli(createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val days = ChronoUnit.DAYS.between(created, LocalDate.now()).toInt().coerceAtLeast(1)

        return HabitStats(current, best, sorted.size, sorted.size.toFloat() / days)
    }
}

