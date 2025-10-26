package com.example.wearther.community.vm

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wearther.community.api.CommentRequest
import com.example.wearther.community.api.CommunityApi
import com.example.wearther.community.api.CreateFeedRequest
import com.example.wearther.community.data.Comment
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CommunityViewModel(private val context: Context) : ViewModel() {

    private val api: CommunityApi = Retrofit.Builder()
        .baseUrl("http://10.79.160.121/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CommunityApi::class.java)

    // StateFlow 선언
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /* ==================== 피드 관련 ==================== */

    fun loadFeeds() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val feedList = api.getFeeds()
                _feeds.value = feedList
                Log.d("CommunityViewModel", "Loaded ${feedList.size} feeds")
            } catch (e: Exception) {
                _errorMessage.value = "피드를 불러오는데 실패했습니다: ${e.message}"
                Log.e("CommunityViewModel", "Error loading feeds", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(feedId: Int) {
        viewModelScope.launch {
            try {
                val updatedFeed = api.toggleLike(feedId)
                _feeds.value = _feeds.value.map { feed ->
                    if (feed.id == feedId) updatedFeed else feed
                }
            } catch (e: Exception) {
                _errorMessage.value = "좋아요 처리에 실패했습니다"
                Log.e("CommunityViewModel", "Error toggling like", e)
            }
        }
    }

    fun addFeed(description: String, temperature: String, weather: String, imageUrl: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CreateFeedRequest(description, temperature, weather, imageUrl)
                val newFeed = api.createFeed(request)
                _feeds.value = listOf(newFeed) + _feeds.value
                Log.d("CommunityViewModel", "Feed created successfully")
            } catch (e: Exception) {
                _errorMessage.value = "게시글 작성에 실패했습니다: ${e.message}"
                Log.e("CommunityViewModel", "Error creating feed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFeedWithImage(
        context: Context,
        description: String,
        temperature: String,
        weather: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val descBody = description.toPlain()
                val tempBody = temperature.toPlain()
                val weatherBody = weather.toPlain()

                val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
                    val cr = context.contentResolver
                    val mime = cr.getType(uri) ?: "image/jpeg"
                    val bytes = cr.openInputStream(uri)!!.use { it.readBytes() }
                    val req = bytes.toRequestBody(mime.toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("image", "upload.jpg", req)
                }

                val newFeed = api.createFeedWithImage(
                    image = imagePart,
                    description = descBody,
                    temperature = tempBody,
                    weather = weatherBody
                )

                _feeds.value = listOf(newFeed) + _feeds.value
                Log.d("CommunityViewModel", "Feed created successfully (with image)")
            } catch (e: Exception) {
                _errorMessage.value = "게시글(이미지 포함) 작성에 실패했습니다: ${e.message}"
                Log.e("CommunityViewModel", "Error creating feed (multipart)", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteFeed(feedId: Int) {
        viewModelScope.launch {
            try {
                api.deleteFeed(feedId)
                _feeds.value = _feeds.value.filter { it.id != feedId }
                _comments.value = _comments.value.toMutableMap().apply {
                    remove(feedId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "게시글 삭제에 실패했습니다"
                Log.e("CommunityViewModel", "Error deleting feed", e)
            }
        }
    }

    /* ==================== 댓글 관련 ==================== */

    fun loadComments(feedId: Int) {
        viewModelScope.launch {
            try {
                val commentList = api.getComments(feedId)
                _comments.value = _comments.value.toMutableMap().apply {
                    put(feedId, commentList)
                }
            } catch (e: Exception) {
                _errorMessage.value = "댓글을 불러오는데 실패했습니다"
                Log.e("CommunityViewModel", "Error loading comments", e)
            }
        }
    }

    fun getCommentsForFeed(feedId: Int): List<Comment> {
        return _comments.value[feedId] ?: emptyList()
    }

    fun addComment(feedId: Int, content: String, userName: String? = null) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _isAddingComment.value = true
            try {
                val authorName = userName ?: _currentUser.value?.userName ?: "현재사용자"
                val request = CommentRequest(content, authorName)
                val newComment = api.addComment(feedId, request)

                val currentComments = _comments.value[feedId] ?: emptyList()
                _comments.value = _comments.value.toMutableMap().apply {
                    put(feedId, currentComments + newComment)
                }

                _feeds.value = _feeds.value.map { feed ->
                    if (feed.id == feedId) {
                        feed.copy(commentCount = feed.commentCount + 1)
                    } else feed
                }
            } catch (e: Exception) {
                _errorMessage.value = "댓글 작성에 실패했습니다"
                Log.e("CommunityViewModel", "Error adding comment", e)
            } finally {
                _isAddingComment.value = false
            }
        }
    }

    fun deleteComment(feedId: Int, commentId: Int) {
        viewModelScope.launch {
            try {
                api.deleteComment(feedId, commentId)

                val currentComments = _comments.value[feedId] ?: return@launch
                val updatedComments = currentComments.filter { it.id != commentId }

                _comments.value = _comments.value.toMutableMap().apply {
                    put(feedId, updatedComments)
                }

                _feeds.value = _feeds.value.map { feed ->
                    if (feed.id == feedId) {
                        feed.copy(commentCount = maxOf(0, feed.commentCount - 1))
                    } else feed
                }
            } catch (e: Exception) {
                _errorMessage.value = "댓글 삭제에 실패했습니다"
                Log.e("CommunityViewModel", "Error deleting comment", e)
            }
        }
    }

    fun toggleCommentLike(feedId: Int, commentId: Int) {
        viewModelScope.launch {
            try {
                val updatedComment = api.toggleCommentLike(feedId, commentId)
                val currentComments = _comments.value[feedId] ?: return@launch

                val updatedComments = currentComments.map { comment ->
                    if (comment.id == commentId) updatedComment else comment
                }

                _comments.value = _comments.value.toMutableMap().apply {
                    put(feedId, updatedComments)
                }
            } catch (e: Exception) {
                _errorMessage.value = "좋아요 처리에 실패했습니다"
                Log.e("CommunityViewModel", "Error toggling comment like", e)
            }
        }
    }

    /* ==================== 사용자 관련 ==================== */

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val results = api.searchUsers(query)
                _searchResults.value = results
            } catch (e: Exception) {
                _errorMessage.value = "사용자 검색에 실패했습니다"
                Log.e("CommunityViewModel", "Error searching users", e)
            }
        }
    }

    fun getUserById(userId: String): User? {
        return _users.value.find { it.userId == userId }
    }

    fun getUserByName(userName: String): User? {
        return _users.value.find { it.userName == userName }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val user = api.getUserById(userId)
                _users.value = _users.value.toMutableList().apply {
                    removeIf { it.userId == userId }
                    add(user)
                }
            } catch (e: Exception) {
                _errorMessage.value = "사용자 정보를 불러오는데 실패했습니다"
                Log.e("CommunityViewModel", "Error loading user profile", e)
            }
        }
    }

    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            try {
                val updatedUser = api.toggleFollow(targetUserId)

                _users.value = _users.value.map { user ->
                    if (user.userId == targetUserId) updatedUser else user
                }

                _currentUser.value = _currentUser.value?.copy(
                    followingCount = if (updatedUser.isFollowing)
                        (_currentUser.value?.followingCount ?: 0) + 1
                    else
                        maxOf(0, (_currentUser.value?.followingCount ?: 0) - 1)
                )

                _searchResults.value = _searchResults.value.map { user ->
                    if (user.userId == targetUserId) updatedUser else user
                }
            } catch (e: Exception) {
                _errorMessage.value = "팔로우 처리에 실패했습니다"
                Log.e("CommunityViewModel", "Error toggling follow", e)
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    companion object {
        fun provide(context: Context): CommunityViewModel {
            return CommunityViewModel(context)
        }
    }
}

/* ==================== 유틸리티 함수 ==================== */

private fun String.toPlain(): RequestBody =
    this.toRequestBody("text/plain".toMediaType())