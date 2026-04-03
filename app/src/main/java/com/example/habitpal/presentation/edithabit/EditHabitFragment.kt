package com.example.habitpal.presentation.edithabit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.habitpal.R
import com.example.habitpal.databinding.FragmentEditHabitBinding
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.util.collectFlow
import com.example.habitpal.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditHabitFragment : Fragment() {

    private var _binding: FragmentEditHabitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditHabitViewModel by viewModels()
    private val args: EditHabitFragmentArgs by navArgs()
    private val habitId: Int by lazy { args.habitId.toInt() }

    private var selectedColor: Int = 0xFF4A90D9.toInt()
    private var selectedFrequency: HabitFrequency = HabitFrequency.DAILY
    private var isPreFilled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFrequencyChips()
        setupColorPicker()
        setupClickListeners()
        observeState()
        viewModel.loadHabit(habitId)
    }

    private fun setupFrequencyChips() {
        binding.chipGroupFrequency.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedFrequency = when {
                checkedIds.contains(R.id.chip_daily) -> HabitFrequency.DAILY
                checkedIds.contains(R.id.chip_weekly) -> HabitFrequency.WEEKLY
                checkedIds.contains(R.id.chip_monthly) -> HabitFrequency.MONTHLY
                else -> HabitFrequency.DAILY
            }
        }
    }

    private fun setupColorPicker() {
        val colorMap = mapOf(
            binding.colorBlue to Pair(0xFF4A90D9.toInt(), "Blue"),
            binding.colorCoral to Pair(0xFFE8664A.toInt(), "Coral"),
            binding.colorGreen to Pair(0xFF5BCE8F.toInt(), "Green"),
            binding.colorAmber to Pair(0xFFE8A838.toInt(), "Amber"),
            binding.colorPurple to Pair(0xFFB368D9.toInt(), "Purple"),
            binding.colorPink to Pair(0xFFE8527A.toInt(), "Pink")
        )

        colorMap.forEach { (view, colorPair) ->
            view.setOnClickListener {
                selectedColor = colorPair.first
                binding.tvSelectedColor.text = "${colorPair.second} selected"
                colorMap.keys.forEach { it.scaleX = 1f; it.scaleY = 1f }
                view.scaleX = 1.3f
                view.scaleY = 1.3f
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveHabit.setOnClickListener {
            viewModel.saveHabit(
                title = binding.etHabitTitle.text.toString(),
                description = binding.etHabitDescription.text.toString(),
                frequency = selectedFrequency,
                color = selectedColor
            )
        }
    }

    private fun observeState() {
        // Pre-fill the form once when the habit first loads
        viewLifecycleOwner.collectFlow(viewModel.habit) { habit ->
            if (habit != null && !isPreFilled) {
                isPreFilled = true

                binding.etHabitTitle.setText(habit.title)
                binding.etHabitDescription.setText(habit.description)

                // Pre-select frequency chip
                val chipId = when (habit.frequency) {
                    HabitFrequency.DAILY -> R.id.chip_daily
                    HabitFrequency.WEEKLY -> R.id.chip_weekly
                    HabitFrequency.MONTHLY -> R.id.chip_monthly
                }
                binding.chipGroupFrequency.check(chipId)
                selectedFrequency = habit.frequency

                // Pre-select color: find the closest color in the map and scale it up
                if (habit.color != 0) {
                    selectedColor = habit.color
                    val colorViews = mapOf(
                        0xFF4A90D9.toInt() to Pair(binding.colorBlue, "Blue"),
                        0xFFE8664A.toInt() to Pair(binding.colorCoral, "Coral"),
                        0xFF5BCE8F.toInt() to Pair(binding.colorGreen, "Green"),
                        0xFFE8A838.toInt() to Pair(binding.colorAmber, "Amber"),
                        0xFFB368D9.toInt() to Pair(binding.colorPurple, "Purple"),
                        0xFFE8527A.toInt() to Pair(binding.colorPink, "Pink")
                    )
                    colorViews[habit.color]?.let { (view, name) ->
                        view.scaleX = 1.3f
                        view.scaleY = 1.3f
                        binding.tvSelectedColor.text = "$name selected"
                    }
                }
            }
        }

        viewLifecycleOwner.collectFlow(viewModel.events) { event ->
            when (event) {
                is EditHabitEvent.HabitSaved -> {
                    toast("Habit updated! ✅")
                    findNavController().navigateUp()
                }
                is EditHabitEvent.Error -> toast(event.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
