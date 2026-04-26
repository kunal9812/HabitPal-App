package com.example.habitpal.data.mapper

import com.example.habitpal.data.local.converter.FrequencyConverter
import com.example.habitpal.data.local.entity.HabitEntity
import com.example.habitpal.data.local.entity.HabitLogEntity
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitCategory
import com.example.habitpal.domain.model.HabitLog

fun HabitEntity.toDomain(completedTodayIds: Set<Int> = emptySet()): Habit = Habit(
    id = id,
    title = title,
    description = description,
    frequency = FrequencyConverter().toFrequency(frequencyJson),
    category = categoryIdToHabitCategory(categoryId),
    categoryId = categoryId,
    reminderTime = reminderTime,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    color = color,
    icon = icon,
    createdAt = createdAt,
    isActive = isActive,
    isCompletedToday = id in completedTodayIds
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    title = title,
    description = description,
    frequencyJson = FrequencyConverter().fromFrequency(frequency),
    categoryId = categoryId ?: habitCategoryToId(category),
    reminderTime = reminderTime,
    reminderHour = reminderHour,
    reminderMinute = reminderMinute,
    color = color,
    icon = icon,
    createdAt = createdAt,
    isActive = isActive
)

fun HabitLogEntity.toDomain(): HabitLog = HabitLog(
    id = id,
    habitId = habitId,
    completedAt = completedAt,
    notes = note
)

fun HabitLog.toEntity(): HabitLogEntity = HabitLogEntity(
    id = id,
    habitId = habitId,
    completedAt = completedAt,
    note = notes
)

private fun categoryIdToHabitCategory(categoryId: Int?): HabitCategory {
    return when (categoryId) {
        1 -> HabitCategory.HEALTH
        2 -> HabitCategory.MIND
        3 -> HabitCategory.SOCIAL
        4 -> HabitCategory.LEARN
        5 -> HabitCategory.WELLNESS
        6 -> HabitCategory.GROWTH
        7 -> HabitCategory.FINANCE
        8 -> HabitCategory.MINDFULNESS
        9 -> HabitCategory.PERSONAL
        else -> HabitCategory.DEFAULT
    }
}

private fun habitCategoryToId(category: HabitCategory): Int {
    return when (category) {
        HabitCategory.HEALTH -> 1
        HabitCategory.MIND -> 2
        HabitCategory.SOCIAL -> 3
        HabitCategory.LEARN -> 4
        HabitCategory.WELLNESS -> 5
        HabitCategory.GROWTH -> 6
        HabitCategory.FINANCE -> 7
        HabitCategory.MINDFULNESS -> 8
        HabitCategory.PERSONAL -> 9
        HabitCategory.DEFAULT -> 0
    }
}
