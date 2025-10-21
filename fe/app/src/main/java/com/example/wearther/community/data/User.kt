package com.example.wearther.community.data

data class User(
    val userId: String,
    val userName: String,  // 이름이자 닉네임
    val profileImage: String? = null,
    val bio: String = "",
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val isFollowing: Boolean = false
)

