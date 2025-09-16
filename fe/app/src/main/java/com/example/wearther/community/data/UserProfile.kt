package com.example.wearther.community.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userProfileImage: String,
    val isFriend: Boolean,
    val followerCount: Int,
    val followingCount: Int,
    val postCount: Int
) : Parcelable 