package com.example.wearther.community.api

import com.example.wearther.community.data.Comment
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/* ===================== API 인터페이스 ===================== */

interface CommunityApi {
    // 피드 관련
    @GET("community/posts")
    suspend fun getFeeds(): List<FeedItem>

    @POST("community/posts/{feedId}/like")
    suspend fun toggleLike(@Path("feedId") feedId: Int): FeedItem

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

    @DELETE("community/posts/{feedId}")
    suspend fun deleteFeed(@Path("feedId") feedId: Int)

    // 댓글 관련
    @GET("community/posts/{feedId}/comments")
    suspend fun getComments(@Path("feedId") feedId: Int): List<Comment>

    @POST("community/posts/{feedId}/comments")
    suspend fun addComment(
        @Path("feedId") feedId: Int,
        @Body comment: CommentRequest
    ): Comment

    @DELETE("community/posts/{feedId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("feedId") feedId: Int,
        @Path("commentId") commentId: Int
    )

    @POST("community/posts/{feedId}/comments/{commentId}/like")
    suspend fun toggleCommentLike(
        @Path("feedId") feedId: Int,
        @Path("commentId") commentId: Int
    ): Comment

    // 사용자 관련 (아직 백엔드에 없음 - 다음 단계에서 추가 예정)
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