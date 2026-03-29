package com.example.habitpal.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.habitpal.databinding.FragmentProfileBinding
import com.example.habitpal.util.collectFlow
import com.example.habitpal.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnEditName.setOnClickListener {
            binding.layoutEditName.visibility = View.VISIBLE
            binding.btnSaveName.visibility = View.VISIBLE
            binding.btnEditName.visibility = View.GONE
            binding.etUserName.requestFocus()
        }

        binding.btnSaveName.setOnClickListener {
            val name = binding.etUserName.text.toString()
            if (name.isNotBlank()) {
                viewModel.updateUserName(name)
                binding.etUserName.setText("")
                binding.layoutEditName.visibility = View.GONE
                binding.btnSaveName.visibility = View.GONE
                binding.btnEditName.visibility = View.VISIBLE
                toast("Name updated!")
            } else {
                toast("Name cannot be empty")
            }
        }

        binding.etUserName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.btnSaveName.performClick()
                true
            } else false
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            viewModel.setNotifications(checked)
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            viewModel.setDarkMode(checked)
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.collectFlow(viewModel.userName) { name ->
            binding.tvUserName.text = name
            binding.tvAvatar.text = name.take(2).uppercase()
        }
        viewLifecycleOwner.collectFlow(viewModel.totalHabits) { count ->
            binding.tvStatHabits.text = count.toString()
        }
        viewLifecycleOwner.collectFlow(viewModel.totalCompletions) { count ->
            binding.tvStatCompletions.text = count.toString()
        }
        viewLifecycleOwner.collectFlow(viewModel.longestStreak) { streak ->
            binding.tvStatStreak.text = streak.toString()
        }
        viewLifecycleOwner.collectFlow(viewModel.notificationsEnabled) { enabled ->
            binding.switchNotifications.isChecked = enabled
        }
        viewLifecycleOwner.collectFlow(viewModel.darkModeEnabled) { enabled ->
            binding.switchDarkMode.isChecked = enabled
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


