package com.example.habitpal.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.habitpal.data.local.entity.CompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completionDate = :date LIMIT 1")
    suspend fun getCompletionForDate(habitId: Int, date: String): CompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: CompletionEntity): Long

    @Delete
    suspend fun deleteCompletion(completion: CompletionEntity)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completionDate DESC")
    suspend fun getCompletionsForHabit(habitId: Int): List<CompletionEntity>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND completionDate BETWEEN :start AND :end")
    fun getCompletionsInRange(habitId: Int, start: String, end: String): Flow<List<CompletionEntity>>
}
