package com.example.habitpal.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habitpal.domain.model.CommunityMember
import com.example.habitpal.domain.repository.HabitRepository
import com.example.habitpal.domain.usecase.progress.GetProgressStatsUseCase
import com.example.habitpal.domain.usecase.progress.GetHabitStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getProgressStatsUseCase: GetProgressStatsUseCase
) : ViewModel() {

    private val _members = MutableStateFlow<List<CommunityMember>>(emptyList())
    val members: StateFlow<List<CommunityMember>> = _members.asStateFlow()

    private val dummyFriends = listOf(
        CommunityMember("Priya", "PR", 0xFF4A90D9.toInt(), 18, 5),
        CommunityMember("Rahul", "RA", 0xFFE8664A.toInt(), 14, 3),
        CommunityMember("Sara", "SA", 0xFF5BCE8F.toInt(), 22, 7),
        CommunityMember("Arjun", "AR", 0xFFE8A838.toInt(), 9, 2),
        CommunityMember("Meera", "ME", 0xFFB368D9.toInt(), 30, 12)
    )

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            getProgressStatsUseCase.observe().collect { stats ->
                val currentUser = CommunityMember(
                    name = "Me",
                    avatarInitials = "ME",
                    avatarColor = 0xFFE8527A.toInt(),
                    totalCompletions = stats.totalCompletions,
                    currentStreak = stats.longestStreak,
                    isCurrentUser = true
                )
                val allMembers = (dummyFriends + currentUser)
                    .sortedByDescending { it.totalCompletions }
                _members.value = allMembers
            }
        }
    }
}
