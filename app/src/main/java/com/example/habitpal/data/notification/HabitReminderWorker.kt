package com.example.habitpal.data.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val habitRepository: HabitRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {


    companion object {
        const val KEY_HABIT_ID = "habit_id"
    }

    override suspend fun doWork(): Result {
        val habitId = inputData.getInt(KEY_HABIT_ID, -1)
        if (habitId == -1) return Result.failure()

        val habit = habitRepository.getHabitById(habitId) ?: return Result.failure()

        // Only notify if not completed today
        val alreadyDone = habitRepository.getLogsForHabitInRange(
            habitId,
            startOfDay(),
            startOfDay() + 86_400_000L
        ).isNotEmpty()

        if (!alreadyDone) {
            NotificationHelper.showReminderNotification(applicationContext, habit.id, habit.title)
        }

        return Result.success()
    }

    private fun startOfDay(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

}
