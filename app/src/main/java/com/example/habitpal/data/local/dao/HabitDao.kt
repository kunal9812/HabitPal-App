package com.example.habitpal.data.local.dao

import androidx.room.*
import com.example.habitpal.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isActive = 1 AND isArchived = 0 ORDER BY sortOrder ASC, createdAt DESC")
    fun getActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY title ASC")
    fun getArchivedHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Int): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("UPDATE habits SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteHabit(id: Int)

    @Query("SELECT * FROM habits WHERE isActive = 1 AND reminderTime IS NOT NULL")
    suspend fun getHabitsWithReminders(): List<HabitEntity>

    @Query("UPDATE habits SET isArchived = 1 WHERE id = :habitId")
    suspend fun archiveHabit(habitId: Int)

    @Query("UPDATE habits SET isArchived = 0 WHERE id = :habitId")
    suspend fun restoreHabit(habitId: Int)

    @Query("UPDATE habits SET sortOrder = :sortOrder WHERE id = :habitId")
    suspend fun updateSortOrder(habitId: Int, sortOrder: Int)

    @Transaction
    suspend fun updateSortOrders(updates: List<Pair<Int, Int>>) {
        updates.forEach { (id, order) -> updateSortOrder(id, order) }
    }
}

