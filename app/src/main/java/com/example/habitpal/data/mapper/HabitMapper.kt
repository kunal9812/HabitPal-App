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
    frequency = HabitFrequency.valueOf(frequency),
    reminderTime = reminderTime,
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
    frequency = frequency.name,
    reminderTime = reminderTime,
    color = color,
    icon = icon,
    createdAt = createdAt,
    isActive = isActive
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

