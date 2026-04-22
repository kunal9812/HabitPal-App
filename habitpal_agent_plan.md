# HabitPal App — Tier 1 & Tier 2 Implementation Plan
> Machine-readable execution plan for an AI coding agent.
> Execute phases and tasks in the exact order listed. Do not reorder.
> Each task specifies: what to create/modify, exact code to write, and acceptance criteria.

---

## CONTEXT

**Project:** HabitPal Android App
**Repo:** https://github.com/kunal9812/HabitPal-App
**Stack:** Kotlin, MVVM + Clean Architecture, Room + KSP, Hilt DI, Coroutines + Flow, DataStore, SafeArgs, ViewBinding, MPAndroidChart
**Package:** `com.example.habitpal`
**Existing layers:** `data` (Room entities, DAOs, repository impl) → `domain` (models, use cases, repo interfaces) → `presentation` (Fragments, ViewModels)

---

## CONSTRAINTS

- Never skip a migration. All schema changes go into a single `MIGRATION_1_2` block.
- Never change the existing package structure. Add new files inside existing layer folders.
- All new use cases must be `suspend fun` or return `Flow`. No blocking calls on main thread.
- All new ViewModels must expose `StateFlow`, not `LiveData`.
- All string literals go in `res/values/strings.xml`.
- All colors go in `res/values/colors.xml`.
- Run the app and verify it compiles after each phase before proceeding.

---

## PHASE 1 — Foundation (Schema & Domain Models)
> Complete this phase entirely before touching any UI or notification code.

---

### TASK 1.1 — Add new dependencies

**File:** `app/build.gradle.kts`

Add the following inside the `dependencies { }` block:

```kotlin
// WorkManager (notifications)
implementation("androidx.work:work-runtime-ktx:2.9.0")
implementation("androidx.hilt:hilt-work:1.2.0")
ksp("androidx.hilt:hilt-compiler:1.2.0")

// Calendar heatmap
implementation("com.kizitonwose.calendar:view:2.5.0")

// Glance widget
implementation("androidx.glance:glance-appwidget:1.1.0")
implementation("androidx.glance:glance-material3:1.1.0")
```

**Acceptance:** Project syncs without error.

---

### TASK 1.2 — Create HabitFrequency domain model

**File:** `domain/model/HabitFrequency.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.domain.model

import java.time.DayOfWeek

sealed class HabitFrequency {
    object Daily : HabitFrequency()
    data class WeeklyDays(val days: Set<DayOfWeek>) : HabitFrequency()
    data class TimesPerWeek(val count: Int) : HabitFrequency()
}
```

---

### TASK 1.3 — Create TimeOfDay domain model

**File:** `domain/model/TimeOfDay.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.domain.model

enum class TimeOfDay {
    MORNING, AFTERNOON, EVENING, CUSTOM
}
```

---

### TASK 1.4 — Create Category domain model

**File:** `domain/model/Category.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.domain.model

data class Category(
    val id: Int,
    val name: String,
    val colorHex: String,
    val iconName: String
)
```

---

### TASK 1.5 — Create FrequencyConverter for Room

**File:** `data/local/converter/FrequencyConverter.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.data.local.converter

import androidx.room.TypeConverter
import com.example.habitpal.domain.model.HabitFrequency
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.DayOfWeek

class FrequencyConverter {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @TypeConverter
    fun fromFrequency(frequency: HabitFrequency): String {
        return when (frequency) {
            is HabitFrequency.Daily -> "daily"
            is HabitFrequency.WeeklyDays -> "weekly:${frequency.days.joinToString(",") { it.name }}"
            is HabitFrequency.TimesPerWeek -> "times:${frequency.count}"
        }
    }

    @TypeConverter
    fun toFrequency(value: String): HabitFrequency {
        return when {
            value == "daily" -> HabitFrequency.Daily
            value.startsWith("weekly:") -> {
                val days = value.removePrefix("weekly:").split(",")
                    .map { DayOfWeek.valueOf(it) }.toSet()
                HabitFrequency.WeeklyDays(days)
            }
            value.startsWith("times:") -> {
                HabitFrequency.TimesPerWeek(value.removePrefix("times:").toInt())
            }
            else -> HabitFrequency.Daily
        }
    }
}
```

---

### TASK 1.6 — Create CategoryEntity

**File:** `data/local/entity/CategoryEntity.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String,
    val iconName: String
)
```

---

### TASK 1.7 — Modify HabitEntity

**File:** `data/local/entity/HabitEntity.kt` (MODIFY)

Add the following fields to the existing `HabitEntity` data class:

```kotlin
// Scheduling
val frequencyJson: String = "daily",
val reminderHour: Int? = null,
val reminderMinute: Int? = null,
val timeOfDay: String? = null,

// Organisation
val categoryId: Int? = null,
val sortOrder: Int = 0,
val isArchived: Boolean = false,
```

---

### TASK 1.8 — Modify CompletionEntity

**File:** `data/local/entity/CompletionEntity.kt` (MODIFY)

Add the following nullable field to the existing `CompletionEntity` data class:

```kotlin
val note: String? = null,
```

---

### TASK 1.9 — Create CategoryDao

