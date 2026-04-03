package com.example.habitpal.domain.usecase.habit

import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.repository.HabitRepository
import javax.inject.Inject

class UpdateHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit) = habitRepository.updateHabit(habit)
}
