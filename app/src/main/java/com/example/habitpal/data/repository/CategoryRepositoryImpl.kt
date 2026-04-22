package com.example.habitpal.data.repository

import com.example.habitpal.data.local.dao.CategoryDao
import com.example.habitpal.data.local.entity.CategoryEntity
import com.example.habitpal.domain.model.Category
import com.example.habitpal.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> =
        categoryDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(category: Category): Long =
        categoryDao.insert(category.toEntity())

    override suspend fun insertAll(categories: List<Category>) =
        categoryDao.insertAll(categories.map { it.toEntity() })

    override suspend fun delete(category: Category) =
        categoryDao.delete(category.toEntity())

    override suspend fun count(): Int = categoryDao.count()

    private fun CategoryEntity.toDomain() = Category(id, name, colorHex, iconName)
    private fun Category.toEntity() = CategoryEntity(id, name, colorHex, iconName)
}