**File:** `data/local/dao/CategoryDao.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.data.local.dao

import androidx.room.*
import com.example.habitpal.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
```

---

### TASK 1.10 — Modify HabitDao

**File:** `data/local/dao/HabitDao.kt` (MODIFY)

Add the following queries to the existing `HabitDao` interface:

```kotlin
@Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY sortOrder ASC")
fun getActiveHabits(): Flow<List<HabitEntity>>

@Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY name ASC")
fun getArchivedHabits(): Flow<List<HabitEntity>>

@Query("UPDATE habits SET isArchived = 1 WHERE id = :habitId")
suspend fun archiveHabit(habitId: Long)

@Query("UPDATE habits SET isArchived = 0 WHERE id = :habitId")
suspend fun restoreHabit(habitId: Long)

@Query("UPDATE habits SET sortOrder = :sortOrder WHERE id = :habitId")
suspend fun updateSortOrder(habitId: Long, sortOrder: Int)

@Transaction
suspend fun updateSortOrders(updates: List<Pair<Long, Int>>) {
    updates.forEach { (id, order) -> updateSortOrder(id, order) }
}
```

Also update the existing `getAll()` query (if present) to filter `WHERE isArchived = 0`.

---

### TASK 1.11 — Modify HabitCompletionDao

**File:** `data/local/dao/HabitCompletionDao.kt` (MODIFY)

Add the following query:

```kotlin
@Query("""
    SELECT * FROM habit_completions 
    WHERE habitId = :habitId 
    AND completionDate BETWEEN :startDate AND :endDate
    ORDER BY completionDate DESC
""")
fun getCompletionsInRange(
    habitId: Long,
    startDate: String,
    endDate: String
): Flow<List<CompletionEntity>>
```

---

### TASK 1.12 — Write Room migration and update HabitDatabase

**File:** `data/local/HabitDatabase.kt` (MODIFY)

1. Bump `version` from current to `2`:
   ```kotlin
   @Database(
       entities = [HabitEntity::class, CompletionEntity::class, CategoryEntity::class],
       version = 2,
       exportSchema = true
   )
   ```

2. Add `FrequencyConverter` to `@TypeConverters`:
   ```kotlin
   @TypeConverters(FrequencyConverter::class)
   ```

3. Add `CategoryDao` abstract function:
   ```kotlin
   abstract fun categoryDao(): CategoryDao
   ```

4. Add the migration object inside the companion object:
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(db: SupportSQLiteDatabase) {
           // HabitEntity new columns
           db.execSQL("ALTER TABLE habits ADD COLUMN frequencyJson TEXT NOT NULL DEFAULT 'daily'")
           db.execSQL("ALTER TABLE habits ADD COLUMN reminderHour INTEGER")
           db.execSQL("ALTER TABLE habits ADD COLUMN reminderMinute INTEGER")
           db.execSQL("ALTER TABLE habits ADD COLUMN timeOfDay TEXT")
           db.execSQL("ALTER TABLE habits ADD COLUMN categoryId INTEGER REFERENCES categories(id)")
           db.execSQL("ALTER TABLE habits ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
           db.execSQL("ALTER TABLE habits ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")

           // CompletionEntity new column
           db.execSQL("ALTER TABLE habit_completions ADD COLUMN note TEXT")

           // New categories table
           db.execSQL("""
               CREATE TABLE IF NOT EXISTS categories (
                   id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                   name TEXT NOT NULL,
                   colorHex TEXT NOT NULL,
                   iconName TEXT NOT NULL
               )
           """)
       }
   }
   ```

5. Pass `MIGRATION_1_2` into the Room builder:
   ```kotlin
   .addMigrations(MIGRATION_1_2)
   ```

**Acceptance:** App launches without `IllegalStateException: Room cannot verify the data integrity` crash.

---

### TASK 1.13 — Create CheckHabitDueUseCase

**File:** `domain/usecase/CheckHabitDueUseCase.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.domain.usecase

import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.domain.repository.HabitRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

class CheckHabitDueUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    suspend fun isDueToday(habit: Habit): Boolean {
        val today = LocalDate.now()
        return when (val f = habit.frequency) {
            is HabitFrequency.Daily -> true
            is HabitFrequency.WeeklyDays -> DayOfWeek.from(today) in f.days
            is HabitFrequency.TimesPerWeek -> {
                val weekStart = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                val completionsThisWeek = repository.getCompletionsInRange(
                    habit.id,
                    weekStart.toString(),
                    today.toString()
                )
                completionsThisWeek.size < f.count
            }
        }
    }
}
```

---

### TASK 1.14 — Create GetHabitStatsUseCase

**File:** `domain/usecase/GetHabitStatsUseCase.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.domain.usecase

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HabitStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val allTimeRate: Float
)

