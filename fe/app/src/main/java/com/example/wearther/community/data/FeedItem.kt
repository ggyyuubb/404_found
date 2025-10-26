package com.example.wearther.community.data

import com.google.gson.annotations.SerializedName

data class FeedItem(
    // 1. Int -> String으로 변경 (가장 중요)
    @SerializedName("id") val id: String,

    // 2. 서버의 JSON 키 이름과 앱의 변수 이름을 매칭
    @SerializedName("nickname") val userName: String,
    @SerializedName("profile_image") val userProfileImage: String? = null,
    @SerializedName("created_at") val postTime: String,
    @SerializedName("content") val description: String,
    @SerializedName("image_urls") val outfitImages: List<String> = emptyList(),
    @SerializedName("temperature") val temperature: String,
    @SerializedName("weather") val weather: String,
    @SerializedName("likes_count") val likeCount: Int,
    @SerializedName("comment_count") val commentCount: Int,
    @SerializedName("liked_by_me") val isLiked: Boolean
)