package com.example.habitpal.presentation.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitpal.databinding.ItemCommunityMemberBinding
import com.example.habitpal.domain.model.CommunityMember

class CommunityAdapter : ListAdapter<CommunityMember, CommunityAdapter.MemberViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemCommunityMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class MemberViewHolder(
        private val binding: ItemCommunityMemberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(member: CommunityMember, rank: Int) {
            binding.tvRank.text = when (rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "#$rank"
            }
            binding.tvInitials.text = member.avatarInitials
            binding.tvInitials.backgroundTintList =
                android.content.res.ColorStateList.valueOf(member.avatarColor)
            binding.tvName.text = if (member.isCurrentUser) "${member.name} (You)" else member.name
            binding.tvCompletions.text = if (member.totalCompletions == 1)
                "1 completion"
            else
                "${member.totalCompletions} completions"
            binding.tvStreak.text = "🔥 ${member.currentStreak} days"

            // highlight current user card
            binding.root.strokeWidth = if (member.isCurrentUser) 3 else 0
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CommunityMember>() {
        override fun areItemsTheSame(oldItem: CommunityMember, newItem: CommunityMember) =
            oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: CommunityMember, newItem: CommunityMember) =
            oldItem == newItem
    }
}