package com.example.habitpal.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.habitpal.R
import com.example.habitpal.domain.repository.HabitRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

class HabitWidget : GlanceAppWidget() {

    companion object {
        val habitIdKey = ActionParameters.Key<Int>("habit_id")
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val habits = loadTodayHabits(context)
        provideContent {
            HabitWidgetContent(
                title = context.getString(R.string.widget_today_habits),
                emptyText = context.getString(R.string.widget_no_habits),
                habits = habits
            )
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetRepoEntryPoint {
        fun habitRepository(): HabitRepository
    }

    private suspend fun loadTodayHabits(context: Context): List<WidgetHabitItem> {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetRepoEntryPoint::class.java)
        return entryPoint.habitRepository().getActiveHabits().first().map {
            WidgetHabitItem(
                id = it.id,
                title = it.title,
                completedToday = it.isCompletedToday
            )
        }
    }
}

@Composable
private fun HabitWidgetContent(
    title: String,
    emptyText: String,
    habits: List<WidgetHabitItem>
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1B2340)))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                color = ColorProvider(Color(0xFF4FC3F7)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        if (habits.isEmpty()) {
            Text(
                text = emptyText,
                style = TextStyle(
                    color = ColorProvider(Color(0xFFFFFFFF)),
                    fontSize = 12.sp
                )
            )
        } else {
            habits.take(4).forEach { habit ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(
                            actionRunCallback<WidgetActionCallback>(
                                parameters = actionParametersOf(HabitWidget.habitIdKey to habit.id)
                            )
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (habit.completedToday) "[x]" else "[ ]",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFFFFF)),
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = habit.title,
                        modifier = GlanceModifier.padding(start = 8.dp),
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFFFFF)),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

data class WidgetHabitItem(
    val id: Int,
    val title: String,
    val completedToday: Boolean
)

