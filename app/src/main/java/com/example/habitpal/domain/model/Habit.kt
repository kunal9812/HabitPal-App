package com.example.habitpal.domain.model

enum class HabitCategory(val displayName: String) {
    DEFAULT("Default"),
    HEALTH("Health"),
    MIND("Mind"),
    SOCIAL("Social"),
    LEARN("Learn"),
    WELLNESS("Wellness"),
    GROWTH("Growth"),
    FINANCE("Finance"),
    MINDFULNESS("Mindfulness"),
    PERSONAL("Personal")
}

data class Habit(
    val id: Int = 0,
    val title: String,
    val description: String,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val category: HabitCategory = HabitCategory.DEFAULT,
    val categoryId: Int? = null,
    val reminderTime: String? = null,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val color: Int = 0,
    val icon: String = "ic_habit_default",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val isCompletedToday: Boolean = false
)
