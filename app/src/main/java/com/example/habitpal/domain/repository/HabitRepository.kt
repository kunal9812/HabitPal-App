package com.example.habitpal.domain.repository

import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitLog
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getActiveHabits(): Flow<List<Habit>>
    fun getArchivedHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: Int): Habit?
    suspend fun addHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun softDeleteHabit(id: Int)
    suspend fun archiveHabit(habitId: Int)
    suspend fun restoreHabit(habitId: Int)
    suspend fun updateSortOrders(updates: List<Pair<Int, Int>>)
    fun getLogsForHabit(habitId: Int): Flow<List<HabitLog>>
    fun getAllLogs(): Flow<List<HabitLog>>
    fun getLogsInRangeFlow(habitId: Int, startMs: Long, endMs: Long): Flow<List<HabitLog>>
    suspend fun logHabitCompletion(log: HabitLog): Long
    suspend fun deleteLog(log: HabitLog)
    suspend fun getLogsForHabitInRange(habitId: Int, startTime: Long, endTime: Long): List<HabitLog>
}


