package com.example.habitpal.presentation.archived

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpal.databinding.ItemArchivedHabitBinding
import com.example.habitpal.domain.model.Habit

class ArchivedHabitAdapter(
    private val onRestore: (Int) -> Unit
) : ListAdapter<Habit, ArchivedHabitAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemArchivedHabitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.tvArchivedTitle.text = habit.title
            binding.tvArchivedFrequency.text = habit.frequency.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
            binding.btnRestore.setOnClickListener { onRestore(habit.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemArchivedHabitBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(a: Habit, b: Habit) = a.id == b.id
        override fun areContentsTheSame(a: Habit, b: Habit) = a == b
    }
}
