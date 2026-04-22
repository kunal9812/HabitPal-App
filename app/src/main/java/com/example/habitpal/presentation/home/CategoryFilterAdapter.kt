package com.example.habitpal.presentation.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpal.databinding.ItemCategoryFilterBinding
import com.example.habitpal.domain.model.Category

class CategoryFilterAdapter(
    private val onCategorySelected: (Category?) -> Unit
) : RecyclerView.Adapter<CategoryFilterAdapter.ViewHolder>() {

    private val categories = mutableListOf<Category>()
    private var selectedCategoryId: Int? = null

    inner class ViewHolder(private val binding: ItemCategoryFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.chipCategory.text = category.name
            binding.chipCategory.isChecked = selectedCategoryId == category.id
            binding.chipCategory.chipBackgroundColor = null
            runCatching { Color.parseColor(category.colorHex) }.getOrNull()?.let { parsed ->
                binding.chipCategory.chipStrokeColor = android.content.res.ColorStateList.valueOf(parsed)
            }
            binding.chipCategory.setOnClickListener {
                val newSelection = if (selectedCategoryId == category.id) null else category.id
                selectedCategoryId = newSelection
                notifyDataSetChanged()
                onCategorySelected(if (newSelection == null) null else category)
            }
        }
    }

    fun submitCategories(items: List<Category>, selectedId: Int?) {
        categories.clear()
        categories.addAll(items)
        selectedCategoryId = selectedId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryFilterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size
}

