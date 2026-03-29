package com.example.habitpal.presentation.home

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpal.databinding.ItemHabitCardBinding
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.util.HabitCardColors

class HabitAdapter(
    private val onHabitClick: (Habit) -> Unit,
    private val onCompleteClick: (Habit) -> Unit
) : ListAdapter<Habit, HabitAdapter.HabitViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(
        private val binding: ItemHabitCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habit: Habit) {
            binding.tvHabitName.text = habit.title

            val color = if (habit.color != 0) habit.color
            else HabitCardColors.getColor(habit.id)

            if (habit.isCompletedToday) {
                val params = binding.root.layoutParams
                params.height = dpToPx(72)
                binding.root.layoutParams = params
                binding.root.alpha = 0.6f
                binding.habitCard.setCardBackgroundColor(blendWithWhite(color, 0.5f))
                binding.tvHabitName.textSize = 15f

                binding.tvFrequency.visibility = View.GONE
                binding.tvCategory.visibility = View.GONE

                binding.ivComplete.setImageResource(com.example.habitpal.R.drawable.ic_check_circle)
                binding.ivComplete.alpha = 1f

            } else {
                val params = binding.root.layoutParams
                params.height = dpToPx(160)
                binding.root.layoutParams = params
                binding.root.alpha = 1f
                binding.habitCard.setCardBackgroundColor(color)
                binding.tvHabitName.textSize = 20f

                binding.tvFrequency.visibility = View.VISIBLE
                binding.tvCategory.visibility = View.VISIBLE
                binding.tvFrequency.text = habit.frequency.name
                    .lowercase().replaceFirstChar { it.uppercase() }
                binding.tvCategory.text = habit.frequency.name
                    .lowercase().replaceFirstChar { it.uppercase() }

                binding.ivComplete.setImageResource(com.example.habitpal.R.drawable.ic_circle_outline)
                binding.ivComplete.alpha = 1f
            }

            binding.habitCard.setOnClickListener { onHabitClick(habit) }
            binding.ivComplete.setOnClickListener {
                if (!habit.isCompletedToday) {
                    animateCompletion(binding) {
                        onCompleteClick(habit)
                    }
                }
            }
        }

        private fun animateCompletion(
            binding: ItemHabitCardBinding,
            onComplete: () -> Unit
        ) {
            val startHeight = binding.root.height
            val endHeight = dpToPx(72)

            val animator = ValueAnimator.ofInt(startHeight, endHeight)
            animator.duration = 400
            animator.addUpdateListener { anim ->
                val value = anim.animatedValue as Int
                val params = binding.root.layoutParams
                params.height = value
                binding.root.layoutParams = params
                binding.root.alpha = 1f - (0.4f * anim.animatedFraction)
            }
            animator.start()
            binding.root.postDelayed({ onComplete() }, 420)
        }

        private fun dpToPx(dp: Int): Int {
            return (dp * binding.root.context.resources.displayMetrics.density).toInt()
        }

        private fun blendWithWhite(color: Int, ratio: Float): Int {
            val r = ((color shr 16 and 0xFF) * (1 - ratio) + 255 * ratio).toInt()
            val g = ((color shr 8 and 0xFF) * (1 - ratio) + 255 * ratio).toInt()
            val b = ((color and 0xFF) * (1 - ratio) + 255 * ratio).toInt()
            return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Habit, newItem: Habit) =
            oldItem == newItem
    }
}