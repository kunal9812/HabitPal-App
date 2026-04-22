package com.example.habitpal.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra(EXTRA_HABIT_ID, -1)
        val habitTitle = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: return
        if (habitId == -1) return

        NotificationHelper.showReminderNotification(context, habitId, habitTitle)
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title"
    }
}
