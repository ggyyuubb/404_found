package com.example.wearther.community.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.wearther.community.api.CommunityApi
import com.example.wearther.community.data.Comment // Import Comment
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.User
import com.example.wearther.remote.RetrofitProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class CommunityViewModel(val context: Context) : ViewModel() {

    internal val api: CommunityApi = RetrofitProvider
        .getInstance(context)
        .create(CommunityApi::class.java)

    // --- 피드 관련 상태 ---
    private val _feeds = MutableStateFlow<List<FeedItem>>(emptyList())
    val feeds: StateFlow<List<FeedItem>> = _feeds.asStateFlow()
    internal fun updateFeeds(newFeeds: List<FeedItem>) { _feeds.value = newFeeds }

    // --- 댓글 관련 상태 ---
    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap()) // Key: String (feedId)
    val comments: StateFlow<Map<String, List<Comment>>> = _comments.asStateFlow()
    internal fun updateComments(newCommentsMap: Map<String, List<Comment>>) { _comments.value = newCommentsMap }

    private val _isAddingComment = MutableStateFlow(false)
    val isAddingComment: StateFlow<Boolean> = _isAddingComment.asStateFlow()
    internal fun setAddingComment(isAdding: Boolean) { _isAddingComment.value = isAdding }

    // --- 사용자 목록/검색 상태 ---
    private val _users = MutableStateFlow<List<User>>(emptyList()) // 전체 사용자 목록 (필요시 사용)
    val users: StateFlow<List<User>> = _users.asStateFlow()
    internal fun updateUsers(newUsers: List<User>) { _users.value = newUsers }

    private val _searchResults = MutableStateFlow<List<User>>(emptyList()) // 사용자 검색 결과
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()
    internal fun updateSearchResults(results: List<User>) { _searchResults.value = results }

    private val _currentUser = MutableStateFlow<User?>(null) // 현재 로그인한 사용자 정보 (필요시 사용)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    internal fun updateCurrentUser(user: User?) { _currentUser.value = user }

    // --- 공통 상태 ---
    private val _isLoading = MutableStateFlow(false) // 일반 로딩 상태 (예: 전체 피드 로딩)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    internal fun setLoading(loading: Boolean) { _isLoading.value = loading }

    private val _errorMessage = MutableStateFlow<String?>(null) // 에러 메시지
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    internal fun setErrorMessage(message: String?) { _errorMessage.value = message }

    private val _uploadSuccessEvent = MutableSharedFlow<Boolean>() // 게시글 업로드 성공 이벤트
    val uploadSuccessEvent: SharedFlow<Boolean> = _uploadSuccessEvent.asSharedFlow()
    internal suspend fun emitUploadSuccess() { _uploadSuccessEvent.emit(true) }


    // --- [ 💡 UserProfileScreen을 위한 상태 변수 추가 💡 ] ---
    private val _viewedUserProfile = MutableStateFlow<User?>(null) // 현재 보고 있는 프로필
    val viewedUserProfile: StateFlow<User?> = _viewedUserProfile.asStateFlow()
    internal fun updateViewedUserProfile(user: User?) { _viewedUserProfile.value = user } // Helper

    private val _userPosts = MutableStateFlow<List<FeedItem>>(emptyList()) // 해당 유저의 게시글 목록
    val userPosts: StateFlow<List<FeedItem>> = _userPosts.asStateFlow()
    internal fun updateUserPosts(posts: List<FeedItem>) { _userPosts.value = posts } // Helper

    private val _isLoadingProfile = MutableStateFlow(false) // 프로필 로딩 상태
    val isLoadingProfile: StateFlow<Boolean> = _isLoadingProfile.asStateFlow()
    internal fun setLoadingProfile(loading: Boolean) { _isLoadingProfile.value = loading } // Helper

    private val _isLoadingPosts = MutableStateFlow(false) // 사용자 게시글 로딩 상태
    val isLoadingPosts: StateFlow<Boolean> = _isLoadingPosts.asStateFlow()
    internal fun setLoadingPosts(loading: Boolean) { _isLoadingPosts.value = loading } // Helper
    // --- [ 추가 끝 ] ---


    // --- 공통 함수 ---
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    companion object {
        fun provide(context: Context): CommunityViewModel {
            return CommunityViewModel(context)
        }
    }
}

// --- 유틸리티 함수 ---
internal fun String.toPlain(): RequestBody =
    this.toRequestBody("text/plain".toMediaType())