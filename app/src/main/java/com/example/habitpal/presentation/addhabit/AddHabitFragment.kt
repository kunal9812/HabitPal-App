package com.example.habitpal.presentation.addhabit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.habitpal.R
import com.example.habitpal.databinding.FragmentAddHabitBinding
import com.example.habitpal.domain.model.HabitFrequency
import com.example.habitpal.util.ReminderScheduler
import com.example.habitpal.util.collectFlow
import com.example.habitpal.util.toast
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AddHabitFragment : Fragment() {

    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddHabitViewModel by viewModels()

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    private var selectedColor: Int = 0xFF4A90D9.toInt()
    private var selectedFrequency: HabitFrequency = HabitFrequency.DAILY
    private var selectedReminderTime: String? = null   // "HH:mm" or null

    // Runtime notification permission launcher (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* permission result — alarm is already saved regardless */ }

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
        setupReminderToggle()
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
                colorMap.keys.forEach { it.scaleX = 1f; it.scaleY = 1f }
                view.scaleX = 1.3f
                view.scaleY = 1.3f
            }
        }
    }

    private fun setupReminderToggle() {
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestNotificationPermissionIfNeeded()
                showTimePicker()
            } else {
                selectedReminderTime = null
                binding.tvReminderTime.text = "No reminder set"
                binding.tvReminderHint.visibility = View.GONE
            }
        }

        // Tapping the time label also re-opens the picker when enabled
        binding.tvReminderTime.setOnClickListener {
            if (binding.switchReminder.isChecked) showTimePicker()
        }
    }

    private fun showTimePicker() {
        val (initHour, initMinute) = parseCurrentTime()
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(initHour)
            .setMinute(initMinute)
            .setTitleText("Set reminder time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val h = picker.hour
            val m = picker.minute
            selectedReminderTime = "%02d:%02d".format(h, m)
            binding.tvReminderTime.text = formatDisplayTime(h, m)
            binding.tvReminderHint.visibility = View.VISIBLE
        }
        picker.addOnNegativeButtonClickListener {
            // User cancelled — uncheck the switch if no time was previously set
            if (selectedReminderTime == null) {
                binding.switchReminder.isChecked = false
            }
        }
        picker.show(parentFragmentManager, "time_picker_add")
    }

    private fun setupClickListeners() {
        binding.btnSaveHabit.setOnClickListener {
            viewModel.addHabit(
                title = binding.etHabitTitle.text.toString(),
                description = binding.etHabitDescription.text.toString(),
                frequency = selectedFrequency,
                reminderTime = selectedReminderTime,
                color = selectedColor
            )
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.collectFlow(viewModel.events) { event ->
            when (event) {
                is AddHabitEvent.HabitAdded -> {
                    // Schedule the alarm after the habit is saved
                    if (event.habit.reminderTime != null) {
                        reminderScheduler.schedule(event.habit)
                        toast("Habit added! Reminder set for ${formatDisplayTime(event.habit.reminderTime)}")
                    } else {
                        toast("Habit added!")
                    }
                    findNavController().navigateUp()
                }
                is AddHabitEvent.Error -> toast(event.message)
            }
        }
    }

    // ---------- helpers ----------

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun parseCurrentTime(): Pair<Int, Int> {
        val raw = selectedReminderTime ?: return Pair(8, 0)
        return try {
            val parts = raw.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            Pair(8, 0)
        }
    }

    /** Converts a "HH:mm" string to locale-aware 12h display. */
    private fun formatDisplayTime(raw: String?): String {
        if (raw == null) return "No reminder set"
        return try {
            val parts = raw.split(":")
            formatDisplayTime(parts[0].toInt(), parts[1].toInt())
        } catch (e: Exception) {
            raw
        }
    }

    private fun formatDisplayTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "%d:%02d %s".format(displayHour, minute, amPm)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
