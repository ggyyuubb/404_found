package com.example.wearther.community.data

data class FeedItem(
    val id: Int,
    val userName: String,
    val userProfileImage: String? = null,
    val postTime: String,
    val description: String,
    val outfitImages: List<String> = emptyList(),
    val temperature: String,
    val weather: String,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean
)