class GetHabitStatsUseCase @Inject constructor() {
    fun calculate(completionDates: List<LocalDate>, createdDate: LocalDate): HabitStats {
        if (completionDates.isEmpty()) return HabitStats(0, 0, 0, 0f)

        val sorted = completionDates.sortedDescending()
        var currentStreak = 0
        var bestStreak = 0
        var streak = 0
        var prev: LocalDate? = null

        for (date in sorted) {
            streak = when {
                prev == null -> 1
                prev == date.plusDays(1) -> streak + 1
                else -> 1
            }
            if (currentStreak == 0) currentStreak = streak
            if (streak > bestStreak) bestStreak = streak
            prev = date
        }

        val daysSinceCreation = ChronoUnit.DAYS.between(createdDate, LocalDate.now()).toInt() + 1
        val allTimeRate = sorted.size.toFloat() / daysSinceCreation.coerceAtLeast(1)

        return HabitStats(currentStreak, bestStreak, sorted.size, allTimeRate)
    }
}
```

---

### TASK 1.15 — Create GetCompletionHistoryUseCase

**File:** `domain/usecase/GetCompletionHistoryUseCase.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.domain.usecase

import com.example.habitpal.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetCompletionHistoryUseCase @Inject constructor(
    private val repository: HabitRepository
) {
    fun execute(habitId: Long, monthsBack: Int = 6): Flow<Map<LocalDate, Boolean>> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusMonths(monthsBack.toLong())
        return repository.getCompletionsInRange(habitId, startDate.toString(), endDate.toString())
            .map { completions ->
                completions.associate { it.completionDate to true }
            }
    }
}
```

---

### TASK 1.16 — Seed default categories on first launch

**File:** `domain/usecase/SeedCategoriesUseCase.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.domain.usecase

import com.example.habitpal.domain.model.Category
import com.example.habitpal.domain.repository.CategoryRepository
import javax.inject.Inject

class SeedCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository
) {
    suspend fun seedIfEmpty() {
        if (repository.count() > 0) return
        val defaults = listOf(
            Category(0, "Health",      "#4CAF50", "ic_health"),
            Category(0, "Work",        "#2196F3", "ic_work"),
            Category(0, "Learning",    "#9C27B0", "ic_learning"),
            Category(0, "Mindfulness", "#00BCD4", "ic_mindfulness"),
            Category(0, "Finance",     "#FF9800", "ic_finance"),
            Category(0, "Personal",    "#F44336", "ic_personal")
        )
        repository.insertAll(defaults)
    }
}
```

**Acceptance:** All domain model files compile. No UI changes yet. Run unit tests if any exist.

---

## PHASE 2 — Tier 1 Core Features

---

### TASK 2.1 — Notifications: AndroidManifest.xml

**File:** `app/src/main/AndroidManifest.xml` (MODIFY)

Add inside `<manifest>` before `<application>`:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```

Add inside `<application>`:
```xml
<receiver
    android:name=".data.notification.BootReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
    </intent-filter>
</receiver>
```

---

### TASK 2.2 — Create NotificationHelper

**File:** `data/notification/NotificationHelper.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.habitpal.R
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "habit_reminders"
        const val CHANNEL_NAME = "Habit Reminders"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for your habits"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun sendReminder(habit: Habit) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("habitId", habit.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, habit.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for: ${habit.name}")
            .setContentText("Keep your streak going!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(habit.id.toInt(), notification)
    }
}
```

> Note: Create `res/drawable/ic_notification.xml` as a simple bell vector icon if it doesn't exist.

---

### TASK 2.3 — Create HabitReminderWorker

**File:** `data/notification/HabitReminderWorker.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.data.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.habitpal.domain.usecase.GetHabitByIdUseCase
import com.example.habitpal.domain.usecase.CheckTodayCompletionUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getHabitByIdUseCase: GetHabitByIdUseCase,
    private val checkTodayCompletionUseCase: CheckTodayCompletionUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_HABIT_ID = "habit_id"
    }

    override suspend fun doWork(): Result {
        val habitId = inputData.getLong(KEY_HABIT_ID, -1L)
        if (habitId == -1L) return Result.failure()

        val habit = getHabitByIdUseCase(habitId) ?: return Result.failure()
        val alreadyDone = checkTodayCompletionUseCase(habitId)

        if (!alreadyDone) {
            notificationHelper.sendReminder(habit)
        }

        return Result.success()
    }
}
```

---

### TASK 2.4 — Create ReminderScheduler

**File:** `data/notification/ReminderScheduler.kt` (CREATE NEW)

```kotlin
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
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun scheduleReminder(habit: Habit) {
        val hour = habit.reminderHour ?: return
        val minute = habit.reminderMinute ?: 0

        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(hour, minute)
        if (target.isBefore(now)) target = target.plusDays(1)

        val delayMs = Duration.between(now, target).toMillis()

        val request = PeriodicWorkRequestBuilder<HabitReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(HabitReminderWorker.KEY_HABIT_ID to habit.id))
            .addTag("reminder_${habit.id}")
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "reminder_${habit.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelReminder(habitId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("reminder_$habitId")
    }

    fun rescheduleAll(habits: List<Habit>) {
        habits.forEach { scheduleReminder(it) }
    }
}
```

---

### TASK 2.5 — Create BootReceiver

**File:** `data/notification/BootReceiver.kt` (CREATE NEW)

```kotlin
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
        CoroutineScope(Dispatchers.IO).launch {
            val habits = habitRepository.getActiveHabits().first()
            reminderScheduler.rescheduleAll(habits)
        }
    }
}
```

---

### TASK 2.6 — Create Hilt NotificationModule

