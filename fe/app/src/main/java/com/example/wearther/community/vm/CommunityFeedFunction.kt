package com.example.wearther.community.vm

// Context import는 여전히 필요 없습니다.
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.wearther.community.api.CreateFeedRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream // InputStream import 추가

/* ==================== 피드 관련 확장 함수 ==================== */

// ... (loadFeeds, toggleLike, addFeed 함수는 동일) ...
fun CommunityViewModel.loadFeeds() {
    viewModelScope.launch {
        setLoading(true)
        setErrorMessage(null)
        try {
            val feedList = api.getFeeds()
            updateFeeds(feedList) // Use helper
            Log.d("CommunityViewModel", "Loaded ${feedList.size} feeds")
        } catch (e: Exception) {
            setErrorMessage("피드를 불러오는데 실패했습니다: ${e.message}") // Use helper
            Log.e("CommunityViewModel", "Error loading feeds", e)
        } finally {
            setLoading(false) // Use helper
        }
    }
}

fun CommunityViewModel.toggleLike(feedId: String) {
    viewModelScope.launch {
        try {
            val updatedFeed = api.toggleLike(feedId)
            val currentFeeds = feeds.value
            updateFeeds(currentFeeds.map { feed ->
                if (feed.id == feedId) updatedFeed else feed
            })
        } catch (e: Exception) {
            setErrorMessage("좋아요 처리에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error toggling like", e)
        }
    }
}

fun CommunityViewModel.addFeed(description: String, temperature: String, weather: String, imageUrl: String? = null) {
    viewModelScope.launch {
        setLoading(true)
        setErrorMessage(null)
        try {
            val request = CreateFeedRequest(description, temperature, weather, imageUrl)
            val newFeed = api.createFeed(request)
            val currentFeeds = feeds.value
            updateFeeds(listOf(newFeed) + currentFeeds)
            Log.d("CommunityViewModel", "Feed created successfully")
            emitUploadSuccess()
        } catch (e: Exception) {
            setErrorMessage("게시글 작성에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error creating feed", e)
        } finally {
            setLoading(false)
        }
    }
}


fun CommunityViewModel.addFeedWithImage(
    // context 파라미터 없음
    description: String,
    temperature: String,
    weather: String,
    imageUri: Uri?
) {
    viewModelScope.launch {
        setLoading(true)
        setErrorMessage(null)
        try {
            val descBody = description.toPlain()
            val tempBody = temperature.toPlain()
            val weatherBody = weather.toPlain()

            val imagePart: MultipartBody.Part? = imageUri?.let { uri ->
                // [ 💡 1. 수정: this@addFeedWithImage.context 사용 ]
                val cr = this@addFeedWithImage.context.contentResolver
                val mime = cr.getType(uri) ?: "image/jpeg"
                // [ 💡 2. 수정: it 대신 inputStream -> 사용 ]
                val bytes = cr.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                } ?: run {
                    // InputStream이 null일 경우 예외 처리
                    Log.e("CommunityViewModel", "Failed to open input stream for URI: $uri")
                    throw Exception("이미지 파일을 열 수 없습니다.")
                }
                // --- [ 수정 끝 ] ---
                val req = bytes.toRequestBody(mime.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("image", "upload.jpg", req)
            }

            val newFeed = api.createFeedWithImage(
                image = imagePart,
                description = descBody,
                temperature = tempBody,
                weather = weatherBody
            )
            val currentFeeds = feeds.value
            updateFeeds(listOf(newFeed) + currentFeeds)
            Log.d("CommunityViewModel", "Feed created successfully (with image)")
            emitUploadSuccess()
        } catch (e: Exception) {
            setErrorMessage("게시글(이미지 포함) 작성에 실패했습니다: ${e.message}")
            Log.e("CommunityViewModel", "Error creating feed (multipart)", e)
        } finally {
            setLoading(false)
        }
    }
}

// ... (deleteFeed 함수는 동일) ...
fun CommunityViewModel.deleteFeed(feedId: String) {
    viewModelScope.launch {
        try {
            api.deleteFeed(feedId)
            val currentFeeds = feeds.value
            updateFeeds(currentFeeds.filter { it.id != feedId }) // Use helper
            val currentComments = comments.value.toMutableMap()
            currentComments.remove(feedId)
            updateComments(currentComments) // Use helper
        } catch (e: Exception) {
            setErrorMessage("게시글 삭제에 실패했습니다: ${e.message}") // Use helper
            Log.e("CommunityViewModel", "Error deleting feed", e)
        }
    }
}