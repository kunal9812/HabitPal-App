package com.example.habitpal.domain.model

enum class HabitFrequency {
    DAILY, WEEKLY, MONTHLY
}

data class Habit(
    val id: Int = 0,
    val title: String,
    val description: String,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val reminderTime: String? = null,
    val color: Int = 0,
    val icon: String = "ic_habit_default",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val isCompletedToday: Boolean = false,
    // v2 fields
    val categoryId: Int? = null,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
)
