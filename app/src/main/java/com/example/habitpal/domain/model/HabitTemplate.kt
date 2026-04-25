package com.example.habitpal.domain.model

data class HabitTemplate(
    val title: String,
    val emoji: String,
    val categoryName: String
) {
    // Backward-compatible alias for older onboarding code paths.
    val name: String
        get() = title

    companion object {
        val defaults = listOf(
            HabitTemplate("Drink water",       "💧", "Health"),
            HabitTemplate("Morning run",       "🏃", "Health"),
            HabitTemplate("Workout",           "💪", "Health"),
            HabitTemplate("Sleep 8 hours",     "😴", "Health"),
            HabitTemplate("Take vitamins",     "💊", "Health"),
            HabitTemplate("Read 10 pages",     "📖", "Learning"),
            HabitTemplate("Study language",    "🗣️", "Learning"),
            HabitTemplate("Watch lecture",     "🎓", "Learning"),
            HabitTemplate("Practice coding",   "💻", "Learning"),
            HabitTemplate("Meditate",          "🧘", "Mindfulness"),
            HabitTemplate("Gratitude journal", "📝", "Mindfulness"),
            HabitTemplate("Deep breathing",    "🌬️", "Mindfulness"),
            HabitTemplate("No social media",   "📵", "Mindfulness"),
            HabitTemplate("Daily planning",    "📅", "Work"),
            HabitTemplate("Email inbox zero",  "📧", "Work"),
            HabitTemplate("Focus session",     "🎯", "Work"),
            HabitTemplate("Track spending",    "💰", "Finance"),
            HabitTemplate("Save daily",        "🏦", "Finance"),
            HabitTemplate("Call a friend",     "📱", "Personal"),
            HabitTemplate("Journaling",        "📓", "Personal")
        )
    }
}
