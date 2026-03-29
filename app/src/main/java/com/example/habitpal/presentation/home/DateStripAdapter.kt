package com.example.habitpal.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpal.databinding.ItemDayBinding
import com.example.habitpal.domain.model.DayItem

class DateStripAdapter : ListAdapter<DayItem, DateStripAdapter.DayViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DayViewHolder(
        private val binding: ItemDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: DayItem) {
            binding.tvDayName.text = day.dayName
            binding.tvDayNumber.text = day.dayNumber.toString()

            if (day.isSelected) {
                binding.root.setBackgroundResource(com.example.habitpal.R.drawable.bg_day_selected)
                binding.tvDayName.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
                binding.tvDayNumber.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
            } else {
                binding.root.setBackgroundResource(com.example.habitpal.R.drawable.bg_day_normal)
                binding.tvDayName.setTextColor(
                    binding.root.context.getColor(com.example.habitpal.R.color.text_secondary)
                )
                binding.tvDayNumber.setTextColor(
                    binding.root.context.getColor(com.example.habitpal.R.color.text_primary)
                )
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DayItem>() {
        override fun areItemsTheSame(oldItem: DayItem, newItem: DayItem) =
            oldItem.dayNumber == newItem.dayNumber
        override fun areContentsTheSame(oldItem: DayItem, newItem: DayItem) =
            oldItem == newItem
    }
}