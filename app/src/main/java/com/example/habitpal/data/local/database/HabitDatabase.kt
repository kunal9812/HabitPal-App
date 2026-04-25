package com.example.habitpal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habitpal.data.local.dao.CategoryDao
import com.example.habitpal.data.local.dao.HabitCompletionDao
import com.example.habitpal.data.local.dao.HabitDao
import com.example.habitpal.data.local.dao.HabitLogDao
import com.example.habitpal.data.local.converter.FrequencyConverter
import com.example.habitpal.data.local.entity.CategoryEntity
import com.example.habitpal.data.local.entity.CompletionEntity
import com.example.habitpal.data.local.entity.HabitEntity
import com.example.habitpal.data.local.entity.HabitLogEntity

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class, CompletionEntity::class, CategoryEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(FrequencyConverter::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val cursor = db.query("PRAGMA table_info(habits)")
                val columns = mutableSetOf<String>()
                while (cursor.moveToNext()) {
                    columns.add(cursor.getString(1))
                }
                cursor.close()

                if (!columns.contains("frequencyJson")) {
                    db.execSQL("ALTER TABLE habits ADD COLUMN frequencyJson TEXT NOT NULL DEFAULT 'DAILY'")
                }
                if (!columns.contains("reminderHour")) {
                    db.execSQL("ALTER TABLE habits ADD COLUMN reminderHour INTEGER")
                }
                if (!columns.contains("reminderMinute")) {
                    db.execSQL("ALTER TABLE habits ADD COLUMN reminderMinute INTEGER")
                }
                if (!columns.contains("timeOfDay")) {
                    db.execSQL("ALTER TABLE habits ADD COLUMN timeOfDay TEXT")
                }
                if (!columns.contains("categoryId")) {
                    db.execSQL("ALTER TABLE habits ADD COLUMN categoryId INTEGER")
                }
                if (!columns.contains("sortOrder")) {
                    db.execSQL("ALTER TABLE habits ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                }
                if (!columns.contains("isArchived")) {
                    db.execSQL("ALTER TABLE habits ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        iconName TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS habit_completions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        habitId INTEGER NOT NULL,
                        completionDate TEXT NOT NULL,
                        note TEXT,
                        FOREIGN KEY (habitId) REFERENCES habits(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_completions_habitId ON habit_completions(habitId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_habit_completions_completionDate ON habit_completions(completionDate)")
            }
        }
    }
}

