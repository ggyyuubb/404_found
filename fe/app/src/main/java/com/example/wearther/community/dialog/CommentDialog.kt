package com.example.wearther.community.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.wearther.R
import com.example.wearther.community.adapter.CommentAdapter
import com.example.wearther.community.data.Comment
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.Reply
import com.example.wearther.community.data.UserProfile
import com.example.wearther.databinding.DialogCommentBinding

class CommentDialog : DialogFragment() {

    private var _binding: DialogCommentBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()
    private var currentFeedItem: FeedItem? = null
    private var currentUserProfile: UserProfile? = null

    private var onAddComment: ((String) -> Unit)? = null
    private var onAddReply: ((Long, String) -> Unit)? = null
    private var onUserProfileClick: ((UserProfile) -> Unit)? = null

    companion object {
        fun newInstance(
            feedItem: FeedItem,
            userProfile: UserProfile,
            onAddComment: (String) -> Unit,
            onAddReply: (Long, String) -> Unit,
            onUserProfileClick: (UserProfile) -> Unit
        ): CommentDialog {
            return CommentDialog().apply {
                currentFeedItem = feedItem
                currentUserProfile = userProfile
                this.onAddComment = onAddComment
                this.onAddReply = onAddReply
                this.onUserProfileClick = onUserProfileClick
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.CommentDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        setupRecyclerView()
        setupCommentInput()
        loadSampleComments()

        // 닫기 버튼
        binding.btnClose.setOnClickListener {
            dismiss()
        }

        // 다이얼로그 외부 클릭 시 닫기
        dialog?.setCanceledOnTouchOutside(true)
    }

    private fun setupHeader() {
        currentFeedItem?.let { feedItem ->
            // 사용자 프로필 클릭 이벤트
            binding.ivUserProfile.setOnClickListener {
                currentUserProfile?.let { profile ->
                    onUserProfileClick?.invoke(profile)
                }
            }

            // 프로필 이미지
            Glide.with(this)
                .load(feedItem.userProfileImage)
                .placeholder(R.drawable.gradient_profile_bg)
                .circleCrop()
                .into(binding.ivUserProfile)

            // 사용자명
            binding.tvUserName.text = feedItem.userName

            // 게시글 설명
            binding.tvPostDescription.text = feedItem.description
        }
    }

    private fun setupRecyclerView() {
        commentAdapter = CommentAdapter(
            comments = comments,
            onReplyClick = { commentId ->
                binding.etCommentInput.hint = "답글을 입력하세요..."
                binding.btnSend.tag = commentId
            },
            onUserProfileClick = { userProfile ->
                onUserProfileClick?.invoke(userProfile)
            }
        )

        binding.recyclerViewComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentAdapter
        }
    }

    private fun setupCommentInput() {
        binding.btnSend.setOnClickListener {
            val commentText = binding.etCommentInput.text.toString().trim()
            if (commentText.isNotEmpty()) {
                val commentId = binding.btnSend.tag as? Long
                if (commentId != null) {
                    // 답글 추가
                    addReply(commentId, commentText)
                    binding.etCommentInput.hint = "댓글을 입력하세요..."
                    binding.btnSend.tag = null
                } else {
                    // 댓글 추가
                    addComment(commentText)
                }
                binding.etCommentInput.text.clear()
            }
        }
    }

    private fun addComment(content: String) {
        currentUserProfile?.let { userProfile ->
            val newComment = Comment(
                id = System.currentTimeMillis(), // 임시 ID
                postId = currentFeedItem?.id ?: 0,
                userId = userProfile.userId,
                userName = userProfile.userName,
                userProfileImage = userProfile.userProfileImage,
                content = content,
                timestamp = "방금 전",
                likeCount = 0,
                isLiked = false,
                replies = emptyList()
            )

            commentAdapter.addComment(newComment)
            onAddComment?.invoke(content)
        }
    }

    private fun addReply(commentId: Long, content: String) {
        currentUserProfile?.let { userProfile ->
            val newReply = Reply(
                id = System.currentTimeMillis(), // 임시 ID
                commentId = commentId,
                userId = userProfile.userId,
                userName = userProfile.userName,
                userProfileImage = userProfile.userProfileImage,
                content = content,
                timestamp = "방금 전",
                likeCount = 0,
                isLiked = false
            )

            commentAdapter.addReply(commentId, newReply)
            onAddReply?.invoke(commentId, content)
        }
    }

    private fun loadSampleComments() {
        comments.clear()
        comments.addAll(getSampleComments())
        commentAdapter.notifyDataSetChanged()
    }

    private fun getSampleComments(): List<Comment> {
        return listOf(
            Comment(
                id = 1,
                postId = 1,
                userId = "user1",
                userName = "패션러버",
                userProfileImage = "https://example.com/profile1.jpg",
                content = "정말 예쁜 코디네요! 🌸",
                timestamp = "1시간 전",
                likeCount = 5,
                isLiked = false,
                replies = listOf(
                    Reply(
                        id = 1,
                        commentId = 1,
                        userId = "user2",
                        userName = "스타일리스트",
                        userProfileImage = "https://example.com/profile2.jpg",
                        content = "저도 추천해요! 👍",
                        timestamp = "30분 전",
                        likeCount = 2,
                        isLiked = false
                    )
                )
            ),
            Comment(
                id = 2,
                postId = 1,
                userId = "user3",
                userName = "코디마스터",
                userProfileImage = "https://example.com/profile3.jpg",
                content = "이런 날씨에 딱이네요!",
                timestamp = "2시간 전",
                likeCount = 3,
                isLiked = true,
                replies = emptyList()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}