package com.example.wearther.community.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wearther.databinding.FragmentCommunityBinding
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.PostSourceOption
import com.example.wearther.community.data.UserProfile
import com.example.wearther.community.dialog.AddFriendBottomSheetDialog
import com.example.wearther.community.dialog.AddPostOptionsBottomSheetDialog
import com.example.wearther.community.dialog.CommentDialog
import com.example.wearther.community.dialog.UserProfileDialog
import androidx.core.os.bundleOf
import android.content.Intent
import android.provider.MediaStore
import android.util.Log


class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedAdapter: FeedAdapter
    private val feedItems = mutableListOf<FeedItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        loadFeedData()
    }

    private fun setupRecyclerView() {
        feedAdapter = FeedAdapter(
            feedItems = feedItems,
            onItemClick = { feedItem ->
                handleFeedItemClick(feedItem)
            },
            onUserProfileClick = { userProfile ->
                showUserProfileDialog(userProfile)
            },
            onCommentClick = { feedItem ->
                showCommentDialog(feedItem)
            }
        )

        binding.recyclerViewFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = feedAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddFriend.setOnClickListener {
            showAddFriendDialog()
        }

        binding.fabAddPost.setOnClickListener {
            showAddPostOptionsDialog()
        }
    }

    private fun loadFeedData() {
        feedItems.clear()
        feedItems.addAll(getSampleFeedData())
        feedAdapter.notifyDataSetChanged()
    }

    private fun getSampleFeedData(): List<FeedItem> {
        return listOf(
            FeedItem(
                id = 1,
                userName = "먹선생",
                userProfileImage = "https://example.com/profile1.jpg",
                postTime = "2시간 전",
                outfitImages = listOf("https://example.com/outfit1.jpg"),
                description = "오늘 날씨에 딱 맞는 코디! 🌤️",
                temperature = "22°C",
                weather = "맑음",
                likeCount = 15,
                commentCount = 3,
                isLiked = false
            ),
            FeedItem(
                id = 2,
                userName = "고라니고은리",
                userProfileImage = "https://example.com/profile2.jpg",
                postTime = "4시간 전",
                outfitImages = listOf("https://example.com/outfit2.jpg", "https://example.com/outfit3.jpg"),
                description = "비 오는 날 코디 추천드려요 ☔",
                temperature = "18°C",
                weather = "비",
                likeCount = 28,
                commentCount = 7,
                isLiked = true
            ),
            FeedItem(
                id = 3,
                userName = "서제로",
                userProfileImage = "https://example.com/profile3.jpg",
                postTime = "1일 전",
                outfitImages = listOf("https://example.com/outfit4.jpg"),
                description = "주말 나들이룩 어떠세요?",
                temperature = "25°C",
                weather = "구름많음",
                likeCount = 42,
                commentCount = 12,
                isLiked = false
            )
        )
    }

    private fun handleFeedItemClick(feedItem: FeedItem) {
        // 피드 상세 화면으로 이동
        // findNavController().navigate(R.id.action_community_to_feed_detail, bundleOf("feedId" to feedItem.id))
    }

    private fun showUserProfileDialog(userProfile: UserProfile) {
        val dialog = UserProfileDialog.newInstance(userProfile) { userId ->
            // 친구 추가 처리
            handleAddFriend(userId)
        }
        dialog.show(childFragmentManager, "UserProfileDialog")
    }

    private fun showCommentDialog(feedItem: FeedItem) {
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

        val dialog = CommentDialog.newInstance(
            feedItem = feedItem,
            userProfile = userProfile,
            onAddComment = { commentText ->
                handleAddComment(feedItem.id, commentText)
            },
            onAddReply = { commentId, replyText ->
                handleAddReply(commentId, replyText)
            },
            onUserProfileClick = { profile ->
                showUserProfileDialog(profile)
            }
        )
        dialog.show(childFragmentManager, "CommentDialog")
    }

    private fun handleAddFriend(userId: String) {
        // 친구 추가 로직
        // TODO: API 호출하여 친구 추가
    }

    private fun handleAddComment(postId: Long, commentText: String) {
        // 댓글 추가 로직
        // TODO: API 호출하여 댓글 추가
    }

    private fun handleAddReply(commentId: Long, replyText: String) {
        // 답글 추가 로직
        // TODO: API 호출하여 답글 추가
    }

    private fun showAddFriendDialog() {
        val dialog = AddFriendBottomSheetDialog()
        dialog.show(childFragmentManager, "AddFriendDialog")
    }

    private fun showAddPostOptionsDialog() {
        val dialog = AddPostOptionsBottomSheetDialog { option ->
            handleAddPostOption(option)
        }
        dialog.show(childFragmentManager, "AddPostOptionsDialog")
    }

    private fun handleAddPostOption(option: PostSourceOption) {
        when (option) {
            PostSourceOption.ALBUM -> {
                // 앨범에서 사진 선택
                openImagePicker()
            }
            PostSourceOption.CLOSET -> {
                // 옷장에서 선택
                navigateToClosetPicker()
            }
            PostSourceOption.AI_RECOMMENDATION -> {
                // AI 추천 코디
                navigateToAIRecommendation()
            }
        }
    }

    private fun openImagePicker() {
        // 이미지 선택 인텐트
        // val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun navigateToClosetPicker() {
        // 옷장 선택 화면으로 이동
        // findNavController().navigate(R.id.action_community_to_closet_picker)
    }

    private fun navigateToAIRecommendation() {
        // AI 추천 화면으로 이동
        // findNavController().navigate(R.id.action_community_to_ai_recommendation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}