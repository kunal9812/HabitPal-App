package com.example.habitpal.presentation.habitdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.habitpal.R
import com.example.habitpal.databinding.FragmentHabitDetailBinding
import com.example.habitpal.databinding.ItemCalendarDayBinding
import com.example.habitpal.util.collectFlow
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class HabitDetailFragment : Fragment() {

    private var _binding: FragmentHabitDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HabitDetailViewModel by viewModels()
    private val args: HabitDetailFragmentArgs by navArgs()
    private val habitId: Int by lazy { args.habitId.toInt() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadHabit(habitId)
        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnComplete.setOnClickListener {
            viewModel.completeHabit(habitId)
            findNavController().navigateUp()
        }
        binding.btnEdit.setOnClickListener {
            val action = HabitDetailFragmentDirections
                .actionHabitDetailFragmentToEditHabitFragment(habitId = habitId.toLong())
            findNavController().navigate(action)
        }
        binding.btnArchive.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.archive_habit)
                .setMessage(R.string.archive_habit_message)
                .setPositiveButton(R.string.archive) { _, _ -> viewModel.archiveHabit() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.collectFlow(viewModel.habit) { habit ->
            habit ?: return@collectFlow
            binding.tvHabitTitle.text = habit.title
            binding.tvHabitDescription.text = habit.description
            binding.tvHabitFrequency.text = habit.frequency.name
                .lowercase()
                .replaceFirstChar { c -> c.uppercase() }
            if (habit.color != 0) {
                binding.viewColorBanner.setBackgroundColor(habit.color)
            }
        }

        viewLifecycleOwner.collectFlow(viewModel.stats) { stats ->
            stats ?: return@collectFlow
            binding.statCurrentStreak.tvStatValue.text = stats.currentStreak.toString()
            binding.statCurrentStreak.tvStatLabel.text = "Streak"
            binding.statBestStreak.tvStatValue.text = stats.bestStreak.toString()
            binding.statBestStreak.tvStatLabel.text = "Best"
            binding.statTotal.tvStatValue.text = stats.totalCompletions.toString()
            binding.statTotal.tvStatLabel.text = "Total"
            binding.statRate.tvStatValue.text = "${(stats.allTimeRate * 100).toInt()}%"
            binding.statRate.tvStatLabel.text = "Rate"
        }

        viewLifecycleOwner.collectFlow(viewModel.completionMap) {
            setupCalendar(it)
        }

        viewLifecycleOwner.collectFlow(viewModel.isArchived) { archived ->
            if (archived) findNavController().navigateUp()
        }
    }

    private fun setupCalendar(completionMap: Map<LocalDate, Boolean>) {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(6)
        val daysOfWeek = daysOfWeek()

        binding.calendarView.setup(startMonth, currentMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.binding.tvDayNumber.text = data.date.dayOfMonth.toString()
                val completed = completionMap[data.date] == true
                val isToday = data.date == LocalDate.now()
                val bg = when {
                    completed && isToday -> R.drawable.bg_day_done_today
                    completed -> R.drawable.bg_day_done
                    isToday -> R.drawable.bg_day_today
                    data.position != DayPosition.MonthDate -> 0
                    else -> R.drawable.bg_day_empty
                }
                if (bg != 0) container.binding.dayContainer.setBackgroundResource(bg)
                else container.binding.dayContainer.background = null
            }
        }

        binding.calendarView.monthHeaderBinder =
            object : com.kizitonwose.calendar.view.MonthHeaderFooterBinder<MonthHeaderContainer> {
                override fun create(view: View) = MonthHeaderContainer(view)
                override fun bind(container: MonthHeaderContainer, data: com.kizitonwose.calendar.core.CalendarMonth) {
                    container.binding.tvMonthTitle.text = data.yearMonth.month
                        .getDisplayName(TextStyle.FULL, Locale.getDefault())
                }
            }
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val binding = ItemCalendarDayBinding.bind(view)
    }

    inner class MonthHeaderContainer(view: View) : ViewContainer(view) {
        val binding = com.example.habitpal.databinding.ItemCalendarMonthHeaderBinding.bind(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}