package com.example.wearther.community.api

import com.example.wearther.community.data.Comment
import com.example.wearther.community.data.FeedItem
import com.example.wearther.community.data.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/* ===================== API 인터페이스 ===================== */

interface CommunityApi {
    // --- 피드 관련 ---
    @GET("community/posts")
    suspend fun getFeeds(): List<FeedItem>

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

    @DELETE("community/posts/{feedId}")
    suspend fun deleteFeed(@Path("feedId") feedId: String)

    // --- 댓글 관련 ---
    @GET("community/posts/{feedId}/comments")
    suspend fun getComments(@Path("feedId") feedId: String): List<Comment>

    @POST("community/posts/{feedId}/comments")
    suspend fun addComment(
        @Path("feedId") feedId: String,
        @Body comment: CommentRequest
    ): Comment

    @DELETE("community/posts/{feedId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("feedId") feedId: String,
        @Path("commentId") commentId: Int
    )

    @POST("community/posts/{feedId}/comments/{commentId}/like")
    suspend fun toggleCommentLike(
        @Path("feedId") feedId: String,
        @Path("commentId") commentId: Int
    ): Comment

    // --- 사용자 관련 ---
    @GET("community/users/search")
    suspend fun searchUsers(@Query("query") query: String): List<User>

    @GET("community/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): User

    @POST("community/users/{userId}/follow")
    suspend fun toggleFollow(@Path("userId") userId: String): User

    // ✅ 새로 추가: 사용자 프로필 조회
    @GET("community/users/{userId}/profile")
    suspend fun getUserProfile(
        @Path("userId") userId: String
    ): Response<User>

    // ✅ 새로 추가: 사용자 게시물 조회
    @GET("community/users/{userId}/posts")
    suspend fun getUserPosts(
        @Path("userId") userId: String
    ): Response<List<FeedItem>>
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