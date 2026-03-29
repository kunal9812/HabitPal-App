package com.example.habitpal.presentation.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitpal.R
import com.example.habitpal.databinding.FragmentHomeBinding
import com.example.habitpal.domain.model.DayItem
import com.example.habitpal.util.Constants
import com.example.habitpal.util.Resource
import com.example.habitpal.util.collectFlow
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var dateStripAdapter: DateStripAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) fetchLocationAndLoadWeather()
        else viewModel.loadWeather(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupRecyclerView()
        setupDateStrip()
        setupClickListeners()
        observeState()
        checkLocationPermissionAndLoadWeather()
    }

    private fun setupDateStrip() {
        dateStripAdapter = DateStripAdapter()
        binding.rvDateStrip.apply {
            adapter = dateStripAdapter
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
        }

        val calendar = Calendar.getInstance()
        val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        val days = (-3..3).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, offset)
            DayItem(
                dayName = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1],
                dayNumber = cal.get(Calendar.DAY_OF_MONTH),
                isSelected = cal.get(Calendar.DAY_OF_MONTH) == todayDay
            )
        }
        dateStripAdapter.submitList(days)
        // scroll to today (index 3)
        binding.rvDateStrip.scrollToPosition(3)
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onHabitClick = { habit ->
                val action = HomeFragmentDirections
                    .actionHomeFragmentToHabitDetailFragment(habitId = habit.id.toLong())
                findNavController().navigate(action)
            },
            onCompleteClick = { habit ->
                viewModel.completeHabit(habit.id)
            }
        )
        binding.rvHabits.apply {
            adapter = habitAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.fabAddHabit.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addHabit)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.collectFlow(viewModel.userName) { name ->
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when {
                hour < 12 -> "Good Morning, $name!"
                hour < 17 -> "Good Afternoon, $name!"
                else -> "Good Evening, $name!"
            }
            binding.tvGreeting.text = greeting
        }
        viewLifecycleOwner.collectFlow(viewModel.habits) { habits ->
            habitAdapter.submitList(habits)
            binding.tvTodayHabits.text = "Today's Habits (${habits.size})"
        }
        viewLifecycleOwner.collectFlow(viewModel.weather) { weatherResource ->
            binding.tvWeather.text = when (weatherResource) {
                is Resource.Success -> "${weatherResource.data.iconEmoji} ${weatherResource.data.cityName} · ${weatherResource.data.temperature}°C · ${weatherResource.data.description}"
                is Resource.Error -> getString(R.string.weather_unavailable)
                Resource.Loading -> getString(R.string.loading_weather)
            }
        }
        viewLifecycleOwner.collectFlow(viewModel.quote) { quoteResource ->
            binding.tvQuote.text = when (quoteResource) {
                is Resource.Success -> "\"${quoteResource.data.content}\" — ${quoteResource.data.author}"
                is Resource.Error -> getString(R.string.quote_unavailable)
                Resource.Loading -> getString(R.string.loading_quote)
            }
        }
    }

    private fun checkLocationPermissionAndLoadWeather() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) fetchLocationAndLoadWeather()
        else locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationAndLoadWeather() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) viewModel.loadWeather(location.latitude, location.longitude)
            else viewModel.loadWeather(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE)
        }.addOnFailureListener {
            viewModel.loadWeather(Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}