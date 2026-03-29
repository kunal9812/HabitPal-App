package com.example.habitpal.util

import com.example.habitpal.R

object HabitCardColors {

    private val colors = listOf(
        0xFFC5E8A0.toInt(),
        0xFFF4A7C3.toInt(),
        0xFFA8D8F0.toInt(),
        0xFFFAE29F.toInt(),
        0xFFC9B8F0.toInt(),
        0xFFFFB347.toInt(),
        0xFF80CBC4.toInt(),
        0xFFFF8A80.toInt()
    )

    fun getColor(habitId: Int): Int {
        return colors[habitId % colors.size]
    }
}