**File:** `di/NotificationModule.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.di

import android.content.Context
import androidx.work.WorkManager
import com.example.habitpal.data.notification.NotificationHelper
import com.example.habitpal.data.notification.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper =
        NotificationHelper(context)

    @Provides
    @Singleton
    fun provideReminderScheduler(@ApplicationContext context: Context): ReminderScheduler =
        ReminderScheduler(context)
}
```

---

### TASK 2.7 — Update Application class for HiltWorkerFactory

**File:** `HabitPalApplication.kt` (MODIFY)

Ensure the Application class implements `Configuration.Provider` for Hilt + WorkManager integration:

```kotlin
@HiltAndroidApp
class HabitPalApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

Also add to `AndroidManifest.xml` inside `<application>`:
```xml
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    android:exported="false"
    tools:node="merge">
    <meta-data
        android:name="androidx.work.WorkManagerInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>
```

---

### TASK 2.8 — Update AddEditHabit UI for scheduling + reminder

**File:** `res/layout/fragment_add_edit_habit.xml` (MODIFY)

Add the following UI elements after the existing habit name and description fields:

```xml
<!-- Frequency picker -->
<TextView
    android:id="@+id/tvFrequencyLabel"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/frequency"
    android:textAppearance="?attr/textAppearanceBodyMedium"/>

<com.google.android.material.chip.ChipGroup
    android:id="@+id/chipGroupFrequency"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:singleSelection="true">

    <com.google.android.material.chip.Chip
        android:id="@+id/chipDaily"
        style="@style/Widget.Material3.Chip.Filter"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/daily"
        android:checked="true"/>

    <com.google.android.material.chip.Chip
        android:id="@+id/chipSpecificDays"
        style="@style/Widget.Material3.Chip.Filter"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/specific_days"/>

    <com.google.android.material.chip.Chip
        android:id="@+id/chipTimesPerWeek"
        style="@style/Widget.Material3.Chip.Filter"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/times_per_week"/>
</com.google.android.material.chip.ChipGroup>

<!-- Day-of-week selector (shown only when chipSpecificDays is selected) -->
<com.google.android.material.chip.ChipGroup
    android:id="@+id/chipGroupDays"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:visibility="gone"
    app:selectionRequired="false">
    <!-- Mon, Tue, Wed, Thu, Fri, Sat, Sun chips here -->
</com.google.android.material.chip.ChipGroup>

<!-- Reminder time picker -->
<TextView
    android:id="@+id/tvReminderLabel"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/reminder_time"/>

<com.google.android.material.switchmaterial.SwitchMaterial
    android:id="@+id/switchReminder"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/enable_reminder"/>

<Button
    android:id="@+id/btnPickTime"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/set_time"
    android:visibility="gone"/>
```

---

### TASK 2.9 — Update AddEditHabitFragment logic

**File:** `presentation/addedithabit/AddEditHabitFragment.kt` (MODIFY)

1. Add frequency chip group listener — show/hide day selector based on selection.
2. Add time picker button logic:
```kotlin
btnPickTime.setOnClickListener {
    val picker = MaterialTimePicker.Builder()
        .setTimeFormat(TimeFormat.CLOCK_12H)
        .setHour(12).setMinute(0)
        .setTitleText(getString(R.string.select_reminder_time))
        .build()
    picker.addOnPositiveButtonClickListener {
        viewModel.setReminder(picker.hour, picker.minute)
        btnPickTime.text = String.format("%02d:%02d", picker.hour, picker.minute)
    }
    picker.show(parentFragmentManager, "time_picker")
}
```
3. Add runtime permission request for Android 13+:
```kotlin
private val notifPermLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) viewModel.saveHabit()
    else Snackbar.make(requireView(), R.string.notification_permission_denied, Snackbar.LENGTH_LONG).show()
}

private fun requestNotifPermissionThenSave() {
    if (Build.VERSION.SDK_INT >= 33) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
    }
    viewModel.saveHabit()
}
```
4. Replace existing save button click with call to `requestNotifPermissionThenSave()`.

---

### TASK 2.10 — Update AddEditHabitViewModel

**File:** `presentation/addedithabit/AddEditHabitViewModel.kt` (MODIFY)

Add the following:

```kotlin
@Inject lateinit var reminderScheduler: ReminderScheduler

private var reminderHour: Int? = null
private var reminderMinute: Int? = null
private var selectedFrequency: HabitFrequency = HabitFrequency.Daily

fun setReminder(hour: Int, minute: Int) {
    reminderHour = hour
    reminderMinute = minute
}

fun setFrequency(frequency: HabitFrequency) {
    selectedFrequency = frequency
}

// In saveHabit(), add to the Habit object being saved:
// frequency = selectedFrequency,
// reminderHour = reminderHour,
// reminderMinute = reminderMinute,

// After saving, schedule the reminder:
// reminderScheduler.scheduleReminder(savedHabit)
```

---

### TASK 2.11 — Add heatmap to HabitDetail layout

**File:** `res/layout/fragment_habit_detail.xml` (MODIFY)

Add after the stats section, before or replacing the MPAndroidChart bar chart:

```xml
<!-- Stats row -->
<LinearLayout
    android:id="@+id/statsRow"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal">

    <include layout="@layout/view_stat_card"
        android:id="@+id/statCurrentStreak"/>
    <include layout="@layout/view_stat_card"
        android:id="@+id/statBestStreak"/>
    <include layout="@layout/view_stat_card"
        android:id="@+id/statTotal"/>
    <include layout="@layout/view_stat_card"
        android:id="@+id/statRate"/>
