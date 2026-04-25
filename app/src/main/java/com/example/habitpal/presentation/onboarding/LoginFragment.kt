package com.example.habitpal.presentation.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.habitpal.R
import com.example.habitpal.databinding.FragmentLoginBinding
import com.example.habitpal.util.collectFlow
import com.example.habitpal.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeEvents()
    }

    private fun setupClickListeners() {
        binding.btnLetsGo.setOnClickListener { submit() }
        binding.etName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { submit(); true } else false
        }
    }

    private fun submit() {
        viewModel.completeOnboarding(binding.etName.text.toString())
    }

    private fun observeEvents() {
        viewLifecycleOwner.collectFlow(viewModel.events) { event ->
            when (event) {
                is LoginEvent.UserDetailsSaved -> {
                    findNavController().navigate(R.id.action_loginFragment_to_onboardingFragment)
                }
                is LoginEvent.Error -> toast(event.message)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
