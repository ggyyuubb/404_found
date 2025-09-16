package com.example.wearther.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wearther.R
import com.example.wearther.community.data.Comment
import com.example.wearther.community.data.Reply
import com.example.wearther.community.data.UserProfile
import com.example.wearther.databinding.ItemCommentBinding
import com.example.wearther.databinding.ItemReplyBinding

class CommentAdapter(
    private val comments: MutableList<Comment>, // MutableList로 변경하여 데이터 변경 가능하도록
    private val onReplyClick: (Long) -> Unit,
    private val onUserProfileClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    // 댓글 추가 함수
    fun addComment(comment: Comment) {
        comments.add(comment)
        notifyItemInserted(comments.size - 1)
    }

    // 답글 추가 함수
    fun addReply(commentId: Long, reply: Reply) {
        val commentIndex = comments.indexOfFirst { it.id == commentId }
        if (commentIndex != -1) {
            val comment = comments[commentIndex]
            val newReplies = comment.replies.toMutableList()
            newReplies.add(reply)
            comments[commentIndex] = comment.copy(replies = newReplies)
            notifyItemChanged(commentIndex)
        }
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            with(binding) {
                // 사용자 프로필 정보
                tvUserName.text = comment.userName
                tvCommentContent.text = comment.content
                tvTimestamp.text = comment.timestamp
                tvLikeCount.text = comment.likeCount.toString()

                // 프로필 이미지
                Glide.with(ivUserProfile.context)
                    .load(comment.userProfileImage)
                    .placeholder(R.drawable.gradient_profile_bg)
                    .circleCrop()
                    .into(ivUserProfile)

                // 좋아요 버튼 상태
                btnLike.isSelected = comment.isLiked
                btnLike.setOnClickListener {
                    toggleLike(comment)
                }

                // 답글 버튼
                btnReply.setOnClickListener {
                    onReplyClick(comment.id)
                }

                // 사용자 프로필 클릭
                ivUserProfile.setOnClickListener {
                    val userProfile = UserProfile(
                        userId = comment.userId,
                        userName = comment.userName,
                        userEmail = "user@example.com", // 실제로는 API에서 가져와야 함
                        userProfileImage = comment.userProfileImage,
                        isFriend = false,
                        followerCount = 0,
                        followingCount = 0,
                        postCount = 0
                    )
                    onUserProfileClick(userProfile)
                }

                // 답글 목록 설정
                setupReplies(comment.replies)
            }
        }

        private fun toggleLike(comment: Comment) {
            // 댓글 객체의 실제 데이터도 업데이트
            val commentIndex = adapterPosition
            if (commentIndex != RecyclerView.NO_POSITION) {
                comments[commentIndex] = comment.copy(
                    isLiked = !comment.isLiked,
                    likeCount = if (!comment.isLiked) comment.likeCount + 1 else comment.likeCount - 1
                )

                // UI 업데이트
                binding.btnLike.isSelected = !binding.btnLike.isSelected
                val currentCount = binding.tvLikeCount.text.toString().toIntOrNull() ?: 0
                val newCount = if (binding.btnLike.isSelected) {
                    currentCount + 1
                } else {
                    currentCount - 1
                }
                binding.tvLikeCount.text = newCount.toString()
            }
        }

        private fun setupReplies(replies: List<Reply>) {
            binding.repliesContainer.removeAllViews()

            if (replies.isNotEmpty()) {
                binding.repliesContainer.visibility = android.view.View.VISIBLE

                replies.forEach { reply ->
                    val replyBinding = ItemReplyBinding.inflate(
                        LayoutInflater.from(binding.root.context),
                        binding.repliesContainer,
                        false
                    )

                    // 답글 정보 설정
                    replyBinding.tvUserName.text = reply.userName
                    replyBinding.tvReplyContent.text = reply.content
                    replyBinding.tvTimestamp.text = reply.timestamp
                    replyBinding.tvLikeCount.text = reply.likeCount.toString()

                    // 프로필 이미지
                    Glide.with(replyBinding.ivUserProfile.context)
                        .load(reply.userProfileImage)
                        .placeholder(R.drawable.gradient_profile_bg)
                        .circleCrop()
                        .into(replyBinding.ivUserProfile)

                    // 좋아요 버튼
                    replyBinding.btnLike.isSelected = reply.isLiked
                    replyBinding.btnLike.setOnClickListener {
                        toggleReplyLike(reply, replyBinding)
                    }

                    // 사용자 프로필 클릭
                    replyBinding.ivUserProfile.setOnClickListener {
                        val userProfile = UserProfile(
                            userId = reply.userId,
                            userName = reply.userName,
                            userEmail = "user@example.com",
                            userProfileImage = reply.userProfileImage,
                            isFriend = false,
                            followerCount = 0,
                            followingCount = 0,
                            postCount = 0
                        )
                        onUserProfileClick(userProfile)
                    }

                    binding.repliesContainer.addView(replyBinding.root)
                }
            } else {
                binding.repliesContainer.visibility = android.view.View.GONE
            }
        }

        private fun toggleReplyLike(reply: Reply, replyBinding: ItemReplyBinding) {
            replyBinding.btnLike.isSelected = !replyBinding.btnLike.isSelected
            val currentCount = replyBinding.tvLikeCount.text.toString().toIntOrNull() ?: 0
            val newCount = if (replyBinding.btnLike.isSelected) {
                currentCount + 1
            } else {
                currentCount - 1
            }
            replyBinding.tvLikeCount.text = newCount.toString()
        }
    }
}