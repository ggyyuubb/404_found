package com.example.wearther.community.data

data class FriendSuggestion(
    val id: String,
    val name: String,
    val profileImage: String,
    val mutualFriends: Int,
    val isFollowing: Boolean
)
