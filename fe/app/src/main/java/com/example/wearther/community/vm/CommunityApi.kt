package com.example.wearther.community.api

import com.example.wearther.community.data.Comment
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/* ===================== API 인터페이스 ===================== */

interface CommunityApi {
    // --- 피드 관련 ---
    @GET("community/posts")
    suspend fun getFeeds(): List<FeedItem>

    // [ 💡 수정: Int -> String ]
    @POST("community/posts/{feedId}/like")
    suspend fun toggleLike(@Path("feedId") feedId: String): FeedItem

    @POST("community/posts")
    suspend fun createFeed(@Body request: CreateFeedRequest): FeedItem

    @Multipart
    @POST("community/posts")
    suspend fun createFeedWithImage(
        @Part image: MultipartBody.Part?,
        @Part("description") description: RequestBody,
        @Part("temperature") temperature: RequestBody,
        @Part("weather") weather: RequestBody
    ): FeedItem

    // [ 💡 수정: Int -> String ]
    @DELETE("community/posts/{feedId}")
    suspend fun deleteFeed(@Path("feedId") feedId: String)

    // --- 댓글 관련 ---
    // [ 💡 수정: Int -> String ]
    @GET("community/posts/{feedId}/comments")
    suspend fun getComments(@Path("feedId") feedId: String): List<Comment>

    // [ 💡 수정: Int -> String ]
    @POST("community/posts/{feedId}/comments")
    suspend fun addComment(
        @Path("feedId") feedId: String,
        @Body comment: CommentRequest
    ): Comment

    // [ 💡 수정: Int -> String ] (commentId는 Int 유지 가정)
    @DELETE("community/posts/{feedId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("feedId") feedId: String,
        @Path("commentId") commentId: Int
    )

    // [ 💡 수정: Int -> String ] (commentId는 Int 유지 가정)
    @POST("community/posts/{feedId}/comments/{commentId}/like")
    suspend fun toggleCommentLike(
        @Path("feedId") feedId: String,
        @Path("commentId") commentId: Int
    ): Comment

    // --- 사용자 관련 --- (String ID 사용 중이므로 수정 불필요)
    @GET("community/users/search")
    suspend fun searchUsers(@Query("query") query: String): List<User>

    @GET("community/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): User

    @POST("community/users/{userId}/follow")
    suspend fun toggleFollow(@Path("userId") userId: String): User
}

/* ===================== 요청 바디 데이터 클래스 ===================== */

data class CommentRequest(
    val content: String,
    val userName: String
)

data class CreateFeedRequest(
    val description: String,
    val temperature: String,
    val weather: String,
    val imageUrl: String? = null
)