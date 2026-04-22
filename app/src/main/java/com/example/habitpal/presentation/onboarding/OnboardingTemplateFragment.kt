package com.example.habitpal.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitpal.R
import com.example.habitpal.databinding.FragmentOnboardingTemplatesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingTemplateFragment : Fragment() {

    private var _binding: FragmentOnboardingTemplatesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OnboardingTemplateViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingTemplatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HabitTemplateAdapter { template -> viewModel.toggleTemplate(template) }
        binding.rvTemplates.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTemplates.adapter = adapter
        adapter.submitList(viewModel.templates)

        binding.btnGetStarted.setOnClickListener {
            viewModel.finishOnboarding {
                findNavController().navigate(R.id.action_onboarding_templates_to_home)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
