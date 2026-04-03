package com.example.habitpal.presentation.habitdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.habitpal.databinding.FragmentHabitDetailBinding
import com.example.habitpal.util.collectFlow
import com.example.habitpal.util.toast
import dagger.hilt.android.AndroidEntryPoint

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
            toast("Habit completed! 🎉")
        }
        binding.btnEdit.setOnClickListener {
            val action = HabitDetailFragmentDirections
                .actionHabitDetailFragmentToEditHabitFragment(habitId = habitId.toLong())
            findNavController().navigate(action)
        }
        binding.btnDelete.setOnClickListener {
            viewModel.deleteHabit()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.collectFlow(viewModel.habit) { habit ->
            habit?.let {
                binding.tvHabitTitle.text = it.title
                binding.tvHabitDescription.text = it.description
                binding.tvHabitFrequency.text = it.frequency.name
                    .lowercase()
                    .replaceFirstChar { c -> c.uppercase() }
                if (it.color != 0) {
                    binding.viewColorBanner.setBackgroundColor(it.color)
                }
            }
        }
        viewLifecycleOwner.collectFlow(viewModel.streak) { streak ->
            binding.tvStreak.text = "🔥 $streak day streak"
        }
        viewLifecycleOwner.collectFlow(viewModel.isDeleted) { deleted ->
            if (deleted) findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}