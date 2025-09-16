package com.example.wearther.community.screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wearther.R
import com.example.wearther.databinding.ItemFeedPostBinding
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.UserProfile

class FeedAdapter(
    private val feedItems: List<FeedItem>,
    private val onItemClick: (FeedItem) -> Unit,
    private val onUserProfileClick: (UserProfile) -> Unit,
    private val onCommentClick: (FeedItem) -> Unit
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding = ItemFeedPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.bind(feedItems[position])
    }

    override fun getItemCount(): Int = feedItems.size

    inner class FeedViewHolder(private val binding: ItemFeedPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(feedItem: FeedItem) {
            with(binding) {
                // 사용자 프로필 정보
                tvUserName.text = feedItem.userName
                tvPostTime.text = feedItem.postTime

                // 프로필 이미지 로드
                Glide.with(ivUserProfile.context)
                    .load(feedItem.userProfileImage)
                    .placeholder(R.drawable.gradient_profile_bg)
                    .circleCrop()
                    .into(ivUserProfile)

                // outfit 이미지 로드 (첫 번째 이미지만 표시)
                if (feedItem.outfitImages.isNotEmpty()) {
                    placeholderContainer.visibility = android.view.View.GONE
                    ivPostImage.visibility = android.view.View.VISIBLE
                    
                    Glide.with(ivPostImage.context)
                        .load(feedItem.outfitImages.first())
                        .placeholder(R.drawable.ic_outfit_placeholder)
                        .into(ivPostImage)
                } else {
                    placeholderContainer.visibility = android.view.View.VISIBLE
                    ivPostImage.visibility = android.view.View.GONE
                }

                // 설명 텍스트
                tvDescription.text = feedItem.description

                // 날씨 정보
                chipWeather.text = "${feedItem.temperature} ${feedItem.weather}"

                // 좋아요, 댓글 수
                tvLikeCount.text = feedItem.likeCount.toString()
                tvCommentCount.text = feedItem.commentCount.toString()

                // 좋아요 버튼 상태
                updateLikeButton(feedItem.isLiked)

                // 클릭 리스너
                root.setOnClickListener { onItemClick(feedItem) }

                // 사용자 프로필 클릭
                ivUserProfile.setOnClickListener {
                    val userProfile = UserProfile(
                        userId = "user_${feedItem.id}",
                        userName = feedItem.userName,
                        userEmail = "${feedItem.userName.lowercase()}@example.com",
                        userProfileImage = feedItem.userProfileImage,
                        isFriend = false,
                        followerCount = (50..200).random(),
                        followingCount = (20..100).random(),
                        postCount = (10..50).random()
                    )
                    onUserProfileClick(userProfile)
                }

                // 좋아요 버튼 클릭
                btnLike.setOnClickListener {
                    toggleLike(feedItem)
                }

                // 댓글 버튼 클릭
                btnComment.setOnClickListener {
                    onCommentClick(feedItem)
                }
            }
        }

        private fun updateLikeButton(isLiked: Boolean) {
            if (isLiked) {
                binding.btnLike.setImageResource(R.drawable.ic_heart_filled)
                binding.btnLike.imageTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#EF4444")
                )
            } else {
                binding.btnLike.setImageResource(R.drawable.ic_heart_outline)
                binding.btnLike.imageTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#EF4444")
                )
            }
        }

        private fun toggleLike(feedItem: FeedItem) {
            val newLikeState = !feedItem.isLiked
            updateLikeButton(newLikeState)

            val currentCount = binding.tvLikeCount.text.toString().toIntOrNull() ?: 0
            val newCount = if (newLikeState) {
                currentCount + 1
            } else {
                currentCount - 1
            }
            binding.tvLikeCount.text = newCount.toString()
        }
    }
}