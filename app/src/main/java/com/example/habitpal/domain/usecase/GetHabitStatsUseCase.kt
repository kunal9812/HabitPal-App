package com.example.habitpal.domain.usecase

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

    fun calculate(completionDates: List<LocalDate>, createdDate: LocalDate): HabitStats {
        if (completionDates.isEmpty()) return HabitStats(0, 0, 0, 0f)

        val sorted = completionDates.sortedDescending()
        var currentStreak = 0
        var bestStreak = 0
        var streak = 0
        var prev: LocalDate? = null

        for (date in sorted) {
            streak = when {
                prev == null -> 1
                prev == date.plusDays(1) -> streak + 1
                else -> 1
            }
            if (currentStreak == 0) currentStreak = streak
            if (streak > bestStreak) bestStreak = streak
            prev = date
        }

        val daysSinceCreation = ChronoUnit.DAYS.between(createdDate, LocalDate.now()).toInt() + 1
        val allTimeRate = sorted.size.toFloat() / daysSinceCreation.coerceAtLeast(1)

        return HabitStats(currentStreak, bestStreak, sorted.size, allTimeRate)
    }

    /** Convenience: convert epoch-millis log timestamps to LocalDate list using device timezone. */
    fun fromEpochMillisList(epochMillisList: List<Long>): List<LocalDate> {
        val zone = ZoneId.systemDefault()
        return epochMillisList.map { ms ->
            Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
        }.distinct()
    }
}
