package com.example.habitpal.presentation.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitpal.databinding.FragmentCommunityBinding
import com.example.habitpal.util.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var communityAdapter: CommunityAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeMembers()
    }

    private fun setupRecyclerView() {
        communityAdapter = CommunityAdapter()
        binding.rvCommunity.apply {
            adapter = communityAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeMembers() {
        viewLifecycleOwner.collectFlow(viewModel.members) { members ->
            communityAdapter.submitList(members)
            val userRank = members.indexOfFirst { it.isCurrentUser } + 1
            binding.tvYourRank.text = "Your rank: #$userRank of ${members.size}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

