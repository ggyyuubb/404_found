package com.example.wearther.community.data

import com.google.gson.annotations.SerializedName

data class FeedItem(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("nickname") val userName: String,
    @SerializedName("profile_image") val userProfileImage: String? = null,
    @SerializedName("created_at") val postTime: String,
    @SerializedName("content") val description: String,
    @SerializedName("image_urls") val outfitImages: List<String> = emptyList(),
    @SerializedName("temperature") val temperature: String? = "N/A",
    @SerializedName("weather") val weather: String? = "N/A",
    @SerializedName("likes_count") val likeCount: Int = 0,
    @SerializedName("comment_count") val commentCount: Int = 0,
    @SerializedName("liked_by_me") val isLiked: Boolean = false
)