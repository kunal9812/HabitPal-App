package com.example.habitpal.di

import android.content.Context
import androidx.room.Room
import com.example.habitpal.data.local.UserPreferencesDataSource
import com.example.habitpal.data.local.dao.CategoryDao
import com.example.habitpal.data.local.dao.HabitCompletionDao
import com.example.habitpal.data.local.dao.HabitDao
import com.example.habitpal.data.local.dao.HabitLogDao
import com.example.habitpal.data.local.database.HabitDatabase
import com.example.habitpal.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase =
        Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addMigrations(HabitDatabase.MIGRATION_1_2, HabitDatabase.MIGRATION_2_3)
            .build()

    @Provides
    @Singleton
    fun provideHabitDao(database: HabitDatabase): HabitDao = database.habitDao()

    @Provides
    @Singleton
    fun provideHabitLogDao(database: HabitDatabase): HabitLogDao = database.habitLogDao()

    @Provides
    @Singleton
    fun provideHabitCompletionDao(database: HabitDatabase): HabitCompletionDao = database.habitCompletionDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: HabitDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideUserPreferencesDataSource(
        @ApplicationContext context: Context
    ): UserPreferencesDataSource = UserPreferencesDataSource(context)
}