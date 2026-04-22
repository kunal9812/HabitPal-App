package com.example.habitpal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habitpal.data.local.dao.CategoryDao
import com.example.habitpal.data.local.dao.HabitDao
import com.example.habitpal.data.local.dao.HabitLogDao
import com.example.habitpal.data.local.entity.CategoryEntity
import com.example.habitpal.data.local.entity.HabitEntity
import com.example.habitpal.data.local.entity.HabitLogEntity

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // HabitEntity new columns
                db.execSQL("ALTER TABLE habits ADD COLUMN frequencyJson TEXT NOT NULL DEFAULT 'DAILY'")
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderHour INTEGER")
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderMinute INTEGER")
                db.execSQL("ALTER TABLE habits ADD COLUMN timeOfDay TEXT")
                db.execSQL("ALTER TABLE habits ADD COLUMN categoryId INTEGER")
                db.execSQL("ALTER TABLE habits ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE habits ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")

                // New categories table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        iconName TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}


