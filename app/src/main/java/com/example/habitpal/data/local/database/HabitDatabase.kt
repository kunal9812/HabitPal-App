package com.example.habitpal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habitpal.data.local.dao.HabitCompletionDao
import com.example.habitpal.data.local.dao.HabitDao
import com.example.habitpal.data.local.dao.HabitLogDao
import com.example.habitpal.data.local.converter.FrequencyConverter
import com.example.habitpal.data.local.entity.CompletionEntity
import com.example.habitpal.data.local.entity.HabitEntity
import com.example.habitpal.data.local.entity.HabitLogEntity

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class, CompletionEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(FrequencyConverter::class)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun habitCompletionDao(): HabitCompletionDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try { db.execSQL("ALTER TABLE habits ADD COLUMN frequencyJson TEXT NOT NULL DEFAULT 'DAILY'") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE habits ADD COLUMN reminderHour INTEGER") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE habits ADD COLUMN reminderMinute INTEGER") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE habits ADD COLUMN timeOfDay TEXT") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE habits ADD COLUMN categoryId INTEGER") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE habits ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE habits ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0") } catch (_: Exception) {}
                try { db.execSQL("ALTER TABLE habit_completions ADD COLUMN note TEXT") } catch (_: Exception) {}
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
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

