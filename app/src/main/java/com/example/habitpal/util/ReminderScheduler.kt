package com.example.habitpal.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.habitpal.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules (or re-schedules) a daily repeating alarm for the given habit.
     * [habit.reminderTime] must be in "HH:mm" format, e.g. "08:30".
     */
    fun schedule(habit: Habit) {
        val reminderTime = habit.reminderTime ?: return
        val (hour, minute) = parseTime(reminderTime) ?: return

        val intent = buildIntent(habit.id, habit.title)

        // Cancel any previous alarm for this habit before re-scheduling
        alarmManager.cancel(intent)

        val triggerAt = nextOccurrence(hour, minute)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    intent
                )
            } catch (e: SecurityException) {
                // Exact alarms not permitted; fall back to inexact
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    intent
                )
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    /**
     * Cancels any scheduled alarm for the given habitId.
     */
    fun cancel(habitId: Int, habitTitle: String = "") {
        val intent = buildIntent(habitId, habitTitle)
        alarmManager.cancel(intent)
    }

    // ---------- helpers ----------

    private fun buildIntent(habitId: Int, habitTitle: String): PendingIntent {
        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_HABIT_ID, habitId)
            putExtra(ReminderBroadcastReceiver.EXTRA_HABIT_TITLE, habitTitle)
        }
        return PendingIntent.getBroadcast(
            context,
            habitId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Returns epoch millis of the next occurrence of HH:mm from now. */
    private fun nextOccurrence(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // If the time has already passed today, schedule for tomorrow
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    /** Parses "HH:mm" into a (hour, minute) pair, or null if invalid. */
    private fun parseTime(time: String): Pair<Int, Int>? {
        return try {
            val parts = time.split(":")
            if (parts.size != 2) return null
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: NumberFormatException) {
            null
        }
    }
}
