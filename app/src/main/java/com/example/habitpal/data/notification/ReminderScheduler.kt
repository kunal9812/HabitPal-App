package com.example.habitpal.data.notification

import android.content.Context
import androidx.work.*
import com.example.habitpal.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(@ApplicationContext private val context: Context) {

    fun schedule(habit: Habit) {
        val hour = habit.reminderHour
        if (hour == null) {
            cancel(habit.id)
            return
        }
        val minute = habit.reminderMinute ?: 0
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(hour, minute)
        if (!target.isAfter(now)) target = target.plusDays(1)

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "reminder_${habit.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(Duration.between(now, target).toMillis(), TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(HabitReminderWorker.KEY_HABIT_ID to habit.id))
                .build()
        )
    }

    fun cancel(habitId: Int) = WorkManager.getInstance(context).cancelUniqueWork("reminder_$habitId")

    fun rescheduleAll(habits: List<Habit>) = habits.forEach { schedule(it) }
}