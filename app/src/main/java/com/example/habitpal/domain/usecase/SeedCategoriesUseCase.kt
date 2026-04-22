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
