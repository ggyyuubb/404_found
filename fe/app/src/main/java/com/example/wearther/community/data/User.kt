package com.example.wearther.community.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id") val userId: String,
    @SerializedName("user_name") val userName: String,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("bio") val bio: String = "",
    @SerializedName("follower_count") val followerCount: Int = 0,
    @SerializedName("following_count") val followingCount: Int = 0,
    @SerializedName("post_count") val postCount: Int = 0,
    @SerializedName("is_following") val isFollowing: Boolean = false
)