</LinearLayout>

<!-- Heatmap calendar -->
<com.kizitonwose.calendar.view.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/item_calendar_day"
    app:cv_monthHeaderResource="@layout/item_calendar_month_header"
    app:cv_orientation="horizontal"
    app:cv_scrollPaged="true"/>
```

**File:** `res/layout/view_stat_card.xml` (CREATE NEW)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="0dp" android:layout_height="wrap_content"
    android:layout_weight="1" android:orientation="vertical"
    android:gravity="center" android:padding="8dp">

    <TextView android:id="@+id/tvStatValue"
        android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:textSize="20sp" android:textStyle="bold"
        android:text="0"/>

    <TextView android:id="@+id/tvStatLabel"
        android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:textSize="11sp" android:text="label"/>
</LinearLayout>
```

**File:** `res/layout/item_calendar_day.xml` (CREATE NEW)

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/dayContainer"
    android:layout_width="0dp" android:layout_height="0dp">
    <TextView
        android:id="@+id/tvDay"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:textSize="12sp"/>
</FrameLayout>
```

---

### TASK 2.12 — Update HabitDetailViewModel for stats + heatmap

**File:** `presentation/habitdetail/HabitDetailViewModel.kt` (MODIFY)

Add the following:

```kotlin
@Inject lateinit var getCompletionHistoryUseCase: GetCompletionHistoryUseCase
@Inject lateinit var getHabitStatsUseCase: GetHabitStatsUseCase

private val _completionMap = MutableStateFlow<Map<LocalDate, Boolean>>(emptyMap())
val completionMap: StateFlow<Map<LocalDate, Boolean>> = _completionMap.asStateFlow()

private val _stats = MutableStateFlow<HabitStats?>(null)
val stats: StateFlow<HabitStats?> = _stats.asStateFlow()

fun loadStats(habitId: Long, createdDate: LocalDate) {
    viewModelScope.launch {
        getCompletionHistoryUseCase.execute(habitId).collect { map ->
            _completionMap.value = map
            _stats.value = getHabitStatsUseCase.calculate(
                map.keys.toList(), createdDate
            )
        }
    }
}
```

---

### TASK 2.13 — Update HabitDetailFragment for stats + heatmap

**File:** `presentation/habitdetail/HabitDetailFragment.kt` (MODIFY)

1. Call `viewModel.loadStats(habitId, habit.createdDate)` in `onViewCreated`.
2. Collect `stats` StateFlow and bind to stat cards:
```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
            viewModel.stats.collectLatest { stats ->
                stats ?: return@collectLatest
                binding.statCurrentStreak.tvStatValue.text = stats.currentStreak.toString()
                binding.statCurrentStreak.tvStatLabel.text = "Current"
                binding.statBestStreak.tvStatValue.text = stats.bestStreak.toString()
                binding.statBestStreak.tvStatLabel.text = "Best"
                binding.statTotal.tvStatValue.text = stats.totalCompletions.toString()
                binding.statTotal.tvStatLabel.text = "Total"
                binding.statRate.tvStatValue.text = "${(stats.allTimeRate * 100).toInt()}%"
                binding.statRate.tvStatLabel.text = "Rate"
            }
        }
        launch {
            viewModel.completionMap.collectLatest { map ->
                setupCalendar(map)
            }
        }
    }
}
```
3. Add `setupCalendar` function:
```kotlin
private fun setupCalendar(completionMap: Map<LocalDate, Boolean>) {
    val currentMonth = YearMonth.now()
    val startMonth = currentMonth.minusMonths(6)
    val daysOfWeek = daysOfWeek(firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek)

    binding.calendarView.setup(startMonth, currentMonth, daysOfWeek.first())
    binding.calendarView.scrollToMonth(currentMonth)

    binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
        override fun create(view: View) = DayViewContainer(view)
        override fun bind(container: DayViewContainer, data: CalendarDay) {
            container.tvDay.text = data.date.dayOfMonth.toString()
            val completed = completionMap[data.date] == true
            val isToday = data.date == LocalDate.now()
            container.dayContainer.setBackgroundResource(
                when {
                    completed && isToday -> R.drawable.bg_day_done_today
                    completed -> R.drawable.bg_day_done
                    isToday -> R.drawable.bg_day_today
                    data.position != DayPosition.MonthDate -> 0
                    else -> R.drawable.bg_day_empty
                }
            )
        }
    }
}
```

---

### TASK 2.14 — Create calendar day drawables

**File:** `res/drawable/bg_day_done.xml` (CREATE NEW)
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <solid android:color="@color/color_habit_done"/>
</shape>
```

**File:** `res/drawable/bg_day_today.xml` (CREATE NEW)
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <stroke android:width="2dp" android:color="@color/color_primary"/>
</shape>
```

**File:** `res/drawable/bg_day_done_today.xml` (CREATE NEW)
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <solid android:color="@color/color_habit_done"/>
    <stroke android:width="2dp" android:color="@color/color_primary_dark"/>
</shape>
```

