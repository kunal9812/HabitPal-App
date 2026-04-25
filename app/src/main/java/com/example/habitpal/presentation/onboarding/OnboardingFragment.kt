package com.example.habitpal.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.habitpal.R
import com.example.habitpal.databinding.FragmentOnboardingBinding
import com.example.habitpal.util.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HabitTemplateAdapter { template ->
            viewModel.toggleTemplate(template)
        }
        binding.rvTemplates.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvTemplates.adapter = adapter
        adapter.submitList(viewModel.templates)

        viewLifecycleOwner.collectFlow(viewModel.userName) { name ->
            val cleanName = name.trim().ifEmpty { getString(R.string.default_user_name) }
            binding.tvOnboardingSubtitle.text =
                getString(R.string.pick_habits_subtitle_personalized, cleanName)
        }

        binding.btnSkip.setOnClickListener {
            viewModel.skipOnboarding { navigateHome() }
        }

        binding.btnGetStarted.setOnClickListener {
            viewModel.finishOnboarding { navigateHome() }
        }
    }

    private fun navigateHome() {
        findNavController().navigate(R.id.action_onboarding_to_home)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



