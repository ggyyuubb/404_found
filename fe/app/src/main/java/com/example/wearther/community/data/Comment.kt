package com.example.wearther.community.data

data class Comment(
    val id: Int,
    val feedId: Int,
    val userId: String,
    val userName: String,
    val userProfileImage: String? = null,
    val content: String,
    val timestamp: String,
    val likeCount: Int = 0,
    val isLiked: Boolean = false
)