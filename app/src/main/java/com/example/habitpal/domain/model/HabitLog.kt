package com.example.habitpal.domain.model

data class HabitLog(
    val id: Int = 0,
    val habitId: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val notes: String? = null
)

