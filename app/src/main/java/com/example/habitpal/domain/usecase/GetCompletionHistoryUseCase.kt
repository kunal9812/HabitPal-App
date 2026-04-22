package com.example.habitpal.domain.usecase

import com.example.habitpal.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetCompletionHistoryUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    /** Returns a Flow<Map<LocalDate, Boolean>> for the past [monthsBack] months. */
    fun execute(habitId: Int, monthsBack: Int = 6): Flow<Map<LocalDate, Boolean>> {
        val zone = ZoneId.systemDefault()
        val endDate = LocalDate.now()
        val startDate = endDate.minusMonths(monthsBack.toLong())

        val startMs = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs   = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        return repository.getLogsInRangeFlow(habitId, startMs, endMs)
            .map { logs ->
                logs.associate { log ->
                    Instant.ofEpochMilli(log.completedAt).atZone(zone).toLocalDate() to true
                }
            }
    }
}
