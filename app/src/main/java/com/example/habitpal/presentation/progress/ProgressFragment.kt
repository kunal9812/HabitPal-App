package com.example.habitpal.presentation.progress

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitpal.databinding.FragmentProgressBinding
import com.example.habitpal.domain.model.HabitRingStats
import com.example.habitpal.util.HabitCardColors
import com.example.habitpal.util.Resource
import com.example.habitpal.util.collectFlow
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by viewModels()
    private lateinit var habitRingAdapter: HabitRingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBarChart()
        setupHabitRings()
        observeStats()
    }

    private fun setupBarChart() {
        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            setTouchEnabled(false)
            animateY(800)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
                textColor = Color.GRAY
                textSize = 12f
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F0F0F0")
                setDrawAxisLine(false)
                textColor = Color.GRAY
                axisMinimum = 0f
                axisMaximum = 100f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float) = "${value.toInt()}%"
                }
            }

            axisRight.isEnabled = false
        }
    }

    private fun setupHabitRings() {
        habitRingAdapter = HabitRingAdapter { habitId ->
            viewModel.toggleHabitSelection(habitId)
        }
        binding.rvHabitRings.apply {
            adapter = habitRingAdapter
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
        }
    }

    private fun observeStats() {
        viewLifecycleOwner.collectFlow(viewModel.stats) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.tvCompletedToday.text = "—"
                    binding.tvCompletionRate.text = "—"
                    binding.tvLongestStreak.text = "—"
                    binding.tvTotalCompletions.text = "—"
                }
                is Resource.Success -> {
                    val stats = resource.data
                    binding.tvCompletedToday.text = stats.completedToday.toString()
                    binding.tvCompletionRate.text = "${stats.completionRatePercent}%"
                    binding.tvLongestStreak.text = stats.longestStreak.toString()
                    binding.tvTotalCompletions.text = stats.totalCompletions.toString()
                }
                is Resource.Error -> {
                    binding.tvCompletedToday.text = "—"
                    binding.tvCompletionRate.text = "—"
                    binding.tvLongestStreak.text = "—"
                    binding.tvTotalCompletions.text = "—"
                }
            }
        }

        viewLifecycleOwner.collectFlow(viewModel.weeklyStats) { weeklyStats ->
            if (weeklyStats.isEmpty()) return@collectFlow

            val entries = weeklyStats.mapIndexed { index, stat ->
                BarEntry(index.toFloat(), stat.completionPercent)
            }

            val colors = weeklyStats.map { stat ->
                if (stat.isToday) Color.parseColor("#4FC3F7")
                else Color.parseColor("#C5E8A0")
            }

            val dataSet = BarDataSet(entries, "Weekly").apply {
                this.colors = colors
                setDrawValues(true)
                valueTextSize = 10f
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float) =
                        if (value > 0) "${value.toInt()}%" else ""
                }
            }

            binding.barChart.apply {
                data = BarData(dataSet).apply { barWidth = 0.6f }
                xAxis.valueFormatter = IndexAxisValueFormatter(
                    weeklyStats.map { it.dayName }
                )
                invalidate()
            }
        }

        viewLifecycleOwner.collectFlow(viewModel.habitRingStats) { rings ->
            val selectedIds = viewModel.selectedHabitIds.value
            val updatedRings = rings.map { it.copy(isSelected = it.habit.id in selectedIds) }
            habitRingAdapter.submitList(updatedRings)
        }

        viewLifecycleOwner.collectFlow(viewModel.selectedHabitIds) { selectedIds ->
            val rings = viewModel.habitRingStats.value
            val updatedRings = rings.map { it.copy(isSelected = it.habit.id in selectedIds) }
            habitRingAdapter.submitList(updatedRings)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}