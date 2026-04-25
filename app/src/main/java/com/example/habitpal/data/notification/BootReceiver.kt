package com.example.habitpal.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.habitpal.domain.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var habitRepository: HabitRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reminderScheduler.rescheduleAll(habitRepository.getActiveHabits().first())
            } finally {
                pendingResult.finish()
            }
        }
    }
}