**File:** `res/drawable/bg_day_empty.xml` (CREATE NEW)
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="oval">
    <solid android:color="@color/color_day_empty"/>
</shape>
```

Add to `res/values/colors.xml`:
```xml
<color name="color_habit_done">#4CAF50</color>
<color name="color_day_empty">#E0E0E0</color>
```

---

### TASK 2.15 — Create onboarding HabitTemplate model

**File:** `domain/model/HabitTemplate.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.domain.model

data class HabitTemplate(
    val name: String,
    val emoji: String,
    val categoryName: String,
    val defaultFrequency: HabitFrequency = HabitFrequency.Daily
) {
    companion object {
        val defaults = listOf(
            HabitTemplate("Drink water", "💧", "Health"),
            HabitTemplate("Morning run", "🏃", "Health"),
            HabitTemplate("Workout", "💪", "Health"),
            HabitTemplate("Sleep 8 hours", "😴", "Health"),
            HabitTemplate("Take vitamins", "💊", "Health"),
            HabitTemplate("Read 10 pages", "📖", "Learning"),
            HabitTemplate("Study language", "🗣️", "Learning"),
            HabitTemplate("Watch lecture", "🎓", "Learning"),
            HabitTemplate("Practice coding", "💻", "Learning"),
            HabitTemplate("Meditate", "🧘", "Mindfulness"),
            HabitTemplate("Gratitude journal", "📝", "Mindfulness"),
            HabitTemplate("Deep breathing", "🌬️", "Mindfulness"),
            HabitTemplate("No social media", "📵", "Mindfulness"),
            HabitTemplate("Daily planning", "📅", "Work"),
            HabitTemplate("Email inbox zero", "📧", "Work"),
            HabitTemplate("Focus session", "🎯", "Work"),
            HabitTemplate("Track spending", "💰", "Finance"),
            HabitTemplate("Save daily", "🏦", "Finance"),
            HabitTemplate("Call a friend", "📱", "Personal"),
            HabitTemplate("Journaling", "📓", "Personal")
        )
    }
}
```

---

### TASK 2.16 — Create OnboardingFragment

**File:** `presentation/onboarding/OnboardingFragment.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habitpal.R
import com.example.habitpal.databinding.FragmentOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HabitTemplateAdapter { template ->
            viewModel.toggleTemplate(template)
        }
        binding.rvTemplates.adapter = adapter
        adapter.submitList(viewModel.templates)

        binding.btnGetStarted.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.finishOnboarding()
                findNavController().navigate(R.id.action_onboarding_to_home)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

---

### TASK 2.17 — Create OnboardingViewModel

**File:** `presentation/onboarding/OnboardingViewModel.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.presentation.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.domain.model.HabitTemplate
import com.example.habitpal.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    val templates = HabitTemplate.defaults
    private val selectedTemplates = mutableSetOf<HabitTemplate>()

    fun toggleTemplate(template: HabitTemplate) {
        if (template in selectedTemplates) selectedTemplates.remove(template)
        else selectedTemplates.add(template)
    }

    fun isSelected(template: HabitTemplate) = template in selectedTemplates

    suspend fun finishOnboarding() {
        selectedTemplates.forEach { template ->
            val habit = Habit(
                name = template.name,
                frequency = template.defaultFrequency,
                createdDate = LocalDate.now()
            )
            habitRepository.insertHabit(habit)
        }
        dataStore.edit { it[ONBOARDING_COMPLETE] = true }
    }
}
```

---

### TASK 2.18 — Create HabitTemplateAdapter

**File:** `presentation/onboarding/HabitTemplateAdapter.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.presentation.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpal.databinding.ItemHabitTemplateBinding
import com.example.habitpal.domain.model.HabitTemplate

class HabitTemplateAdapter(
    private val onToggle: (HabitTemplate) -> Unit
) : ListAdapter<HabitTemplate, HabitTemplateAdapter.ViewHolder>(DiffCallback()) {

    private val selected = mutableSetOf<Int>()

    inner class ViewHolder(private val binding: ItemHabitTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(template: HabitTemplate, isSelected: Boolean) {
            binding.tvEmoji.text = template.emoji
            binding.tvName.text = template.name
            binding.tvCategory.text = template.categoryName
            binding.root.isSelected = isSelected
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos in selected) selected.remove(pos) else selected.add(pos)
                notifyItemChanged(pos)
                onToggle(template)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemHabitTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position in selected)

    class DiffCallback : DiffUtil.ItemCallback<HabitTemplate>() {
        override fun areItemsTheSame(a: HabitTemplate, b: HabitTemplate) = a.name == b.name
        override fun areContentsTheSame(a: HabitTemplate, b: HabitTemplate) = a == b
    }
}
```

---

### TASK 2.19 — Gate onboarding in MainActivity

**File:** `presentation/MainActivity.kt` (MODIFY)

```kotlin
private fun checkOnboarding() {
    lifecycleScope.launch {
        val complete = dataStore.data.first()[OnboardingViewModel.ONBOARDING_COMPLETE] ?: false
        if (!complete) {
            navController.navigate(R.id.onboardingFragment)
        }
    }
}
```

