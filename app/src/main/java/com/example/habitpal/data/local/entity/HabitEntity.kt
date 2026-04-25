package com.example.habitpal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val frequency: String = "DAILY",
    val frequencyJson: String = "DAILY",
    val reminderTime: String? = null,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val color: Int = 0,
    val icon: String = "ic_habit_default",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,

    // Scheduling (v2)
    val timeOfDay: String? = null,

    // Organisation (v2)
    val categoryId: Int? = null,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
)


