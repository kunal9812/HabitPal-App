package com.example.habitpal.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.data.local.UserPreferencesDataSource
import com.example.habitpal.domain.model.Category
import com.example.habitpal.domain.model.Habit
import com.example.habitpal.domain.model.Quote
import com.example.habitpal.domain.model.Weather
import com.example.habitpal.domain.repository.CategoryRepository
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.domain.usecase.habit.CompleteHabitUseCase
import com.example.habitpal.domain.usecase.habit.GetHabitsUseCase
import com.example.habitpal.domain.usecase.SeedCategoriesUseCase
import com.example.habitpal.domain.usecase.quote.GetQuoteUseCase
import com.example.habitpal.domain.usecase.weather.GetWeatherUseCase
import com.example.habitpal.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val getWeatherUseCase: GetWeatherUseCase,
    private val getQuoteUseCase: GetQuoteUseCase,
    private val userPreferences: UserPreferencesDataSource,
    private val habitRepository: HabitRepository,
    categoryRepository: CategoryRepository,
    private val seedCategoriesUseCase: SeedCategoriesUseCase
) : ViewModel() {

    private val _allHabits = MutableStateFlow<List<Habit>>(emptyList())

    private val _weather = MutableStateFlow<Resource<Weather>>(Resource.Loading)
    val weather: StateFlow<Resource<Weather>> = _weather.asStateFlow()

    private val _quote = MutableStateFlow<Resource<Quote>>(Resource.Loading)
    val quote: StateFlow<Resource<Quote>> = _quote.asStateFlow()

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<Category>> = categoryRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<Habit>> = combine(_allHabits, _selectedCategory) { habits, cat ->
        if (cat == null) habits
        else habits.filter { it.categoryId == cat.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedCategories()
        loadHabits()
        loadQuote()
        loadUserName()
    }

    private fun seedCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            seedCategoriesUseCase.seedIfEmpty()
        }
    }

    private fun loadUserName() {
        viewModelScope.launch {
            userPreferences.userName.collect { _userName.value = it }
        }
    }

    private fun loadHabits() {
        viewModelScope.launch {
            getHabitsUseCase().collect { _allHabits.value = it }
        }
    }

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weather.value = Resource.Loading
            _weather.value = getWeatherUseCase(lat, lon)
        }
    }

    private fun loadQuote() {
        viewModelScope.launch {
            _quote.value = Resource.Loading
            _quote.value = getQuoteUseCase()
        }
    }

    fun completeHabit(habitId: Int, note: String? = null) {
        viewModelScope.launch {
            completeHabitUseCase(habitId, note)
        }
    }

    fun selectCategory(category: Category?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun reorderHabits(fromPos: Int, toPos: Int) {
        val currentList = _allHabits.value.toMutableList()
        if (fromPos < 0 || toPos < 0 || fromPos >= currentList.size || toPos >= currentList.size) return
        val moved = currentList.removeAt(fromPos)
        currentList.add(toPos, moved)
        _allHabits.value = currentList
        viewModelScope.launch(Dispatchers.IO) {
            val updates = currentList.mapIndexed { index, habit -> Pair(habit.id, index) }
            habitRepository.updateSortOrders(updates)
        }
    }
}


