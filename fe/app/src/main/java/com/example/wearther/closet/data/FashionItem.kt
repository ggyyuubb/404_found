package com.example.wearther.ui.screens.closet.data

data class FashionItem(
    val id: String,
    val category: String? = "",
    val image: String,
    val isLiked: Boolean = false,
    val colors: List<String>? = emptyList(),
    val material: String? = "",
    val suitable_temperature: String? = ""
)