package com.example.habitpal.presentation.addhabit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.habitpal.R
import com.example.habitpal.databinding.FragmentAddHabitBinding
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.util.collectFlow
import com.example.habitpal.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddHabitFragment : Fragment() {

    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddHabitViewModel by viewModels()

    private var selectedColor: Int = 0xFF4A90D9.toInt()
    private var selectedFrequency: HabitFrequency = HabitFrequency.DAILY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFrequencyChips()
        setupColorPicker()
        setupClickListeners()
        observeEvents()
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
                // reset all scales
                colorMap.keys.forEach { it.scaleX = 1f; it.scaleY = 1f }
                // highlight selected
                view.scaleX = 1.3f
                view.scaleY = 1.3f
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveHabit.setOnClickListener {
            viewModel.addHabit(
                title = binding.etHabitTitle.text.toString(),
                description = binding.etHabitDescription.text.toString(),
                frequency = selectedFrequency,
                color = selectedColor
            )
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.collectFlow(viewModel.events) { event ->
            when (event) {
                is AddHabitEvent.HabitAdded -> {
                    toast("Habit added!")
                    findNavController().navigateUp()
                }
                is AddHabitEvent.Error -> toast(event.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


