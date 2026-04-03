package com.example.habitpal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.habitpal.data.local.UserPreferencesDataSource
import com.example.habitpal.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var userPreferences: UserPreferencesDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Apply dark mode before inflating
        lifecycleScope.launch {
            val darkMode = userPreferences.darkModeEnabled.first()
            AppCompatDelegate.setDefaultNightMode(
                if (darkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // If already onboarded, skip to Home
        val hasOnboarded = runBlocking { userPreferences.hasOnboarded.first() }
        if (hasOnboarded) {
            val graph = navController.navInflater.inflate(R.navigation.nav_graph)
            graph.setStartDestination(R.id.homeFragment)
            navController.setGraph(graph, null)
        }

        binding.bottomNavigation.setupWithNavController(navController)

        // Hide bottom nav on onboarding screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val onboardingIds = setOf(R.id.getStartedFragment, R.id.loginFragment)
            binding.bottomNavigation.visibility =
                if (destination.id in onboardingIds) android.view.View.GONE
                else android.view.View.VISIBLE
        }
    }
}