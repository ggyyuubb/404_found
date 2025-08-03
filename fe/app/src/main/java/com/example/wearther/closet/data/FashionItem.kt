package com.example.wearther.ui.screens.closet.data

data class FashionItem(
    val id: Int,
    val category: String,
    val image: String,
    val isLiked: Boolean = false
)