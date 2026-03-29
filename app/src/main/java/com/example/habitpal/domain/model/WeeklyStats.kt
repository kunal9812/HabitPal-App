package com.example.habitpal.domain.model

data class WeeklyStats(
    val dayName: String,
    val completionPercent: Float,
    val isToday: Boolean
)

data class HabitRingStats(
    val habit: Habit,
    val completionPercent: Float,
    val currentStreak: Int,
    val isSelected: Boolean = false
)