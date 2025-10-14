package com.example.wearther.community.data

import android.util.Log

data class FeedItem(
    val id: Int,
    val userName: String,
    val postTime: String,
    val description: String,
    val temperature: String,
    val weather: String,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean
) {
    init {
        Log.d("FeedItem", "Creating FeedItem: id=$id, userName=$userName")
    }
}