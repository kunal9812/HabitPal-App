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
            binding.cbSelected.isChecked = isSelected
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_ID.toInt()) return@setOnClickListener
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
