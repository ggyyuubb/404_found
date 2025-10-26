package com.example.wearther.community.vm

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.wearther.community.api.CommentRequest
import com.example.wearther.community.data.Comment
import com.example.wearther.community.data.User
import kotlinx.coroutines.launch

/* ==================== 댓글 관련 확장 함수 ==================== */

fun CommunityViewModel.loadComments(feedId: String) {
    viewModelScope.launch {
        try {
            val commentList = api.getComments(feedId)
            val currentComments = comments.value.toMutableMap()
            currentComments[feedId] = commentList
            updateComments(currentComments)
        } catch (e: Exception) {
            setErrorMessage("댓글을 불러오는데 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error loading comments", e)
        }
    }
}

fun CommunityViewModel.getCommentsForFeed(feedId: String): List<Comment> {
    return comments.value[feedId] ?: emptyList()
}

fun CommunityViewModel.addComment(feedId: String, content: String, userName: String? = null) {
    if (content.isBlank()) return

    viewModelScope.launch {
        setAddingComment(true)
        try {
            val authorName = userName ?: currentUser.value?.userName ?: "현재사용자"
            val request = CommentRequest(content, authorName)
            val newComment = api.addComment(feedId, request)

            val currentCommentsMap = comments.value.toMutableMap()
            val currentFeedComments = currentCommentsMap[feedId] ?: emptyList()
            currentCommentsMap[feedId] = currentFeedComments + newComment
            updateComments(currentCommentsMap)

            val currentFeeds = feeds.value
            updateFeeds(currentFeeds.map { feed ->
                if (feed.id == feedId) {
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

fun CommunityViewModel.deleteComment(feedId: String, commentId: Int) {
    viewModelScope.launch {
        try {
            api.deleteComment(feedId, commentId)

            val currentCommentsMap = comments.value.toMutableMap()
            val currentFeedComments = currentCommentsMap[feedId] ?: return@launch
            val updatedComments = currentFeedComments.filter { it.id != commentId }
            currentCommentsMap[feedId] = updatedComments
            updateComments(currentCommentsMap)

            val currentFeeds = feeds.value
            updateFeeds(currentFeeds.map { feed ->
                if (feed.id == feedId) {
                    feed.copy(commentCount = maxOf(0, feed.commentCount - 1))
                } else feed
            })
        } catch (e: Exception) {
            setErrorMessage("댓글 삭제에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error deleting comment", e)
        }
    }
}

fun CommunityViewModel.toggleCommentLike(feedId: String, commentId: Int) {
    viewModelScope.launch {
        try {
            val updatedComment = api.toggleCommentLike(feedId, commentId)
            val currentCommentsMap = comments.value.toMutableMap()
            val currentFeedComments = currentCommentsMap[feedId] ?: return@launch

            val updatedComments = currentFeedComments.map { comment ->
                if (comment.id == commentId) updatedComment else comment
            }
            currentCommentsMap[feedId] = updatedComments
            updateComments(currentCommentsMap)
        } catch (e: Exception) {
            setErrorMessage("좋아요 처리에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error toggling comment like", e)
        }
    }
}

/* ==================== 사용자 관련 확장 함수 ==================== */

fun CommunityViewModel.searchUsers(query: String) {
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
    return users.value.find { it.userId == userId }
}

fun CommunityViewModel.getUserByName(userName: String): User? {
    return users.value.find { it.userName == userName }
}

// ✅ 수정: getUserProfile API 사용
fun CommunityViewModel.loadUserProfile(userId: String) {
    viewModelScope.launch {
        setLoadingProfile(true)
        setErrorMessage(null)
        try {
            Log.d("CommunityViewModel", "📱 프로필 로드 시작: $userId")
            val response = api.getUserProfile(userId)

            if (response.isSuccessful) {
                val user = response.body()
                updateViewedUserProfile(user)
                Log.d("CommunityViewModel", "✅ 프로필 로드 성공: ${user?.userName}")
            } else {
                val errorBody = response.errorBody()?.string()
                setErrorMessage("프로필을 불러올 수 없습니다.")
                updateViewedUserProfile(null)
                Log.e("CommunityViewModel", "❌ 프로필 로드 실패: ${response.code()}, $errorBody")
            }
        } catch (e: Exception) {
            setErrorMessage("프로필 로드 중 오류가 발생했습니다: ${e.message}")
            updateViewedUserProfile(null)
            Log.e("CommunityViewModel", "❌ 프로필 로드 에러", e)
        } finally {
            setLoadingProfile(false)
        }
    }
}

// ✅ 수정: getUserPosts API 사용
fun CommunityViewModel.loadPostsForUser(userId: String) {
    viewModelScope.launch {
        setLoadingPosts(true)
        setErrorMessage(null)
        try {
            Log.d("CommunityViewModel", "📝 사용자 게시물 로드 시작: $userId")
            val response = api.getUserPosts(userId)

            if (response.isSuccessful) {
                val posts = response.body() ?: emptyList()
                updateUserPosts(posts)
                Log.d("CommunityViewModel", "✅ 사용자 게시물 로드 성공: ${posts.size}개")
            } else {
                val errorBody = response.errorBody()?.string()
                setErrorMessage("게시물을 불러올 수 없습니다.")
                updateUserPosts(emptyList())
                Log.e("CommunityViewModel", "❌ 게시물 로드 실패: ${response.code()}, $errorBody")
            }
        } catch (e: Exception) {
            setErrorMessage("게시물 로드 중 오류가 발생했습니다: ${e.message}")
            updateUserPosts(emptyList())
            Log.e("CommunityViewModel", "❌ 게시물 로드 에러", e)
        } finally {
            setLoadingPosts(false)
        }
    }
}

fun CommunityViewModel.toggleFollow(targetUserId: String) {
    viewModelScope.launch {
        try {
            val updatedUser = api.toggleFollow(targetUserId)

            updateUsers(users.value.map { user ->
                if (user.userId == targetUserId) updatedUser else user
            })

            if (viewedUserProfile.value?.userId == targetUserId) {
                updateViewedUserProfile(updatedUser)
            }

            val currentUserVal = currentUser.value
            updateCurrentUser(currentUserVal?.copy(
                followingCount = if (updatedUser.isFollowing)
                    (currentUserVal.followingCount ?: 0) + 1
                else
                    maxOf(0, (currentUserVal.followingCount ?: 0) - 1)
            ))

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
    updateSearchResults(emptyList())
}