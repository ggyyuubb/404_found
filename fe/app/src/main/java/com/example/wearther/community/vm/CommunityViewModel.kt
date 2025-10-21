package com.example.wearther.community.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.Comment
import com.example.wearther.community.data.User
import com.example.wearther.community.data.FollowRelation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommunityViewModel : ViewModel() {

    private val _feeds = MutableStateFlow<List<FeedItem>>(emptyList())
    val feeds: StateFlow<List<FeedItem>> = _feeds.asStateFlow()

    private val _comments = MutableStateFlow<Map<Int, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<Int, List<Comment>>> = _comments.asStateFlow()

    private val _isAddingComment = MutableStateFlow(false)
    val isAddingComment: StateFlow<Boolean> = _isAddingComment.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // 샘플 사용자 데이터
    private val sampleUsers = listOf(
        User(
            userId = "user1",
            userName = "김민수",
            bio = "패션을 사랑하는 직장인 👔",
            followerCount = 142,
            followingCount = 89,
            postCount = 3,
            isFollowing = false
        ),
        User(
            userId = "user2",
            userName = "이지은",
            bio = "일상 속 소소한 패션 기록 ✨",
            followerCount = 328,
            followingCount = 156,
            postCount = 1,
            isFollowing = true
        ),
        User(
            userId = "user3",
            userName = "박서준",
            bio = "데일리룩 공유합니다 📸",
            followerCount = 521,
            followingCount = 234,
            postCount = 1,
            isFollowing = false
        ),
        User(
            userId = "user4",
            userName = "최유나",
            bio = "빈티지 패션 애호가 🎨",
            followerCount = 89,
            followingCount = 67,
            postCount = 0,
            isFollowing = false
        ),
        User(
            userId = "user5",
            userName = "정민호",
            bio = "심플한 스타일을 추구합니다",
            followerCount = 234,
            followingCount = 178,
            postCount = 0,
            isFollowing = true
        )
    )

    private val currentUserData = User(
        userId = "current_user",
        userName = "현재사용자",
        bio = "나의 스타일을 공유합니다 ✨",
        followerCount = 45,
        followingCount = 2,  // 이지은, 정민호 팔로잉 중
        postCount = 0,
        isFollowing = false
    )

    private val sampleComments = mapOf(
        1 to listOf(
            Comment(
                id = 1,
                feedId = 1,
                userId = "user1",
                userName = "박지민",
                content = "정말 예쁜 코디네요! 저도 따라해봐야겠어요 👍",
                timestamp = "1시간 전",
                likeCount = 3,
                isLiked = false
            ),
            Comment(
                id = 2,
                feedId = 1,
                userId = "user2",
                userName = "최수빈",
                content = "가을 느낌 물씬 나네요!",
                timestamp = "30분 전",
                likeCount = 1,
                isLiked = true
            ),
            Comment(
                id = 3,
                feedId = 1,
                userId = "user3",
                userName = "김하늘",
                content = "저도 이 조합 좋아해요 ㅎㅎ",
                timestamp = "15분 전",
                likeCount = 0,
                isLiked = false
            )
        ),
        2 to listOf(
            Comment(
                id = 4,
                feedId = 2,
                userId = "user4",
                userName = "이수현",
                content = "첫 출근 축하드려요! 화이팅!",
                timestamp = "3시간 전",
                likeCount = 5,
                isLiked = true
            ),
            Comment(
                id = 5,
                feedId = 2,
                userId = "user5",
                userName = "정민호",
                content = "옷 어디서 구매하셨어요?",
                timestamp = "2시간 전",
                likeCount = 2,
                isLiked = false
            )
        ),
        3 to listOf(
            Comment(
                id = 6,
                feedId = 3,
                userId = "user6",
                userName = "윤서아",
                content = "완전 세련됐어요!",
                timestamp = "20시간 전",
                likeCount = 8,
                isLiked = false
            ),
            Comment(
                id = 7,
                feedId = 3,
                userId = "user7",
                userName = "강태양",
                content = "데이트룩 레전드...👏",
                timestamp = "18시간 전",
                likeCount = 4,
                isLiked = true
            ),
            Comment(
                id = 8,
                feedId = 3,
                userId = "user8",
                userName = "홍다은",
                content = "참고할게요!",
                timestamp = "15시간 전",
                likeCount = 1,
                isLiked = false
            )
        )
    )

    init {
        _comments.value = sampleComments
        _users.value = sampleUsers
        _currentUser.value = currentUserData
    }

    // ========== 피드 관련 ==========

    fun loadFeeds() {
        _feeds.value = listOf(
            FeedItem(
                id = 1,
                userName = "김민수",
                postTime = "2시간 전",
                description = "오늘 날씨 완전 좋아요! 가을 코디 추천합니다 🍂",
                temperature = "18°C",
                weather = "맑음",
                likeCount = 24,
                commentCount = 3,
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
                commentCount = 2,
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
                commentCount = 3,
                isLiked = false
            )
        )
    }

    fun toggleLike(feedId: Int) {
        _feeds.value = _feeds.value.map { feed ->
            if (feed.id == feedId) {
                feed.copy(
                    isLiked = !feed.isLiked,
                    likeCount = if (feed.isLiked) feed.likeCount - 1 else feed.likeCount + 1
                )
            } else feed
        }
    }

    /**
     * 게시글 추가 - 현재 사용자 이름 자동 사용
     */
    fun addFeed(
        description: String,
        temperature: String,
        weather: String
    ) {
        val currentUserName = _currentUser.value?.userName ?: "현재사용자"
        val newId = (_feeds.value.maxOfOrNull { it.id } ?: 0) + 1

        val newFeed = FeedItem(
            id = newId,
            userName = currentUserName,
            postTime = "방금 전",
            description = description,
            temperature = temperature,
            weather = weather,
            likeCount = 0,
            commentCount = 0,
            isLiked = false
        )

        _feeds.value = listOf(newFeed) + _feeds.value

        // 현재 사용자의 게시글 수 증가
        _currentUser.value = _currentUser.value?.copy(
            postCount = (_currentUser.value?.postCount ?: 0) + 1
        )
    }

    fun deleteFeed(feedId: Int) {
        _feeds.value = _feeds.value.filter { it.id != feedId }

        _comments.value = _comments.value.toMutableMap().apply {
            remove(feedId)
        }

        // 현재 사용자의 게시글 수 감소
        _currentUser.value = _currentUser.value?.copy(
            postCount = maxOf(0, (_currentUser.value?.postCount ?: 0) - 1)
        )
    }

    // ========== 댓글 관련 ==========

    /**
     * 특정 피드의 댓글 가져오기 (실시간 업데이트)
     */
    fun getCommentsForFeed(feedId: Int): List<Comment> {
        return _comments.value[feedId] ?: emptyList()
    }

    /**
     * 댓글 추가
     */
    fun addComment(feedId: Int, content: String, userName: String? = null) {
        if (content.isBlank()) return

        _isAddingComment.value = true

        val currentComments = _comments.value[feedId] ?: emptyList()
        val newCommentId = (currentComments.maxOfOrNull { it.id } ?: 0) + 1
        val authorName = userName ?: _currentUser.value?.userName ?: "현재사용자"

        val newComment = Comment(
            id = newCommentId,
            feedId = feedId,
            userId = _currentUser.value?.userId ?: "current_user",
            userName = authorName,
            content = content,
            timestamp = "방금 전",
            likeCount = 0,
            isLiked = false
        )

        val updatedComments = currentComments + newComment
        _comments.value = _comments.value.toMutableMap().apply {
            put(feedId, updatedComments)
        }

        // 피드의 댓글 수 증가
        _feeds.value = _feeds.value.map { feed ->
            if (feed.id == feedId) {
                feed.copy(commentCount = feed.commentCount + 1)
            } else feed
        }

        _isAddingComment.value = false
    }

    /**
     * 댓글 삭제
     */
    fun deleteComment(feedId: Int, commentId: Int) {
        val currentComments = _comments.value[feedId] ?: return
        val updatedComments = currentComments.filter { it.id != commentId }

        _comments.value = _comments.value.toMutableMap().apply {
            put(feedId, updatedComments)
        }

        // 피드의 댓글 수 감소
        _feeds.value = _feeds.value.map { feed ->
            if (feed.id == feedId) {
                feed.copy(commentCount = maxOf(0, feed.commentCount - 1))
            } else feed
        }
    }

    /**
     * 댓글 좋아요 토글
     */
    fun toggleCommentLike(feedId: Int, commentId: Int) {
        val currentComments = _comments.value[feedId] ?: return

        val updatedComments = currentComments.map { comment ->
            if (comment.id == commentId) {
                comment.copy(
                    isLiked = !comment.isLiked,
                    likeCount = if (comment.isLiked)
                        maxOf(0, comment.likeCount - 1)
                    else
                        comment.likeCount + 1
                )
            } else comment
        }

        _comments.value = _comments.value.toMutableMap().apply {
            put(feedId, updatedComments)
        }
    }

    // ========== 사용자 관련 ==========

    /**
     * 사용자 검색
     */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        val results = _users.value.filter { user ->
            user.userName.contains(query, ignoreCase = true)
        }

        _searchResults.value = results
    }

    /**
     * 사용자 ID로 사용자 정보 가져오기
     */
    fun getUserById(userId: String): User? {
        return _users.value.find { it.userId == userId }
    }

    /**
     * 사용자 이름으로 사용자 정보 가져오기
     */
    fun getUserByName(userName: String): User? {
        return _users.value.find { it.userName == userName }
    }

    /**
     * 팔로우/언팔로우 토글
     */
    fun toggleFollow(targetUserId: String) {
        _users.value = _users.value.map { user ->
            if (user.userId == targetUserId) {
                val newFollowingState = !user.isFollowing
                user.copy(
                    isFollowing = newFollowingState,
                    followerCount = if (newFollowingState)
                        user.followerCount + 1
                    else
                        maxOf(0, user.followerCount - 1)
                )
            } else user
        }

        // 현재 사용자의 팔로잉 수 업데이트
        val targetUser = _users.value.find { it.userId == targetUserId }
        if (targetUser != null) {
            _currentUser.value = _currentUser.value?.copy(
                followingCount = if (targetUser.isFollowing)
                    (_currentUser.value?.followingCount ?: 0) + 1
                else
                    maxOf(0, (_currentUser.value?.followingCount ?: 0) - 1)
            )
        }

        // 검색 결과도 업데이트
        _searchResults.value = _searchResults.value.map { user ->
            if (user.userId == targetUserId) {
                _users.value.find { it.userId == targetUserId } ?: user
            } else user
        }
    }

    /**
     * 모든 사용자 목록 가져오기
     */
    fun getAllUsers(): List<User> {
        return _users.value
    }

    /**
     * 팔로잉 중인 사용자 목록
     */
    fun getFollowingUsers(): List<User> {
        return _users.value.filter { it.isFollowing }
    }

    /**
     * 검색 결과 초기화
     */
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    /**
     * 현재 사용자 정보 가져오기
     */
    fun getCurrentUser(): User? {
        return _currentUser.value
    }

    companion object {
        fun provide(context: Context): CommunityViewModel {
            return CommunityViewModel()
        }
    }
}