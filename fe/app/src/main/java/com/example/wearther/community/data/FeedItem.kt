package com.example.wearther.community.data

data class FeedItem(
    val id: Long,
    val userName: String,
    val userProfileImage: String,
    val postTime: String,
    val outfitImages: List<String>,
    val description: String,
    val temperature: String,
    val weather: String,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean
)
