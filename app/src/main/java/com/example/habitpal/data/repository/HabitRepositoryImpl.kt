package com.example.habitpal.data.repository

import com.example.habitpal.data.local.dao.HabitDao
import com.example.habitpal.data.local.dao.HabitLogDao
import com.example.habitpal.data.mapper.toDomain
import com.example.habitpal.data.mapper.toEntity
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitLog
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.util.startOfDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> =
        combine(habitDao.getAllHabits(), habitLogDao.getAllLogs()) { entities, logs ->
            val todayStart = startOfDay()
            val todayEnd = todayStart + TimeUnit.DAYS.toMillis(1)
            val completedTodayIds = logs
                .filter { it.completedAt in todayStart until todayEnd }
                .map { it.habitId }
                .toSet()
            entities.map { it.toDomain(completedTodayIds) }
        }

    override fun getActiveHabits(): Flow<List<Habit>> =
        combine(habitDao.getActiveHabits(), habitLogDao.getAllLogs()) { entities, logs ->
            val todayStart = startOfDay()
            val todayEnd = todayStart + TimeUnit.DAYS.toMillis(1)
            val completedTodayIds = logs
                .filter { it.completedAt in todayStart until todayEnd }
                .map { it.habitId }
                .toSet()
            entities.map { it.toDomain(completedTodayIds) }
        }

    override fun getArchivedHabits(): Flow<List<Habit>> =
        habitDao.getArchivedHabits().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getHabitById(id: Int): Habit? =
        habitDao.getHabitById(id)?.toDomain()

    override suspend fun addHabit(habit: Habit): Long =
        habitDao.insertHabit(habit.toEntity())

    override suspend fun updateHabit(habit: Habit) =
        habitDao.updateHabit(habit.toEntity())

    override suspend fun deleteHabit(habit: Habit) =
        habitDao.deleteHabit(habit.toEntity())

    override suspend fun softDeleteHabit(id: Int) =
        habitDao.softDeleteHabit(id)

    override suspend fun archiveHabit(habitId: Int) =
        habitDao.archiveHabit(habitId)

    override suspend fun restoreHabit(habitId: Int) =
        habitDao.restoreHabit(habitId)

    override suspend fun updateSortOrders(updates: List<Pair<Int, Int>>) =
        habitDao.updateSortOrders(updates)

    override fun getLogsForHabit(habitId: Int): Flow<List<HabitLog>> =
        habitLogDao.getLogsForHabit(habitId).map { entities -> entities.map { it.toDomain() } }

    override fun getAllLogs(): Flow<List<HabitLog>> =
        habitLogDao.getAllLogs().map { entities -> entities.map { it.toDomain() } }

    override fun getLogsInRangeFlow(habitId: Int, startMs: Long, endMs: Long): Flow<List<HabitLog>> =
        habitLogDao.getLogsInRangeFlow(habitId, startMs, endMs).map { entities -> entities.map { it.toDomain() } }

    override suspend fun logHabitCompletion(log: HabitLog): Long =
        habitLogDao.insertLog(log.toEntity())

    override suspend fun deleteLog(log: HabitLog) =
        habitLogDao.deleteLog(log.toEntity())

    override suspend fun getLogsForHabitInRange(
        habitId: Int,
        startTime: Long,
        endTime: Long
    ): List<HabitLog> =
        habitLogDao.getLogsForHabitInRange(habitId, startTime, endTime).map { it.toDomain() }
}


