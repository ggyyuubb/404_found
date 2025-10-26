package com.example.wearther.community.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.wearther.community.api.CommentRequest // 필요한 import 확인
import com.example.wearther.community.data.Comment
import com.example.wearther.community.data.User
import kotlinx.coroutines.launch

/* ==================== 댓글 관련 확장 함수 ==================== */
// (댓글 관련 함수는 feedId가 String으로 잘 수정되어 있으므로 변경 없음)

fun CommunityViewModel.loadComments(feedId: String) {
    viewModelScope.launch {
        try {
            val commentList = api.getComments(feedId) // feedId는 String
            val currentComments = comments.value.toMutableMap()
            currentComments[feedId] = commentList // String key 사용
            updateComments(currentComments)
        } catch (e: Exception) {
            setErrorMessage("댓글을 불러오는데 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error loading comments", e)
        }
    }
}

fun CommunityViewModel.getCommentsForFeed(feedId: String): List<Comment> {
    return comments.value[feedId] ?: emptyList() // String key 사용
}


fun CommunityViewModel.addComment(feedId: String, content: String, userName: String? = null) {
    if (content.isBlank()) return

    viewModelScope.launch {
        setAddingComment(true)
        try {
            val authorName = userName ?: currentUser.value?.userName ?: "현재사용자"
            val request = CommentRequest(content, authorName)
            val newComment = api.addComment(feedId, request) // feedId는 String

            val currentCommentsMap = comments.value.toMutableMap()
            val currentFeedComments = currentCommentsMap[feedId] ?: emptyList() // String key 사용
            currentCommentsMap[feedId] = currentFeedComments + newComment // String key 사용
            updateComments(currentCommentsMap)

            val currentFeeds = feeds.value
            updateFeeds(currentFeeds.map { feed ->
                if (feed.id == feedId) { // String 비교
                    feed.copy(commentCount = feed.commentCount + 1)
                } else feed
            })
        } catch (e: Exception) {
            setErrorMessage("댓글 작성에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error adding comment", e)
        } finally {
            setAddingComment(false)
        }
    }
}

fun CommunityViewModel.deleteComment(feedId: String, commentId: Int) { // commentId는 Int 유지 가정
    viewModelScope.launch {
        try {
            api.deleteComment(feedId, commentId) // feedId는 String

            val currentCommentsMap = comments.value.toMutableMap()
            val currentFeedComments = currentCommentsMap[feedId] ?: return@launch // String key 사용
            val updatedComments = currentFeedComments.filter { it.id != commentId } // commentId는 Int 비교
            currentCommentsMap[feedId] = updatedComments // String key 사용
            updateComments(currentCommentsMap)

            val currentFeeds = feeds.value
            updateFeeds(currentFeeds.map { feed ->
                if (feed.id == feedId) { // String 비교
                    feed.copy(commentCount = maxOf(0, feed.commentCount - 1))
                } else feed
            })
        } catch (e: Exception) {
            setErrorMessage("댓글 삭제에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error deleting comment", e)
        }
    }
}

fun CommunityViewModel.toggleCommentLike(feedId: String, commentId: Int) { // commentId는 Int 유지 가정
    viewModelScope.launch {
        try {
            val updatedComment = api.toggleCommentLike(feedId, commentId) // feedId는 String
            val currentCommentsMap = comments.value.toMutableMap()
            val currentFeedComments = currentCommentsMap[feedId] ?: return@launch // String key 사용

            val updatedComments = currentFeedComments.map { comment ->
                if (comment.id == commentId) updatedComment else comment // commentId는 Int 비교
            }
            currentCommentsMap[feedId] = updatedComments // String key 사용
            updateComments(currentCommentsMap)
        } catch (e: Exception) {
            setErrorMessage("좋아요 처리에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error toggling comment like", e)
        }
    }
}


/* ==================== 사용자 관련 확장 함수 ==================== */

