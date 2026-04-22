package com.example.habitpal.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.habitpal.domain.usecase.habit.ToggleHabitCompletionUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WidgetActionCallback : ActionCallback {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun toggleHabitUseCase(): ToggleHabitCompletionUseCase
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val habitId = parameters[HabitWidget.habitIdKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)

        withContext(Dispatchers.IO) {
            entryPoint.toggleHabitUseCase().invoke(habitId)
        }

        HabitWidget().updateAll(context)
    }
}


