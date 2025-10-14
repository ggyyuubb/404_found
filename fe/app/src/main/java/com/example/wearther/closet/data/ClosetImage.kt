package com.example.wearther.closet.data

data class ClosetImage(
    val id: String,
    val filename: String? = "",
    val url: String,
    val uploaded_at: String? = "",
    val clothing_type: String? = "",
    val colors: List<String>? = emptyList(),
    val material: String? = "",
    val suitable_temperature: String? = ""
)