fun CommunityViewModel.searchUsers(query: String) {
    // (기존 코드 동일)
    if (query.isBlank()) {
        updateSearchResults(emptyList())
        return
    }
    viewModelScope.launch {
        try {
            val results = api.searchUsers(query)
            updateSearchResults(results)
        } catch (e: Exception) {
            setErrorMessage("사용자 검색에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error searching users", e)
        }
    }
}

fun CommunityViewModel.getUserById(userId: String): User? {
    // (기존 코드 동일 - _users 상태에서 찾는 로직)
    return users.value.find { it.userId == userId }
}

fun CommunityViewModel.getUserByName(userName: String): User? {
    // (기존 코드 동일 - _users 상태에서 찾는 로직)
    return users.value.find { it.userName == userName }
}

// [ 💡 1. 수정: _viewedUserProfile 업데이트 하도록 변경 ]
fun CommunityViewModel.loadUserProfile(userId: String) {
    viewModelScope.launch {
        setLoadingProfile(true) // 프로필 로딩 시작
        setErrorMessage(null)
        try {
            val user = api.getUserById(userId) // 백엔드에서 사용자 정보 가져오기
            updateViewedUserProfile(user) // 가져온 정보로 _viewedUserProfile 상태 업데이트
        } catch (e: Exception) {
            setErrorMessage("사용자 정보를 불러오는데 실패했습니다: ${e.message}")
            updateViewedUserProfile(null) // 실패 시 null로 설정
            Log.e("CommunityViewModel", "Error loading user profile", e)
        } finally {
            setLoadingProfile(false) // 프로필 로딩 종료
        }
    }
}

// [ 💡 2. 추가: 특정 사용자의 게시글 로드 함수 ]
// (주의: 이 함수가 작동하려면 백엔드에 /community/users/{userId}/posts 같은 API가 필요합니다)
fun CommunityViewModel.loadPostsForUser(userId: String) {
    viewModelScope.launch {
        setLoadingPosts(true) // 게시글 로딩 시작
        setErrorMessage(null)
        try {
            // TODO: 백엔드 API 구현 후 아래 코드 활성화 및 수정
            // 예시: val posts = api.getPostsByUserId(userId)
            // updateUserPosts(posts)

            // --- 임시 코드 (실제 API 연결 전까지 빈 목록 표시) ---
            updateUserPosts(emptyList())
            Log.d("CommunityViewModel", "Loaded posts for user $userId (placeholder)")
            // --- 임시 코드 끝 ---

        } catch (e: Exception) {
            setErrorMessage("사용자 게시글을 불러오는데 실패했습니다: ${e.message}")
            updateUserPosts(emptyList()) // 실패 시 빈 목록
            Log.e("CommunityViewModel", "Error loading posts for user", e)
        } finally {
            setLoadingPosts(false) // 게시글 로딩 종료
        }
    }
}


fun CommunityViewModel.toggleFollow(targetUserId: String) {
    // (기존 코드 동일)
    viewModelScope.launch {
        try {
            val updatedUser = api.toggleFollow(targetUserId)
            // _users 상태 업데이트 (필요시)
            updateUsers(users.value.map { user ->
                if (user.userId == targetUserId) updatedUser else user
            })

            if (viewedUserProfile.value?.userId == targetUserId) { // 상태 읽기는 public val 사용
                updateViewedUserProfile(updatedUser)
            }
            val currentUserVal = currentUser.value
            updateCurrentUser(currentUserVal?.copy(
                followingCount = if (updatedUser.isFollowing)
                    (currentUserVal.followingCount ?: 0) + 1
                else
                    maxOf(0, (currentUserVal.followingCount ?: 0) - 1)
            ))
            // _searchResults 상태 업데이트 (검색 결과에 있다면)
            updateSearchResults(searchResults.value.map { user ->
                if (user.userId == targetUserId) updatedUser else user
            })
        } catch (e: Exception) {
            setErrorMessage("팔로우 처리에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error toggling follow", e)
        }
    }
}

fun CommunityViewModel.clearSearchResults() {
    // (기존 코드 동일)
    updateSearchResults(emptyList())
}