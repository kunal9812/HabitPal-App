package com.example.habitpal.domain.model

data class CommunityMember(
    val name: String,
    val avatarInitials: String,
    val avatarColor: Int,
    val totalCompletions: Int,
    val currentStreak: Int,
    val isCurrentUser: Boolean = false
)