Call `checkOnboarding()` inside `onCreate()` after the NavController is set up.

---

### TASK 2.20 — Add onboarding destination to nav graph

**File:** `res/navigation/nav_graph.xml` (MODIFY)

```xml
<fragment
    android:id="@+id/onboardingFragment"
    android:name="com.example.habitpal.presentation.onboarding.OnboardingFragment"
    android:label="Get Started"
    tools:layout="@layout/fragment_onboarding">
    <action
        android:id="@+id/action_onboarding_to_home"
        app:destination="@id/homeFragment"/>
</fragment>
```

**Acceptance:** New users see the onboarding screen on first launch. Existing users (with DataStore flag set) go directly to home.

---

## PHASE 3 — Tier 2 Depth Features

---

### TASK 3.1 — Home screen widget: AppWidget info XML

**File:** `res/xml/habit_widget_info.xml` (CREATE NEW)

```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:targetCellWidth="2"
    android:targetCellHeight="2"
    android:updatePeriodMillis="1800000"
    android:description="@string/widget_description"
    android:previewImage="@drawable/widget_preview"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"/>
```

---

### TASK 3.2 — Create HabitWidget (Glance)

**File:** `presentation/widget/HabitWidget.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.presentation.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionRunCallback
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class HabitWidget : GlanceAppWidget() {

    companion object {
        val habitIdKey = ActionParameters.Key<Long>("habit_id")
    }

    override suspend fun provideGlance(context: android.content.Context, id: GlanceId) {
        val habits = loadTodayHabits(context) // fetch from Room directly
        provideContent {
            HabitWidgetContent(habits)
        }
    }

    @Composable
    fun HabitWidgetContent(habits: List<WidgetHabitItem>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(androidx.glance.color.ColorProvider(android.graphics.Color.WHITE))
        ) {
            Text(
                text = "Today's Habits",
                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(GlanceModifier.height(8.dp))
            habits.take(4).forEach { habit ->
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CheckBox(
                        checked = habit.completedToday,
                        onCheckedChange = actionRunCallback<WidgetActionCallback>(
                            actionParametersOf(habitIdKey to habit.id)
                        )
                    )
                    Text(
                        text = habit.name,
                        modifier = GlanceModifier.padding(start = 6.dp),
                        style = TextStyle(fontSize = 13.sp)
                    )
                }
            }
        }
    }

    private suspend fun loadTodayHabits(context: android.content.Context): List<WidgetHabitItem> {
        // Use EntryPoints to get repository from Hilt graph
        // Return list of WidgetHabitItem(id, name, completedToday)
        return emptyList() // implement with Hilt EntryPoint
    }
}

data class WidgetHabitItem(val id: Long, val name: String, val completedToday: Boolean)
```

---

### TASK 3.3 — Create WidgetActionCallback

**File:** `presentation/widget/WidgetActionCallback.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.presentation.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.example.habitpal.domain.usecase.ToggleHabitCompletionUseCase
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
            entryPoint.toggleHabitUseCase().invoke(habitId, null)
        }
        HabitWidget().updateAll(context)
    }
}
```

---

### TASK 3.4 — Create HabitWidgetReceiver

**File:** `presentation/widget/HabitWidgetReceiver.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.presentation.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class HabitWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HabitWidget()
}
```

Add to `AndroidManifest.xml` inside `<application>`:
```xml
<receiver
    android:name=".presentation.widget.HabitWidgetReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE"/>
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/habit_widget_info"/>
</receiver>
```

---

### TASK 3.5 — Habit journal: modify ToggleHabitCompletionUseCase

**File:** `domain/usecase/ToggleHabitCompletionUseCase.kt` (MODIFY)

Update the function signature to accept an optional note:

```kotlin
suspend fun invoke(habitId: Long, note: String? = null) {
    val today = LocalDate.now().toString()
    val existing = repository.getCompletionForDate(habitId, today)
    if (existing != null) {
        repository.deleteCompletion(existing)
    } else {
        repository.insertCompletion(
            CompletionEntity(habitId = habitId, completionDate = today, note = note)
        )
    }
}
```

---

### TASK 3.6 — Create CompletionNoteBottomSheet

**File:** `presentation/home/CompletionNoteBottomSheet.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.habitpal.databinding.BottomSheetCompletionNoteBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CompletionNoteBottomSheet(
    private val onSave: (String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCompletionNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetCompletionNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSaveNote.setOnClickListener {
            onSave(binding.etNote.text?.toString()?.takeIf { it.isNotBlank() })
            dismiss()
        }
        binding.btnSkip.setOnClickListener {
            onSave(null)
            dismiss()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
```

In `HomeFragment`, after marking a habit complete, show this sheet:
```kotlin
CompletionNoteBottomSheet { note ->
    viewModel.completeHabit(habitId, note)
}.show(childFragmentManager, "note_sheet")
```

---

### TASK 3.7 — Habit reordering: add sortOrder migration

Already included in TASK 1.12 (`MIGRATION_1_2`). No additional migration needed.

---

### TASK 3.8 — Create HabitItemTouchHelper

