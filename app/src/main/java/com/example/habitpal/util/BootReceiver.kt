package com.example.habitpal.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habitpal.data.local.dao.HabitDao
import com.example.habitpal.data.mapper.toDomain
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitDao: HabitDao

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Re-schedule all active habits that have a reminder set
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = habitDao.getHabitsWithReminders()
                habits.forEach { entity ->
                    reminderScheduler.schedule(entity.toDomain())
                }
            } finally {
                pending.finish()
            }
        }
    }
}
