package com.example.wearther.community.data

data class UserProfile(
    val userId: String,
    val userName: String,
    val userEmail: String? = null,
    val userProfileImage: String? = null,
    val bio: String? = null,
    val isFriend: Boolean = false,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val posts: List<FeedItem> = emptyList()
)