**File:** `presentation/home/HabitItemTouchHelper.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.presentation.home

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

interface HabitDragListener {
    fun onItemMoved(fromPosition: Int, toPosition: Int)
    fun onDropCompleted()
}

class HabitItemTouchHelper(private val listener: HabitDragListener) :
    ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) = makeMovementFlags(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    )

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        listener.onItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        listener.onDropCompleted()
    }

    override fun isLongPressDragEnabled() = false // use drag handle instead
}
```

---

### TASK 3.9 — Update HabitAdapter for drag handle + reordering

**File:** `presentation/home/HabitAdapter.kt` (MODIFY)

1. Add `HabitDragListener` implementation to the adapter class.
2. Add a drag handle view (`@id/dragHandle`) to `item_habit.xml`.
3. In `onBindViewHolder`, set:
```kotlin
binding.dragHandle.setOnTouchListener { _, event ->
    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
        startDragCallback?.invoke(holder)
    }
    false
}
```
4. Add `onItemMoved` to swap items in the list locally for smooth animation.
5. Add `onDropCompleted` to notify the ViewModel to persist the new order.

---

### TASK 3.10 — Update HomeViewModel for reordering

**File:** `presentation/home/HomeViewModel.kt` (MODIFY)

```kotlin
fun reorderHabits(fromPos: Int, toPos: Int, currentList: List<Habit>) {
    viewModelScope.launch(Dispatchers.IO) {
        val reordered = currentList.toMutableList().apply { add(toPos, removeAt(fromPos)) }
        val updates = reordered.mapIndexed { index, habit -> Pair(habit.id, index) }
        habitRepository.updateSortOrders(updates)
    }
}
```

---

### TASK 3.11 — Archive feature: ArchiveHabitUseCase

**File:** `domain/usecase/ArchiveHabitUseCase.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.domain.usecase

import com.example.habitpal.data.notification.ReminderScheduler
import com.example.habitpal.domain.repository.HabitRepository
import javax.inject.Inject

class ArchiveHabitUseCase @Inject constructor(
    private val repository: HabitRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend fun archive(habitId: Long) {
        repository.archiveHabit(habitId)
        reminderScheduler.cancelReminder(habitId)
    }

    suspend fun restore(habitId: Long) {
        repository.restoreHabit(habitId)
    }
}
```

---

### TASK 3.12 — Create ArchivedHabitsFragment

**File:** `presentation/archived/ArchivedHabitsFragment.kt` (CREATE NEW)

```kotlin
package com.example.habitpal.presentation.archived

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.habitpal.databinding.FragmentArchivedBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArchivedHabitsFragment : Fragment() {

    private var _binding: FragmentArchivedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArchivedHabitsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentArchivedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ArchivedHabitAdapter { habitId -> viewModel.restoreHabit(habitId) }
        binding.rvArchived.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.archivedHabits.collect { adapter.submitList(it) }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
```

---

### TASK 3.13 — Update HabitDetailFragment: replace Delete with Archive

**File:** `presentation/habitdetail/HabitDetailFragment.kt` (MODIFY)

Replace the Delete menu item / button behavior:

```kotlin
// Primary action: Archive
binding.btnArchive.setOnClickListener {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.archive_habit)
        .setMessage(R.string.archive_habit_message)
        .setPositiveButton(R.string.archive) { _, _ -> viewModel.archiveHabit() }
        .setNegativeButton(R.string.cancel, null)
        .show()
}

// Secondary action: Permanent delete (keep as overflow menu item only)
```

---

### TASK 3.14 — Add category filter to HomeFragment

**File:** `presentation/home/HomeFragment.kt` (MODIFY)

1. Add a horizontal `RecyclerView` above the habits list for category chips.
2. Collect categories from `HomeViewModel.categories` and bind to `CategoryFilterAdapter`.
3. On chip tap, update `HomeViewModel.selectedCategory`.

**File:** `presentation/home/HomeViewModel.kt` (MODIFY)

```kotlin
private val _selectedCategory = MutableStateFlow<Category?>(null)
val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

val filteredHabits: StateFlow<List<Habit>> = combine(
    allHabits, _selectedCategory
) { habits, category ->
    if (category == null) habits
    else habits.filter { it.categoryId == category.id }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

fun selectCategory(category: Category?) {
    _selectedCategory.value = if (_selectedCategory.value == category) null else category
}
```

---

## FINAL CHECKS

After all tasks are complete, verify the following:

- [ ] App builds without warnings from KSP/Hilt
- [ ] Fresh install shows onboarding screen
- [ ] Second launch skips onboarding
- [ ] Creating a habit with "Specific Days" frequency only shows habit on selected days
- [ ] Setting a reminder schedules a WorkManager periodic task (verify in App Inspection → Background Task Inspector)
- [ ] Device reboot reschedules all reminders (test with BootReceiver)
- [ ] Habit detail screen shows current streak, best streak, total, rate
- [ ] Heatmap calendar shows green dots on completed days
- [ ] Marking a habit complete shows note bottom sheet
- [ ] Drag handle allows reordering; order persists after app restart
- [ ] Archiving a habit removes it from home list but not from DB
- [ ] Archived habits screen shows archived items with restore option
- [ ] Home screen widget appears in widget picker and shows today's habits
- [ ] All migrations pass without Room schema errors on upgrade from v1

