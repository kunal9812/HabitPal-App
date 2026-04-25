package com.example.habitpal.data.local.converter

import androidx.room.TypeConverter
import com.example.habitpal.domain.model.HabitFrequency

class FrequencyConverter {
    @TypeConverter
    fun fromFrequency(f: HabitFrequency): String = when (f) {
        HabitFrequency.DAILY -> "DAILY"
        HabitFrequency.WEEKLY -> "WEEKLY"
        HabitFrequency.MONTHLY -> "MONTHLY"
    }

    @TypeConverter
    fun toFrequency(value: String): HabitFrequency =
        HabitFrequency.entries.firstOrNull { it.name == value } ?: HabitFrequency.DAILY
}