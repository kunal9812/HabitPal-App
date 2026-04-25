package com.example.habitpal.data.mapper

import com.example.habitpal.data.local.converter.FrequencyConverter
import com.example.habitpal.data.local.entity.HabitEntity
import com.example.habitpal.data.local.entity.HabitLogEntity
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitLog

fun HabitEntity.toDomain(completedTodayIds: Set<Int> = emptySet()): Habit = Habit(
    id = id,
    title = title,
    description = description,
    frequency = FrequencyConverter().toFrequency(frequencyJson),
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

