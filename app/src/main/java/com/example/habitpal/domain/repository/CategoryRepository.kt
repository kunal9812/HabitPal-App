package com.example.habitpal.domain.repository

import com.example.habitpal.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    suspend fun insert(category: Category): Long
    suspend fun insertAll(categories: List<Category>)
    suspend fun delete(category: Category)
    suspend fun count(): Int
}
