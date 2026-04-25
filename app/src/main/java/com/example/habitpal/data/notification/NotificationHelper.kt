package com.example.habitpal.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.habitpal.MainActivity
import com.example.habitpal.R
import com.example.habitpal.domain.model.Habit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        const val CHANNEL_ID = "habit_reminders"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Habit Reminders", NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun sendReminder(habit: Habit) {
        val pi = PendingIntent.getActivity(
            context, habit.id,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("habitId", habit.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        context.getSystemService(NotificationManager::class.java).notify(
            habit.id,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("⏰ ${habit.title}")
                .setContentText("Don't break your streak!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi).setAutoCancel(true).build()
        )
    }
}