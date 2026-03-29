package com.example.habitpal.presentation.progress

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpal.databinding.ItemHabitRingBinding
import com.example.habitpal.domain.model.HabitRingStats
import com.example.habitpal.util.HabitCardColors

class HabitRingAdapter(
    private val onHabitToggle: (Int) -> Unit
) : ListAdapter<HabitRingStats, HabitRingAdapter.RingViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingViewHolder {
        val binding = ItemHabitRingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RingViewHolder(
        private val binding: ItemHabitRingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stats: HabitRingStats) {
            binding.tvHabitName.text = stats.habit.title
            binding.tvStreak.text = "🔥 ${stats.currentStreak} days"
            binding.tvPercent.text = "${stats.completionPercent.toInt()}%"

            val color = if (stats.habit.color != 0) stats.habit.color
            else HabitCardColors.getColor(stats.habit.id)

            // safely set color after view is attached
            binding.progressRing.post {
                try {
                    binding.progressRing.setIndicatorColor(color)
                    binding.progressRing.progress = stats.completionPercent.toInt()
                } catch (e: Exception) {
                    binding.progressRing.progress = stats.completionPercent.toInt()
                }
            }

            binding.root.alpha = if (stats.isSelected) 1f else 0.4f
            binding.root.setOnClickListener { onHabitToggle(stats.habit.id) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<HabitRingStats>() {
        override fun areItemsTheSame(oldItem: HabitRingStats, newItem: HabitRingStats) =
            oldItem.habit.id == newItem.habit.id
        override fun areContentsTheSame(oldItem: HabitRingStats, newItem: HabitRingStats) =
            oldItem == newItem
    }
}