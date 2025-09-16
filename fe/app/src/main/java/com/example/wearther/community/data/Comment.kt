package com.example.wearther.community.data

data class Comment(
    val id: Long,
    val postId: Long,
    val userId: String,
    val userName: String,
    val userProfileImage: String,
    val content: String,
    val timestamp: String,
    val likeCount: Int,
    val isLiked: Boolean,
    val replies: List<Reply> = emptyList()
)

data class Reply(
    val id: Long,
    val commentId: Long,
    val userId: String,
    val userName: String,
    val userProfileImage: String,
    val content: String,
    val timestamp: String,
    val likeCount: Int,
    val isLiked: Boolean
) 