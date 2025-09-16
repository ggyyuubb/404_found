package com.example.wearther.community.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wearther.R
import com.example.wearther.databinding.ItemFriendSuggestionBinding
import com.example.wearther.community.data.FriendSuggestion

class FriendSuggestionAdapter(
    private val suggestions: List<FriendSuggestion>,
    private val onAddFriendClick: (FriendSuggestion) -> Unit
) : RecyclerView.Adapter<FriendSuggestionAdapter.SuggestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemFriendSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(suggestions[position])
    }

    override fun getItemCount(): Int = suggestions.size

    inner class SuggestionViewHolder(private val binding: ItemFriendSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(suggestion: FriendSuggestion) {
            with(binding) {
                textName.text = suggestion.name
                textMutualFriends.text = "공통 친구 ${suggestion.mutualFriends}명"

                // 프로필 이미지 로드
                Glide.with(imageProfile.context)
                    .load(suggestion.profileImage)
                    .placeholder(R.drawable.gradient_profile_bg)
                    .error(R.drawable.gradient_profile_bg)
                    .circleCrop()
                    .into(imageProfile)

                // 팔로우 버튼 상태
                buttonFollow.text = if (suggestion.isFollowing) "팔로잉" else "팔로우"
                buttonFollow.isEnabled = !suggestion.isFollowing
                
                // 버튼 배경 설정
                buttonFollow.setBackgroundResource(
                    if (suggestion.isFollowing) R.drawable.button_disabled_background
                    else R.drawable.send_button_background
                )

                // 팔로우 버튼 클릭
                buttonFollow.setOnClickListener {
                    onAddFriendClick(suggestion)
                }
            }
        }
    }
}