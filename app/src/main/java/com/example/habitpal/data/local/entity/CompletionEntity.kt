package com.example.habitpal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("completionDate")]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val habitId: Int,
    /** ISO-8601 date string, e.g. "2025-04-25" */
    val completionDate: String,
    val note: String? = null
)
