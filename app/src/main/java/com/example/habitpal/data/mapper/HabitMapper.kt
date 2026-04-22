package com.example.habitpal.data.mapper

import com.example.habitpal.data.local.entity.HabitEntity
import com.example.habitpal.data.local.entity.HabitLogEntity
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.domain.model.HabitLog

fun HabitEntity.toDomain(completedTodayIds: Set<Int> = emptySet()): Habit = Habit(
    id = id,
    title = title,
    description = description,
    frequency = try { HabitFrequency.valueOf(frequency) } catch (e: Exception) { HabitFrequency.DAILY },
    reminderTime = reminderTime,
    color = color,
    icon = icon,
    createdAt = createdAt,
    isActive = isActive,
    isCompletedToday = id in completedTodayIds,
    categoryId = categoryId,
    sortOrder = sortOrder,
    isArchived = isArchived
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    title = title,
    description = description,
    frequency = frequency.name,
    reminderTime = reminderTime,
    color = color,
    icon = icon,
    createdAt = createdAt,
    isActive = isActive,
    frequencyJson = frequency.name,
    categoryId = categoryId,
    sortOrder = sortOrder,
    isArchived = isArchived
)

fun HabitLogEntity.toDomain(): HabitLog = HabitLog(
    id = id,
    habitId = habitId,
    completedAt = completedAt,
    notes = notes
)

fun HabitLog.toEntity(): HabitLogEntity = HabitLogEntity(
    id = id,
    habitId = habitId,
    completedAt = completedAt,
    notes = notes
)
