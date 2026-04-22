package com.example.habitpal.data.local.dao

import androidx.room.*
import com.example.habitpal.data.local.entity.HabitLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitLogDao {

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY completedAt DESC")
    fun getLogsForHabit(habitId: Int): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs ORDER BY completedAt DESC")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Delete
    suspend fun deleteLog(log: HabitLogEntity)

    @Query(
        "SELECT * FROM habit_logs WHERE habitId = :habitId " +
        "AND completedAt BETWEEN :startTime AND :endTime"
    )
    suspend fun getLogsForHabitInRange(
        habitId: Int,
        startTime: Long,
        endTime: Long
    ): List<HabitLogEntity>

    @Query(
        "SELECT * FROM habit_logs WHERE habitId = :habitId " +
        "AND completedAt BETWEEN :startTime AND :endTime"
    )
    fun getLogsInRangeFlow(
        habitId: Int,
        startTime: Long,
        endTime: Long
    ): Flow<List<HabitLogEntity>>

    @Query(
        "SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId " +
        "AND completedAt BETWEEN :dayStart AND :dayEnd"
    )
    suspend fun countForDay(habitId: Int, dayStart: Long, dayEnd: Long): Int
}


