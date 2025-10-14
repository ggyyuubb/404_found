package com.example.wearther.community.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearther.community.data.FeedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {

    companion object {
        private const val TAG = "CommunityViewModel"

        fun provide(context: Context): CommunityViewModel {
            Log.d(TAG, "Creating CommunityViewModel instance")
            return CommunityViewModel()
        }
    }

    private val _feeds = MutableStateFlow<List<FeedItem>>(emptyList())
    val feeds: StateFlow<List<FeedItem>> = _feeds.asStateFlow()

    init {
        Log.d(TAG, "CommunityViewModel initialized")
    }

    fun loadFeeds() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading feeds started")

                val feedList = listOf(
                    FeedItem(
                        id = 1,
                        userName = "김민수",
                        postTime = "2시간 전",
                        description = "오늘 날씨 완전 좋아요! 가을 코디 추천합니다 🍂",
                        temperature = "18°C",
                        weather = "맑음",
                        likeCount = 24,
                        commentCount = 5,
                        isLiked = false
                    ),
                    FeedItem(
                        id = 2,
                        userName = "이지은",
                        postTime = "5시간 전",
                        description = "첫 출근 코디! 긴장되네요 ㅎㅎ",
                        temperature = "20°C",
                        weather = "구름 조금",
                        likeCount = 42,
                        commentCount = 12,
                        isLiked = true
                    ),
                    FeedItem(
                        id = 3,
                        userName = "박서준",
                        postTime = "1일 전",
                        description = "주말 데이트룩 어때요?",
                        temperature = "22°C",
                        weather = "맑음",
                        likeCount = 89,
                        commentCount = 23,
                        isLiked = false
                    )
                )

                _feeds.value = feedList
                Log.d(TAG, "Feeds loaded successfully: ${feedList.size} items")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading feeds", e)
                Log.e(TAG, "Error message: ${e.message}")
                Log.e(TAG, "Error stacktrace: ${e.stackTraceToString()}")
            }
        }
    }

    fun toggleLike(feedId: Int) {
        try {
            Log.d(TAG, "Toggling like for feed ID: $feedId")

            _feeds.value = _feeds.value.map { feed ->
                if (feed.id == feedId) {
                    val newLiked = !feed.isLiked
                    val newCount = if (feed.isLiked) feed.likeCount - 1 else feed.likeCount + 1

                    Log.d(TAG, "Feed $feedId: isLiked=$newLiked, likeCount=$newCount")

                    feed.copy(
                        isLiked = newLiked,
                        likeCount = newCount
                    )
                } else feed
            }

            Log.d(TAG, "Like toggled successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like for feed $feedId", e)
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "Error stacktrace: ${e.stackTraceToString()}")
        }
    }
}